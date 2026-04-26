# Dimension Rules

Dimension rules let you disable dice rolling entirely in specific dimensions, or
scale all attribute targets up or down before rolls happen. A 1.25× multiplier in
the Nether means mobs there roll against 25% higher targets on average — making
them tougher without changing the variance.

---

## File Location

```
data/mobdiceattribs/dimension_rules/<name>.json
```

The feature is inert without any datapack files — no config flag required.

> Files must use the `mobdiceattribs` namespace. See [Spawn Profiles — Datapack Namespacing](spawn-profiles.md#datapack-namespacing).

---

## Format

```json
{
  "targets": ["minecraft:the_nether", "minecraft:the_end"],
  "enable": true,
  "multiplier": 1.25
}
```

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `targets` | list | required | Dimension resource IDs |
| `enable` | boolean | `true` | `false` suppresses all rolling in these dimensions |
| `multiplier` | number | `1.0` | Scales every attribute target before rolling. `1.25` = 25% tougher on average. |

---

## How Multipliers Stack

Dimension multipliers stack multiplicatively with [Difficulty Rules](difficulty-rules.md)
multipliers and [Distance Scaling](configuration.md#distance-scaling) (health only):

```
healthMultiplier  = dimMultiplier × diffMultiplier × distanceMultiplier
otherMultiplier   = dimMultiplier × diffMultiplier
```

**Example:** Nether (×1.25) + Hard difficulty (×1.5) = ×1.875 combined for all
attributes, ×1.875+ for health if distance scaling is also active.

---

## Gate Order

If a dimension rule has `enable: false`, rolling is skipped entirely for that
dimension — the mob spawns with vanilla attribute values.

```
1. Dimension gate   — if disabled, skip all rolling
2. Difficulty gate  — if disabled, skip all rolling
3. Roll attributes  — using combined multiplier
```

---

## Example: Nether and End are Harder

```json
{
  "targets": ["minecraft:the_nether", "minecraft:the_end"],
  "enable": true,
  "multiplier": 1.25
}
```

## Example: Disable Rolling in the Overworld

```json
{
  "targets": ["minecraft:overworld"],
  "enable": false
}
```

---

## Notes

- Reloads on `/reload` — no restart needed.
- A dimension with no matching rule uses `enable: true` and `multiplier: 1.0` (default behavior).
- Multiple files can target the same dimension — last datapack loaded wins.
