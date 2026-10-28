# Event Scouting Plugin
A RuneLite plugin for automatically scouting for various events as you encounter them. Events will be processed and
posted to the Log Hunters discord (https://discord.gg/loghunters).

The plugin will automatically scout for various event types and report the events to the server, if you have enabled
connection to the server and that specific event type. No additional effort is required on your part!

Make sure to enable each event type in the plugin settings if you want to contribute!

- Right now, we support Forestry events and special Sailing Shoals.
- Many other events (implings, shooting stars, etc.) may be added in the future!

## Ent Pathfinding (optional)

Off by default. Turn on **Use 'Shortest Path' Plugin** under *Ent Pathfinding* and, while you are
logged in on world 444, the plugin routes you to each called ent with the
[Shortest Path](https://github.com/Skretzo/shortest-path) plugin, oldest first, and lists them on
screen with their time left. Reaching an ent moves the path on to the next one. Only ents called
since you logged in on the world are used, as you must be logged in when an ent spawns to be eligible.

Calls arrive from a relay that reads the scouting feed once on behalf of every player, rather than
each player polling it. The relay is only connected while the feature is on and you are on world 444,
plus ten minutes after you leave so that hops and relogs reuse one connection.
