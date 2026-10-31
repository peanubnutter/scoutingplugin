package com.scouting.pathfinder;

import static com.scouting.pathfinder.CallLocation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * Resolves a location callout or shorthand to its matching locations. Input must already be
 * trimmed and lowercased by the caller.
 * <ul>
 * <li>Keys are (base, direction) pairs. Registration order breaks ties, so keys listed earlier
 * are treated as more common calls.</li>
 * <li>Spaced forms ("dray e", "north seer") are derived from the {@code of(base, direction)}
 * keys; attached forms ("snemus") are registered so they can also be fuzzy-matched as names.</li>
 * <li>An explicit direction never falls back to another direction, only to the directionless base.</li>
 * <li>The fuzzy rules (length, first character, anagrams, repeated letters) live in
 * {@code evaluateFuzzyCandidate}.</li>
 * </ul>
 */
public class CallLocationFinder
{
	private static final Map<LocationKey, Set<CallLocation>> LOCATION_KEYS = new LinkedHashMap<>();
	private static final LevenshteinDistance LEVENSHTEIN = LevenshteinDistance.getDefaultInstance();
	private static final Predicate<LocationKey> FUZZY_NONE = key -> key.direction == null;

	static
	{
		// Common locations
		key(ARCEUUS, of("arceuus"), of("arc"), of("botd5"));
		key(CHURCH, of("church"));
		key(DRAY, of("dray"), of("draynor"));
		key(DRAY_OAK, of("dray oak"), of("draynor oak"));
		key(EDGE, of("edge"), of("edgeville"));
		key(GLADE, of("glade"));
		key(MYTH, of("myth"));
		key(NEMUS, of("nemus"));
		key(NEMUS_S, of("snemus"), of("nemus", Direction.S));
		key(NSEER, of("nseer"), of("nseers"), of("seer", Direction.N), of("seers", Direction.N));
		key(NSORC, of("nsorc"), of("nmage"), of("sorc", Direction.N), of("mage", Direction.N));
		key(NWSEER, of("nwseer"), of("nwseers"), of("seer", Direction.NW), of("seers", Direction.NW));
		key(OASIS, of("oasis"), of("ajp", Direction.E));
		key(PMAGE, of("pmage"), of("prif mage"));
		key(PTEAK, of("pteak"), of("pmahog"), of("prif teak"), of("prif mahog"));
		key(SEER, of("seer"), of("seers"));
		key(SOULS_BJP_W, of("souls"), of("bjp", Direction.W));
		key(YAK, of("yak"), of("yaks"));
		key(ZALC, of("zalc"), of("zalcano"));

		// Uncommon locations
		key(AIS_E, of("ais", Direction.E));
		key(AIS_SW, of("ais", Direction.SW));
		key(APE, of("ape"), of("apeatoll"));
		key(BARB, of("barb"), of("ba"));
		key(BKP_W, of("bkp", Direction.W));
		key(CAMELOT, of("camelot"), of("cam"));
		key(CIR_W_MAHOG, of("cir w mahog"), of("cir", Direction.W));
		key(CIR_W_MAPLE, of("cir w maple"), of("cir", Direction.W));
		key(DRAY_EAST, of("dray", Direction.E), of("draynor", Direction.E));
		key(DRUMSTICK, of("drum"), of("ds"));
		key(ECABBAGE, of("ecabbage"), of("cabbage", Direction.E));
		key(FLAX, of("flax"));
		key(GE, of("ge"));
		key(GORGE, of("gorge"));
		key(HEART, of("heart"));
		key(HEART_E, of("kingstown"), of("heart", Direction.E));
		key(KASTORI, of("kastori"), of("kas"), of("kast"));
		key(LOOKOUT, of("lookout"));
		key(MIST_MAPLE_YEW, of("mist maple yew"), of("mist"), of("mistrock"));
		key(MTA_E, of("mta", Direction.E));
		key(MTA_W, of("mta", Direction.W));
		key(NCONCH, of("nconch"), of("cjq"), of("conch", Direction.N));
		key(NE_GLADE, of("neglade"), of("glade", Direction.NE));
		key(NEMUS_E, of("enemus"), of("nemus", Direction.E));
		key(NEMUS_SW, of("swnemus"), of("nemus", Direction.SW));
		key(NIEVE, of("nieve"));
		key(NWALD_MAGIC, of("nwald magic"), of("nwald"), of("ald magic", Direction.NW),
			of("ald", Direction.NW), of("ckq", Direction.NW));
		key(RADA_E, of("rada", Direction.E));
		key(RADA_SE, of("rada", Direction.SE));
		key(RADA_W, of("rada", Direction.W));
		key(RELLEKKA_SW, of("rpines"), of("rellekka", Direction.SW));
		key(RELEKKA_SW, of("rpines"), of("relekka", Direction.SW));
		key(SUNBLEAK, of("sb"), of("sunbleak"), of("sun"), of("sunb"));
		key(BOTD3_SHAY, of("botd3"), of("shay"), of("shayzien"));
		key(SORC, of("sorc"), of("smage"), of("mage", Direction.S));
		key(SW_CONCH, of("shore"), of("conch", Direction.SW), of("cjq", Direction.SW));
		key(TEKLAN, of("teklan"), of("tek"));
		key(VALE, of("vale"));
		key(VALE_E, of("vale", Direction.E), of("ais", Direction.N));
		key(VALE_S, of("vale", Direction.S));
		key(VALE_W, of("vale", Direction.W));

		// Obscure locations
		key(AJP, of("ajp"));
		key(APE_W, of("ape", Direction.W), of("apeatoll", Direction.W));
		key(ARDY_MONASTRY, of("ardy monastry"), of("ardy"));
		key(BATTLEFRONT, of("battlefront"));
		key(CATHERBY, of("catherby"), of("cath"));
		key(CHAMP, of("champ"), of("champion"));
		key(CHAMP_E, of("champ", Direction.E), of("champion", Direction.E));
		key(CORSAIR, of("corsair"));
		key(CRAFT, of("craft"));
		key(CW_EAST, of("cw", Direction.E));
		key(FALO, of("falo"));
		key(FALLY_GATE, of("fally gate"), of("fally"));
		key(FORTIS_POND_N, of("fortis"), of("fortis pond", Direction.N));
		key(FORTIS_POND_E, of("fortis"), of("fortis pond", Direction.E));
		key(GE_FOUNTAIN, of("ge water fountain"), of("ge fountain"));
		key(GROTTO, of("grotto"));
		key(GWENNITH, of("gwennith"));
		key(HSPIRIT, of("hspirit"));
		key(KASTORI_N, of("kastori", Direction.N), of("kas", Direction.N), of("kast", Direction.N));
		key(KASTORI_W, of("kastori", Direction.W), of("kas", Direction.W), of("kast", Direction.W));
		key(LGRAVE, of("lgrave"));
		key(LLETYA, of("lletya"));
		key(LUM_TREE_PATCH, of("lum tree patch"), of("ltree patch"), of("ltree"));
		key(MOS_LEHARM, of("mos leharm"));
		key(N_GLADE, of("nglade"), of("glade", Direction.N));
		key(N_TITHE_WILLOW, of("ntithe"), of("tithe willow", Direction.N), of("tithe", Direction.N));
		key(N_YAK, of("yak", Direction.N));
		key(NALD_YEW, of("nald yew"), of("nald"), of("naldarin"), of("ald yew", Direction.N),
			of("ald", Direction.N), of("aldarin", Direction.N));
		key(NCABBAGE, of("ncabbage"), of("cabbage", Direction.N));
		key(NEMUS_NW, of("nwnemus"), of("nemus", Direction.NW));
		key(PHAS, of("phas"));
		key(PISC, of("pisc"));
		key(RALOS_MAGICS, of("ralos magics"), of("ralos"));
		key(RIM, of("rim"), of("rimm"), of("rimmington"));
		key(RIMM_SE, of("rimm", Direction.SE));
		key(SARIM, of("sarim"));
		key(SCABBAGE, of("scabbage"), of("cabbage", Direction.S));
		key(SE_GNOME, of("gnome", Direction.SE));
		key(SE_PRIF, of("prif", Direction.SE));
		key(SHERLOCK, of("sherlock"));
		key(SINC, of("sinc"), of("sinclair"), of("sinclair mansion"));
		key(SINCLAIR, of("sinc"), of("sinclair"), of("sinclair mansion"));
		key(SLEGEND, of("slegend"), of("legend", Direction.S));
		key(SLEPE, of("slepe"));
		key(SLUMBER, of("slumber"), of("lumber", Direction.S));
		key(SW_FARM_GUILD, of("farm guild", Direction.SW), of("farm", Direction.SW));
		key(SW_HOS, of("hos", Direction.SW));
		key(SWLUMBER, of("swlumber"), of("lumber", Direction.SW));
		key(TBW_GROVE, of("tbw grove"), of("tbw"));
		key(TLATI_N, of("tlati", Direction.N));
		key(TLATI_NE, of("tlati", Direction.NE));
		key(UZER, of("uzer"));
		key(VALE_N, of("vale", Direction.N));
		key(VARROCK_W_BANK, of("varrock w bank"));
		key(WATSON, of("watson"));
		key(WC_TUTOR, of("wc tutor"));
		key(WCG, of("wcg"), of("wc guild"));
		key(WLEGEND, of("wlegend"), of("legend", Direction.W));
		key(WLUMBER, of("wlumber"), of("lumber", Direction.W));
		key(WOODLAND_NW, of("woodland", Direction.NW));
		key(ZANARIS, of("zanaris"));
	}

