/*
 * This file is part of  Mob Dice Attributes.
 * Copyright (c) 2023 Mark Gottschling (gottsch)
 *
 * Mob Dice Attributes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mob Dice Attributes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mob Dice Attributes. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package mod.gottsch.forge.mda.core.config;

import mod.gottsch.forge.gottschcore.config.AbstractConfig;
import mod.gottsch.forge.mda.core.enums.DiceType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Mark Gottschling Feb 8, 2023
 *
 */
public class Config extends AbstractConfig {
	public static final String CATEGORY_DIV = "##############################";
	public static final String UNDERLINE_DIV = "------------------------------";

	protected static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
	protected static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec SERVER_CONFIG;

	public static final Logging LOGGING;
	public static final ServerConfig SERVER;
	public static Config instance = new Config();

	static {
		LOGGING = new Logging(COMMON_BUILDER);
		COMMON_CONFIG = COMMON_BUILDER.build();

		SERVER = new ServerConfig(SERVER_BUILDER);
		SERVER_CONFIG = SERVER_BUILDER.build();
	}

	/*
	 *
	 */
	public static class ServerConfig {

		public HealthConfig health;
		public SpeedConfig speed;
		public DamageConfig damage;
		public KnockbackConfig knockback;
		public ArmorConfig armor;
		public ArmorToughnessConfig armorToughness;
		public AttackSpeedConfig attackSpeed;
		public FollowRangeConfig followRange;
		public DistanceScalingConfig distanceScaling;
		public EliteMobsConfig eliteMobs;
		public LootScalingConfig lootScaling;
		public RarityTiersConfig rarityTiers;
		public SpawnHooksConfig spawnHooks;

		public ServerConfig(ForgeConfigSpec.Builder builder) {
			health = new HealthConfig(builder);
			speed = new SpeedConfig(builder);
			damage = new DamageConfig(builder);
			knockback = new KnockbackConfig(builder);
			armor = new ArmorConfig(builder);
			armorToughness = new ArmorToughnessConfig(builder);
			attackSpeed = new AttackSpeedConfig(builder);
			followRange = new FollowRangeConfig(builder);
			distanceScaling = new DistanceScalingConfig(builder);
			eliteMobs = new EliteMobsConfig(builder);
			lootScaling = new LootScalingConfig(builder);
			rarityTiers = new RarityTiersConfig(builder);
			spawnHooks = new SpawnHooksConfig(builder);
		}
	}

	/*
	 *
	 */
	public static class DistanceScalingConfig {
		public ForgeConfigSpec.BooleanValue enable;
		public ForgeConfigSpec.IntValue blocksPerTier;
		public ForgeConfigSpec.DoubleValue multiplierPerTier;
		public ForgeConfigSpec.DoubleValue cap;
		public DistanceScalingConfig(ForgeConfigSpec.Builder builder) {
			builder.comment(CATEGORY_DIV, "Distance-from-Spawn Scaling", CATEGORY_DIV)
					.push("distanceScaling");

			enable = builder
					.comment(
							" Scales mob MAX HEALTH based on horizontal distance from world spawn.",
							" Each 'blocksPerTier' blocks adds 'multiplierPerTier' to the health multiplier,",
							" capped at 'cap'. Only affects health — speed, damage and knockback are unchanged.",
							"",
							" *** DISABLED BY DEFAULT — READ BEFORE ENABLING ***",
							"",
							" Several popular mods already implement distance-based scaling:",
							"   - Scaling Health      (https://www.curseforge.com/minecraft/mc-mods/scaling-health)",
							"   - Gradual Difficulty  (https://www.curseforge.com/minecraft/mc-mods/gradual-difficulty)",
							"   - Better Difficulty   (https://modrinth.com/mod/better-difficulty)",
							"",
							" If any of those mods are installed alongside this one, enabling this option",
							" will cause DOUBLE-SCALING: that mod scales the mob's base HP first, then this",
							" mod reads that already-inflated value as its 'target' and scales it again.",
							" The result is mobs far from spawn with extremely inflated HP that grows",
							" multiplicatively, not additively — likely unintended and hard to tune.",
							"",
							" Enable this ONLY if no other distance-scaling mod is present on your server.",
							" When in doubt, leave it false.")
					.define("enable", false);

			blocksPerTier = builder
					.comment(
							" Horizontal distance (in blocks) between each scaling tier.",
							" Example: 1000 means the multiplier increases every 1000 blocks from spawn.")
					.defineInRange("blocksPerTier", 1000, 100, 100000);

			multiplierPerTier = builder
					.comment(
							" How much the health multiplier increases per tier.",
							" Example: 0.1 means +10% per tier (tier 1 = ×1.1, tier 2 = ×1.2, ...).",
							" Stacks multiplicatively with active dimension and difficulty rule multipliers.")
					.defineInRange("multiplierPerTier", 0.1D, 0.01D, 10.0D);

			cap = builder
					.comment(
							" Maximum combined distance multiplier (before dimension/difficulty stacking).",
							" Example: 3.0 means mobs cap at 3× vanilla HP regardless of distance.")
					.defineInRange("cap", 3.0D, 1.0D, 100.0D);

			builder.pop();
		}
	}

