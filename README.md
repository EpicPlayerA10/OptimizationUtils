# OptimizationUtils

A plugin that allows you to dynamically change mobcap and performance settings without a server restart!

## Requirements

- Paper 1.21+ (older versions may not work. Pull requests are appreciated!)
- Java 21+

## Downloads
You can download the plugin from a [Releases](https://github.com/EpicPlayerA10/OptimizationUtils/releases/latest) tab by clicking on a `OptimizationUtils-x.x.x.jar`

## Commands

Base command: `/optimizationutils` (aliases: `/ou`, `/opt`)

- `/ou analyzechunks` - Shows in which chunk are the most entities.
- `/ou setsimulationdistance <distance>` - Set simulation distance for all worlds. This also changes spigot and paper configs according to https://paper-chan.moe/paper-optimization/?ref=paper-chan.moe#despawn-ranges-notes
- `/ou setspawnlimit <category> <limit>` - Set mob spawn limit (MONSTER, ANIMAL, etc.). Equivalent to `spawn-limits` in `bukkit.yml`. [(ref)](https://paper-chan.moe/paper-optimization/?ref=paper-chan.moe#spawn-limits)
- `/ou setticksperspawn <category> <ticks>` - Set mob spawn frequency. Equivalent to `ticks-per` in `bukkit.yml`. [(ref)](https://paper-chan.moe/paper-optimization/?ref=paper-chan.moe#ticks-per)

All these commands only set the values in memory, they do not change the config files. The changes will be lost on server restart.

## Permissions

- `optimizationutils.admin` - Access to all commands

## Contributing

You can build the project using IntelliJ IDEA

```bash
gradlew build
```
