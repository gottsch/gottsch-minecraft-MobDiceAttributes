package mod.gottsch.forge.mda.core.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;

import java.util.Map;
import java.util.Optional;

public class SpawnHookRegistry {

	private static volatile Map<MobSpawnType, ResourceLocation> hooks = Map.of();

	public static Optional<ResourceLocation> lookup(MobSpawnType spawnType) {
		return Optional.ofNullable(hooks.get(spawnType));
	}

	public static void set(Map<MobSpawnType, ResourceLocation> newHooks) {
		hooks = Map.copyOf(newHooks);
	}
}
