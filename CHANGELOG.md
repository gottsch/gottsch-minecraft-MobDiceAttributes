# Changelog for Mob Dice Attributes 1.21.1

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.0.0] - 2026-04-25

### ➕ Added

- **Improved Dice Algorithm:**
  - Die count (`x`) is now computed from the mob's vanilla attribute value and `rangeFactor`, then the bonus (`b`) absorbs the rounding error so the rolled average is always exactly equal to the vanilla value — no more systematic buff/nerf from rounding drift
  - Formula: `xdn + b` where `b = target − x · dieAvg`

- **Auto Dice Type Selection:**
  - New `autoDice` config option (default `true`) per attribute section — picks the most "natural-feeling" die for each mob automatically based on its vanilla attribute value
  - Health curve: d4 (≤6 HP) → d6 (≤15) → d8 (≤30) → d10 (≤60) → d12 (≤120) → d20
  - Speed curve: d4 (≤0.15) → d6 (≤0.25) → d8 (≤0.35) → d10
  - Damage curve: d4 (≤2) → d6 (≤5) → d8 (≤10) → d10 (≤20) → d12
  - Knockback curve: d4 (≤0.5) → d6 (≤1.0) → d8 (≤2.0) → d10
  - Set `autoDice = false` to revert to the global `diceType` fallback

- **Per-mob Datapack Overrides:**
  - Override dice / rangeFactor / bonus per mob ID or entity tag via datapack JSON files under `data/<namespace>/mob_overrides/`
  - Partial overrides supported — omit any field to fall through to the auto-pick or global config value
  - Tag support via `#` prefix (e.g. `#minecraft:raiders`)
  - Reloads on `/reload` with no restart required
  - Example file shipped in jar: `data/mobdiceattribs/mob_overrides/example.json`
  - Resolution order: datapack override → auto-pick → config diceType fallback

- **Dimension Filter:**
  - Rolling can be disabled or scaled per dimension via datapack JSON files under `data/<namespace>/dimension_rules/`
  - `enable` field (optional, default `true`) — set to `false` to suppress all rolling in that dimension
  - `multiplier` field (optional, default `1.0`) — scales every attribute target before dice are rolled
  - Example file shipped in jar: `data/mobdiceattribs/dimension_rules/example.json`
  - Reloads on `/reload`

- **Difficulty Scaling:**
  - Rolling can be disabled or scaled per world difficulty via datapack JSON files under `data/<namespace>/difficulty_rules/`
  - Targets: `"peaceful"`, `"easy"`, `"normal"`, `"hard"` (lowercase)
  - Multipliers stack multiplicatively with any active dimension rule multiplier
  - Example file shipped in jar: `data/mobdiceattribs/difficulty_rules/example.json`
  - Reloads on `/reload`

- **Distance-from-Spawn Health Scaling:**
  - Scales mob max health based on horizontal distance from world spawn (health only)
  - Controlled via `[distanceScaling]` config section — **disabled by default**
  - Config fields: `enable` (default `false`), `blocksPerTier` (default `1000`), `multiplierPerTier` (default `0.1`), `cap` (default `3.0`)
  - Detailed conflict warning in config comments: enabling alongside mods like Scaling Health or Gradual Difficulty causes double-scaling

- **Additional Attributes:**
  - Four new attributes now support dice rolling, each with its own full config section and datapack override support:
    - `armor` — only rolls for mobs with base armor > 0 (zombie=2, warden=30)
    - `armorToughness` — only rolls for mobs with base armor toughness > 0 (warden=3)
    - `attackSpeed` — only rolls if base > 0; note: limited visible effect on most hostile mobs
    - `followRange` — detection/pursuit range (zombie=35, skeleton=16, blaze=48)
  - Example file `mob_overrides/example_all_attributes.json` shipped in jar showing all eight attributes

- **Elite Mobs:**
  - Mobs that roll above a configurable HP ratio threshold are tagged as "elite"
  - Elite mobs display a custom name tag (e.g. `Elite Zombie`) that is always visible
  - Config section: `[eliteMobs]` — `enable`, `healthThreshold` (default `1.2`), `namePrefix` (default `"Elite"`), `showName` (default `true`)
  - Threshold note: with `rangeFactor=2.0` the practical rolled maximum is ~1.35×; values above that will never fire. Recommended range: 1.15–1.35
  - Profile `namePrefix` (see Spawn Profiles) takes priority over the elite name when set

- **Loot Scaling:**
  - Mobs that roll above-average health drop bonus loot rolls and/or extra XP on death
  - `mda_hp_ratio` stored in entity NBT for any mob that rolls above its vanilla HP target
  - Config section: `[lootScaling]` — `enable`, `threshold` (default `1.0`), `xpMultiplier` (default `2.0`), `lootBonusRolls` (default `1`)
  - Set `threshold` equal to `eliteMobs.healthThreshold` to restrict bonuses to elite mobs only
  - Spawn profile `xpMultiplier` / `lootBonusRolls` fields (see Spawn Profiles) override config for that mob

