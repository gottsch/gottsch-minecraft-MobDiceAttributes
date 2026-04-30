package mod.gottsch.forge.mda.core.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.function.Function;

public final class MobOverrideRegistry {

	public enum Attrib {
		HEALTH(MobOverride::health),
		SPEED(MobOverride::speed),
		DAMAGE(MobOverride::damage),
		KNOCKBACK(MobOverride::knockback),
		ARMOR(MobOverride::armor),
		ARMOR_TOUGHNESS(MobOverride::armorToughness),
		ATTACK_SPEED(MobOverride::attackSpeed),
		FOLLOW_RANGE(MobOverride::followRange);

		final Function<MobOverride, AttribOverride> accessor;
		Attrib(Function<MobOverride, AttribOverride> accessor) { this.accessor = accessor; }
	}

	private static volatile Snapshot snapshot = Snapshot.empty();

	private MobOverrideRegistry() {}

	public record Snapshot(Map<Attrib, Map<ResourceLocation, AttribOverride>> byId,
	                       Map<Attrib, Map<TagKey<EntityType<?>>, AttribOverride>> byTag) {
		public static Snapshot empty() { return new Snapshot(Map.of(), Map.of()); }
	}

	public static void set(Snapshot next) {
		snapshot = next;
	}

	public static AttribOverride lookup(EntityType<?> type, Attrib which) {
		Snapshot s = snapshot;
		AttribOverride tagResult = AttribOverride.EMPTY;
		Map<TagKey<EntityType<?>>, AttribOverride> tagMap = s.byTag.get(which);
		if (tagMap != null && !tagMap.isEmpty()) {
			var holder = type.builtInRegistryHolder();
			for (var e : tagMap.entrySet()) {
				if (holder.is(e.getKey())) {
					tagResult = tagResult.merge(e.getValue());
				}
			}
		}
		Map<ResourceLocation, AttribOverride> idMap = s.byId.get(which);
		ResourceLocation id = EntityType.getKey(type);
		AttribOverride idResult = idMap == null ? AttribOverride.EMPTY
				: idMap.getOrDefault(id, AttribOverride.EMPTY);
		return tagResult.merge(idResult);
	}

	@SuppressWarnings("unused")
	private static TagKey<EntityType<?>> tag(ResourceLocation loc) {
		return TagKey.create(Registries.ENTITY_TYPE, loc);
	}
}
