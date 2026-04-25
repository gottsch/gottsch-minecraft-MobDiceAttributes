package mod.gottsch.forge.mda.core.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import mod.gottsch.forge.mda.MDA;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class SpawnProfileReloadListener extends SimpleJsonResourceReloadListener {

	private static final Gson GSON = new GsonBuilder().create();
	private static final String DIRECTORY = "spawn_profiles";

	public SpawnProfileReloadListener() {
		super(GSON, DIRECTORY);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files,
	                     ResourceManager resourceManager,
	                     ProfilerFiller profiler) {

		Map<ResourceLocation, SpawnProfile> loaded = new HashMap<>();

		for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
			ResourceLocation fileId = entry.getKey();
			if (!fileId.getNamespace().equals(MDA.MOD_ID)) continue;
			SpawnProfile.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
					.resultOrPartial(err -> MDA.LOGGER.warn("Skipping malformed spawn_profile {}: {}", fileId, err))
					.ifPresent(profile -> loaded.put(fileId, profile));
		}

		SpawnProfileRegistry.set(loaded);
		MDA.LOGGER.info("Loaded {} spawn profile(s)", loaded.size());
	}
}
