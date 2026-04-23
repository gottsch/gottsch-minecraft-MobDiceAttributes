package mod.gottsch.forge.mda.core.event;

import mod.gottsch.neo.gottschcore.world.WorldInfo;
import mod.gottsch.forge.mda.MDA;
import mod.gottsch.forge.mda.core.manager.DiceAttributeManger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 *
 * @author Mark Gottschling Feb 8, 2023
 *
 */
@EventBusSubscriber(modid = MDA.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class WorldEvents {
	private static final String ROLLED = ResourceLocation.fromNamespaceAndPath(MDA.MOD_ID, "rolled").toString();

	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {

		if (WorldInfo.isClientSide(event.getLevel())) {
			return;
		}

		Entity entity = event.getEntity();
		if (!DiceAttributeManger.isValidEntity(entity)) {
			return;
		}

		CompoundTag tags = entity.getPersistentData();
		boolean isAlreadyRolled = tags.getBoolean(ROLLED);
		if (isAlreadyRolled) {
			return;
		}

		DiceAttributeManger.applyRolls(entity);

		// flag that entity has its rolls applied
		tags.putBoolean(ROLLED, true);
	}
}
