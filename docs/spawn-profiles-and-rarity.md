# Spawn Profiles, Rarity Tiers, and Spawn Hooks

This page explains three related features that let you customize how mobs behave
when they first appear in the world — giving certain mobs stronger stats, special
names, and better loot based on how rare they are or how they spawned.

---

## What Is a Spawn Profile?

A **spawn profile** is a named bundle of settings that gets applied to a mob when
it spawns. Think of it like a character sheet template: you define it once, give it
a name, and then tell the mod when to use it.

Profiles are defined as JSON files in your datapack under:

```
data/mobdiceattribs/spawn_profiles/<name>.json
```

The file's path becomes the profile's ID. For example,
`data/mobdiceattribs/spawn_profiles/legendary.json` has the ID `mobdiceattribs:legendary`.

All spawn profiles, rarity tier files, and spawn hook files must use the
`mobdiceattribs` namespace. The mod only loads files from that namespace so it
doesn't accidentally pick up directories from unrelated mods that happen to use
the same folder name for a different purpose.

This is intentional in Minecraft's datapack system — any datapack can write files
into any namespace. A modpack can add `data/mobdiceattribs/spawn_profiles/legendary.json`
without shipping the mod itself, and the mod will load it normally.

### Profile Fields

All fields are optional — you only need to include the ones you want to override.

```json
{
  "namePrefix":     "Legendary",
  "nameColor":      "dark_purple",
  "xpMultiplier":   5.0,
  "lootBonusRolls": 3,
  "health":         { "dice": 20, "rangeFactor": 1.2, "bonus": 0.0 },
  "speed":          { "dice": 8,  "rangeFactor": 1.5 },
  "damage":         { "dice": 12, "rangeFactor": 1.2 },
  "knockback":      { "dice": 8 },
  "armor":          { "dice": 6,  "rangeFactor": 2.0 },
  "armorToughness": { "dice": 6 },
  "attackSpeed":    { "dice": 8 },
  "followRange":    { "dice": 10, "rangeFactor": 1.5 }
}
```

| Field | What it does |
|-------|-------------|
| `namePrefix` | Text added before the mob's name (e.g. `"Legendary"` → `"Legendary Zombie"`) |
| `nameColor` | Color of the name tag. Uses Minecraft chat color names: `"gold"`, `"dark_purple"`, `"red"`, `"aqua"`, etc. |
| `xpMultiplier` | Multiplies the XP the mob drops. `2.0` = double XP. |
| `lootBonusRolls` | How many extra copies of each item the mob drops. `1` = one extra copy of everything. |
| `health` / `speed` / `damage` / etc. | Per-attribute dice overrides. Same format as `mob_overrides/`. |

Within each attribute section:

| Field | What it does |
|-------|-------------|
| `dice` | Which die to roll: `4`, `6`, `8`, `10`, `12`, or `20`. |
| `rangeFactor` | How spread out the rolls are. Higher = less swingy, closer to average. |
| `bonus` | A flat value added on top of the roll every time. |

### A Minimal Profile

You don't need all eight attributes. This profile just makes a mob hit harder and
drop more XP, without touching anything else:

```json
{
  "namePrefix":   "Brutal",
  "nameColor":    "red",
  "xpMultiplier": 3.0,
  "damage":       { "dice": 12 }
}
```

---

## How Profiles Fit Into the Mod's Rules

When a mob spawns, the mod decides what settings to use in this order (highest
priority first):

```
1. Per-mob datapack override  (data/.../mob_overrides/)
2. Spawn profile              (from rarity tier or spawn hook)
3. Auto-dice curve / config   (global fallback)
```

Settings are merged field by field. If a per-mob override specifies `"dice": 20`
for health but the profile specifies `"rangeFactor": 1.5`, the mob gets both —
the override's dice and the profile's range factor.

If a profile sets `namePrefix`, it replaces the global elite mob name entirely for
that mob. If a profile sets `lootBonusRolls`, it replaces the global loot scaling
for that mob. The global elite and loot systems still fire as fallbacks for mobs
that don't have a profile.

---

## Rarity Tiers

Rarity tiers let you assign profiles by chance. Every time a mob spawns, the mod
rolls once and picks a tier from a weighted list. The tier's profile then gets
applied to that mob.

Define your tier pool in a datapack file at:

```
data/<namespace>/rarity_tiers/default.json
```

```json
{
  "tiers": [
    { "profile": "mobdiceattribs:common",    "weight": 60 },
    { "profile": "mobdiceattribs:uncommon",  "weight": 25 },
    { "profile": "mobdiceattribs:rare",      "weight": 10 },
    { "profile": "mobdiceattribs:legendary", "weight":  5 }
  ]
}
```