	private CallLocationFinder()
	{
	}

	/**
	 * Finds the locations represented by {@code input}.
	 * <p>
	 * The caller must trim and lowercase the input. Null, empty, direction-only and unrecognized
	 * input return an empty set. The returned set is always a fresh {@link LinkedHashSet}.
	 *
	 * @param input location name or shorthand
	 * @return matching locations in registration order, or an empty set
	 */
	public static Set<CallLocation> find(String input)
	{
		return input == null || input.isEmpty() || isDirectionOnly(input)
			? new LinkedHashSet<>() : new LinkedHashSet<>(lookup(input));
	}

	// Each strategy is an ordered list of steps; the first non-empty result wins.
	private static Set<CallLocation> lookup(String input)
	{
		Set<LocationKey> spaced = parseDirections(input, true);
		Set<LocationKey> adjacent = parseDirections(input, false);
		if (!spaced.isEmpty())
		{
			return firstMatch(
				() -> earliest(spaced),
				() -> exactNone(input),
				() -> fuzzyDirectional(spaced),
				() -> fuzzyNone(input),
				() -> directionlessBase(spaced));
		}
		if (!adjacent.isEmpty())
		{
			// "ndray" and "nwseer" are ambiguous with ordinary names, so complete-input matches come first.
			return firstMatch(
				() -> exactNone(input),
				() -> earliest(adjacent),
				() -> exactDirectionalBase(input),
				() -> fuzzyNone(input),
				() -> fuzzyDirectionalBase(input),
				() -> fuzzyDirectional(adjacent),
				() -> directionlessBase(adjacent));
		}
		return firstMatch(
			() -> exactNone(input),
			() -> exactDirectionalBase(input),
			() -> fuzzyNone(input),
			() -> fuzzyDirectionalBase(input));
	}

