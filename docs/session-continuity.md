# Session Continuity — Mob Dice Attributes

Use this document to resume work in a new Claude session.

---

## Project Overview

**Mod:** Mob Dice Attributes (`mobdiceattribs`)
**Platform:** NeoForge 1.21.1
**Java package root:** `mod.gottsch.forge.mda`
**Local repo:** `C:\Development\workspace\git\MobDiceAttributes-1.20.1`
**Branch:** `neoforge-1.21.1-main`
**Version:** 2.0.0
**Dependency:** GottschCore `gottschcore-neoforge` 2.6.0

### What the mod does

When a hostile mob spawns, it rolls RPG dice (`xdn + b`) to set its attributes —
health, speed, attack damage, knockback, armor, armor toughness, attack speed, and
follow range — instead of using vanilla's fixed values. The rolled *average* always
matches vanilla so balance isn't affected, but individual mobs vary. Rolls can be
scaled or suppressed per dimension and per difficulty via datapack rules, and health
can optionally scale with distance from world spawn. Mobs can be assigned a **spawn
profile** (via rarity tier weighted-random draw or spawn-event hook) that overrides
attribute dice, naming, loot, and XP for that mob. Elite mobs and loot scaling provide
additional outlier rewards. Commands allow force-rerolling and inspecting entities.

---

## Key Design Decisions (do not change without good reason)

1. **Average always equals target.** The bonus `b = target − x · dieAvg` absorbs
   rounding drift. Do NOT use the old formula (`b = target / rangeFactor`, rounding
   after) — it drifted off-target.

2. **Server-only.** No client install required. The mod registers no network payloads;
   NeoForge 1.21.1 makes it automatically client-optional. All dependency entries in
   `neoforge.mods.toml` are `side = "SERVER"`.

3. **No `ModConfigEvent` listener.** A premature validation call during config load
   caused `IllegalStateException: Cannot get config value before config is loaded`.
   The `validate()` method in `Config.java` exists but is never called. `defineInList`
   auto-corrects invalid `diceType` values.

4. **Gradle 8.12.1 pinned.** `moddev 2.0.78` is incompatible with Gradle 9's changed
   reflection APIs. Do not upgrade the Gradle wrapper without testing IDE sync.

5. **Datapack overrides beat auto-pick, which beats config.** Resolution order for
   die type: per-mob datapack `dice` field → `autoDice` curve → config `diceType`.

6. **Whitelist/blacklist gates all rolling.** Overrides only apply to mobs that pass
   the existing `mobWhitelist` / `mobBlacklist` check in `applyRolls()`.

7. **Namespace filtering on all reload listeners.** All `SimpleJsonResourceReloadListener`
   subclasses skip files whose namespace ≠ `MDA.MOD_ID`. This prevents collision with
   other mods that happen to have directories of the same name.

8. **Spawn profile merge order (highest priority wins per field):**
   ```
   per-mob datapack override  (mob_overrides/)
            ↓  merge()
       spawn profile          (rarity tier or spawn hook)
            ↓  merge()
     auto-dice curve / config fallback
   ```
   Implemented as `profile.attrib().merge(mobOverride)` — the mob override argument
   wins field-by-field.

9. **HP base stored in NBT.** `mda_base_hp` is written on the first health roll and
   used by reroll to reset to vanilla before re-rolling so HP doesn't compound. Other
   attributes still compound on reroll (known limitation).

10. **Profile name beats elite name.** `rollHealth()` calls `markAsElite()` only if
    `!data.getBoolean("mda_named")` — profile name is applied first, elite fires as
    fallback only.

---

## File Map

### Core mod