	/*
	 *
	 */
	public static abstract class AttribConfig {
		public ForgeConfigSpec.BooleanValue enable;
		public ForgeConfigSpec.BooleanValue autoDice;
		public ConfigValue<Integer> diceType;
		public ForgeConfigSpec.DoubleValue rangeFactor;
		public ForgeConfigSpec.DoubleValue bonus;

		public ConfigValue<List<? extends String>> mobWhitelist;
		public ConfigValue<List<? extends String>> mobBlacklist;

		public void configure(ForgeConfigSpec.Builder builder) {

			enable = builder
					.comment(" Enables modification for this attribute.")
					.define("enable", true);

			autoDice = builder
					.comment(" When true, the die type is picked automatically based on the",
							" mob's vanilla attribute value (e.g. low-HP mobs use small dice,",
							" bosses use d20). The 'diceType' setting below is used only as a",
							" fallback and when autoDice is false.",
							" Per-mob datapack overrides still take precedence over both.")
					.define("autoDice", true);
			
			diceType = builder
					.comment(" The type of dice to use for calculations.",
							" See https://www.dieharddice.com/pages/dnd-dice-explained",
							" Values: 2, 4, 6, 8, 10, 12, 20")
					.defineInList("diceType", 6, Stream.of(DiceType.values()).map(x -> x.getDice()).collect(Collectors.toList()));

			rangeFactor = builder
					.comment("the range factor")
					.defineInRange("rangeFactor", 2D, 2D, 10D);

			mobWhitelist = builder
					.comment(" Permitted mobs for that should receive attribute modification.",
							" ex. minecraft:zombie")
					.defineList("mobWhitelist", new ArrayList<String>(), s -> s instanceof String);

			mobBlacklist = builder
					.comment(" Denied mobs for that should not receive attribute modification.",
							" ex. minecraft:ghast")
					.defineList("mobBlacklist", new ArrayList<String>(), s -> s instanceof String);
		}
	}

