# Commands

All commands require **operator level 2** or higher.

---

## `/mda reroll <targets>`

Force-rerolls the dice attributes of one or more entities. All attribute values are
reset to their stored vanilla baselines before rolling, so results don't compound
across multiple rerolls. Any previously assigned spawn profile is cleared and a new
one is drawn.

**Selector examples**

```
# Reroll the nearest mob
/mda reroll @e[type=!player,sort=nearest,limit=1]

# Reroll all zombies within 20 blocks
/mda reroll @e[type=minecraft:zombie,distance=..20]

# Reroll every loaded mob (use carefully on busy servers)
/mda reroll @e[type=!player]
```

---

## `/mda inspect <target>`

Prints current attribute values and MDA metadata for a single entity to chat.
Useful for verifying rolls, checking which profile was assigned, and confirming
spawn hooks are firing correctly.

**Selector examples**

```
# Inspect the nearest mob
/mda inspect @e[type=!player,sort=nearest,limit=1]
```

> Use `limit=1` — this command accepts only a single entity. A selector matching
> multiple entities will fail.

**Output includes:**

| Field | Description |
|-------|-------------|
| HP, Speed, Damage, etc. | Current rolled attribute values |
| `HP ratio` | Rolled HP ÷ vanilla HP (e.g. `1.24` = 24% above average) |
| `Base HP` | Stored vanilla HP used as the reroll baseline |
| `Profile` | Assigned spawn profile ID (e.g. `mobdiceattribs:legendary`) |
| `Spawn type` | How this mob spawned (e.g. `NATURAL`, `SPAWNER`, `EVENT`) |

---

## Typical Testing Workflow

```
# 1. Spawn a zombie
/summon minecraft:zombie

# 2. Check its rolled stats and assigned profile
/mda inspect @e[type=minecraft:zombie,sort=nearest,limit=1]

# 3. Reroll it
/mda reroll @e[type=minecraft:zombie,sort=nearest,limit=1]

# 4. Inspect again to compare
/mda inspect @e[type=minecraft:zombie,sort=nearest,limit=1]
```
