# Mob Overrides

Mob overrides let you set specific dice, range factor, or bonus values for individual
mob types or entity tags. They are the highest-priority layer in the attribute
resolution chain — they beat spawn profiles, auto-dice curves, and config defaults.

---

## File Location

```
data/mobdiceattribs/mob_overrides/<name>.json
```

You can have as many files as you like. Each file can target multiple mobs.

> Files must use the `mobdiceattribs` namespace. See [Spawn Profiles — Datapack Namespacing](spawn-profiles.md#datapack-namespacing).

---

## Format

```json
{
  "targets": ["minecraft:zombie", "#minecraft:raiders"],
  "health":         { "dice": 10, "rangeFactor": 1.5, "bonus": 0.0 },
  "speed":          { "dice": 6 },
  "damage":         { "dice": 8,  "rangeFactor": 1.2 },
  "knockback":      { "dice": 6 },
  "armor":          { "dice": 4 },
  "armorToughness": { "dice": 4 },
  "attackSpeed":    { "dice": 6 },
  "followRange":    { "dice": 8 }
}
```

- `targets` — list of mob IDs or entity tags. Prefix tags with `#`.
- All eight attribute sections are optional.
- Within each section, all three fields (`dice`, `rangeFactor`, `bonus`) are optional.
  Omit any field to fall through to the auto-dice curve or config default for that field.

---

## Targeting

### By mob ID

```json
{
  "targets": ["minecraft:ender_dragon"],
  "health": { "dice": 20, "rangeFactor": 3.0 }
}
```

### By entity tag

```json
{
  "targets": ["#minecraft:raiders"],
  "damage": { "dice": 10 }
}
```

### Multiple targets in one file

```json
{
  "targets": ["minecraft:zombie", "minecraft:husk", "minecraft:drowned"],
  "health": { "dice": 8 }
}
```

### Exact ID beats tag

If a mob matches both a tag override and an ID override, the ID override wins
field by field.

---

## Resolution Order

```
Per-mob ID override
       ↓ merge (ID wins per field)
Per-mob tag override
       ↓ merge (tag wins over profile)
Spawn profile
       ↓ merge
Auto-dice curve / config fallback
```

---

## Example Files

The mod ships two example files in the jar:

**`mob_overrides/example.json`** — Ender Dragon with high-variance health and damage:
```json
{
  "targets": ["minecraft:ender_dragon"],
  "health": { "dice": 20, "rangeFactor": 3.0, "bonus": 0.0 },
  "damage": { "dice": 12 }
}
```

**`mob_overrides/example_all_attributes.json`** — Shows all eight fields,
targeting a non-existent mob so it never fires in-game.

---

## Notes

- Multiple files can target the same mob. Last datapack loaded wins (standard
  Minecraft datapack precedence).
- Reloads on `/reload` — no restart needed.
- The `targets` list supports both ID strings and tag strings in the same list.
