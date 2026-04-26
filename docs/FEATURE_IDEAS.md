# Mob Dice Attributes — Feature Ideas

A running list of features worth considering, grouped by implementation effort.
Items marked **[done]** have already shipped; everything else is open.

---

## Low effort / config-driven

### Per-mob overrides *(done — datapack-based)*
Let users specify different dice / rangeFactor / bonus for specific mobs
(or entity tags), falling back to the global attribute config for any field
they don't override.

Implemented as a datapack loader at `data/<namespace>/mob_overrides/*.json`.
See the example file shipped in the jar.

### Dimension filter
Restrict attribute rolling to specific dimensions, or scale the formula per
dimension. Example: mobs in the Nether get +50% HP variability; mobs in the
Overworld are unchanged.

Config shape: `dimensionMultipliers = { "minecraft:the_nether" = 1.5 }`.

### Distance-from-spawn scaling
Multiply the target value by `1 + distance / N` so mobs get tougher the
further from world spawn. Classic progression driver — makes exploration
feel meaningful without any per-mob tuning.

### Difficulty scaling
Tie `variability` (or a flat multiplier) to the vanilla `Difficulty` enum.
HARD gets bigger dice and beefier bonuses; EASY gets tamer rolls.

---

## Medium effort

### More attributes
The current `computeRolledValue()` helper is attribute-agnostic — any
numeric attribute with a sensible base value can plug in. Candidates:
- `generic.armor`
- `generic.armor_toughness`
- `generic.attack_speed`
- `generic.follow_range`

Each would just need a new config section and a `rollX` method that
mirrors the existing pattern.

### Elite name tags + glow
After rolling, compare the result against the mob's expected average.
If it's more than, say, 1.5 standard deviations above, rename it
("Elite Zombie", "Veteran Skeleton") and optionally apply a glow effect.
Makes the randomness visible and turns lucky rolls into memorable
encounters.

### Loot / XP scaling
Mobs that rolled above-average HP drop proportionally more XP and/or
extra loot. Rewards the player for the extra effort; costs nothing
for below-average mobs.

---

## Higher effort

### `/mda reroll` command
An admin/testing command that re-rolls attributes on selected entities:

```
/mda reroll @e[type=zombie,distance=..16]
```

Useful for testing formula changes, and genuinely fun as a creative-mode
toy.

### Tiers / rarity system
Roll a **rarity** first (common / rare / legendary) using weighted
probabilities, then pick a dice profile per tier. Gives much more
controllable distribution than a single bell-curve — modpack authors
can guarantee "1 in 50 zombies is a mini-boss" rather than relying on
statistical outliers.

### Persistent roll display
Expose the rolled values through a nameplate tooltip or probe-compat
integration (Jade, TheOneProbe). Players love numbers; letting them
see "HP: 24 (rolled 2d8+11)" makes the system feel tangible rather
than magical.

---

## Ideas intentionally not pursued

- **Client-sync of override data**: the mod is server-authoritative
  (see `neoforge.mods.toml` → `side="SERVER"`). Clients don't need the
  rolled formulas, only the resulting attribute values, which vanilla
  already syncs.
- **GUI for editing overrides in-game**: JSON in datapacks is the Minecraft-native
  way. A custom GUI would be a lot of code for little benefit over editing
  a file and running `/reload`.
