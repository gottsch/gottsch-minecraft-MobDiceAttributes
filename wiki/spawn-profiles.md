# Spawn Profiles

A **spawn profile** is a named bundle of settings applied to a mob when it spawns.
Think of it like a character class template: define it once, give it a name, and
tell the mod when to use it via [Rarity Tiers](rarity-tiers) or [Spawn Hooks](spawn-hooks).

---

## File Location

```
data/mobdiceattribs/spawn_profiles/<name>.json
```

The file path becomes the profile's ID. For example:
`data/mobdiceattribs/spawn_profiles/legendary.json` → ID: `mobdiceattribs:legendary`

> All profile files must use the `mobdiceattribs` namespace. See [Datapack Namespacing](#datapack-namespacing).

---

## Profile Fields

All fields are optional — include only what you want to override.

```json
{
  "namePrefix":     "Legendary",
  "nameColor":      "dark_purple",
  "xpMultiplier":   5.0,
  "lootBonusRolls": 3,
  "health":         { "dice": 20, "rangeFactor": 1.2 },
  "speed":          { "dice": 8,  "rangeFactor": 1.5 },
  "damage":         { "dice": 12, "rangeFactor": 1.2 },
  "knockback":      { "dice": 8 },
  "armor":          { "dice": 6,  "rangeFactor": 2.0 },
  "armorToughness": { "dice": 6 },
  "attackSpeed":    { "dice": 8 },
  "followRange":    { "dice": 10, "rangeFactor": 1.5 }
}
```

### Top-level fields

| Field | Type | Description |
|-------|------|-------------|
| `namePrefix` | string | Prepended to the mob's name (e.g. `"Legendary"` → `"Legendary Zombie"`) |
| `nameColor` | string | Minecraft chat color name: `"gold"`, `"dark_purple"`, `"red"`, `"aqua"`, `"blue"`, `"green"`, etc. |
| `xpMultiplier` | number | Multiplies XP dropped. `2.0` = double XP. Overrides `[lootScaling]` config for this mob. |
| `lootBonusRolls` | integer | Extra copies of each item dropped. `1` = one extra of everything. Overrides `[lootScaling]` config. |

### Attribute fields

Each attribute section (`health`, `speed`, `damage`, `knockback`, `armor`,
`armorToughness`, `attackSpeed`, `followRange`) accepts:

| Field | Type | Description |
|-------|------|-------------|
| `dice` | integer | Die size: `4`, `6`, `8`, `10`, `12`, or `20` |
| `rangeFactor` | number | Spread of rolls. Higher = less swingy. See [Dice Formula](dice-formula). |
| `bonus` | number | Flat value added on top of every roll |

---

## Shipped Default Profiles

The mod ships four profiles out of the box for use with the default rarity tier pool:

| Profile | ID | Effect |
|---------|----|--------|
| Common | `mobdiceattribs:common` | Empty — baseline dice rolls, no name or bonuses |
| Uncommon | `mobdiceattribs:uncommon` | Slight 1.1× boost to health, speed, and damage |
| Rare | `mobdiceattribs:rare` | Blue "Rare" name, 2× XP, 1 bonus loot roll |
| Legendary | `mobdiceattribs:legendary` | Purple "Legendary" name, 5× XP, 3 bonus loot rolls, all attributes boosted |

You can override any of these by adding a file with the same path to your own datapack.

---

## How Profiles Merge With Other Settings

Profile fields merge with per-mob overrides field by field. The per-mob override
always wins when both specify the same field:

```
Per-mob override (mob_overrides/)   ← highest priority
       ↓ merge
  Spawn profile
       ↓ merge
  Auto-dice curve / config          ← lowest priority
```

**Example:** If a Zombie override sets `"dice": 20` for health, and the legendary
profile sets `"rangeFactor": 1.2` for health, the zombie gets both — d20 from the
override and rangeFactor 1.2 from the profile.

If a profile sets `namePrefix`, it replaces the global [Elite Mobs](configuration#elite-mobs)
name for that mob. If a profile sets `lootBonusRolls` or `xpMultiplier`, it replaces
the global [Loot Scaling](configuration#loot-scaling) for that mob.

---

## Minimal Profile Examples

Just a name and XP bonus — no stat changes:
```json
{
  "namePrefix":   "Cursed",
  "nameColor":    "dark_red",
  "xpMultiplier": 2.0
}
```

Spawner-buff profile — tighter rolls, no name:
```json
{
  "health": { "rangeFactor": 1.5 },
  "damage": { "rangeFactor": 1.5 }
}
```

---

## Datapack Namespacing

All files under `spawn_profiles/`, `rarity_tiers/`, and `spawn_hooks/` must use
the `mobdiceattribs` namespace. The mod filters out all other namespaces to avoid
accidentally loading files from unrelated mods that happen to use the same folder names.

This is intentional — any datapack can write into the `mobdiceattribs` namespace:

```
your-datapack/
  data/
    mobdiceattribs/
      spawn_profiles/
        my_custom_profile.json    ← loaded by the mod
```

The mod ID in the path (`mobdiceattribs`) is the namespace, not a folder you own.
Modpacks and server owners can add profiles this way without modifying the mod itself.