The `weight` values don't need to add up to 100 — the mod calculates the
probability automatically. In this example:

| Tier | Chance |
|------|--------|
| Common | 60% |
| Uncommon | 25% |
| Rare | 10% |
| Legendary | 5% |

The `common` profile can be an empty file `{}` if you just want normal behavior
for most mobs:

```json
{}
```

### Enabling Rarity Tiers

In your server config, set:

```toml
[rarityTiers]
enable = true
```

No other config is needed — the tiers themselves live in the datapack, so modpacks
and server owners can swap them out with `/reload`.

---

## Spawn Hooks

Spawn hooks let you assign profiles based on *how* a mob spawned rather than by
chance. This is useful for making raid mobs tougher, or giving spawner-bred mobs
different stats than naturally-spawned ones.

Define your hook map in a datapack file at:

```
data/<namespace>/spawn_hooks/default.json
```

```json
{
  "natural":          null,
  "spawner":          "mobdiceattribs:spawner_profile",
  "chunk_generation": null,
  "event":            "mobdiceattribs:raid_profile",
  "command":          null
}
```

| Context | When it fires |
|---------|--------------|
| `natural` | Mob spawned on its own in the world |
| `spawner` | Mob spawned from a spawner block |
| `chunk_generation` | Mob spawned as part of world generation |
| `event` | Mob spawned as part of a game event (raids, etc.) |
| `command` | Mob spawned via a command |

Set a context to `null` to leave it alone (no profile override for that context).

### Priority: Hooks Beat Tiers

If both a spawn hook and a rarity tier apply to the same mob, the spawn hook wins.
Rarity tiers still fire for contexts that have `null` in the hook map.

---

## Putting It All Together

Here's an example setup that makes raids dangerous, gives spawner mobs a slight
boost, and adds a 15% chance for any naturally-spawned mob to be rare or legendary.

All files below live in your datapack under the `mobdiceattribs` namespace.
Whether the files are shipped with the mod or added by a modpack, the namespace
is always `mobdiceattribs` — that's how the mod knows to load them.

**Profiles:**

`data/mobdiceattribs/spawn_profiles/common.json` — empty, normal behavior
```json
{}
```

`data/mobdiceattribs/spawn_profiles/rare.json`
```json
{
  "namePrefix":     "Rare",
  "nameColor":      "gold",
  "xpMultiplier":   2.0,
  "lootBonusRolls": 1,
  "health": { "dice": 10 },
  "damage": { "dice": 8 }
}
```

`data/mobdiceattribs/spawn_profiles/legendary.json`
```json
{
  "namePrefix":     "Legendary",
  "nameColor":      "dark_purple",
  "xpMultiplier":   5.0,
  "lootBonusRolls": 3,
  "health": { "dice": 20, "rangeFactor": 1.2 },
  "damage": { "dice": 12, "rangeFactor": 1.2 },
  "speed":  { "dice": 8 }
}
```

`data/mobdiceattribs/spawn_profiles/raid_mob.json`
```json
{
  "namePrefix":     "Raider",
  "nameColor":      "red",
  "xpMultiplier":   3.0,
  "lootBonusRolls": 2,
  "health": { "dice": 12, "rangeFactor": 1.5 },
  "damage": { "dice": 10 }
}
```

`data/mobdiceattribs/spawn_profiles/spawner_mob.json`
```json
{
  "health": { "rangeFactor": 1.5 },
  "damage": { "rangeFactor": 1.5 }
}
```

**Rarity tiers** `data/mobdiceattribs/rarity_tiers/default.json`:
```json
{
  "tiers": [
    { "profile": "mobdiceattribs:common",    "weight": 85 },
    { "profile": "mobdiceattribs:rare",      "weight": 12 },
    { "profile": "mobdiceattribs:legendary", "weight":  3 }
  ]
}
```

**Spawn hooks** `data/mobdiceattribs/spawn_hooks/default.json`:
```json
{
  "natural":          null,
  "spawner":          "mobdiceattribs:spawner_mob",
  "chunk_generation": null,
  "event":            "mobdiceattribs:raid_mob",
  "command":          null
}
```

With this setup:
- Raid mobs always get the `raid_mob` profile regardless of rarity tier.
- Spawner mobs always get slightly tighter rolls.
- Naturally-spawned mobs have an 85% chance to be normal, 12% rare, 3% legendary.
- The per-mob `mob_overrides/` system still takes priority over all of this for any
  mob you've specifically configured there.