	/*
	 *
	 */
	public static class HealthConfig extends AttribConfig {
		public HealthConfig(ForgeConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Health Dice Properties", CATEGORY_DIV)
					.push("health");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ForgeConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute)Attributes.MAX_HEALTH).getMaxValue());
		}
	}

	/*
	 *
	 */
	public static class SpeedConfig extends AttribConfig {
		public SpeedConfig(ForgeConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Speed Dice Properties", CATEGORY_DIV)
					.push("speed");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ForgeConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute)Attributes.MOVEMENT_SPEED).getMaxValue());
		}
	}

	public static class DamageConfig extends AttribConfig {
		public DamageConfig(ForgeConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Damage Dice Properties", CATEGORY_DIV)
					.push("damage");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ForgeConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute)Attributes.ATTACK_DAMAGE).getMaxValue());
		}
	}

	public static class KnockbackConfig extends AttribConfig {
		public KnockbackConfig(ForgeConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Attack Knockback Dice Properties", CATEGORY_DIV)
					.push("knockback");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ForgeConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute)Attributes.ATTACK_KNOCKBACK).getMaxValue());
		}
	}

	public static class ArmorConfig extends AttribConfig {
		public ArmorConfig(ForgeConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Armor Dice Properties", CATEGORY_DIV,
							" Note: only applies to mobs whose base armor value is > 0.",
							" Mobs with no natural armor will not have armor rolled onto them.")
					.push("armor");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ForgeConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute) Attributes.ARMOR).getMaxValue());
		}
	}

	public static class ArmorToughnessConfig extends AttribConfig {
		public ArmorToughnessConfig(ForgeConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Armor Toughness Dice Properties", CATEGORY_DIV,
							" Note: only applies to mobs whose base armor toughness value is > 0.",
							" Very few vanilla hostile mobs have non-zero armor toughness (e.g. warden=3).")
					.push("armorToughness");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ForgeConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute) Attributes.ARMOR_TOUGHNESS).getMaxValue());
		}
	}

	public static class AttackSpeedConfig extends AttribConfig {
		public AttackSpeedConfig(ForgeConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Attack Speed Dice Properties", CATEGORY_DIV,
							" Note: attack speed has limited visible effect on most hostile mobs",
							" as they use AI goals for attack timing rather than the attribute directly.")
					.push("attackSpeed");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ForgeConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute) Attributes.ATTACK_SPEED).getMaxValue());
		}
	}

	public static class FollowRangeConfig extends AttribConfig {
		public FollowRangeConfig(ForgeConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Follow Range Dice Properties", CATEGORY_DIV,
							" Controls how far a mob can detect and pursue its target.",
							" zombie=35, skeleton=16, blaze=48, warden=24.")
					.push("followRange");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ForgeConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute) Attributes.FOLLOW_RANGE).getMaxValue());
		}
	}

	public static class EliteMobsConfig {
		public ForgeConfigSpec.BooleanValue enable;
		public ForgeConfigSpec.DoubleValue healthThreshold;
		public ConfigValue<String> namePrefix;
		public ForgeConfigSpec.BooleanValue showName;

		public EliteMobsConfig(ForgeConfigSpec.Builder builder) {
			builder.comment(CATEGORY_DIV, "Elite Mob Settings", CATEGORY_DIV,
							" Mobs that roll significantly above their average health are marked as 'elite'",
							" and receive a custom name tag.")
					.push("eliteMobs");

			enable = builder
					.comment(" Enable elite mob tagging.")
					.define("enable", true);

			healthThreshold = builder
					.comment(" Ratio of rolled HP to target HP required to qualify as elite.",
							" Example: 1.2 means the mob must roll at least 20% above its average health.",
							" With the default rangeFactor=2.0, the maximum achievable ratio is roughly",
							" 1.35x-1.41x depending on die size, so values above that will never trigger.",
							" Recommended range: 1.15 (common) to 1.35 (near-maximum, very rare).")
					.defineInRange("healthThreshold", 1.2D, 1.01D, 10.0D);

			namePrefix = builder
					.comment(" Prefix added to the mob's display name when marked as elite.",
							" Example: 'Elite' produces 'Elite Zombie'.")
					.define("namePrefix", "Elite");

			showName = builder
					.comment(" When true, the named mob's custom name tag is always visible above its head.",
							" Default false — most players use Jade or similar mods to inspect mobs.")
					.define("showName", false);

			builder.pop();
		}
	}

	public static class LootScalingConfig {
		public ForgeConfigSpec.BooleanValue enable;
		public ForgeConfigSpec.DoubleValue threshold;
		public ForgeConfigSpec.DoubleValue xpMultiplier;
		public ForgeConfigSpec.IntValue lootBonusRolls;

		public LootScalingConfig(ForgeConfigSpec.Builder builder) {
			builder.comment(CATEGORY_DIV, "Loot Scaling Settings", CATEGORY_DIV,
							" Mobs that rolled above-average health drop bonus loot and XP.",
							" Operates independently of elite mob tagging.")
					.push("lootScaling");

			enable = builder
					.comment(" Enable loot and XP scaling for above-average rolls.")
					.define("enable", true);

			threshold = builder
					.comment(" Minimum rolled-HP-to-target-HP ratio required to receive bonus loot/XP.",
							" 1.0 means any above-average roll qualifies.",
							" 1.1 means the mob must roll at least 10% above average.",
							" Set equal to eliteMobs.healthThreshold to restrict bonuses to elite mobs only.")
					.defineInRange("threshold", 1.0D, 1.0D, 10.0D);

			xpMultiplier = builder
					.comment(" Multiplier applied to XP dropped by qualifying mobs.",
							" Example: 2.0 means double XP.")
					.defineInRange("xpMultiplier", 2.0D, 1.0D, 100.0D);

			lootBonusRolls = builder
					.comment(" Number of extra copies of each dropped item for qualifying mobs.",
							" Example: 1 adds one extra copy of every item the mob normally drops.",
							" Set to 0 to disable bonus drops while keeping the XP multiplier.")
					.defineInRange("lootBonusRolls", 1, 0, 10);

			builder.pop();
		}
	}

	public static class RarityTiersConfig {
		public ForgeConfigSpec.BooleanValue enable;

		public RarityTiersConfig(ForgeConfigSpec.Builder builder) {
			builder.comment(CATEGORY_DIV, "Rarity Tier Settings", CATEGORY_DIV,
							" Assigns a random spawn profile to each mob on spawn using a weighted tier pool.",
							" Tiers are defined in datapack files under data/mobdiceattribs/rarity_tiers/.",
							" Has no effect if no rarity_tiers datapack files are present.")
					.push("rarityTiers");

			enable = builder
					.comment(" Enable the rarity tier system.")
					.define("enable", true);

			builder.pop();
		}
	}

	public static class SpawnHooksConfig {
		public ForgeConfigSpec.BooleanValue enable;

		public SpawnHooksConfig(ForgeConfigSpec.Builder builder) {
			builder.comment(CATEGORY_DIV, "Spawn Hook Settings", CATEGORY_DIV,
							" Assigns a spawn profile based on how the mob spawned (natural, spawner, raid, etc.).",
							" Hooks are defined in datapack files under data/mobdiceattribs/spawn_hooks/.",
							" Spawn hook profiles take priority over rarity tier profiles.",
							" Has no effect if no spawn_hooks datapack files are present.")
					.push("spawnHooks");

			enable = builder
					.comment(" Enable the spawn hook system.")
					.define("enable", true);

			builder.pop();
		}
	}

	@Deprecated(forRemoval = true, since = "2.0")
	// TODO move validate method into config classes
	public static void validate(ServerConfig config) {
		if (config.health.enable.get()) {
			Integer dice = DiceType.validate(config.health.diceType.get());
			if (dice != config.health.diceType.get()) {
				config.health.diceType.set(dice);
			}
		}

		if (config.speed.enable.get()) {
			Integer dice = DiceType.validate(config.speed.diceType.get());
			if (dice != config.speed.diceType.get()) {
				config.speed.diceType.set(dice);
			}
		}

		if (config.damage.enable.get()) {
			Integer dice = DiceType.validate(config.damage.diceType.get());
			if (dice != config.damage.diceType.get()) {
				config.damage.diceType.set(dice);
			}
		}

		if (config.knockback.enable.get()) {
			Integer dice = DiceType.validate(config.knockback.diceType.get());
			if (dice != config.knockback.diceType.get()) {
				config.knockback.diceType.set(dice);
			}
		}
	}

	@Override
	public String getLogsFolder() {
		return Config.LOGGING.folder.get();
	}

	public void setLogsFolder(String folder) {
		Config.LOGGING.folder.set(folder);
	}

	@Override
	public String getLoggingLevel() {
		return Config.LOGGING.level.get();
	}
}