	@SafeVarargs
	private static Set<CallLocation> firstMatch(Supplier<Set<CallLocation>>... steps)
	{
		return Arrays.stream(steps).map(Supplier::get).filter(match -> !match.isEmpty())
			.findFirst().orElse(Collections.emptySet());
	}

	/** An explicit direction may fall back to a directionless key for its base, never to another direction. */
	private static Set<CallLocation> directionlessBase(Set<LocationKey> parses)
	{
		Set<CallLocation> exact = earliest(parses.stream().map(parse -> of(parse.base)).collect(Collectors.toSet()));
		return !exact.isEmpty() ? exact
			: resolve(parses.stream().map(parse -> bestFuzzy(parse.base, FUZZY_NONE)).reduce(null, CallLocationFinder::better));
	}

	private static boolean isDirectionOnly(String input)
	{
		return Arrays.stream(Direction.values())
			.anyMatch(direction -> input.equals(direction.abbreviation) || input.equals(direction.fullName));
	}

	/**
	 * Returns every way {@code input} can be read as a base with a direction at the start or end.
	 * Spaced parses (direction separated by whitespace) and adjacent parses (abbreviation attached
	 * to the base) are requested separately. Full names and abbreviations can both be attached
	 * ("drayeast", "draye"), but an abbreviation is never split off the front of a full name
	 * ("northdray" is "north" + "dray", not "n" + "orthdray").
	 */
	private static Set<LocationKey> parseDirections(String input, boolean spaced)
	{
		boolean startsWithName = Arrays.stream(Direction.values()).anyMatch(d -> input.startsWith(d.fullName));
		Set<LocationKey> parses = new LinkedHashSet<>();

		for (Direction direction : Direction.values())
		{
			for (String token : new String[] {direction.abbreviation, direction.fullName})
			{
				int rest = input.length() - token.length();
				if (rest <= 0)
				{
					continue;
				}

				// "<direction> <base>" or "<abbreviation><base>"
				if (input.startsWith(token)
					&& Character.isWhitespace(input.charAt(token.length())) == spaced
					&& (spaced || token.equals(direction.fullName) || !startsWithName))
				{
					parses.add(new LocationKey(input.substring(token.length()).stripLeading(), direction));
				}

				// "<base> <direction>" or "<base><abbreviation>"
				if (input.endsWith(token) && Character.isWhitespace(input.charAt(rest - 1)) == spaced)
				{
					parses.add(new LocationKey(input.substring(0, rest).stripTrailing(), direction));
				}
			}
		}
		parses.removeIf(parse -> parse.base.isEmpty());
		return parses;
	}

	private static Set<CallLocation> exactNone(String input)
	{
		return LOCATION_KEYS.getOrDefault(of(input), Collections.emptySet());
	}

