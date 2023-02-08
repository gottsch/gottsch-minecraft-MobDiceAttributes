/*
 * This file is part of  Mob Dice Attributes2.
 * Copyright (c) 2023 Mark Gottschling (gottsch)
 *
 * Mob Dice Attributes2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mob Dice Attributes2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mob Dice Attributes2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package mod.gottsch.forge.mda;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mod.gottsch.forge.mda.core.setup.CommonSetup;
import mod.gottsch.forge.mda.core.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * 
 * @author Mark Gottschling Feb 8, 2023
 *
 */
@Mod(value = MDA.MOD_ID)
public class MDA {
	// logger
	public static Logger LOGGER = LogManager.getLogger(MDA.MOD_ID);

	public static final String MOD_ID = "mobdiceattrib";

	/**
	 * 
	 */
	public MDA() {
//		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
//		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
		
		// register the deferred registries
        Registration.init();
        
		// Register the setup method for modloading
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(CommonSetup::common);
	}
}
