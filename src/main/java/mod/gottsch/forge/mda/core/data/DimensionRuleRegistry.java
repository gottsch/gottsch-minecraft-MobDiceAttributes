package mod.gottsch.forge.mda.core.data;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Holds the active dimension rules loaded from datapacks.
 * The snapshot is replaced atomically on each {@code /reload}.
 */
public final class DimensionRuleRegistry {

	private static volatile Map<ResourceLocation, DimensionRule> snapshot = Map.of();

	private DimensionRuleRegistry() {}

	/**
	 * Atomically replaces the active rule set.
	 * Called by {@link DimensionRuleReloadListener} at the end of each reload.
	 */
	public static void set(Map<ResourceLocation, DimensionRule> rules) {
		snapshot = rules;
	}

	/**
	 * Returns the rule for the given dimension, or {@link DimensionRule#EMPTY}
	 * if no rule has been loaded for it (default: enabled, multiplier 1.0).
	 */
	public static DimensionRule lookup(ResourceLocation dimension) {
		return snapshot.getOrDefault(dimension, DimensionRule.EMPTY);
	}
}
