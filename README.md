<a href="https://modrinth.com/plugin/optimizationutils"><img alt="modrinth" height="40" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg"></a>

# OptimizationUtils

A minecraft plugin with some useful optimization utils (see below).

## Requirements

- Paper 1.21+ (older versions may not work. Pull requests are appreciated!)
- Java 21+

## Installation
- [Modrinth](https://modrinth.com/plugin/ia-edit)

## Features

- **Dynamic mobcap** - automatically throttles mob spawning (optionally spawners too) when MSPT exceeds a configurable threshold.
- **Dynamic random tick speed** - automatically turns random ticks off when MSPT exceeds a configurable threshold.
- **Disable entity ticking** - stops ticking selected mobs (they stay in the world, but no AI/movement). Two modes: disabled entity ticking entirely, or uses Bukkit's `Mob#setAware`. Mobs can be filtered by entity type or Bukkit class, with include/exclude lists.
- **Runtime tweaks via commands** - change view distance (globally or per player, persisted), simulation distance, mobcaps, mob spawn frequency and villager tick rates without a restart.
- **Diagnostics** - analyze which chunks hold the most entities, kill entities/animals far away from players, and a detailed `/ou info` overview.

## Commands

Main command: `/optimizationutils` (aliases: `/ou`, `/opt`)

| Command | Description |
|---------|-------------|
| `/ou info` | Displays server and plugin information (view/simulation distances, entity counts, loaded chunks, tick rates, feature status) |
| `/ou analyzechunks` | Lists the top 10 loaded chunks with the most entities |
| `/ou setviewdistance <distance> [player]` | Sets view distance for all worlds, or for a specific player (persisted across restarts) |
| `/ou resetviewdistance <player>` | Resets a player's view distance to the server default |
| `/ou setsimulationdistance <distance>` | Sets simulation distance for all worlds while respecting despawn ranges |
| `/ou setspawnlimit <spawn category> <limit>` | Sets the mobcap for all worlds |
| `/ou setticksperspawn <spawn category> <ticks>` | Sets mob spawn frequency (ticks between spawn attempts) for all worlds |
| `/ou setvillagersensortickrate <ticks>` | Sets villager sensor tick rate for all worlds |
| `/ou setvillagerbehaviortickrate <ticks>` | Sets villager behavior tick rate for all worlds |
| `/ou killoutofrange <entity type> <range>` | Kills entities of the given type that are out of range of all players |
| `/ou killanimalsoutofrange <range>` | Kills animals that are out of range of all players |
| `/ou reload` | Reloads the configuration |

## Permissions

- `optimizationutils.admin` - Access to all commands

## Contributing

You can build the project using IntelliJ IDEA

```bash
gradlew build
```

## Stats

![](https://bstats.org/signatures/bukkit/OptimizationUtils.svg)
