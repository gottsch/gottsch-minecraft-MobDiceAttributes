# Mob Dice Attributes — Feature Ideas

A catalog of ideas for extending the mod, grouped by implementation effort.
Status key: **[done]**, **[sketched]**, **[idea]**.

---

## Low effort (config-driven)

### [done] Per-mob overrides via datapack
Let users specify different dice / rangeFactor / bonus per mob ID or entity tag.
Implemented as datapack JSON files under `data/<namespace>/mob_overrides/`.

- Exact mob ID beats tag match.
- Missing fields fall through to the global config.
- Whitelist/blacklist still gates whether any rolling happens.
- Reload with `/reload`.

Example:
```json
{
  "targets": ["minecraft:ender_dragon"],
  "health": { "dice": 20, "rangeFactor": 3.0, "bonus": 50.0 },
  "damage": { "dice": 12 }
}
```

### [done] Auto-pick die type from vanilla attribute value
Instead of using a single configured `diceType` for every mob, the mod now picks a
"natural-feeling" die per mob based on its vanilla attribute value. Curves are
hardcoded per attribute:

- **Health**: d4 (≤6) → d6 (≤15) → d8 (≤30) → d10 (≤60) → d12 (≤120) → d20 (above)
- **Speed**: d4 (≤0.15) → d6 (≤0.25) → d8 (≤0.35) → d10
- **Damage**: d4 (≤2) → d6 (≤5) → d8 (≤10) → d10 (≤20) → d12
- **Knockback**: d4 (≤0.5) → d6 (≤1) → d8 (≤2) → d10

Resolution order: per-mob datapack override → auto-pick (if `autoDice=true` in
config) → config `diceType` fallback.

### [idea] Datapack-driven dice curves
Let modpack authors shift cutoffs without recompiling by loading the curves from
`data/<namespace>/dice_curves/<attrib>.json`. The hardcoded defaults would still
ship as fallbacks. Useful for "hard mode" packs that want to push everything one
tier up, or to retune for mod-added bosses.

Shape:
```json
{
  "buckets": [
    { "max": 6,   "dice": 4 },
    { "max": 15,  "dice": 6 },
    { "max": 30,  "dice": 8 },
    { "max": 60,  "dice": 10 },
    { "max": 120, "dice": 12 }
  ],
  "default": 20
}
```

### [done] Dimension filter
Rolling can be disabled or scaled per dimension via datapack JSON files under
`data/<namespace>/dimension_rules/`. No config section — feature is inert without datapack files.

- `enable` (optional, default `true`) — set to `false` to skip all rolling in that dimension.
- `multiplier` (optional, default `1.0`) — scales every attribute target before dice are rolled.
  The "average = target × multiplier" invariant is preserved by `computeRolledValue`.
- Gate order in `applyRolls()`: mob whitelist/blacklist → dimension gate → per-attribute rolls.
- Reloads on `/reload`.

Example:
```json
{
  "targets": ["minecraft:the_nether", "minecraft:the_end"],
  "enable": true,
  "multiplier": 1.25
}
```

### [done] Distance-from-spawn scaling
Scales mob **max health only** based on horizontal distance from world spawn.
Controlled via config (`[distanceScaling]` section) — **disabled by default**.

- `enable` (default `false`) — must be explicitly enabled; see conflict warning below
- `blocksPerTier` (default `1000`) — distance between scaling tiers
- `multiplierPerTier` (default `0.1`) — health multiplier increase per tier (+10%)
- `cap` (default `3.0`) — maximum distance multiplier before dim/diff stacking
- Stacks multiplicatively with active dimension and difficulty rule multipliers
- Only health is scaled — speed, damage, and knockback are unaffected

**Conflict warning** (detailed in config comments): mods like Scaling Health, Gradual
Difficulty, and Better Difficulty already do distance scaling. Enabling this alongside
them causes double-scaling that compounds multiplicatively — likely unintended.

### [done] Difficulty scaling
Rolling can be disabled or scaled per world difficulty via datapack JSON files under
`data/<namespace>/difficulty_rules/`. Inert without datapack files.

- Targets: `"peaceful"`, `"easy"`, `"normal"`, `"hard"` (lowercase strings)
- `enable` (optional, default `true`) — `false` suppresses all rolling at that difficulty
- `multiplier` (optional, default `1.0`) — scales every attribute target before rolling
- Multipliers stack multiplicatively with any active dimension rule multiplier
  (e.g. Nether ×1.25 + Hard ×1.5 = ×1.875 combined)
- Gate order in `applyRolls()`: mob whitelist/blacklist → dimension gate → difficulty gate → rolls
- Reloads on `/reload`

Example:
```json
{
  "targets": ["hard"],
  "multiplier": 1.5
}
```

---

## Medium effort

### [done] More attributes
Four additional attributes now support dice rolling alongside health, speed, damage,
and knockback. Each has its own config section with the same full set of options
(`enable`, `autoDice`, `diceType`, `rangeFactor`, `bonus`, `mobWhitelist`, `mobBlacklist`).
All four also support per-mob datapack overrides via `mob_overrides/`.

- **armor** — only rolls for mobs with base armor > 0 (e.g. zombie=2, warden=30)
- **armorToughness** — only rolls for mobs with base toughness > 0 (e.g. warden=3)
- **attackSpeed** — only rolls if base > 0; note: has limited visible effect on most
  hostile mobs as they use AI goals for attack timing, not the attribute directly
