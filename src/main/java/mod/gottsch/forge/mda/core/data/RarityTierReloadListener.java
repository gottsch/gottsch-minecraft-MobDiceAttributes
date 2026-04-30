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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RarityTierReloadListener extends SimpleJsonResourceReloadListener {

	private static final Gson GSON = new GsonBuilder().create();
	private static final String DIRECTORY = "rarity_tiers";

	// Codec for the { "tiers": [...] } wrapper object
	private static final com.mojang.serialization.Codec<List<RarityTier>> FILE_CODEC =
			com.mojang.serialization.codecs.RecordCodecBuilder.create(i -> i.group(
					RarityTier.CODEC.listOf().fieldOf("tiers").forGetter(list -> list)
			).apply(i, list -> list));

	public RarityTierReloadListener() {
		super(GSON, DIRECTORY);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files,
	                     ResourceManager resourceManager,
	                     ProfilerFiller profiler) {

		List<RarityTier> loaded = new ArrayList<>();

		for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
			ResourceLocation fileId = entry.getKey();
			if (!fileId.getNamespace().equals(MDA.MOD_ID)) continue;
			FILE_CODEC.parse(JsonOps.INSTANCE, entry.getValue())
					.resultOrPartial(err -> MDA.LOGGER.warn("Skipping malformed rarity_tiers {}: {}", fileId, err))
					.ifPresent(tiers -> {
						for (RarityTier tier : tiers) {
							if (tier.weight() <= 0) {
								MDA.LOGGER.warn("Skipping rarity tier with non-positive weight in {}: {}", fileId, tier.profile());
								continue;
							}
							loaded.add(tier);
						}
					});
		}

		RarityTierRegistry.set(loaded);
		MDA.LOGGER.info("Loaded {} rarity tier(s)", loaded.size());
	}
}
