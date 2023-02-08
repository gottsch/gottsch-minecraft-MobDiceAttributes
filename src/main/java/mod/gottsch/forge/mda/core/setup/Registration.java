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
package mod.gottsch.forge.mda.core.setup;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * 
 * @author Mark Gottschling Feb 8, 2023
 *
 */
public class Registration {

	/**
	 * 
	 */
	public static void init() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
	}
}
