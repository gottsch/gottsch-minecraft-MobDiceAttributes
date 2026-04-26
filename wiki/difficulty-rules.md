# Difficulty Rules

Difficulty rules let you scale all attribute targets up or down based on the world's
current difficulty setting, or disable rolling entirely on specific difficulties.

---

## File Location

```
data/mobdiceattribs/difficulty_rules/<name>.json
```

The feature is inert without any datapack files — no config flag required.

> Files must use the `mobdiceattribs` namespace. See [Spawn Profiles — Datapack Namespacing](spawn-profiles.md#datapack-namespacing).

---

## Format

```json
{
  "targets": ["hard"],
  "enable": true,
  "multiplier": 1.5
}
```

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `targets` | list | required | Difficulty names (lowercase): `"peaceful"`, `"easy"`, `"normal"`, `"hard"` |
| `enable` | boolean | `true` | `false` suppresses all rolling at these difficulties |
| `multiplier` | number | `1.0` | Scales every attribute target before rolling |

---

## How Multipliers Stack

Difficulty multipliers stack multiplicatively with [Dimension Rules](dimension-rules.md)
multipliers:

```
combined = dimMultiplier × diffMultiplier
```

**Example:** Nether (×1.25) + Hard (×1.5) = ×1.875 combined.

---

## Example: Scale by Difficulty

```json
{
  "targets": ["easy"],
  "multiplier": 0.75
}
```
```json
{
  "targets": ["hard"],
  "multiplier": 1.5
}
```

Two separate files. Easy mobs roll 25% weaker on average; Hard mobs roll 50% stronger.

## Example: No Rolling on Peaceful

```json
{
  "targets": ["peaceful"],
  "enable": false
}
```

---

## Notes

- Reloads on `/reload` — no restart needed.
- A difficulty with no matching rule uses `enable: true` and `multiplier: 1.0`.
- Multiple difficulties can be listed in a single file's `targets` array.
