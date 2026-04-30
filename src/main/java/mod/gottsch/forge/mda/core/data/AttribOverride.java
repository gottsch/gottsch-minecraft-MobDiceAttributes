package mod.gottsch.forge.mda.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record AttribOverride(Optional<Integer> dice,
                             Optional<Double> rangeFactor,
                             Optional<Double> bonus) {

	public static final AttribOverride EMPTY =
			new AttribOverride(Optional.empty(), Optional.empty(), Optional.empty());

	public static final Codec<AttribOverride> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.optionalFieldOf("dice").forGetter(AttribOverride::dice),
			Codec.DOUBLE.optionalFieldOf("rangeFactor").forGetter(AttribOverride::rangeFactor),
			Codec.DOUBLE.optionalFieldOf("bonus").forGetter(AttribOverride::bonus)
	).apply(i, AttribOverride::new));

	public AttribOverride merge(AttribOverride higherPriority) {
		return new AttribOverride(
				higherPriority.dice.or(() -> this.dice),
				higherPriority.rangeFactor.or(() -> this.rangeFactor),
				higherPriority.bonus.or(() -> this.bonus));
	}
}
