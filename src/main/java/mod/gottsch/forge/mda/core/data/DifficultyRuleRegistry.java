package mod.gottsch.forge.mda.core.data;

import net.minecraft.world.Difficulty;

import java.util.Map;

/**
 * Holds the active difficulty rules loaded from datapacks.
 * The snapshot is replaced atomically on each {@code /reload}.
 */
public final class DifficultyRuleRegistry {

	private static volatile Map<Difficulty, DifficultyRule> snapshot = Map.of();

	private DifficultyRuleRegistry() {}

	/**
	 * Atomically replaces the active rule set.
	 * Called by {@link DifficultyRuleReloadListener} at the end of each reload.
	 */
	public static void set(Map<Difficulty, DifficultyRule> rules) {
		snapshot = rules;
	}

	/**
	 * Returns the rule for the given difficulty, or {@link DifficultyRule#EMPTY}
	 * if no rule has been loaded for it (default: enabled, multiplier 1.0).
	 */
	public static DifficultyRule lookup(Difficulty difficulty) {
		return snapshot.getOrDefault(difficulty, DifficultyRule.EMPTY);
	}
}
