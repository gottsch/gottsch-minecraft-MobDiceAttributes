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
import net.minecraft.world.Difficulty;

import java.util.EnumMap;
import java.util.Map;

public class DifficultyRuleReloadListener extends SimpleJsonResourceReloadListener {

	private static final Gson GSON = new GsonBuilder().create();
	private static final String DIRECTORY = "difficulty_rules";

	public DifficultyRuleReloadListener() {
		super(GSON, DIRECTORY);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files,
	                     ResourceManager resourceManager,
	                     ProfilerFiller profiler) {

		Map<Difficulty, DifficultyRule> rules = new EnumMap<>(Difficulty.class);

		for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
			ResourceLocation fileId = entry.getKey();
			if (!fileId.getNamespace().equals(MDA.MOD_ID)) continue;
			DifficultyRule.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
					.resultOrPartial(err -> MDA.LOGGER.warn("Skipping malformed difficulty_rule {}: {}", fileId, err))
					.ifPresent(rule -> {
						for (String target : rule.targets()) {
							Difficulty difficulty = parseDifficulty(target);
							if (difficulty == null) {
								MDA.LOGGER.warn("Unknown difficulty '{}' in {} — valid values: peaceful, easy, normal, hard",
										target, fileId);
								continue;
							}
							// Last loaded wins — vanilla datapack precedence
							rules.put(difficulty, rule);
						}
					});
		}

		DifficultyRuleRegistry.set(rules);
		MDA.LOGGER.info("Loaded {} difficulty rule file(s)", files.size());
	}

	/**
	 * Parses a lowercase difficulty name to the corresponding enum value.
	 * Returns {@code null} for unrecognised names so the caller can log a warning.
	 */
	private static Difficulty parseDifficulty(String name) {
		switch (name.toLowerCase()) {
			case "peaceful": return Difficulty.PEACEFUL;
			case "easy":     return Difficulty.EASY;
			case "normal":   return Difficulty.NORMAL;
			case "hard":     return Difficulty.HARD;
			default:         return null;
		}
	}
}
