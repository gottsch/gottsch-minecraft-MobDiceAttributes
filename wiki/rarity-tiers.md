# Rarity Tiers

Rarity tiers let you assign [Spawn Profiles](spawn-profiles) by chance. Every time
an eligible mob spawns, the mod does one weighted random draw from your tier pool and
applies the winning profile to that mob.

---

## Enabling Rarity Tiers

In `serverconfig/mobdiceattribs-server.toml`:

```toml
[rarityTiers]
enable = true
```

The mod ships a ready-to-use `rarity_tiers/default.json` pool. Enable the config
flag and mobs will immediately start drawing from it.

---

## File Location

```
data/mobdiceattribs/rarity_tiers/<name>.json
```

> Files must use the `mobdiceattribs` namespace. See [Spawn Profiles — Datapack Namespacing](spawn-profiles#datapack-namespacing).

---

## Pool Format

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

- `profile` — the ID of the [Spawn Profile](spawn-profiles) to apply
- `weight` — relative probability. Weights do not need to sum to 100.

### Default pool probabilities

| Tier | Weight | Chance |
|------|--------|--------|
| Common | 60 | 60% |
| Uncommon | 25 | 25% |
| Rare | 10 | 10% |
| Legendary | 5 | 5% |

---

## Customizing the Pool

To change the weights or add tiers, add a `rarity_tiers/default.json` to your own
datapack. Your file replaces the mod's default pool for that datapack's load order.

**Harder server example** — remove common, boost legendary chance:
```json
{
  "tiers": [
    { "profile": "mobdiceattribs:uncommon",  "weight": 50 },
    { "profile": "mobdiceattribs:rare",      "weight": 35 },
    { "profile": "mobdiceattribs:legendary", "weight": 15 }
  ]
}
```

**Subtle variation example** — mostly normal, rare outliers only:
```json
{
  "tiers": [
    { "profile": "mobdiceattribs:common",    "weight": 90 },
    { "profile": "mobdiceattribs:rare",      "weight":  8 },
    { "profile": "mobdiceattribs:legendary", "weight":  2 }
  ]
}
```

---

## Interaction With Spawn Hooks

[Spawn Hooks](spawn-hooks) take priority over rarity tiers. If a spawn hook maps
the mob's spawn context to a profile, the rarity draw is skipped entirely for that
mob. Rarity tiers fire only for contexts that have no hook (or are set to `null`
in the hook file).

---

## Notes

- **One global pool.** All eligible mobs draw from the same tier pool regardless
  of mob type or dimension. Per-mob or per-dimension pools are a potential future feature.
- **Reloads on `/reload`.** No server restart needed after changing tier files.
- If a tier references a profile that doesn't exist, `SpawnProfileRegistry` returns
  an empty profile and no changes are applied — the mob rolls with default settings.