| File | Purpose |
|------|---------|
| `MDA.java` | `@Mod` entry point. Registers configs + `CommonSetup`. |
| `core/config/Config.java` | `ModConfigSpec` definitions. `AttribConfig` base with `enable`, `autoDice`, `diceType`, `rangeFactor`, `bonus`, `mobWhitelist`, `mobBlacklist`. Eight attribute subclasses. Plus `DistanceScalingConfig`, `EliteMobsConfig`, `LootScalingConfig`, `RarityTiersConfig`, `SpawnHooksConfig`. |
| `core/event/WorldEvents.java` | Game event handlers: `onFinalizeSpawn` (stores `mda_spawn_type`), `onJoin` (calls `applyRolls`), `onLivingDrops` (bonus loot), `onExperienceDrop` (XP multiplier), `onRegisterCommands`. |
| `core/event/ReloadListeners.java` | Registers all six reload listeners on `AddReloadListenerEvent`. |
| `core/manager/DiceAttributeManger.java` | Main logic. `applyRolls()` → resolveProfile → dimension/difficulty gates → eight roll methods. `resolveProfile()` checks spawn hooks then rarity tiers. `reroll()` public entry point for command. |
| `core/manager/DiceCurves.java` | Hardcoded per-attribute value→dice-type curves. `autoDice(Attrib, double)` dispatcher. |
| `core/enums/DiceType.java` | Enum D4/D6/D8/D10/D12/D20, each with `getAvg()`. `validate()` corrects bad config values. |
| `core/command/MdaCommand.java` | Brigadier commands: `/mda reroll <targets>` (op 2, multi-entity) and `/mda inspect <target>` (single-entity). |

### Datapack override system

| File | Purpose |
|------|---------|
| `core/data/AttribOverride.java` | Record: `Optional<Integer> dice`, `Optional<Double> rangeFactor`, `Optional<Double> bonus`. Codec + `EMPTY` + `merge()`. |
| `core/data/MobOverride.java` | Record: `List<String> targets` + eight `AttribOverride` fields. Codec. |
| `core/data/MobOverrideRegistry.java` | Static volatile `Snapshot` (maps by ID and tag). `lookup(EntityType, Attrib)` merges tag → id. |
| `core/data/MobOverrideReloadListener.java` | `mob_overrides/`, namespace-filtered, parses `MobOverride.CODEC`. |
| `core/data/DimensionRule.java` | Record: `List<String> targets`, `Optional<Boolean> enable`, `Optional<Double> multiplier`. Codec + `EMPTY`. |
| `core/data/DimensionRuleRegistry.java` | Static volatile `Map<ResourceLocation, DimensionRule>`. |
| `core/data/DimensionRuleReloadListener.java` | `dimension_rules/`, namespace-filtered. |
| `core/data/DifficultyRule.java` | Record: targets/enable/multiplier. Codec + `EMPTY`. |
| `core/data/DifficultyRuleRegistry.java` | Static volatile `Map<Difficulty, DifficultyRule>`. |
| `core/data/DifficultyRuleReloadListener.java` | `difficulty_rules/`, namespace-filtered, parses lowercase difficulty names. |
| `core/data/SpawnProfile.java` | Record: `Optional<String> namePrefix/nameColor`, `Optional<Double> xpMultiplier`, `Optional<Integer> lootBonusRolls`, eight `AttribOverride` fields. Codec + `EMPTY` + `isEmpty()`. |
| `core/data/SpawnProfileRegistry.java` | Static volatile `Map<ResourceLocation, SpawnProfile>`. `lookup()` returns `EMPTY` on miss. |
| `core/data/SpawnProfileReloadListener.java` | `spawn_profiles/`, namespace-filtered, keyed by file `ResourceLocation`. |
| `core/data/RarityTier.java` | Record: `ResourceLocation profile`, `int weight`. Codec. |
| `core/data/RarityTierRegistry.java` | Static volatile `List<RarityTier>`. `selectProfile(RandomSource)` does weighted random draw. |
| `core/data/RarityTierReloadListener.java` | `rarity_tiers/`, namespace-filtered, parses `{ "tiers": [...] }` wrapper. |
| `core/data/SpawnHookRegistry.java` | Static volatile `Map<MobSpawnType, ResourceLocation>`. |
| `core/data/SpawnHookReloadListener.java` | `spawn_hooks/`, namespace-filtered, maps 15 spawn context strings to profile `ResourceLocation`s. |

### Resources

