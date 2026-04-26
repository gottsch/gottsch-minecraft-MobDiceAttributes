# Spawn Hooks

Spawn hooks let you assign a [Spawn Profile](spawn-profiles.md) based on *how* a mob
spawned rather than by chance. This lets you make raid mobs always dangerous, or give
spawner mobs slightly tighter rolls, regardless of what the rarity tier draw would
have given them.

---

## Enabling Spawn Hooks

In `serverconfig/mobdiceattribs-server.toml`:

```toml
[spawnHooks]
enable = true
```

---

## File Location

```
data/mobdiceattribs/spawn_hooks/<name>.json
```

> Files must use the `mobdiceattribs` namespace. See [Spawn Profiles — Datapack Namespacing](spawn-profiles.md#datapack-namespacing).

---

## Hook File Format

The file is a JSON object mapping spawn context names to profile IDs.
Omit a context or set it to `null` to fall through to [Rarity Tiers](rarity-tiers.md).

```json
{
  "natural":          null,
  "spawner":          "mobdiceattribs:rare",
  "chunk_generation": null,
  "event":            "mobdiceattribs:legendary",
  "command":          null
}
```

---

## Valid Spawn Contexts

| Context | When it fires |
|---------|--------------|
| `natural` | Mob spawned on its own in the world |
| `spawner` | Mob spawned from a spawner block |
| `chunk_generation` | Mob spawned as part of world generation |
| `structure` | Mob spawned as part of a structure |
| `breeding` | Mob created by breeding two parents |
| `mob_summoned` | Mob summoned by another mob (e.g. reinforcements) |
| `jockey` | Mob spawned as a jockey (e.g. spider jockey) |
| `event` | Mob spawned as part of a game event (raids, etc.) |
| `conversion` | Mob created by conversion (e.g. zombie villager) |
| `reinforcement` | Zombie calling for reinforcements |
| `triggered` | Spawned by a trigger mechanism |
| `bucket` | Released from a bucket (fish, axolotl, etc.) |
| `spawn_egg` | Spawned via spawn egg |
| `command` | Spawned via `/summon` or similar command |
| `patrol` | Spawned as part of a pillager patrol |

---

## Priority: Hooks Beat Tiers

Spawn hooks take priority over rarity tiers. If a hook maps a context to a profile,
that profile is used and the rarity draw is skipped. Set a context to `null` to let
rarity tiers handle it normally.

```
Spawn Hook (if context is mapped)
       ↓  falls through if null
  Rarity Tier draw
       ↓  falls through if disabled or empty
  No profile (default dice rolls apply)
```

---

## Example: Raid & Spawner Setup

```json
{
  "event":   "mobdiceattribs:raid_mob",
  "spawner": "mobdiceattribs:spawner_mob"
}
```

- All raid mobs always get the `raid_mob` profile.
- All spawner mobs always get the `spawner_mob` profile.
- All other spawn contexts still draw from the rarity tier pool.

---

## Notes

- Reloads on `/reload` — no restart needed.
- Spawn type is captured on spawn and stored in the mob's NBT as `mda_spawn_type`.
  You can verify it with `/mda inspect`.
- If a hook references a profile that doesn't exist, no profile is applied and the
  mob rolls with default settings.
