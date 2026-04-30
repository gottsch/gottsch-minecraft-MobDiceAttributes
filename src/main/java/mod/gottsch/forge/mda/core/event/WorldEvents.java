package mod.gottsch.forge.mda.core.event;

import mod.gottsch.forge.gottschcore.world.WorldInfo;
import mod.gottsch.forge.mda.MDA;
import mod.gottsch.forge.mda.core.command.MdaCommand;
import mod.gottsch.forge.mda.core.config.Config;
import mod.gottsch.forge.mda.core.data.SpawnProfile;
import mod.gottsch.forge.mda.core.data.SpawnProfileRegistry;
import mod.gottsch.forge.mda.core.manager.DiceAttributeManger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Mark Gottschling Feb 8, 2023
 *
 */
@Mod.EventBusSubscriber(modid = MDA.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class WorldEvents {
	private static final String ROLLED = new ResourceLocation(MDA.MOD_ID, "rolled").toString();

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		MdaCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
		// Store spawn type in NBT so applyRolls() can resolve the correct spawn hook profile.
		Mob mob = event.getEntity();
		mob.getPersistentData().putString("mda_spawn_type", event.getSpawnType().name());
	}

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

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event) {
		int bonusRolls = resolveLootBonusRolls(event.getEntity().getPersistentData());
		if (bonusRolls <= 0) return;

		List<ItemEntity> existing = new ArrayList<>(event.getDrops());
		for (int i = 0; i < bonusRolls; i++) {
			for (ItemEntity drop : existing) {
				ItemEntity extra = new ItemEntity(
						event.getEntity().level(),
						drop.getX(), drop.getY(), drop.getZ(),
						drop.getItem().copy());
				event.getDrops().add(extra);
			}
		}
	}

	@SubscribeEvent
	public static void onExperienceDrop(LivingExperienceDropEvent event) {
		double multiplier = resolveXpMultiplier(event.getEntity().getPersistentData());
		if (multiplier <= 1.0) return;
		event.setDroppedExperience((int) (event.getDroppedExperience() * multiplier));
	}

	/**
	 * Profile lootBonusRolls takes priority over [lootScaling] config.
	 * Falls back to [lootScaling] if the mob has no profile or the profile doesn't specify loot.
	 */
	private static int resolveLootBonusRolls(CompoundTag data) {
		String profileIdStr = data.getString("mda_profile");
		if (!profileIdStr.isEmpty()) {
			ResourceLocation profileId = ResourceLocation.tryParse(profileIdStr);
			if (profileId != null) {
				SpawnProfile profile = SpawnProfileRegistry.lookup(profileId);
				if (profile.lootBonusRolls().isPresent()) {
					return profile.lootBonusRolls().get();
				}
			}
		}
		if (!Config.SERVER.lootScaling.enable.get()) return 0;
		double ratio = data.getDouble("mda_hp_ratio");
		if (ratio < Config.SERVER.lootScaling.threshold.get()) return 0;
		return Config.SERVER.lootScaling.lootBonusRolls.get();
	}

	/**
	 * Profile xpMultiplier takes priority over [lootScaling] config.
	 * Falls back to [lootScaling] if the mob has no profile or the profile doesn't specify XP.
	 */
	private static double resolveXpMultiplier(CompoundTag data) {
		String profileIdStr = data.getString("mda_profile");
		if (!profileIdStr.isEmpty()) {
			ResourceLocation profileId = ResourceLocation.tryParse(profileIdStr);
			if (profileId != null) {
				SpawnProfile profile = SpawnProfileRegistry.lookup(profileId);
				if (profile.xpMultiplier().isPresent()) {
					return profile.xpMultiplier().get();
				}
			}
		}
		if (!Config.SERVER.lootScaling.enable.get()) return 1.0;
		double ratio = data.getDouble("mda_hp_ratio");
		if (ratio < Config.SERVER.lootScaling.threshold.get()) return 1.0;
		return Config.SERVER.lootScaling.xpMultiplier.get();
	}
}
