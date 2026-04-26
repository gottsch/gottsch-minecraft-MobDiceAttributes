# The Dice Formula

Mob Dice Attributes uses the `xdn + b` formula — the same notation used in tabletop
RPGs — to replace Minecraft's fixed mob stats with randomized rolls.

---

## What Does `xdn + b` Mean?

| Part | Meaning |
|------|---------|
| `x` | Number of dice to roll |
| `n` | Size of each die (4, 6, 8, 10, 12, or 20) |
| `b` | A flat bonus added to the result |

**Example:** `3d6 + 10` means roll three six-sided dice and add 10.
- Minimum: 3 + 10 = **13**
- Maximum: 18 + 10 = **28**
- Average: 10.5 + 10 = **20.5** ≈ **20** (a zombie's vanilla HP)

---

## Why Does the Average Always Match Vanilla?

This is the core design guarantee: no matter what you configure, the *average* rolled
value equals the mob's vanilla attribute value. Mobs vary, but the overall difficulty
of the world stays the same on average.

The formula achieves this by computing the bonus `b` to absorb any rounding error:

```
dieAvg       = (n + 1) / 2
variability  = 1.0 / rangeFactor
x            = round(target × variability / dieAvg)
b            = target − x × dieAvg
```

Because `b` is calculated from the target rather than being a user-set value,
`average(xdn + b) = x × dieAvg + b = target` exactly.

---

## What Is `rangeFactor`?

`rangeFactor` controls how swingy the rolls are. It is the main tuning knob.

- **Higher `rangeFactor`** → less variation, rolls cluster closer to the vanilla value
- **Lower `rangeFactor`** → more variation, wider spread between min and max

| rangeFactor | Effect |
|-------------|--------|
| `2.0` (default) | ~50% of the target comes from dice, rest is flat bonus |
| `4.0` | ~25% from dice — very tightly clustered |
| `10.0` | ~10% from dice — almost always near vanilla |

**Example with a zombie (20 HP, d6, rangeFactor = 2.0):**
- `variability = 0.5`, `diceContribution = 10`, `x = 3`, `b = 10`
- Roll: `3d6 + 10` → range **13–28**, average **20**

**Same zombie with rangeFactor = 4.0:**
- `variability = 0.25`, `diceContribution = 5`, `x = 1`, `b = 15`
- Roll: `1d6 + 15` → range **16–21**, average **18.5** ≈ **19**

---

## Auto Dice Selection

By default (`autoDice = true`), the mod picks the die size automatically based on
the mob's vanilla attribute value. Weak mobs get small dice; bosses get big dice.

### Health

| Vanilla HP | Die |
|-----------|-----|
| ≤ 6 | d4 |
| ≤ 15 | d6 |
| ≤ 30 | d8 |
| ≤ 60 | d10 |
| ≤ 120 | d12 |
| > 120 | d20 |

### Speed

| Vanilla speed | Die |
|--------------|-----|
| ≤ 0.15 | d4 |
| ≤ 0.25 | d6 |
| ≤ 0.35 | d8 |
| > 0.35 | d10 |

### Attack Damage

| Vanilla damage | Die |
|---------------|-----|
| ≤ 2 | d4 |
| ≤ 5 | d6 |
| ≤ 10 | d8 |
| ≤ 20 | d10 |
| > 20 | d12 |

### Knockback

| Vanilla knockback | Die |
|------------------|-----|
| ≤ 0.5 | d4 |
| ≤ 1.0 | d6 |
| ≤ 2.0 | d8 |
| > 2.0 | d10 |

### Armor

| Vanilla armor | Die |
|--------------|-----|
| ≤ 2 | d4 |
| ≤ 6 | d6 |
| ≤ 15 | d8 |
| > 15 | d10 |

### Armor Toughness

| Vanilla toughness | Die |
|------------------|-----|
| ≤ 1 | d4 |
| ≤ 5 | d6 |
| > 5 | d8 |

### Attack Speed

| Vanilla attack speed | Die |
|--------------------|-----|
| ≤ 2.0 | d4 |
| ≤ 4.0 | d6 |
| ≤ 8.0 | d8 |
| > 8.0 | d10 |

### Follow Range

| Vanilla follow range | Die |
|--------------------|-----|
| ≤ 10 | d4 |
| ≤ 24 | d6 |
| ≤ 48 | d8 |
| > 48 | d10 |

---

## Fractional Dice (Small Attributes)

Some attributes like movement speed have very small vanilla values (e.g. `0.23` for
a zombie). When the computed number of dice would be less than 1, the mod uses a
single fractional die roll — scaling a d4 or d6 roll by the raw dice count — to
preserve variation without losing the average guarantee.

---

## Resolution Order for Die Type

When the mod picks a die for a roll, it checks in this order:

```
1. Spawn profile's attribute override  (e.g. legendary profile forces d20 for health)
2. Per-mob datapack override           (mob_overrides/ file for this mob type)
3. Auto-dice curve                     (if autoDice = true in config)
4. Config diceType fallback            (if autoDice = false)
```

`rangeFactor` and `bonus` follow the same priority order independently.