| File | Purpose |
|------|---------|
| `src/main/templates/META-INF/neoforge.mods.toml` | Mod metadata template. All deps `side=SERVER`. |
| `data/mobdiceattribs/mob_overrides/example.json` | Example override (Ender Dragon). |
| `data/mobdiceattribs/mob_overrides/example_all_attributes.json` | All eight attributes, targeting non-existent mob. |
| `data/mobdiceattribs/dimension_rules/example.json` | Example dimension rule. |
| `data/mobdiceattribs/difficulty_rules/example.json` | Example difficulty rule. |
| `data/mobdiceattribs/spawn_profiles/common.json` | Empty profile (baseline rolls). |
| `data/mobdiceattribs/spawn_profiles/uncommon.json` | Slight 1.1× boost to health/speed/damage. |
| `data/mobdiceattribs/spawn_profiles/rare.json` | Blue "Rare" prefix, 2× XP, 1 bonus loot roll. |
| `data/mobdiceattribs/spawn_profiles/legendary.json` | Purple "Legendary", 5× XP, 3 bonus rolls, all 8 attributes. |
| `data/mobdiceattribs/spawn_profiles/example_legendary.json` | Reference/template profile showing all fields. |
| `data/mobdiceattribs/rarity_tiers/default.json` | Active tier pool (60/25/10/5 common→legendary). |
| `data/mobdiceattribs/spawn_hooks/example.json` | Example hooks (maps `event` → `example_legendary`). |

### Docs

| File | Purpose |
|------|---------|
| `CHANGELOG.md` | Keep a Changelog style. |
| `docs/dice-formula-explained.md` | Algorithm walkthrough. |
| `docs/feature-ideas.md` | Feature backlog with `[done]` / `[idea]` status. |
| `docs/spawn-profiles-and-rarity.md` | Wiki-style guide to spawn profiles, rarity tiers, spawn hooks. |

---

## Algorithm Summary (`computeRolledValue`)

```java
// target  = vanilla attribute value
// diceType = die size (4, 6, 8, 10, 12, or 20)
// rangeFactor = swinginess knob (config, default 2.0); variability = 1/rangeFactor
// configBonus = flat user-configured bonus added on top

dieAvg = (diceType + 1) / 2.0
variability = 1.0 / rangeFactor
diceContribution = target * variability
rawDice = diceContribution / dieAvg

if rawDice < 1:
    rolledValue = rawDice * roll(1, diceType)   // fractional die — for tiny targets like speed
    computedBonus = target - diceContribution
else:
    x = round(rawDice)
    rolledValue = roll(x, diceType)
    computedBonus = target - x * dieAvg         // absorbs rounding error; guarantees avg == target

result = rolledValue + computedBonus + configBonus
```

---

## Dice Auto-Pick Curves (`DiceCurves.java`)

| Attrib | d4 | d6 | d8 | d10 | d12 | d20 |
|--------|----|----|-----|-----|-----|-----|
| Health | ≤6 | ≤15 | ≤30 | ≤60 | ≤120 | >120 |
| Speed | ≤0.15 | ≤0.25 | ≤0.35 | >0.35 | — | — |
| Damage | ≤2 | ≤5 | ≤10 | ≤20 | >20 | — |
| Knockback | ≤0.5 | ≤1.0 | ≤2.0 | >2.0 | — | — |
| Armor | ≤2 | ≤6 | ≤15 | >15 | — | — |
| Armor Toughness | ≤1 | ≤5 | >5 | — | — | — |
| Attack Speed | ≤2.0 | ≤4.0 | ≤8.0 | >8.0 | — | — |
| Follow Range | ≤10 | ≤24 | ≤48 | >48 | — | — |

---

## Spawn Profile Flow

```
MobSpawnEvent.FinalizeSpawn
  └─ stores mda_spawn_type = event.getSpawnType().name()

EntityJoinLevelEvent
  └─ DiceAttributeManger.applyRolls(entity)
       └─ resolveProfile(entity)
            ├─ [spawnHooks.enable] read mda_spawn_type → SpawnHookRegistry.lookup()
            │    └─ if found: return SpawnProfileRegistry.lookup(profileId)
            └─ [rarityTiers.enable] RarityTierRegistry.selectProfile(random)
                 └─ weighted draw → SpawnProfileRegistry.lookup(profileId)
```

Profile stored as `mda_profile` NBT. Name/color applied before rolls. All 8 roll methods
receive the profile; overrides merge as `profile.attrib().merge(mobOverride)`.

---

## Entity NBT Flags