- **followRange** — detection/pursuit range (zombie=35, skeleton=16, blaze=48)

`luck` was omitted — it is a player attribute and does not meaningfully affect hostile mobs.

### [done] Elite mobs (outlier tagging)
If a mob rolls above a configurable HP ratio threshold it is marked as "elite":

- Custom name tag (`Elite Zombie`, etc.) with configurable prefix.
- Name always visible above head (configurable).
- Threshold default 1.2× — with `rangeFactor=2.0` the practical max is ~1.35×,
  so values above that will never fire. Recommended range: 1.15 (common) to 1.35 (very rare).
- No glow effect — would show mobs through walls.
- Config section: `[eliteMobs]` — `enable`, `healthThreshold`, `namePrefix`, `showName`.

### [done] Loot scaling
Mobs that rolled above-average health drop bonus loot and XP. Operates independently
of elite tagging — threshold, XP multiplier, and bonus loot rolls are all separate config.

- `mda_hp_ratio` stored in NBT for any mob that rolls above its target HP.
- Config section: `[lootScaling]` — `enable`, `threshold` (default 1.0 = any above-average
  roll), `xpMultiplier` (default 2.0), `lootBonusRolls` (default 1).
- Set `threshold` equal to `eliteMobs.healthThreshold` to restrict bonuses to elite mobs only.

---

## Higher effort

### [done] Reroll command
- `/mda reroll <targets>` — force-rerolls matched entities, bypassing the `rolled` NBT flag.
  Resets HP to stored vanilla value (`mda_base_hp`) before rolling so results don't compound.
  Requires op level 2. Accepts standard entity selectors (`@e[type=minecraft:zombie,distance=..20]`).
- `/mda inspect <target>` — prints current attribute values (HP, speed, damage, knockback,
  armor, armor toughness, attack speed, follow range) plus `mda_hp_ratio` and `mda_base_hp`
  if present. Single-entity selector only.
- Note: only HP is reset to vanilla on reroll — other attributes compound. Full fix requires
  the "Persist rolled values" feature.

### [done] Rarity tiers + Spawn-event hooks (shared profile system)

Both features share a common **spawn profile** primitive. Implement the profile
loader first, then build rarity tiers and spawn hooks on top of it.

#### Spawn profiles
Reusable named bundles stored in `data/<namespace>/spawn_profiles/`. All fields
optional. Attribute sections reuse the existing `AttribOverride` codec.

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

Resolution order (field-by-field merge, highest priority first):
```
per-mob datapack override  (mob_overrides/)
         ↓  merge()
    spawn profile          (rarity tier or spawn hook)
         ↓  merge()
  auto-dice curve / config fallback
```

Profile `namePrefix` replaces `[eliteMobs].namePrefix` entirely for that mob.
Profile loot fields replace `[lootScaling]` for that mob.
Both elite tagging and loot scaling still fire as fallbacks when no profile covers
those fields.

#### Rarity tiers
One global weighted pool. Datapack: `data/<namespace>/rarity_tiers/default.json`.
Config: `[rarityTiers]` with just an `enable` toggle.

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

On spawn: weighted-random pick → look up profile → merge into resolution chain.
A `common` profile can be `{}` (no overrides).

#### Spawn-event hooks
Global context → profile map. Datapack: `data/<namespace>/spawn_hooks/default.json`.
`null` = no profile override for that context (falls through to rarity tier or config).
Spawn hook profile beats rarity tier when both apply.

```json
{
  "natural":          null,
  "spawner":          "mobdiceattribs:spawner_profile",
  "chunk_generation": null,
  "event":            "mobdiceattribs:raid_profile",
  "command":          null
}
```

### [idea] Persist the rolled values
Currently the roll happens on spawn and the NBT flag `rolled` prevents re-rolling.
Two improvements:

- **Store the original vanilla HP** on the entity so future balance changes can
  "undo" the roll cleanly.
- **Expose rolled values** via nameplate tooltip or compat with probe mods
  (Jade, The One Probe).

### [idea] Per-world seeded rolls
Option to make rolls deterministic from a seed + entity UUID, so a given mob always
rolls the same values in a given world. Useful for speedruns, map makers, and
reproducing bug reports.

### [idea] Custom nameplate and glow (Champions-style)
Inspired by the Champions mod — give named mobs (profile or elite) a styled nameplate
and optional glow effect visible to nearby players.

- **Glow color** per profile/tier — e.g. purple glow for legendary, blue for rare.
  Note: vanilla glow (`setGlowingTag`) shows through walls. Either gate it behind a
  config warning (same as distanceScaling) or use a client-side shader/overlay approach
  to avoid the through-walls issue.
- **Nameplate styling** — color, prefix icon, or suffix (e.g. ★ Legendary Zombie ★).
  Requires either a client-side mod or a resource pack for custom fonts/icons.
- **Compat path**: Jade and The One Probe both have plugin APIs — a separate companion
  mod could register a provider that reads `mda_profile` NBT and renders a custom tooltip
  panel (tier badge, stat deltas vs vanilla) without requiring client install of the main mod.

---

## Backlog / speculative

- **Config-driven formula templates**: let users write raw `xdn+b` formulas per mob
  instead of going through rangeFactor arithmetic.
- **Attribute dependency rules**: e.g. "if this mob rolled +50% HP, also give it
  +25% damage" — correlated randomness for more "themed" elites.
- **Web UI / in-game GUI** for editing overrides without touching JSON.
- **Telemetry / debug log** of every roll for tuning.
