# Mob Dice Attributes

> *Every mob. Every spawn. A fresh roll.*

Mob Dice Attributes replaces Minecraft's fixed mob stats with tabletop RPG dice rolls.
A zombie isn't always 20 HP — it might roll 13 or 27. A skeleton might track you from
twice its normal range, or barely notice you across the room. Every encounter is different.

**Balance is preserved by design.** The dice formula is constructed so the average rolled
value always equals the vanilla value. Mobs aren't globally buffed or nerfed — they vary.

---

## ⚔️ Core Features

### 🎲 Dice Rolling for All Mob Attributes
Eight attributes are rolled on spawn using the `xdn + b` formula:
- Max Health, Movement Speed, Attack Damage, Knockback
- Armor, Armor Toughness, Attack Speed, Follow Range

Die type is auto-selected per mob based on its vanilla value (low-HP mobs use d6,
bosses use d20). Everything is configurable per attribute.

### 🏅 Rarity Tiers
Every mob spawn draws from a weighted rarity pool — like an RPG loot table, but for mobs.

| Tier | Default chance | Effect |
|------|---------------|--------|
| Common | 60% | Baseline rolls |
| Uncommon | 25% | Slightly above average |
| Rare | 10% | Blue name, 2× XP, bonus loot |
| Legendary | 5% | Purple name, 5× XP, 3× bonus loot, all stats boosted |

Tier weights and profiles are fully customizable via datapack.

### ⭐ Elite Mobs
Mobs that roll significantly above their average HP are automatically tagged as **Elite** —
given a custom name and (optionally) a visible name tag. Threshold and prefix are configurable.

### 💰 Loot & XP Scaling
Above-average mobs drop more. Configure the XP multiplier and bonus loot rolls globally,
or set them per rarity tier profile. Rare mobs are worth hunting.

### 🪝 Spawn Hooks
Map specific spawn contexts to profiles. Always give raid mobs a legendary profile.
Force spawner mobs to be rare. Or leave it to the rarity draw. Your call.

Valid contexts: `natural`, `spawner`, `chunk_generation`, `structure`, `breeding`,
`mob_summoned`, `jockey`, `event`, `conversion`, `reinforcement`, `triggered`,
`bucket`, `spawn_egg`, `command`, `patrol`

---

## 🗂️ Datapack Customization

Everything is driven by datapacks under `data/mobdiceattribs/`:

| Folder | Purpose |
|--------|---------|
| `spawn_profiles/` | Named stat bundles (name, color, XP, loot, dice overrides) |
| `rarity_tiers/` | Weighted pool of profiles drawn on every spawn |
| `spawn_hooks/` | Context → profile mappings |
| `mob_overrides/` | Per-mob or per-tag dice overrides |
| `dimension_rules/` | Enable/disable rolling or scale stats per dimension |
| `difficulty_rules/` | Scale stats per world difficulty |

All reloaded live with `/reload` — no restart required.

---

## 🖥️ Commands

| Command | Description |
|---------|-------------|
| `/mda reroll <targets>` | Force-reroll matched entities (op level 2) |
| `/mda inspect <target>` | Print current stats, HP ratio, and assigned profile |

---

## ⚙️ Configuration

A full `server.toml` config controls every attribute independently:
- Enable/disable per attribute
- Auto dice selection or manual die type
- Range factor (how swingy the rolls are)
- Mob whitelist / blacklist
- Distance-from-spawn health scaling (disabled by default)
- Elite mob threshold, prefix, and name tag visibility

---

## 🔌 Compatibility

- **Server-side only** — clients do not need the mod installed
- **Jade** / **The One Probe** — custom names are visible on hover
- Reloads on `/reload` — fully compatible with datapack managers

---

## 📖 Documentation

| Page | Link |
|------|------|
| Home | [Wiki](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/home.md) |
| Dice Formula | [How `xdn+b` works](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/dice-formula.md) |
| Spawn Profiles | [Profile format & fields](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/spawn-profiles.md) |
| Rarity Tiers | [Weighted pool setup](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/rarity-tiers.md) |
| Spawn Hooks | [Context-based profiles](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/spawn-hooks.md) |
| Mob Overrides | [Per-mob dice overrides](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/mob-overrides.md) |
| Dimension Rules | [Scale by dimension](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/dimension-rules.md) |
| Difficulty Rules | [Scale by difficulty](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/difficulty-rules.md) |
| Configuration | [Full config reference](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/configuration.md) |
| Commands | [/mda reroll & inspect](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes/blob/neoforge-1.21.1-main/wiki/commands.md) |

---

## 📦 Source & Issues

[GitHub](https://github.com/gottsch/gottsch-minecraft-MobDiceAttributes)
