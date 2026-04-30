package mod.gottsch.forge.mda.core.manager;

import mod.gottsch.forge.gottschcore.random.RandomHelper;
import mod.gottsch.forge.mda.core.config.Config;
import mod.gottsch.forge.mda.core.data.*;
import mod.gottsch.forge.mda.core.data.MobOverrideRegistry.Attrib;
import mod.gottsch.forge.mda.core.enums.DiceType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

import java.util.Optional;

/**
 * 
 * @author Mark Gottschling Feb 8, 2023
 *
 */
public class DiceAttributeManger {

	/**
	 * 
	 * @param entity
	 * @return
	 */
	public static boolean isValidEntity(final Entity entity) {
		return entity instanceof Monster;
	}

	public static void applyRolls(Entity entity) {
		if (Config.SERVER.health.enable.get()) {
			String key = EntityType.getKey(entity.getType()).toString();

			ResourceLocation dimId = entity.level().dimension().location();
			DimensionRule dimRule = DimensionRuleRegistry.lookup(dimId);
			if (!dimRule.isEnabled()) {
				return;
			}

			Difficulty difficulty = entity.level().getDifficulty();
			DifficultyRule diffRule = DifficultyRuleRegistry.lookup(difficulty);
			if (!diffRule.isEnabled()) {
				return;
			}

			double multiplier = dimRule.getMultiplier() * diffRule.getMultiplier();
			double healthMultiplier = multiplier * computeDistanceMultiplier(entity);

			// Resolve spawn profile (hook → rarity tier → EMPTY) and apply name before rolling.
			SpawnProfile profile = resolveProfile(entity);
			if (!profile.isEmpty()) {
				profile.namePrefix().ifPresent(prefix -> applyProfileName(entity, prefix, profile.nameColor()));
			}

			if (Config.SERVER.health.mobWhitelist.get().contains(key) ||
					(Config.SERVER.health.mobWhitelist.get().isEmpty() &&
							!Config.SERVER.health.mobBlacklist.get().contains(key))) {
				rollHealth(entity, healthMultiplier, profile);
			}

			if (Config.SERVER.speed.mobWhitelist.get().contains(key) ||
					(Config.SERVER.speed.mobWhitelist.get().isEmpty() &&
							!Config.SERVER.speed.mobBlacklist.get().contains(key))) {
				rollSpeed(entity, multiplier, profile);
			}

			if (Config.SERVER.damage.mobWhitelist.get().contains(key) ||
					(Config.SERVER.damage.mobWhitelist.get().isEmpty() &&
							!Config.SERVER.damage.mobBlacklist.get().contains(key))) {
				rollAttackDamage(entity, multiplier, profile);
			}

			if (Config.SERVER.knockback.mobWhitelist.get().contains(key) ||
					(Config.SERVER.knockback.mobWhitelist.get().isEmpty() &&
							!Config.SERVER.knockback.mobBlacklist.get().contains(key))) {
				rollKnockback(entity, multiplier, profile);
			}

			if (Config.SERVER.armor.enable.get()) {
				if (Config.SERVER.armor.mobWhitelist.get().contains(key) ||
						(Config.SERVER.armor.mobWhitelist.get().isEmpty() &&
								!Config.SERVER.armor.mobBlacklist.get().contains(key))) {
					rollArmor(entity, multiplier, profile);
				}
			}

			if (Config.SERVER.armorToughness.enable.get()) {
				if (Config.SERVER.armorToughness.mobWhitelist.get().contains(key) ||
						(Config.SERVER.armorToughness.mobWhitelist.get().isEmpty() &&
								!Config.SERVER.armorToughness.mobBlacklist.get().contains(key))) {
					rollArmorToughness(entity, multiplier, profile);
				}
			}

			if (Config.SERVER.attackSpeed.enable.get()) {
				if (Config.SERVER.attackSpeed.mobWhitelist.get().contains(key) ||
						(Config.SERVER.attackSpeed.mobWhitelist.get().isEmpty() &&
								!Config.SERVER.attackSpeed.mobBlacklist.get().contains(key))) {
					rollAttackSpeed(entity, multiplier, profile);
				}
			}

			if (Config.SERVER.followRange.enable.get()) {
				if (Config.SERVER.followRange.mobWhitelist.get().contains(key) ||
						(Config.SERVER.followRange.mobWhitelist.get().isEmpty() &&
								!Config.SERVER.followRange.mobBlacklist.get().contains(key))) {
					rollFollowRange(entity, multiplier, profile);
				}
			}
		}
	}

