package mod.gottsch.forge.mda.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

/**
 * A per-dimension rule that can disable rolling entirely and/or scale
 * attribute targets by a multiplier before dice are rolled.
 *
 * <p>Resolution: absence of a rule for a dimension means default behaviour
 * (enabled, multiplier 1.0). Use {@link #EMPTY} for the no-rule case.
 */
public record DimensionRule(List<String> targets,
                            Optional<Boolean> enable,
                            Optional<Double> multiplier) {

	/** Returned for dimensions with no datapack rule — fully inert. */
	public static final DimensionRule EMPTY =
			new DimensionRule(List.of(), Optional.empty(), Optional.empty());

	public static final Codec<DimensionRule> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.listOf().fieldOf("targets").forGetter(DimensionRule::targets),
			Codec.BOOL.optionalFieldOf("enable").forGetter(DimensionRule::enable),
			Codec.DOUBLE.optionalFieldOf("multiplier").forGetter(DimensionRule::multiplier)
	).apply(i, DimensionRule::new));

	/** Whether rolling should occur in this dimension. Defaults to {@code true}. */
	public boolean isEnabled() {
		return enable.orElse(true);
	}

	/** Scale factor applied to every attribute target before rolling. Defaults to {@code 1.0}. */
	public double getMultiplier() {
		return multiplier.orElse(1.0);
	}
}
