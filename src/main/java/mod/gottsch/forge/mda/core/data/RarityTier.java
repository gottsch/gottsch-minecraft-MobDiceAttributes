package mod.gottsch.forge.mda.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public record RarityTier(ResourceLocation profile, int weight) {

	public static final Codec<RarityTier> CODEC = RecordCodecBuilder.create(i -> i.group(
			ResourceLocation.CODEC.fieldOf("profile").forGetter(RarityTier::profile),
			Codec.INT.fieldOf("weight").forGetter(RarityTier::weight)
	).apply(i, RarityTier::new));
}
