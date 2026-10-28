package com.scouting.pathfinder;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Holds one WebSocket to the ent relay, reads the snapshots it pushes, and keeps time with it.
 * Reconnects back off with jitter, so an outage or a relay restart spends few of the relay's
 * 100,000 free connections a day.
 */
@Slf4j
class RelayClient
{
	private static final long FIRST_RETRY_MILLIS = 5_000;
	private static final long MAX_RETRY_MILLIS = 5 * 60_000;

	/** A connection must last this long before a drop resets the backoff. */
	private static final long STABLE_CONNECTION_MILLIS = 60_000;

	/** Protocol-level pings, answered by the relay's runtime without waking it. */
	private static final long PING_INTERVAL_SECONDS = 45;

	/** A relay timestamp further than this from the PC's clock is treated as garbled. */
	private static final long MAX_PLAUSIBLE_SKEW_SECONDS = 24 * 60 * 60;

	private static final int CLOSE_NORMAL = 1000;

	enum Status
	{
		DISCONNECTED,
		CONNECTING,
		CONNECTED,
		WAITING_TO_RETRY
	}

	/** Receives each snapshot on OkHttp's thread. */
	interface Listener
	{
		void onCalls(List<Call> calls);
	}

	private final OkHttpClient httpClient;
	private final Gson gson;
	private final ScheduledExecutorService executor;
	private final Listener listener;
	private final Random random = new Random();

	/** The relay's clock minus the PC's, so ent timers follow the relay. */
	private volatile long clockSkewSeconds;

	// Guarded by "this".
	private String url;
	private WebSocket socket;
	private ScheduledFuture<?> pendingRetry;
	private long connectedAtMillis;
	private int failedAttempts;
	private Status status = Status.DISCONNECTED;
	private boolean closed;