	/**
	 * Resolves the active spawn profile for this entity.
	 * Priority: spawn hook (by spawn type stored in NBT) > rarity tier (weighted random) > EMPTY.
	 * Stores the resolved profile ID in NBT as "mda_profile" for use by loot/XP event handlers.
	 */
	private static SpawnProfile resolveProfile(Entity entity) {
		ResourceLocation profileId = null;

		if (Config.SERVER.spawnHooks.enable.get()) {
			String spawnTypeName = entity.getPersistentData().getString("mda_spawn_type");
			if (!spawnTypeName.isEmpty()) {
				try {
					MobSpawnType spawnType = MobSpawnType.valueOf(spawnTypeName);
					Optional<ResourceLocation> hookId = SpawnHookRegistry.lookup(spawnType);
					if (hookId.isPresent()) {
						profileId = hookId.get();
					}
				} catch (IllegalArgumentException ignored) {}
			}
		}

		if (profileId == null && Config.SERVER.rarityTiers.enable.get() && !RarityTierRegistry.isEmpty()) {
			profileId = RarityTierRegistry.selectProfile(entity.level().getRandom()).orElse(null);
		}

		if (profileId == null) return SpawnProfile.EMPTY;

		SpawnProfile profile = SpawnProfileRegistry.lookup(profileId);
		if (!profile.isEmpty()) {
			entity.getPersistentData().putString("mda_profile", profileId.toString());
		}
		return profile;
	}

	private static void applyProfileName(Entity entity, String prefix, Optional<String> colorName) {
		MutableComponent name = Component.literal(prefix + " ").append(entity.getType().getDescription());
		colorName.ifPresent(c -> {
			ChatFormatting fmt = ChatFormatting.getByName(c);
			if (fmt != null && fmt.isColor()) {
				name.withStyle(fmt);
			}
		});
		entity.setCustomName(name);
		entity.setCustomNameVisible(Config.SERVER.eliteMobs.showName.get());
		entity.getPersistentData().putBoolean("mda_named", true);
	}

	/**
	 * Builds an xdn+b formula whose average equals target, then rolls it.
	 * variability = 1/rangeFactor controls how much of target comes from dice vs a flat bonus.
	 * For rawDice < 1, a fractional die is used (preserves small-value attributes like speed).
	 * For rawDice >= 1, the integer-rounded dice count's drift is absorbed into the bonus,
	 * so average(rolledValue + computedBonus) == target exactly.
	 */
	private static double computeRolledValue(double target, int diceType, double rangeFactor, double configBonus) {
		double dieAvg = DiceType.valueOf(diceType).getAvg();
		double variability = 1.0 / rangeFactor;
		double diceContribution = target * variability;
		double rawDice = diceContribution / dieAvg;

		double rolledValue;
		double computedBonus;
		if (rawDice < 1.0) {
			rolledValue = rawDice * roll(1, diceType);
			computedBonus = target - diceContribution;
		} else {
			int x = (int) Math.round(rawDice);
			rolledValue = roll(x, diceType);
			computedBonus = target - x * dieAvg;
		}
		return rolledValue + computedBonus + configBonus;
	}

	/**
	 * Resolution order for the die type:
	 *   1. merged override's "dice" field (mob override wins over profile override)
	 *   2. auto-picked curve (when config.autoDice is true) — see DiceCurves
	 *   3. config.diceType fallback
	 */
	private static int resolveDice(AttribOverride ov, Attrib which, double target,
	                               boolean autoDice, int configDice) {
		if (ov.dice().isPresent()) return ov.dice().get();
		if (autoDice) return DiceCurves.autoDice(which, target);
		return configDice;
	}

