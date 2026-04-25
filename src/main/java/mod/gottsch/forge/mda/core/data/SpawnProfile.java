package mod.gottsch.forge.mda.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record SpawnProfile(
		Optional<String> namePrefix,
		Optional<String> nameColor,
		Optional<Double> xpMultiplier,
		Optional<Integer> lootBonusRolls,
		AttribOverride health,
		AttribOverride speed,
		AttribOverride damage,
		AttribOverride knockback,
		AttribOverride armor,
		AttribOverride armorToughness,
		AttribOverride attackSpeed,
		AttribOverride followRange
) {
	public static final SpawnProfile EMPTY = new SpawnProfile(
			Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
			AttribOverride.EMPTY, AttribOverride.EMPTY, AttribOverride.EMPTY, AttribOverride.EMPTY,
			AttribOverride.EMPTY, AttribOverride.EMPTY, AttribOverride.EMPTY, AttribOverride.EMPTY
	);

	public static final Codec<SpawnProfile> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.optionalFieldOf("namePrefix").forGetter(SpawnProfile::namePrefix),
			Codec.STRING.optionalFieldOf("nameColor").forGetter(SpawnProfile::nameColor),
			Codec.DOUBLE.optionalFieldOf("xpMultiplier").forGetter(SpawnProfile::xpMultiplier),
			Codec.INT.optionalFieldOf("lootBonusRolls").forGetter(SpawnProfile::lootBonusRolls),
			AttribOverride.CODEC.optionalFieldOf("health", AttribOverride.EMPTY).forGetter(SpawnProfile::health),
			AttribOverride.CODEC.optionalFieldOf("speed", AttribOverride.EMPTY).forGetter(SpawnProfile::speed),
			AttribOverride.CODEC.optionalFieldOf("damage", AttribOverride.EMPTY).forGetter(SpawnProfile::damage),
			AttribOverride.CODEC.optionalFieldOf("knockback", AttribOverride.EMPTY).forGetter(SpawnProfile::knockback),
			AttribOverride.CODEC.optionalFieldOf("armor", AttribOverride.EMPTY).forGetter(SpawnProfile::armor),
			AttribOverride.CODEC.optionalFieldOf("armorToughness", AttribOverride.EMPTY).forGetter(SpawnProfile::armorToughness),
			AttribOverride.CODEC.optionalFieldOf("attackSpeed", AttribOverride.EMPTY).forGetter(SpawnProfile::attackSpeed),
			AttribOverride.CODEC.optionalFieldOf("followRange", AttribOverride.EMPTY).forGetter(SpawnProfile::followRange)
	).apply(i, SpawnProfile::new));

	public boolean isEmpty() {
		return this == EMPTY;
	}
}