| Key | Type | Set by | Purpose |
|-----|------|--------|---------|
| `mobdiceattribs:rolled` | boolean | `onJoin` | Prevents re-rolling on rejoin |
| `mda_spawn_type` | string | `onFinalizeSpawn` | Spawn context for hook lookup |
| `mda_profile` | string | `resolveProfile` | Profile ResourceLocation (for loot/XP lookup) |
| `mda_base_hp` | double | `rollHealth` | Vanilla HP — reset to this before reroll |
| `mda_base_speed` | double | `rollSpeed` | Vanilla speed — reset to this before reroll |
| `mda_base_damage` | double | `rollAttackDamage` | Vanilla damage — reset to this before reroll |
| `mda_base_knockback` | double | `rollKnockback` | Vanilla knockback — reset to this before reroll |
| `mda_base_armor` | double | `rollArmor` | Vanilla armor — reset to this before reroll |
| `mda_base_armor_toughness` | double | `rollArmorToughness` | Vanilla armor toughness — reset to this before reroll |
| `mda_base_attack_speed` | double | `rollAttackSpeed` | Vanilla attack speed — reset to this before reroll |
| `mda_base_follow_range` | double | `rollFollowRange` | Vanilla follow range — reset to this before reroll |
| `mda_hp_ratio` | double | `rollHealth` | rolled / vanilla — used for elite check and loot scaling |
| `mda_named` | boolean | `applyProfileName` / `markAsElite` | Prevents elite overwriting a profile name |

---

## Config Structure (server config)

```toml
[health]
enable = true
autoDice = true
diceType = 6
rangeFactor = 2.0
bonus = 0.0
mobWhitelist = []
mobBlacklist = []

# [speed], [damage], [knockback], [armor], [armorToughness], [attackSpeed], [followRange]
# — same fields. armor/armorToughness/attackSpeed only roll if base > 0.

[distanceScaling]
enable = false           # DEFAULT FALSE — conflict warning in config comments
blocksPerTier = 1000
multiplierPerTier = 0.1
cap = 3.0

[eliteMobs]
enable = true
healthThreshold = 1.2    # practical max with rangeFactor=2.0 is ~1.35×
namePrefix = "Elite"
showName = true

[lootScaling]
enable = true
threshold = 1.0          # set equal to eliteMobs.healthThreshold to restrict to elites only
xpMultiplier = 2.0
lootBonusRolls = 1

[rarityTiers]
enable = true            # requires rarity_tiers/ datapack files to do anything

[spawnHooks]
enable = true            # requires spawn_hooks/ datapack files to do anything
```

---

## Gate Order in `applyRolls()`

```
1. mob whitelist/blacklist check (per-attribute config)
2. dimension gate  — if disabled, return early; else get dimMultiplier
3. difficulty gate — if disabled, return early; else get diffMultiplier
4. resolveProfile() — spawn hooks → rarity tiers → SpawnProfile
5. apply profile name/color if present
6. combined = dimMultiplier × diffMultiplier
7. healthMultiplier = combined × distanceMultiplier (if distanceScaling.enable)
   rollHealth(entity, profile, healthMultiplier)
   rollSpeed/Damage/Knockback/Armor/etc.(entity, profile, combined)
```

---

## Pending / Deferred Features

All remaining features from `docs/feature-ideas.md` are deferred to a future version:

| Feature | Effort | Notes |
|---------|--------|-------|
| Datapack dice curves | Medium | Let modpack authors shift auto-pick cutoffs; ship defaults in jar |
| Persist rolled values | Medium | Store all 8 rolled values in NBT; expose via Jade/TOP |
| Per-world seeded rolls | High | Deterministic from seed + entity UUID |
| Attribute dependency rules | High | Correlated randomness ("if +50% HP, also +25% damage") |

---

## Known Quirks

- **`DiceAttributeManger` typo.** The class name is missing an 'a' (`Manger` not
  `Manager`). Intentional legacy naming — do not rename without checking all references.

- **`isValidEntity` only checks `Monster`.** Phantoms, zombie horses, and similar
  mobs that don't implement `Monster` are excluded. Known gap, not a bug.

- **`showName` gates all name visibility.** Both profile names (rarity tier) and elite names
  use `Config.SERVER.eliteMobs.showName`. Default is `false` — names are set on the mob and
  visible via Jade/TOP hover, but the floating tag is hidden unless `showName = true`.

- **One rarity tier pool, all mobs.** There is no per-mob or per-dimension tier pool.
  Every eligible mob draws from the same global pool.

---

## Build Notes

- Gradle wrapper: **8.12.1** (do not upgrade)
- NeoForge moddev plugin: **2.0.78**
- Run `./gradlew build` to produce jar
- `generateModMetadata` task substitutes `${...}` variables into `neoforge.mods.toml`
  before compilation — this is why the template lives in `src/main/templates/`
- Jade mod included as `implementation` dependency (`jade_version` in `gradle.properties`)
