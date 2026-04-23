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

import com.electronwill.nightconfig.core.CommentedConfig;

import mod.gottsch.forge.mda.core.config.Config;
import mod.gottsch.forge.mda.core.setup.CommonSetup;
import mod.gottsch.forge.mda.core.setup.Registration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent;

/**
 *
 * @author Mark Gottschling Feb 8, 2023
 *
 */
@Mod(value = MDA.MOD_ID)
public class MDA {
	// logger
	public static Logger LOGGER = LogManager.getLogger(MDA.MOD_ID);

	public static final String MOD_ID = "mobdiceattribs";

	/**
	 *
	 */
	public MDA(IEventBus modEventBus, ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
		modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

		// register the deferred registries
		Registration.init(modEventBus);

		// TODO phantoms and zombie horses and such aren't included - find out their parent interfaces

		// Register the setup method for modloading
		modEventBus.addListener(CommonSetup::common);
		modEventBus.addListener(this::config);
		// TODO add config listener to check config settings to ensure they are correct.
	}

	/**
	 * On a config event.
	 * @param event
	 */
	private void config(final ModConfigEvent event) {
		if (event.getConfig().getModId().equals(MOD_ID)) {
			if (event.getConfig().getType() == Type.SERVER) {
				IConfigSpec spec = event.getConfig().getSpec();
				// get the toml config data
				CommentedConfig commentedConfig = event.getConfig().getConfigData();

				if (spec == Config.SERVER_CONFIG) {
					LOGGER.debug("validating config...");
					// validate the config
					Config.validate(Config.SERVER);
				}
			}
		}
	}
}
