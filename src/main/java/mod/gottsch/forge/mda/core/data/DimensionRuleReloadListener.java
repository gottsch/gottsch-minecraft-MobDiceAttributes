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

public class DimensionRuleReloadListener extends SimpleJsonResourceReloadListener {

	private static final Gson GSON = new GsonBuilder().create();
	private static final String DIRECTORY = "dimension_rules";

	public DimensionRuleReloadListener() {
		super(GSON, DIRECTORY);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files,
	                     ResourceManager resourceManager,
	                     ProfilerFiller profiler) {

		Map<ResourceLocation, DimensionRule> rules = new HashMap<>();

		for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
			ResourceLocation fileId = entry.getKey();
			if (!fileId.getNamespace().equals(MDA.MOD_ID)) continue;
			DimensionRule.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
					.resultOrPartial(err -> MDA.LOGGER.warn("Skipping malformed dimension_rule {}: {}", fileId, err))
					.ifPresent(rule -> {
						for (String target : rule.targets()) {
							ResourceLocation dimId = ResourceLocation.tryParse(target);
							if (dimId == null) {
								MDA.LOGGER.warn("Invalid dimension target '{}' in {}", target, fileId);
								continue;
							}
							// Last loaded wins — vanilla datapack precedence
							rules.put(dimId, rule);
						}
					});
		}

		DimensionRuleRegistry.set(rules);
		MDA.LOGGER.info("Loaded {} dimension rule file(s)", files.size());
	}
}
