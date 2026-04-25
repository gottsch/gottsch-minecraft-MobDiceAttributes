package mod.gottsch.forge.mda.core.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mod.gottsch.forge.mda.MDA;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.MobSpawnType;

import java.util.EnumMap;
import java.util.Map;

public class SpawnHookReloadListener extends SimpleJsonResourceReloadListener {

	private static final Gson GSON = new GsonBuilder().create();
	private static final String DIRECTORY = "spawn_hooks";

	public SpawnHookReloadListener() {
		super(GSON, DIRECTORY);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files,
	                     ResourceManager resourceManager,
	                     ProfilerFiller profiler) {

		Map<MobSpawnType, ResourceLocation> hooks = new EnumMap<>(MobSpawnType.class);

		for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
			ResourceLocation fileId = entry.getKey();
			if (!fileId.getNamespace().equals(MDA.MOD_ID)) continue;

			if (!entry.getValue().isJsonObject()) {
				MDA.LOGGER.warn("Skipping malformed spawn_hooks {}: expected a JSON object", fileId);
				continue;
			}

			JsonObject obj = entry.getValue().getAsJsonObject();
			for (Map.Entry<String, JsonElement> hookEntry : obj.entrySet()) {
				// Omitted or null values mean no profile for this context
				if (hookEntry.getValue().isJsonNull()) continue;

				MobSpawnType spawnType = parseSpawnType(hookEntry.getKey());
				if (spawnType == null) {
					MDA.LOGGER.warn("Unknown spawn type '{}' in {} — valid types: {}",
							hookEntry.getKey(), fileId, validTypeNames());
					continue;
				}

				ResourceLocation profileId = ResourceLocation.tryParse(hookEntry.getValue().getAsString());
				if (profileId == null) {
					MDA.LOGGER.warn("Invalid profile ID '{}' in {}", hookEntry.getValue().getAsString(), fileId);
					continue;
				}

				// Last loaded wins — vanilla datapack precedence
				hooks.put(spawnType, profileId);
			}
		}

		SpawnHookRegistry.set(hooks);
		MDA.LOGGER.info("Loaded spawn hooks for {} spawn type(s)", hooks.size());
	}

	private static MobSpawnType parseSpawnType(String name) {
		return switch (name.toLowerCase()) {
			case "natural"           -> MobSpawnType.NATURAL;
			case "spawner"           -> MobSpawnType.SPAWNER;
			case "chunk_generation"  -> MobSpawnType.CHUNK_GENERATION;
			case "structure"         -> MobSpawnType.STRUCTURE;
			case "breeding"          -> MobSpawnType.BREEDING;
			case "mob_summoned"      -> MobSpawnType.MOB_SUMMONED;
			case "jockey"            -> MobSpawnType.JOCKEY;
			case "event"             -> MobSpawnType.EVENT;
			case "conversion"        -> MobSpawnType.CONVERSION;
			case "reinforcement"     -> MobSpawnType.REINFORCEMENT;
			case "triggered"         -> MobSpawnType.TRIGGERED;
			case "bucket"            -> MobSpawnType.BUCKET;
			case "spawn_egg"         -> MobSpawnType.SPAWN_EGG;
			case "command"           -> MobSpawnType.COMMAND;
			case "patrol"            -> MobSpawnType.PATROL;
			default                  -> null;
		};
	}

	private static String validTypeNames() {
		return "natural, spawner, chunk_generation, structure, breeding, mob_summoned, " +
				"jockey, event, conversion, reinforcement, triggered, bucket, spawn_egg, command, patrol";
	}
}
