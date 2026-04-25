package mod.gottsch.forge.mda.core.manager;

import mod.gottsch.forge.mda.core.data.MobOverrideRegistry.Attrib;

/**
 * Picks a "natural-feeling" die size based on the vanilla value of an attribute.
 *
 * <p>The rounding-absorb trick in {@code DiceAttributeManger.computeRolledValue}
 * guarantees that the rolled average equals the target regardless of which die is
 * picked, so choosing differently here only changes the <em>feel</em> of the
 * randomness, never the balance:
 * <ul>
 *   <li>small targets use small dice (tight swings, low granularity)</li>
 *   <li>large targets use large dice (dramatic swings, bossy feel)</li>
 * </ul>
 *
 * <p>Cutoffs are hardcoded. A future enhancement may load these curves from a
 * datapack — see {@code docs/feature-ideas.md}.
 */
public final class DiceCurves {
	private DiceCurves() {}

	public static int autoDice(Attrib which, double target) {
		switch (which) {
			case HEALTH:          return forHealth(target);
			case SPEED:           return forSpeed(target);
			case DAMAGE:          return forDamage(target);
			case KNOCKBACK:       return forKnockback(target);
			case ARMOR:           return forArmor(target);
			case ARMOR_TOUGHNESS: return forArmorToughness(target);
			case ATTACK_SPEED:    return forAttackSpeed(target);
			case FOLLOW_RANGE:    return forFollowRange(target);
			default:              return 6;
		}
	}

	// chicken=4, rabbit=3  → d4
	// bat=6, silverfish=8   → d6
	// zombie=20, creeper=20 → d8
	// husk=20, wolf=20      → d8
	// polar bear=30, ravager=100 territory → d10/d12
	// wither=300, dragon=200, warden=500 → d20
	private static int forHealth(double target) {
		if (target <= 6)   return 4;
		if (target <= 15)  return 6;
		if (target <= 30)  return 8;
		if (target <= 60)  return 10;
		if (target <= 120) return 12;
		return 20;
	}

	// Speed is a small float, typically 0.2–0.35 for hostile mobs.
	private static int forSpeed(double target) {
		if (target <= 0.15) return 4;
		if (target <= 0.25) return 6;
		if (target <= 0.35) return 8;
		return 10;
	}

	// Base attack damage: zombie=3, creeper=0 (explodes), skeleton≈2, ravager=12.
	private static int forDamage(double target) {
		if (target <= 2)  return 4;
		if (target <= 5)  return 6;
		if (target <= 10) return 8;
		if (target <= 20) return 10;
		return 12;
	}

	// Attack knockback: most mobs 0; ravager≈1.5; hoglin≈1.
	private static int forKnockback(double target) {
		if (target <= 0.5) return 4;
		if (target <= 1.0) return 6;
		if (target <= 2.0) return 8;
		return 10;
	}

	// Armor: zombie=2, husk=2, iron golem=6, wither=4, warden=30.
	private static int forArmor(double target) {
		if (target <= 2)  return 4;
		if (target <= 6)  return 6;
		if (target <= 15) return 8;
		return 10;
	}

	// Armor toughness: most hostile mobs=0, warden=3. Very few non-zero values.
	private static int forArmorToughness(double target) {
		if (target <= 1) return 4;
		if (target <= 5) return 6;
		return 8;
	}

	// Attack speed: most mobs default to 4.0; varies little among hostiles.
	private static int forAttackSpeed(double target) {
		if (target <= 2.0) return 4;
		if (target <= 4.0) return 6;
		if (target <= 8.0) return 8;
		return 10;
	}

	// Follow range: zombie=35, skeleton=16, blaze=48, warden=24.
	private static int forFollowRange(double target) {
		if (target <= 10) return 4;
		if (target <= 24) return 6;
		if (target <= 48) return 8;
		return 10;
	}
}
