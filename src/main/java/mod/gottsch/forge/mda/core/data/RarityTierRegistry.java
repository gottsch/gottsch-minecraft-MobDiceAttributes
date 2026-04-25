package mod.gottsch.forge.mda.core.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Optional;

public class RarityTierRegistry {

	private static volatile List<RarityTier> tiers = List.of();

	public static void set(List<RarityTier> newTiers) {
		tiers = List.copyOf(newTiers);
	}

	public static boolean isEmpty() {
		return tiers.isEmpty();
	}

	public static Optional<ResourceLocation> selectProfile(RandomSource random) {
		if (tiers.isEmpty()) return Optional.empty();

		int total = tiers.stream().mapToInt(RarityTier::weight).sum();
		int roll = random.nextInt(total);
		int cumulative = 0;
		for (RarityTier tier : tiers) {
			cumulative += tier.weight();
			if (roll < cumulative) return Optional.of(tier.profile());
		}
		// Fallback — should not be reached with correct weights
		return Optional.of(tiers.get(tiers.size() - 1).profile());
	}
}