	private static void rollHealth(Entity entity, double multiplier, SpawnProfile profile) {
		LivingEntity monster = (LivingEntity) entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.MAX_HEALTH);
		if (attribute != null) {
			CompoundTag data = entity.getPersistentData();
			if (!data.contains("mda_base_hp")) {
				data.putDouble("mda_base_hp", monster.getMaxHealth());
			}
			double hp = data.getDouble("mda_base_hp") * multiplier;
			if (hp <= 0D) return;

			AttribOverride ov = profile.health().merge(MobOverrideRegistry.lookup(entity.getType(), Attrib.HEALTH));
			int dice = resolveDice(ov, Attrib.HEALTH, hp,
					Config.SERVER.health.autoDice.get(),
					Config.SERVER.health.diceType.get());
			double newHealth = computeRolledValue(hp,
					dice,
					ov.rangeFactor().orElse(Config.SERVER.health.rangeFactor.get()),
					ov.bonus().orElse(Config.SERVER.health.bonus.get()));
			attribute.setBaseValue(newHealth);
			monster.setHealth(monster.getMaxHealth());

			double ratio = newHealth / hp;
			if (ratio > 1.0) {
				data.putDouble("mda_hp_ratio", ratio);
			}

			// Elite name is a fallback — only fires if the profile hasn't already set a name.
			if (Config.SERVER.eliteMobs.enable.get()
					&& ratio >= Config.SERVER.eliteMobs.healthThreshold.get()
					&& !data.getBoolean("mda_named")) {
				markAsElite(entity);
			}
		}
	}

	private static void markAsElite(Entity entity) {
		String prefix = Config.SERVER.eliteMobs.namePrefix.get();
		Component mobName = entity.getType().getDescription();
		entity.setCustomName(Component.literal(prefix + " ").append(mobName));
		if (Config.SERVER.eliteMobs.showName.get()) {
			entity.setCustomNameVisible(true);
		}
		entity.getPersistentData().putBoolean("mda_named", true);
	}

	public static void reroll(Entity entity) {
		CompoundTag data = entity.getPersistentData();
		LivingEntity living = (LivingEntity) entity;

		resetAttribute(living, Attributes.MAX_HEALTH,      data, "mda_base_hp");
		resetAttribute(living, Attributes.MOVEMENT_SPEED,  data, "mda_base_speed");
		resetAttribute(living, Attributes.ATTACK_DAMAGE,   data, "mda_base_damage");
		resetAttribute(living, Attributes.ATTACK_KNOCKBACK,data, "mda_base_knockback");
		resetAttribute(living, Attributes.ARMOR,           data, "mda_base_armor");
		resetAttribute(living, Attributes.ARMOR_TOUGHNESS, data, "mda_base_armor_toughness");
		resetAttribute(living, Attributes.ATTACK_SPEED,    data, "mda_base_attack_speed");
		resetAttribute(living, Attributes.FOLLOW_RANGE,    data, "mda_base_follow_range");

		if (data.contains("mda_base_hp")) {
			living.setHealth((float) data.getDouble("mda_base_hp"));
		}

		data.remove("mda_hp_ratio");
		data.remove("mda_profile");

		if (data.getBoolean("mda_named")) {
			entity.setCustomName(null);
			entity.setCustomNameVisible(false);
			data.remove("mda_named");
		}

		applyRolls(entity);
	}

	private static void resetAttribute(LivingEntity entity, Attribute attribute,
                                       CompoundTag data, String nbtKey) {
		if (data.contains(nbtKey)) {
			AttributeInstance inst = entity.getAttribute(attribute);
			if (inst != null) inst.setBaseValue(data.getDouble(nbtKey));
		}
	}

	private static void rollSpeed(Entity entity, double multiplier, SpawnProfile profile) {
		LivingEntity monster = (LivingEntity) entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attribute != null) {
			CompoundTag data = entity.getPersistentData();
			if (!data.contains("mda_base_speed")) {
				data.putDouble("mda_base_speed", attribute.getBaseValue());
			}
			double speed = data.getDouble("mda_base_speed") * multiplier;
			if (speed <= 0D) return;
			AttribOverride ov = profile.speed().merge(MobOverrideRegistry.lookup(entity.getType(), Attrib.SPEED));
			int dice = resolveDice(ov, Attrib.SPEED, speed,
					Config.SERVER.speed.autoDice.get(),
					Config.SERVER.speed.diceType.get());
			double newValue = computeRolledValue(speed,
					dice,
					ov.rangeFactor().orElse(Config.SERVER.speed.rangeFactor.get()),
					ov.bonus().orElse(Config.SERVER.speed.bonus.get()));
			attribute.setBaseValue(newValue);
		}
	}

	private static void rollAttackDamage(Entity entity, double multiplier, SpawnProfile profile) {
		LivingEntity monster = (LivingEntity) entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.ATTACK_DAMAGE);
		if (attribute != null) {
			CompoundTag data = entity.getPersistentData();
			if (!data.contains("mda_base_damage")) {
				data.putDouble("mda_base_damage", attribute.getBaseValue());
			}
			double damage = data.getDouble("mda_base_damage") * multiplier;
			if (damage <= 0D) return;
			AttribOverride ov = profile.damage().merge(MobOverrideRegistry.lookup(entity.getType(), Attrib.DAMAGE));
			int dice = resolveDice(ov, Attrib.DAMAGE, damage,
					Config.SERVER.damage.autoDice.get(),
					Config.SERVER.damage.diceType.get());
			double newValue = computeRolledValue(damage,
					dice,
					ov.rangeFactor().orElse(Config.SERVER.damage.rangeFactor.get()),
					ov.bonus().orElse(Config.SERVER.damage.bonus.get()));
			attribute.setBaseValue(newValue);
		}
	}

	private static void rollKnockback(Entity entity, double multiplier, SpawnProfile profile) {
		LivingEntity monster = (LivingEntity) entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.ATTACK_KNOCKBACK);
		if (attribute != null) {
			CompoundTag data = entity.getPersistentData();
			if (!data.contains("mda_base_knockback")) {
				data.putDouble("mda_base_knockback", attribute.getBaseValue());
			}
			double knockback = data.getDouble("mda_base_knockback") * multiplier;
			if (knockback <= 0D) return;
			AttribOverride ov = profile.knockback().merge(MobOverrideRegistry.lookup(entity.getType(), Attrib.KNOCKBACK));
			int dice = resolveDice(ov, Attrib.KNOCKBACK, knockback,
					Config.SERVER.knockback.autoDice.get(),
					Config.SERVER.knockback.diceType.get());
			double newValue = computeRolledValue(knockback,
					dice,
					ov.rangeFactor().orElse(Config.SERVER.knockback.rangeFactor.get()),
					ov.bonus().orElse(Config.SERVER.knockback.bonus.get()));
			attribute.setBaseValue(newValue);
		}
	}

	private static void rollArmor(Entity entity, double multiplier, SpawnProfile profile) {
		LivingEntity monster = (LivingEntity) entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.ARMOR);
		if (attribute != null) {
			CompoundTag data = entity.getPersistentData();
			if (!data.contains("mda_base_armor")) {
				data.putDouble("mda_base_armor", attribute.getBaseValue());
			}
			double armor = data.getDouble("mda_base_armor") * multiplier;
			if (armor <= 0D) return;
			AttribOverride ov = profile.armor().merge(MobOverrideRegistry.lookup(entity.getType(), Attrib.ARMOR));
			int dice = resolveDice(ov, Attrib.ARMOR, armor,
					Config.SERVER.armor.autoDice.get(),
					Config.SERVER.armor.diceType.get());
			double newValue = computeRolledValue(armor,
					dice,
					ov.rangeFactor().orElse(Config.SERVER.armor.rangeFactor.get()),
					ov.bonus().orElse(Config.SERVER.armor.bonus.get()));
			attribute.setBaseValue(newValue);
		}
	}

	private static void rollArmorToughness(Entity entity, double multiplier, SpawnProfile profile) {
		LivingEntity monster = (LivingEntity) entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.ARMOR_TOUGHNESS);
		if (attribute != null) {
			CompoundTag data = entity.getPersistentData();
			if (!data.contains("mda_base_armor_toughness")) {
				data.putDouble("mda_base_armor_toughness", attribute.getBaseValue());
			}
			double toughness = data.getDouble("mda_base_armor_toughness") * multiplier;
			if (toughness <= 0D) return;
			AttribOverride ov = profile.armorToughness().merge(MobOverrideRegistry.lookup(entity.getType(), Attrib.ARMOR_TOUGHNESS));
			int dice = resolveDice(ov, Attrib.ARMOR_TOUGHNESS, toughness,
					Config.SERVER.armorToughness.autoDice.get(),
					Config.SERVER.armorToughness.diceType.get());
			double newValue = computeRolledValue(toughness,
					dice,
					ov.rangeFactor().orElse(Config.SERVER.armorToughness.rangeFactor.get()),
					ov.bonus().orElse(Config.SERVER.armorToughness.bonus.get()));
			attribute.setBaseValue(newValue);
		}
	}

	private static void rollAttackSpeed(Entity entity, double multiplier, SpawnProfile profile) {
		LivingEntity monster = (LivingEntity) entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.ATTACK_SPEED);
		if (attribute != null) {
			CompoundTag data = entity.getPersistentData();
			if (!data.contains("mda_base_attack_speed")) {
				data.putDouble("mda_base_attack_speed", attribute.getBaseValue());
			}
			double speed = data.getDouble("mda_base_attack_speed") * multiplier;
			if (speed <= 0D) return;
			AttribOverride ov = profile.attackSpeed().merge(MobOverrideRegistry.lookup(entity.getType(), Attrib.ATTACK_SPEED));
			int dice = resolveDice(ov, Attrib.ATTACK_SPEED, speed,
					Config.SERVER.attackSpeed.autoDice.get(),
					Config.SERVER.attackSpeed.diceType.get());
			double newValue = computeRolledValue(speed,
					dice,
					ov.rangeFactor().orElse(Config.SERVER.attackSpeed.rangeFactor.get()),
					ov.bonus().orElse(Config.SERVER.attackSpeed.bonus.get()));
			attribute.setBaseValue(newValue);
		}
	}

	private static void rollFollowRange(Entity entity, double multiplier, SpawnProfile profile) {
		LivingEntity monster = (LivingEntity) entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.FOLLOW_RANGE);
		if (attribute != null) {
			CompoundTag data = entity.getPersistentData();
			if (!data.contains("mda_base_follow_range")) {
				data.putDouble("mda_base_follow_range", attribute.getBaseValue());
			}
			double range = data.getDouble("mda_base_follow_range") * multiplier;
			if (range <= 0D) return;
			AttribOverride ov = profile.followRange().merge(MobOverrideRegistry.lookup(entity.getType(), Attrib.FOLLOW_RANGE));
			int dice = resolveDice(ov, Attrib.FOLLOW_RANGE, range,
					Config.SERVER.followRange.autoDice.get(),
					Config.SERVER.followRange.diceType.get());
			double newValue = computeRolledValue(range,
					dice,
					ov.rangeFactor().orElse(Config.SERVER.followRange.rangeFactor.get()),
					ov.bonus().orElse(Config.SERVER.followRange.bonus.get()));
			attribute.setBaseValue(newValue);
		}
	}

	private static double computeDistanceMultiplier(Entity entity) {
		if (!Config.SERVER.distanceScaling.enable.get()) {
			return 1.0;
		}
		ServerLevel level = (ServerLevel) entity.level();
		BlockPos spawn = level.getSharedSpawnPos();
		BlockPos pos = entity.blockPosition();

		double dx = pos.getX() - spawn.getX();
		double dz = pos.getZ() - spawn.getZ();
		double distance = Math.sqrt(dx * dx + dz * dz);

		int tier = (int) (distance / Config.SERVER.distanceScaling.blocksPerTier.get());
		double multiplier = 1.0 + tier * Config.SERVER.distanceScaling.multiplierPerTier.get();
		return Math.min(multiplier, Config.SERVER.distanceScaling.cap.get());
	}

	private static int roll(int num, int diceType) {
		int result = 0;
		for (int i = 0; i < num; i++) {
			result += RandomHelper.randomInt(1, diceType);
		}
		return result;
	}
}
