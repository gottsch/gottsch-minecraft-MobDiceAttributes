package mod.gottsch.forge.mda.core.event;

import mod.gottsch.forge.mda.MDA;
import mod.gottsch.forge.mda.core.data.DifficultyRuleReloadListener;
import mod.gottsch.forge.mda.core.data.DimensionRuleReloadListener;
import mod.gottsch.forge.mda.core.data.MobOverrideReloadListener;
import mod.gottsch.forge.mda.core.data.RarityTierReloadListener;
import mod.gottsch.forge.mda.core.data.SpawnHookReloadListener;
import mod.gottsch.forge.mda.core.data.SpawnProfileReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = MDA.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
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
