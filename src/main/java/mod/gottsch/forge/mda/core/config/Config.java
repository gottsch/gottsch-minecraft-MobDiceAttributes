/*
 * This file is part of  Mob Dice Attributes2.
 * Copyright (c) 2023 Mark Gottschling (gottsch)
 *
 * Mob Dice Attributes2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mob Dice Attributes2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mob Dice Attributes2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package mod.gottsch.forge.mda.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mod.gottsch.forge.gottschcore.config.AbstractConfig;
import mod.gottsch.forge.mda.core.enums.DiceType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

/**
 *
 * @author Mark Gottschling Feb 8, 2023
 *
 */
public class Config extends AbstractConfig {
	public static final String CATEGORY_DIV = "##############################";
	public static final String UNDERLINE_DIV = "------------------------------";

	protected static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
	protected static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

	public static ModConfigSpec COMMON_CONFIG;
	public static ModConfigSpec SERVER_CONFIG;

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

		public ServerConfig(ModConfigSpec.Builder builder) {
			health = new HealthConfig(builder);
			speed = new SpeedConfig(builder);
			damage = new DamageConfig(builder);
			knockback = new KnockbackConfig(builder);
		}
	}

	/*
	 *
	 */
	public static abstract class AttribConfig {
		public ModConfigSpec.BooleanValue enable;
		public ConfigValue<Integer> diceType;
		public ModConfigSpec.DoubleValue rangeFactor;
		public ModConfigSpec.DoubleValue bonus;

		public ConfigValue<List<? extends String>> mobWhitelist;
		public ConfigValue<List<? extends String>> mobBlacklist;

		public void configure(ModConfigSpec.Builder builder) {

			enable = builder
					.comment(" Enables modification for this attribute.")
					.define("enable", true);

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
		public HealthConfig(ModConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Health Dice Properties", CATEGORY_DIV)
			.push("health");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ModConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute) Attributes.MAX_HEALTH.value()).getMaxValue());
		}
	}

	/*
	 *
	 */
	public static class SpeedConfig extends AttribConfig {
		public SpeedConfig(ModConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Speed Dice Properties", CATEGORY_DIV)
			.push("speed");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ModConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute) Attributes.MOVEMENT_SPEED.value()).getMaxValue());
		}
	}

	public static class DamageConfig extends AttribConfig {
		public DamageConfig(ModConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Damage Dice Properties", CATEGORY_DIV)
			.push("damage");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ModConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute) Attributes.ATTACK_DAMAGE.value()).getMaxValue());
		}
	}

	public static class KnockbackConfig extends AttribConfig {
		public KnockbackConfig(ModConfigSpec.Builder builder) {

			builder.comment(CATEGORY_DIV, "Attack Knockback Dice Properties", CATEGORY_DIV)
			.push("knockback");

			configure(builder);

			builder.pop();
		}

		@Override
		public void configure(ModConfigSpec.Builder builder) {
			super.configure(builder);

			bonus = builder
					.comment("bonus")
					.defineInRange("bonus", 0, 0, ((RangedAttribute) Attributes.ATTACK_KNOCKBACK.value()).getMaxValue());
		}
	}

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
