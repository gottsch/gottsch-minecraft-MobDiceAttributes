# Mob Dice Attributes — Wiki

Welcome to the Mob Dice Attributes wiki. Use the pages below to learn how the mod
works, how to configure it, and how to create datapacks that customize mob behavior.

---

## Pages

| Page | What it covers |
|------|---------------|
| [Dice Formula](dice-formula.md) | How `xdn + b` works and why average always equals vanilla |
| [Spawn Profiles](spawn-profiles.md) | Named stat bundles applied to mobs on spawn |
| [Rarity Tiers](rarity-tiers.md) | Weighted random profile selection on every spawn |
| [Spawn Hooks](spawn-hooks.md) | Assign profiles by spawn context (natural, spawner, raid…) |
| [Mob Overrides](mob-overrides.md) | Per-mob or per-tag dice overrides via datapack |
| [Dimension Rules](dimension-rules.md) | Scale or disable rolling per dimension |
| [Difficulty Rules](difficulty-rules.md) | Scale or disable rolling per world difficulty |
| [Configuration](configuration.md) | Full `server.toml` reference |
| [Commands](commands.md) | `/mda reroll` and `/mda inspect` |

---

## Quick Start

1. Install the mod on your server. No client install required.
2. Start your server — default config is created automatically in `serverconfig/`.
3. Mobs will begin rolling dice attributes on spawn immediately.
4. To add rarity tiers, create a datapack with a `rarity_tiers/default.json` file
   and set `[rarityTiers] enable = true` in the server config.
5. Use `/mda inspect <target>` to see a mob's rolled stats.

---

## Key Concepts

- **Average is always preserved.** The dice formula is constructed so the rolled
  average always equals the vanilla value. Mobs vary — they aren't globally buffed.
- **Server-side only.** Clients do not need the mod installed.
- **Datapack-driven.** Profiles, tiers, hooks, and overrides all live in datapacks
  and reload live with `/reload`.
- **Resolution order (highest priority first):**
  ```
  Per-mob override  →  Spawn profile  →  Auto-dice curve  →  Config fallback
  ```