	/** The earliest registered key among {@code wanted} wins; candidates are never combined. */
	private static Set<CallLocation> earliest(Collection<LocationKey> wanted)
	{
		return LOCATION_KEYS.entrySet().stream()
			.filter(entry -> wanted.contains(entry.getKey()))
			.map(Map.Entry::getValue)
			.findFirst().orElse(Collections.emptySet());
	}

	/** All directional variants registered for a base, in registration order. */
	private static Set<CallLocation> exactDirectionalBase(String base)
	{
		return LOCATION_KEYS.entrySet().stream()
			.filter(entry -> entry.getKey().direction != null && entry.getKey().base.equals(base))
			.flatMap(entry -> entry.getValue().stream())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private static Set<CallLocation> fuzzyNone(String input)
	{
		return resolve(bestFuzzy(input, FUZZY_NONE));
	}

	private static Set<CallLocation> fuzzyDirectional(Set<LocationKey> parses)
	{
		return resolve(parses.stream()
			.map(parse -> bestFuzzy(parse.base, key -> key.direction == parse.direction))
			.reduce(null, CallLocationFinder::better));
	}

	/** Matches a base, then returns every directional variant of it. */
	private static Set<CallLocation> fuzzyDirectionalBase(String input)
	{
		FuzzyCandidate best = bestFuzzy(input, key -> key.direction != null);
		return best == null ? Collections.emptySet() : exactDirectionalBase(best.key.base);
	}

	private static Set<CallLocation> resolve(FuzzyCandidate candidate)
	{
		return candidate == null ? Collections.emptySet() : LOCATION_KEYS.get(candidate.key);
	}

	private static FuzzyCandidate bestFuzzy(String input, Predicate<LocationKey> eligible)
	{
		String collapsed = input.replaceAll("(?s)(.)\\1+", "$1"); // "drrayy" -> "dray"
		FuzzyCandidate best = null;
		int order = 0;
		for (LocationKey key : LOCATION_KEYS.keySet())
		{
			order++;
			FuzzyCandidate candidate = evaluateFuzzyCandidate(input, collapsed, key, order);
			if (candidate != null && eligible.test(key))
			{
				best = better(best, candidate);
			}
		}
		return best;
	}

	/** Returns null when the key is not an acceptable fuzzy match for the input. */
	private static FuzzyCandidate evaluateFuzzyCandidate(String input, String collapsedInput, LocationKey key, int order)
	{
		String candidate = key.base;
		if (input.length() < 4 || candidate.length() < 4
			|| input.charAt(0) != candidate.charAt(0))
		{
			return null;
		}

		boolean anagram = isAnagram(input, candidate);
		int distance = Math.min(LEVENSHTEIN.apply(input, candidate), LEVENSHTEIN.apply(collapsedInput, candidate));
		int threshold = Math.min(3, candidate.length() / 3);
		return anagram || distance <= threshold ? new FuzzyCandidate(key, distance, anagram, order) : null;
	}

	/** Anagrams first, then lowest distance, then earliest registration. */
	private static final Comparator<FuzzyCandidate> BEST = Comparator
		.comparing((FuzzyCandidate c) -> !c.anagram)
		.thenComparingInt(c -> c.distance)
		.thenComparingInt(c -> c.order);

	private static FuzzyCandidate better(FuzzyCandidate current, FuzzyCandidate challenger)
	{
		return challenger != null && (current == null || BEST.compare(challenger, current) < 0) ? challenger : current;
	}

	private static boolean isAnagram(String a, String b)
	{
		return Arrays.equals(a.chars().sorted().toArray(), b.chars().sorted().toArray());
	}

	private static LocationKey of(String base)
	{
		return new LocationKey(base, null);
	}

	private static LocationKey of(String base, Direction direction)
	{
		return new LocationKey(base, direction);
	}

	private static void key(CallLocation location, LocationKey... keys)
	{
		for (LocationKey locationKey : keys)
		{
			LOCATION_KEYS.computeIfAbsent(locationKey, k -> new LinkedHashSet<>()).add(location);
		}
	}

	@RequiredArgsConstructor
	private enum Direction
	{
		N("n", "north"),
		NE("ne", "northeast"),
		E("e", "east"),
		SE("se", "southeast"),
		S("s", "south"),
		SW("sw", "southwest"),
		W("w", "west"),
		NW("nw", "northwest");

		private final String abbreviation;
		private final String fullName;
	}

	@Value
	private static class FuzzyCandidate
	{
		LocationKey key;
		int distance;
		boolean anagram;
		int order;
	}

	/** A lowercase base plus a direction (null means directionless); also used for the parsed readings of an input. */
	@Value
	private static class LocationKey
	{
		String base;
		Direction direction;
	}
}
