# Configuration

All server-side settings live in `serverconfig/mobdiceattribs-server.toml`.
The file is created automatically on first launch.

---

## Attribute Sections

Each of the eight attributes has its own config section with the same set of fields.

```toml
[health]
enable = true
autoDice = true
diceType = 6
rangeFactor = 2.0
bonus = 0.0
mobWhitelist = []
mobBlacklist = []
```

| Field | Default | Description |
|-------|---------|-------------|
| `enable` | `true` | Enables dice rolling for this attribute |
| `autoDice` | `true` | Picks die type automatically by vanilla value. See [Dice Formula](dice-formula#auto-dice-selection). |
| `diceType` | `6` | Fallback die when `autoDice = false`. Valid values: `4`, `6`, `8`, `10`, `12`, `20` |
| `rangeFactor` | `2.0` | Controls spread of rolls. Higher = less swingy. See [Dice Formula](dice-formula#what-is-rangefactor). |
| `bonus` | `0.0` | Flat value added to every roll for this attribute |
| `mobWhitelist` | `[]` | If non-empty, only these mobs roll this attribute. Example: `["minecraft:zombie"]` |
| `mobBlacklist` | `[]` | Mobs that never roll this attribute. Used when whitelist is empty. |

The eight sections are: `[health]`, `[speed]`, `[damage]`, `[knockback]`,
`[armor]`, `[armorToughness]`, `[attackSpeed]`, `[followRange]`.

> **Note:** `armor`, `armorToughness`, and `attackSpeed` only roll for mobs that
> have a non-zero vanilla value for that attribute.

---

## Elite Mobs

Mobs that roll significantly above their vanilla HP are tagged as "elite" — given
a custom name and optionally a visible floating name tag.

```toml
[eliteMobs]
enable = true
healthThreshold = 1.2
namePrefix = "Elite"
showName = false
```

| Field | Default | Description |
|-------|---------|-------------|
| `enable` | `true` | Enables elite mob tagging |
| `healthThreshold` | `1.2` | HP ratio (rolled ÷ vanilla) required to become elite. With default `rangeFactor = 2.0`, the practical maximum is ~1.35×. Recommended range: 1.15–1.35. |
| `namePrefix` | `"Elite"` | Prefix added to the mob's name (e.g. `"Elite Zombie"`) |
| `showName` | `false` | When `true`, the name tag floats above the mob's head permanently. When `false`, the name is still set and visible on hover via Jade or The One Probe. |

> Spawn profile `namePrefix` takes priority over the elite name. Elite tagging only
> fires as a fallback when no profile has set a name.

---

## Loot Scaling

Mobs that roll above-average HP drop bonus loot and extra XP.

```toml
[lootScaling]
enable = true
threshold = 1.0
xpMultiplier = 2.0
lootBonusRolls = 1
```

| Field | Default | Description |
|-------|---------|-------------|
| `enable` | `true` | Enables loot and XP scaling |
| `threshold` | `1.0` | Minimum HP ratio to trigger bonuses. `1.0` = any above-average roll. Set equal to `eliteMobs.healthThreshold` to restrict to elite mobs only. |
| `xpMultiplier` | `2.0` | Multiplies XP dropped by qualifying mobs |
| `lootBonusRolls` | `1` | Extra copies of each item dropped by qualifying mobs |

> Spawn profile `xpMultiplier` and `lootBonusRolls` override these config values
> for mobs assigned a profile.

---

## Rarity Tiers

```toml
[rarityTiers]
enable = true
```

Enables the weighted rarity draw on every mob spawn. Requires a `rarity_tiers/`
datapack file to have any effect. See [Rarity Tiers](rarity-tiers).

---

## Spawn Hooks

```toml
[spawnHooks]
enable = true
```

Enables context-based profile assignment. Requires a `spawn_hooks/` datapack file.
See [Spawn Hooks](spawn-hooks).

---

## Distance Scaling

Scales mob **max health only** based on horizontal distance from world spawn.
**Disabled by default** — read the warning before enabling.

```toml
[distanceScaling]
enable = false
blocksPerTier = 1000
multiplierPerTier = 0.1
cap = 3.0
```

| Field | Default | Description |
|-------|---------|-------------|
| `enable` | `false` | Enables distance-based health scaling |
| `blocksPerTier` | `1000` | Horizontal distance between scaling tiers |
| `multiplierPerTier` | `0.1` | Health multiplier added per tier (+10% per 1000 blocks) |
| `cap` | `3.0` | Maximum distance multiplier before dimension/difficulty stacking |

> **Warning:** Mods like Scaling Health, Gradual Difficulty, and Better Difficulty
> already implement distance scaling. Enabling this alongside them causes
> double-scaling that compounds multiplicatively — likely unintended.
> Enable only if no other distance-scaling mod is present.