- **Spawn Profiles:**
  - Reusable named attribute bundles stored in `data/<namespace>/spawn_profiles/`
  - Fields: `namePrefix`, `nameColor`, `xpMultiplier`, `lootBonusRolls`, and all eight `AttribOverride` sections (all optional)
  - Profile overrides merge on top of mob-specific overrides; profile name/loot fields replace the corresponding elite/lootScaling config for that mob
  - Four default profiles shipped in jar: `common`, `uncommon`, `rare` (blue, 2× XP, 1 bonus roll), `legendary` (purple, 5× XP, 3 bonus rolls, all attributes boosted)
  - Example reference profile: `data/mobdiceattribs/spawn_profiles/example_legendary.json`
  - Reloads on `/reload`

- **Rarity Tiers:**
  - On every mob spawn, a weighted random roll selects a spawn profile from a global tier pool
  - Tier pool defined via `data/<namespace>/rarity_tiers/` — `{ "tiers": [ { "profile": "...", "weight": N }, ... ] }`
  - Weights are proportional (do not need to sum to 100)
  - Config section: `[rarityTiers]` — `enable` toggle only
  - Example pool shipped in jar: `data/mobdiceattribs/rarity_tiers/example.json` (60/25/10/5 weights for common/uncommon/rare/legendary)
  - Reloads on `/reload`

- **Spawn Hooks:**
  - Map specific spawn contexts (natural, spawner, raid event, command, etc.) to a fixed spawn profile, bypassing rarity tier selection for that context
  - Defined via `data/<namespace>/spawn_hooks/` — JSON object with context name keys and profile `ResourceLocation` values; omit a key or set to `null` to fall through to rarity tiers
  - Valid contexts: `natural`, `spawner`, `chunk_generation`, `structure`, `breeding`, `mob_summoned`, `jockey`, `event`, `conversion`, `reinforcement`, `triggered`, `bucket`, `spawn_egg`, `command`, `patrol`
  - Spawn hook profile beats rarity tier when both apply
  - Config section: `[spawnHooks]` — `enable` toggle only
  - Example file shipped in jar: `data/mobdiceattribs/spawn_hooks/example.json`
  - Reloads on `/reload`

- **Commands:**
  - `/mda reroll <targets>` — force-rerolls matched entities; resets HP to stored vanilla value (`mda_base_hp`) before rolling so results don't compound. Requires op level 2. Accepts standard entity selectors.
  - `/mda inspect <target>` — prints current attribute values plus `mda_hp_ratio`, `mda_base_hp`, and assigned profile. Single-entity selector only.

- **Jade Mod Integration:**
  - `jade` added as a runtime dependency (`implementation`) in `build.gradle`

### ⚙️ Changed

- All eight `roll*()` methods now accept a `SpawnProfile` parameter; attribute overrides merge as `profile.attrib().merge(mobOverride)` so per-mob overrides beat profile
- `applyRolls()` gate order: mob whitelist/blacklist → dimension gate → difficulty gate → profile resolution → per-attribute rolls
- Distance multiplier (health only) stacks on top of the combined dimension × difficulty multiplier
- All `SimpleJsonResourceReloadListener` implementations now filter by `MDA.MOD_ID` namespace — only `data/mobdiceattribs/` files are loaded, preventing collisions with other mods

---

## [1.0.0] - 2026-04-23

### ⚙️ Changed

- **Platform port: Forge 1.20.1 → NeoForge 1.21.1**
  - Build system migrated to `net.neoforged.moddev` (Gradle plugin)
  - `@Mod` class now uses constructor injection of `IEventBus` and `ModContainer`
  - `Mod.EventBusSubscriber` replaced with `@EventBusSubscriber(bus = Bus.GAME)`
  - `ForgeConfigSpec` replaced with `ModConfigSpec` throughout
  - `ResourceLocation.fromNamespaceAndPath()` API adopted
  - Attributes API updated: `Attributes.X` returns `Holder<Attribute>`; `.value()` used to reach `RangedAttribute`
  - GottschCore package updated: `mod.gottsch.forge.gottschcore.*` → `mod.gottsch.neo.gottschcore.*`
  - GottschCore artifact updated: `gottschcore` → `gottschcore-neoforge`
  - `neoforge.mods.toml` moved to `src/main/templates/META-INF/` with `generateModMetadata` task for property substitution
  - Mod config registered directly via `modContainer.registerConfig()` — removed premature `ModConfigEvent` validation listener that caused "Cannot get config value before config is loaded" crash
  - `isValidEntity()` simplified to `entity instanceof Monster` (Enemy interface removed in 1.21)
  - Gradle wrapper pinned to 8.12.1 for compatibility with moddev 2.0.78
  - All dependencies declared `side = "SERVER"` — mod is server-only and clients can connect without it installed
