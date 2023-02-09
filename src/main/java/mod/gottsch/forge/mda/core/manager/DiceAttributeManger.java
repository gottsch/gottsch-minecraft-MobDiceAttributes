package mod.gottsch.forge.mda.core.manager;

import mod.gottsch.forge.gottschcore.random.RandomHelper;
import mod.gottsch.forge.mda.core.config.Config;
import mod.gottsch.forge.mda.core.enums.DiceType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;

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
		return entity instanceof LivingEntity && (entity instanceof Monster || entity instanceof Enemy);
	}

	public static void applyRolls(Entity entity) {
		if (Config.SERVER.health.enable.get()) {
			String key = EntityType.getKey(entity.getType()).toString();
			if (Config.SERVER.health.mobWhitelist.get().contains(key) ||
					(Config.SERVER.health.mobWhitelist.get().isEmpty() &&
							!Config.SERVER.health.mobBlacklist.get().contains(key))) {
				rollHealth(entity);
			}
			
			if (Config.SERVER.speed.mobWhitelist.get().contains(key) ||
					(Config.SERVER.speed.mobWhitelist.get().isEmpty() &&
							!Config.SERVER.speed.mobBlacklist.get().contains(key))) {
				rollSpeed(entity);
			}
			
			if (Config.SERVER.damage.mobWhitelist.get().contains(key) ||
					(Config.SERVER.damage.mobWhitelist.get().isEmpty() &&
							!Config.SERVER.damage.mobBlacklist.get().contains(key))) {
				rollAttackDamage(entity);
			}
			
			if (Config.SERVER.knockback.mobWhitelist.get().contains(key) ||
					(Config.SERVER.knockback.mobWhitelist.get().isEmpty() &&
							!Config.SERVER.knockback.mobBlacklist.get().contains(key))) {
				rollKnockback(entity);
			}
		}
	}

	/**
	 * 
	 * @param entity
	 */
	private static void rollHealth(Entity entity) {
		LivingEntity monster = (LivingEntity)entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.MAX_HEALTH);
		if (attribute != null) {
			float hp = monster.getMaxHealth();
			float rangeBonus = (float) (hp / Config.SERVER.health.rangeFactor.get());
			int dice = Math.round((hp - rangeBonus) / DiceType.valueOf(Config.SERVER.health.diceType.get()).getAvg());
			float newHealth =  ((float) roll(dice, Config.SERVER.health.diceType.get())) + rangeBonus + Config.SERVER.health.bonus.get().floatValue();
			attribute.setBaseValue(newHealth);
			monster.setHealth(monster.getMaxHealth());
		}
	}
	
	/**
	 * 
	 * @param entity
	 */
	private static void rollSpeed(Entity entity) {
		LivingEntity monster = (LivingEntity)entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attribute != null) {
			float speed = monster.getSpeed();
			if (speed <= 0F) {
				return;
			}
			float rangeBonus = (float) (speed / Config.SERVER.speed.rangeFactor.get());
			float dice = (speed - rangeBonus) / DiceType.valueOf(Config.SERVER.speed.diceType.get()).getAvg();
			float newValue = 0;
			if (dice < 1F) {
				newValue = dice * roll(1, Config.SERVER.speed.diceType.get()) + rangeBonus + Config.SERVER.speed.bonus.get().floatValue();
			}
			else {
				newValue = ((float) roll(Math.round(dice), Config.SERVER.speed.diceType.get())) + rangeBonus + Config.SERVER.speed.bonus.get().floatValue();
			}
			attribute.setBaseValue(newValue);
			monster.setSpeed(newValue);
		}
	}
	
	private static void rollAttackDamage(Entity entity) {
		LivingEntity monster = (LivingEntity)entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.ATTACK_DAMAGE);
		if (attribute != null) {
			double damage = attribute.getBaseValue();
			if (damage <= 0F) {
				return;
			}
			float rangeBonus = (float) (damage / Config.SERVER.damage.rangeFactor.get());
			int dice = (int) Math.round((damage - rangeBonus) / DiceType.valueOf(Config.SERVER.damage.diceType.get()).getAvg());
			float newValue =  ((float) roll(dice, Config.SERVER.damage.diceType.get())) + rangeBonus + Config.SERVER.damage.bonus.get().floatValue();
			attribute.setBaseValue(newValue);
		}
	}
	
	/*
	 * 
	 */
	private static void rollKnockback(Entity entity) {
		LivingEntity monster = (LivingEntity)entity;
		AttributeInstance attribute = monster.getAttribute(Attributes.ATTACK_KNOCKBACK);
		if (attribute != null) {
			double knockback = attribute.getBaseValue();
			if (knockback <= 0) {
				return;
			}
			double rangeBonus = knockback / Config.SERVER.knockback.rangeFactor.get();
			double dice = (knockback - rangeBonus) / DiceType.valueOf(Config.SERVER.knockback.diceType.get()).getAvg();
			double newValue = 0;
			if (dice < 1) {
				newValue = dice * roll(1, Config.SERVER.knockback.diceType.get()) + rangeBonus + Config.SERVER.knockback.bonus.get();
			}
			else {
				newValue = roll((int)Math.round(dice), Config.SERVER.knockback.diceType.get()) + rangeBonus + Config.SERVER.knockback.bonus.get();
			}
			attribute.setBaseValue(newValue);
		}
	}

	/**
	 * 
	 * @param num
	 * @param diceType
	 * @return
	 */
	private static int roll(int num, int diceType) {
		int result = 0;
		for (int i = 0; i < num; i++) {
			result += RandomHelper.randomInt(1, diceType);
		}
		return result;
	}
}
