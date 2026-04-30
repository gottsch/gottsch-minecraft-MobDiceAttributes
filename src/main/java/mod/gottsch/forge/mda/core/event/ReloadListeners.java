package mod.gottsch.forge.mda.core.event;


import mod.gottsch.forge.mda.MDA;
import mod.gottsch.forge.mda.core.data.*;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MDA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReloadListeners {

	@SubscribeEvent
	public static void onAddReloadListeners(AddReloadListenerEvent event) {
		event.addListener(new MobOverrideReloadListener());
		event.addListener(new DimensionRuleReloadListener());
		event.addListener(new DifficultyRuleReloadListener());
		event.addListener(new SpawnProfileReloadListener());
		event.addListener(new RarityTierReloadListener());
		event.addListener(new SpawnHookReloadListener());
	}
}
