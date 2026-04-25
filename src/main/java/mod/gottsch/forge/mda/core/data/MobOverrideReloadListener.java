package mod.gottsch.forge.mda.core.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import mod.gottsch.forge.mda.MDA;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class MobOverrideReloadListener extends SimpleJsonResourceReloadListener {

	private static final Gson GSON = new GsonBuilder().create();
	private static final String DIRECTORY = "mob_overrides";

	public MobOverrideReloadListener() {
		super(GSON, DIRECTORY);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files,
	                     ResourceManager resourceManager,
	                     ProfilerFiller profiler) {

		Map<MobOverrideRegistry.Attrib, Map<ResourceLocation, AttribOverride>> byId = new EnumMap<>(MobOverrideRegistry.Attrib.class);
		Map<MobOverrideRegistry.Attrib, Map<TagKey<EntityType<?>>, AttribOverride>> byTag = new EnumMap<>(MobOverrideRegistry.Attrib.class);
		for (MobOverrideRegistry.Attrib a : MobOverrideRegistry.Attrib.values()) {
			byId.put(a, new HashMap<>());
			byTag.put(a, new HashMap<>());
		}

		for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
			ResourceLocation fileId = entry.getKey();
			if (!fileId.getNamespace().equals(MDA.MOD_ID)) continue;
			MobOverride.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
					.resultOrPartial(err -> MDA.LOGGER.warn("Skipping malformed mob_override {}: {}", fileId, err))
					.ifPresent(mo -> absorb(mo, fileId, byId, byTag));
		}

		MobOverrideRegistry.set(new MobOverrideRegistry.Snapshot(byId, byTag));
		MDA.LOGGER.info("Loaded {} mob override file(s)", files.size());
	}

	private static void absorb(MobOverride mo,
	                           ResourceLocation fileId,
	                           Map<MobOverrideRegistry.Attrib, Map<ResourceLocation, AttribOverride>> byId,
	                           Map<MobOverrideRegistry.Attrib, Map<TagKey<EntityType<?>>, AttribOverride>> byTag) {

		for (String target : mo.targets()) {
			if (target.startsWith("#")) {
				String raw = target.substring(1);
				ResourceLocation tagLoc = ResourceLocation.tryParse(raw);
				if (tagLoc == null) {
					MDA.LOGGER.warn("Invalid tag target '{}' in {}", target, fileId);
					continue;
				}
				TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, tagLoc);
				putForEach(mo, byTag, tag);
			} else {
				ResourceLocation mobId = ResourceLocation.tryParse(target);
				if (mobId == null) {
					MDA.LOGGER.warn("Invalid mob target '{}' in {}", target, fileId);
					continue;
				}
				putForEach(mo, byId, mobId);
			}
		}
	}

	private static <K> void putForEach(MobOverride mo,
	                                   Map<MobOverrideRegistry.Attrib, Map<K, AttribOverride>> dest,
	                                   K key) {
		for (MobOverrideRegistry.Attrib a : MobOverrideRegistry.Attrib.values()) {
			AttribOverride ov = a.accessor.apply(mo);
			if (ov == null || ov.equals(AttribOverride.EMPTY)) continue;
			Map<K, AttribOverride> map = dest.get(a);
			AttribOverride existing = map.get(key);
			map.put(key, existing == null ? ov : existing.merge(ov));
		}
	}
}
