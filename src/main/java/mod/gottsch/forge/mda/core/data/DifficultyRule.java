package mod.gottsch.forge.mda.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

/**
 * A per-difficulty rule that can disable rolling entirely and/or scale
 * attribute targets by a multiplier before dice are rolled.
 *
 * <p>Target strings in JSON are lowercase difficulty names:
 * {@code "peaceful"}, {@code "easy"}, {@code "normal"}, {@code "hard"}.
 *
 * <p>When both a dimension rule and a difficulty rule are active, their
 * multipliers are multiplied together. Either gate can independently disable rolling.
 *
 * <p>Absence of a rule for a difficulty = default behaviour (enabled, multiplier 1.0).
 * Use {@link #EMPTY} for the no-rule case.
 */
public record DifficultyRule(List<String> targets,
                             Optional<Boolean> enable,
                             Optional<Double> multiplier) {

	/** Returned for difficulties with no datapack rule — fully inert. */
	public static final DifficultyRule EMPTY =
			new DifficultyRule(List.of(), Optional.empty(), Optional.empty());

	public static final Codec<DifficultyRule> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.listOf().fieldOf("targets").forGetter(DifficultyRule::targets),
			Codec.BOOL.optionalFieldOf("enable").forGetter(DifficultyRule::enable),
			Codec.DOUBLE.optionalFieldOf("multiplier").forGetter(DifficultyRule::multiplier)
	).apply(i, DifficultyRule::new));

	/** Whether rolling should occur at this difficulty. Defaults to {@code true}. */
	public boolean isEnabled() {
		return enable.orElse(true);
	}

	/**
	 * Scale factor applied to every attribute target before rolling.
	 * Stacks multiplicatively with any active dimension rule multiplier.
	 * Defaults to {@code 1.0}.
	 */
	public double getMultiplier() {
		return multiplier.orElse(1.0);
	}
}
