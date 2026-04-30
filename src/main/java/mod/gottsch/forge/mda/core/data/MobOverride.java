package mod.gottsch.forge.mda.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record MobOverride(List<String> targets,
                          AttribOverride health,
                          AttribOverride speed,
                          AttribOverride damage,
                          AttribOverride knockback,
                          AttribOverride armor,
                          AttribOverride armorToughness,
                          AttribOverride attackSpeed,
                          AttribOverride followRange) {

	public static final Codec<MobOverride> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.listOf().fieldOf("targets").forGetter(MobOverride::targets),
			AttribOverride.CODEC.optionalFieldOf("health",         AttribOverride.EMPTY).forGetter(MobOverride::health),
			AttribOverride.CODEC.optionalFieldOf("speed",          AttribOverride.EMPTY).forGetter(MobOverride::speed),
			AttribOverride.CODEC.optionalFieldOf("damage",         AttribOverride.EMPTY).forGetter(MobOverride::damage),
			AttribOverride.CODEC.optionalFieldOf("knockback",      AttribOverride.EMPTY).forGetter(MobOverride::knockback),
			AttribOverride.CODEC.optionalFieldOf("armor",          AttribOverride.EMPTY).forGetter(MobOverride::armor),
			AttribOverride.CODEC.optionalFieldOf("armorToughness", AttribOverride.EMPTY).forGetter(MobOverride::armorToughness),
			AttribOverride.CODEC.optionalFieldOf("attackSpeed",    AttribOverride.EMPTY).forGetter(MobOverride::attackSpeed),
			AttribOverride.CODEC.optionalFieldOf("followRange",    AttribOverride.EMPTY).forGetter(MobOverride::followRange)
	).apply(i, MobOverride::new));
}
