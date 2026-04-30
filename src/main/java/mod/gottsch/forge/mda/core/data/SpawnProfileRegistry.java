package mod.gottsch.forge.mda.core.data;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class SpawnProfileRegistry {

	private static volatile Map<ResourceLocation, SpawnProfile> profiles = Map.of();

	public static SpawnProfile lookup(ResourceLocation id) {
		return profiles.getOrDefault(id, SpawnProfile.EMPTY);
	}

	public static void set(Map<ResourceLocation, SpawnProfile> newProfiles) {
		profiles = Map.copyOf(newProfiles);
	}
}