	RelayClient(OkHttpClient httpClient, Gson gson, ScheduledExecutorService executor, Listener listener)
	{
		this.httpClient = httpClient.newBuilder().pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS).build();
		this.gson = gson;
		this.executor = executor;
		this.listener = listener;
	}

	/** Be connected to {@code url}. Does nothing if already connected or retrying there. */
	synchronized void connect(String url)
	{
		if (closed || (url.equals(this.url) && status != Status.DISCONNECTED))
		{
			return;
		}

		closeCurrent();
		this.url = url;
		failedAttempts = 0;
		open();
	}

	/** Close the connection and stop retrying. A later connect() opens it again. */
	synchronized void disconnect()
	{
		if (status == Status.DISCONNECTED)
		{
			return;
		}

		closeCurrent();
		url = null;
		status = Status.DISCONNECTED;
	}

	/** Disconnect for good, so a connect() still in flight at shutdown cannot reopen it. */
	synchronized void close()
	{
		closed = true;
		disconnect();
	}

	synchronized Status getStatus()
	{
		return status;
	}

	/** Now, in epoch seconds, by the relay's clock. */
	long serverNow()
	{
		return localNowSeconds() + clockSkewSeconds;
	}

	private void open()
	{
		connectedAtMillis = 0;
		status = Status.CONNECTING;

		try
		{
			socket = httpClient.newWebSocket(new Request.Builder().url(url).build(), new Callbacks());
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Invalid ent relay URL: {}", url);
			socket = null;
			scheduleRetry();
		}
	}

	private void closeCurrent()
	{
		if (pendingRetry != null)
		{
			pendingRetry.cancel(false);
			pendingRetry = null;
		}
		if (socket != null)
		{
			socket.close(CLOSE_NORMAL, null);
			socket = null;
		}
	}

	private synchronized void handleOpen(WebSocket opened)
	{
		if (opened == socket)
		{
			connectedAtMillis = System.currentTimeMillis();
			status = Status.CONNECTED;
		}
	}

	private void handleMessage(WebSocket from, String text)
	{
		synchronized (this)
		{
			if (from != socket)
			{
				return;
			}
		}

		// Outside the lock, so a slow listener never holds up connecting or closing.
		RelayMessage message = parseMessage(gson, text);
		if (message != null)
		{
			long localNow = localNowSeconds();
			if (isPlausibleServerTime(message.serverTime, localNow))
			{
				clockSkewSeconds = message.serverTime - localNow;
			}
			listener.onCalls(message.calls);
		}
	}

	/** The connection ended. Reconnect unless it was closed on purpose. */
	private synchronized void handleDrop(WebSocket dropped)
	{
		if (dropped != socket)
		{
			return;
		}
		socket = null;

		if (url == null)
		{
			return;
		}
		// Only a connection that lasted earns a fast reconnect, so a relay that accepts and then
		// immediately drops cannot pull clients into a tight loop.
		if (connectedAtMillis > 0 && System.currentTimeMillis() - connectedAtMillis >= STABLE_CONNECTION_MILLIS)
		{
			failedAttempts = 0;
		}
		scheduleRetry();
	}

	private void scheduleRetry()
	{
		long delay = retryDelayMillis(failedAttempts++, random);
		log.debug("Ent relay connection lost, retrying in {}ms", delay);

		status = Status.WAITING_TO_RETRY;
		pendingRetry = executor.schedule(this::retry, delay, TimeUnit.MILLISECONDS);
	}

	private synchronized void retry()
	{
		pendingRetry = null;
		if (!closed && url != null && socket == null)
		{
			open();
		}
	}

	/** 5s, 10s, 20s... up to five minutes, each randomised to between half and all of that. */
	private static long retryDelayMillis(int attempts, Random random)
	{
		long ceiling = retryCeilingMillis(attempts);
		long floor = ceiling / 2;
		return floor + (long) (random.nextDouble() * (ceiling - floor));
	}

	private static long retryCeilingMillis(int attempts)
	{
		// Capping the shift keeps it far from overflowing a long.
		return Math.min(FIRST_RETRY_MILLIS << Math.min(attempts, 20), MAX_RETRY_MILLIS);
	}

	private static boolean isPlausibleServerTime(long serverTimeSeconds, long localNowSeconds)
	{
		return serverTimeSeconds > 0
			&& Math.abs(serverTimeSeconds - localNowSeconds) <= MAX_PLAUSIBLE_SKEW_SECONDS;
	}

	private static long localNowSeconds()
	{
		return System.currentTimeMillis() / 1000L;
	}

	/** Parse one relay message. Null for anything that is not a well-formed snapshot. */
	private static RelayMessage parseMessage(Gson gson, String text)
	{
		try
		{
			RelayMessage message = gson.fromJson(text, RelayMessage.class);
			if (message == null || !RelayMessage.TYPE_CALLS.equals(message.type))
			{
				return null;
			}
			if (message.calls == null)
			{
				message.calls = Collections.emptyList();
			}
			return message;
		}
		catch (JsonParseException e)
		{
			log.debug("Ignoring malformed ent relay message", e);
			return null;
		}
	}

	/** One push: every current call matching this client's subscription. */
	private static class RelayMessage
	{
		static final String TYPE_CALLS = "calls";

		String type;
		long serverTime;
		List<Call> calls;
	}

	/**
	 * One scout's sighting: where they saw an entling, and when. Field names mirror the scouting API.
	 * Value equality lets an ent ignore a row it already holds, as every push resends them all.
	 */
	@Getter
	@EqualsAndHashCode
	static class Call
	{
		@SerializedName("event_type")
		private String eventType;

		@SerializedName("world")
		private int world;

		@SerializedName("x_coord")
		private int xCoord;

		@SerializedName("y_coord")
		private int yCoord;

		@SerializedName("plane")
		private int plane;

		/** Epoch seconds, from the scout's own clock. */
		@SerializedName("discovered_time")
		private long discoveredTime;
	}

	private class Callbacks extends WebSocketListener
	{
		@Override
		public void onOpen(WebSocket webSocket, Response response)
		{
			handleOpen(webSocket);
		}

		@Override
		public void onMessage(WebSocket webSocket, String text)
		{
			handleMessage(webSocket, text);
		}

		@Override
		public void onClosing(WebSocket webSocket, int code, String reason)
		{
			webSocket.close(CLOSE_NORMAL, null);
		}

		@Override
		public void onClosed(WebSocket webSocket, int code, String reason)
		{
			handleDrop(webSocket);
		}

		@Override
		public void onFailure(WebSocket webSocket, Throwable t, Response response)
		{
			log.debug("Ent relay connection failed", t);
			handleDrop(webSocket);
		}
	}
}
