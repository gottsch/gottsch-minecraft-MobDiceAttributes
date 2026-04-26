# Mob Dice Attributes — Commands

All commands require **operator level 2** or higher.

---

## `/mda reroll <targets>`

Force-rerolls the dice attributes of one or more entities. The entity's HP is reset
to its stored vanilla value before rolling, so results don't compound across multiple
rerolls. The `rolled` NBT flag is cleared first so the roll fires even if the mob was
already rolled on spawn.

**Selector examples**

```
# Reroll the mob you're looking at (nearest single entity, any type)
/mda reroll @e[type=!player,sort=nearest,limit=1]

# Reroll all zombies within 20 blocks
/mda reroll @e[type=minecraft:zombie,distance=..20]

# Reroll every loaded mob (use carefully on busy servers)
/mda reroll @e[type=!player]

# Reroll a specific mob by its UUID
/mda reroll 12345678-1234-1234-1234-123456789abc
```

**Notes**
- Only HP is guaranteed to reset to vanilla before re-rolling. Other attributes
  (speed, damage, etc.) still compound. Full reset for all attributes requires the
  "Persist rolled values" feature (deferred to a later version).
- If the mob was assigned a spawn profile (rarity tier or spawn hook), the profile
  is cleared and a new one is drawn on reroll.

---

## `/mda inspect <target>`

Prints the current attribute values and MDA metadata for a single entity to chat.

**Selector examples**

```
# Inspect the nearest non-player entity
/mda inspect @e[type=!player,sort=nearest,limit=1]

# Inspect a specific mob by UUID
/mda inspect 12345678-1234-1234-1234-123456789abc
```

**Output includes**
- Current attribute values: max health, movement speed, attack damage, knockback
  strength, armor, armor toughness, attack speed, follow range
- `mda_hp_ratio` — rolled HP divided by vanilla HP (e.g. `1.24` = 24% above average)
- `mda_base_hp` — stored vanilla HP used as the reroll baseline
- `mda_profile` — the spawn profile assigned to this mob (e.g. `mobdiceattribs:legendary`),
  or absent if none was applied

**Note:** This command takes a single-entity selector. Using a selector that matches
more than one entity will cause an error — add `limit=1` to be safe.

---

## Typical testing workflow

```
# 1. Spawn a zombie nearby
/summon minecraft:zombie

# 2. Inspect it to see its rolled stats
/mda inspect @e[type=minecraft:zombie,sort=nearest,limit=1]

# 3. Reroll it until you get an interesting result
/mda reroll @e[type=minecraft:zombie,sort=nearest,limit=1]

# 4. Inspect again to compare
/mda inspect @e[type=minecraft:zombie,sort=nearest,limit=1]
```
