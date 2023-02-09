package mod.gottsch.forge.mda.core.event;

import mod.gottsch.forge.gottschcore.world.WorldInfo;
import mod.gottsch.forge.mda.MDA;
import mod.gottsch.forge.mda.core.manager.DiceAttributeManger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * 
 * @author Mark Gottschling Feb 8, 2023
 *
 */
@Mod.EventBusSubscriber(modid = MDA.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class WorldEvents {
	private static final String ROLLED = new ResourceLocation(MDA.MOD_ID, "rolled").toString();
	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {
		
		if (WorldInfo.isClientSide(event.getLevel())) {
			return;
		}
		
		Entity entity = event.getEntity();
		if (!DiceAttributeManger.isValidEntity(entity)) {
			return;
		}
		
//		MDA.LOGGER.info("entity -> {} joined world!", entity.getDisplayName().getString());
		CompoundTag tags = entity.getPersistentData();
		boolean isAlreadyRolled = tags.getBoolean(ROLLED);
		if (isAlreadyRolled) {
			return;
		}
//		MDA.LOGGER.info("applyng attrib changes to entity -> {}", entity.getDisplayName().getString());

		DiceAttributeManger.applyRolls(entity);
		
		// flag that entity has its rolls applied
		tags.putBoolean(ROLLED, true);

	}
}
