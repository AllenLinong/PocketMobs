# PocketMobs

A Bukkit/Paper plugin that lets players catch mobs into throwable "ball" items and
release them later, preserving the mob's full state — villager trades, variants,
tamed/owned state, attributes, potion effects, custom names and more.

> **Fork notice**
> This is a **ground-up rebuild** of the original
> [**PocketMobs** by **LoneDev**](https://github.com/JavaPlugins/PocketMobs)
> (which targeted Minecraft **1.16.5**), rewritten to run on **Paper 1.21.4+** and
> to be **Folia-compatible**. Maintained by
> [**HuiDu_OwO** (IOVEYOUMC0)](https://github.com/IOVEYOUMC0).
>
> Original work © LoneDev. Modifications © HuiDu_OwO.
> Licensed under the **GNU GPL v3.0** — see [LICENSE](LICENSE).

## What was rebuilt vs. the original

The upstream plugin does not run on modern Minecraft. This fork re-implements it
on the current Paper stack:

### Platform & Folia

- **Paper 1.21.4 / Java 21** (the original targeted 1.16.5 / Java 8-era APIs).
- **Folia-compatible.** All entity, world and inventory access is routed through
  Paper's **global / region / entity schedulers** (via a small `Sched` wrapper),
  uses **`teleportAsync`** for cross-region moves, and tracks despawns through
  `EntityRemoveEvent`. There are no legacy `Bukkit.getScheduler()` calls that
  throw on Folia.

### Dependencies removed (now runs on stock Paper)

- Dropped **LoneLibs** (the original's proprietary NBT/reflection library),
  **ProtocolLib**, **FastParticles**, and **commons-io**; removed the shade plugin.
- The only hard requirement is Paper itself. **Vault** stays optional (for the shop).
- Deleted the old cross-version helper dumps (`Mat.java` ~1,200 lines,
  `Sound.java` ~1,000 lines) that existed only to bridge 1.16-era enums.

### Mob capture / restore rewritten

- Native serialization via **`entity.getAsString()`** + **`EntitySnapshot.createEntity`** —
  restores the entire entity (attributes, effects, trades, variants, tamed/owner
  state, PDC and custom name) in a single native call, with no reflection library.
- Captured data is stored in the item's **PersistentDataContainer** as a structured
  sub-container, not a flat delimited string.
- The capture/restore subsystem was collapsed from ~3,000 to ~1,000 lines.

### Correctness & compatibility fixes

- Fixed a **fatal startup crash**: the code referenced `commons-lang` (v2), which
  Paper removed from its runtime classpath.
- Vault economy now uses the **typed API** instead of reflection.
- Fixed buy-flow money/delivery edge cases, a malformed craft-recipe crash, a
  Folia region-thread crash in the hologram feedback, a data-particle crash, and a
  horse saddle/armor downgrade on release.
- Region-protection integration via public `MobCatchEvent` / `MobReleaseEvent`.

### Content & config

- **MiniMessage** color syntax in the language files (`<gold>`, `<#rrggbb>`, gradients…).
- Config **migration + versioning**; per-ball permissions registered at runtime.
- Bundled English + Chinese language files.
- The Villager Ball now also catches Iron Golem / Snow Golem / Allay.

## Features

- Throwable balls that catch and release mobs, preserving their full state.
- Per-ball configuration: catchable/blacklisted mobs, catch chance, usages/durability,
  custom head texture or item + model data, particle & sound effects.
- Crafting recipes and an optional in-game shop (buy balls via Vault economy).
- Dropper support: a filled ball dispensed from a dropper releases its mob.
- Boss mobs are excluded from capture.

## Requirements

- **Paper 1.21.4+** (or a fork such as Folia / Purpur).
- **Java 21**.
- **Vault** + an economy plugin — optional, only for the buy feature.

## Building

```bash
mvn clean package
```

Requires JDK 21. The plugin jar is written to `target/PocketMobs.jar`.

## License

[GNU General Public License v3.0](LICENSE).

This project is free software. If you redistribute it (modified or not) you must
keep it licensed under GPL-3.0, preserve the copyright and attribution above, make
the corresponding source available, and state that you changed it.

## Issues

Original issue tracker: <https://github.com/PluginBugs/Issues-PocketMobs/issues>
