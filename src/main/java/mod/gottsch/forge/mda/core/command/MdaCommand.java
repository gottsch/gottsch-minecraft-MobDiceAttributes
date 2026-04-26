package mod.gottsch.forge.mda.core.command;

import java.util.Collection;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import mod.gottsch.forge.mda.core.manager.DiceAttributeManger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MdaCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal("mda")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("reroll")
					.then(Commands.argument("targets", EntityArgument.entities())
						.executes(MdaCommand::executeReroll)))
				.then(Commands.literal("inspect")
					.then(Commands.argument("target", EntityArgument.entity())
						.executes(MdaCommand::executeInspect)))
		);
	}

	private static int executeReroll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
		int count = 0;
		for (Entity entity : targets) {
			if (DiceAttributeManger.isValidEntity(entity)) {
				DiceAttributeManger.reroll(entity);
				count++;
			}
		}
		final int result = count;
		ctx.getSource().sendSuccess(
				() -> Component.literal("Rerolled " + result + " mob(s)."), true);
		return result;
	}

	private static int executeInspect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Entity entity = EntityArgument.getEntity(ctx, "target");

		if (!DiceAttributeManger.isValidEntity(entity)) {
			ctx.getSource().sendFailure(
					Component.literal(entity.getDisplayName().getString() + " is not a valid mob."));
			return 0;
		}

		LivingEntity living = (LivingEntity) entity;
		StringBuilder sb = new StringBuilder();
		sb.append("--- ").append(entity.getDisplayName().getString()).append(" ---\n");

		appendAttrib(sb, "HP",           living.getMaxHealth());
		appendAttrib(sb, "Speed",        getBase(living, Attributes.MOVEMENT_SPEED));
		appendAttrib(sb, "Damage",       getBase(living, Attributes.ATTACK_DAMAGE));
		appendAttrib(sb, "Knockback",    getBase(living, Attributes.ATTACK_KNOCKBACK));
		appendAttrib(sb, "Armor",        getBase(living, Attributes.ARMOR));
		appendAttrib(sb, "Armor Tough.", getBase(living, Attributes.ARMOR_TOUGHNESS));
		appendAttrib(sb, "Atk Speed",    getBase(living, Attributes.ATTACK_SPEED));
		appendAttrib(sb, "Follow Range", getBase(living, Attributes.FOLLOW_RANGE));

		double ratio = entity.getPersistentData().getDouble("mda_hp_ratio");
		if (ratio > 0) {
			sb.append(String.format("HP ratio:    %.2f\n", ratio));
		}
		double baseHp = entity.getPersistentData().getDouble("mda_base_hp");
		if (baseHp > 0) {
			sb.append(String.format("Base HP:     %.2f\n", baseHp));
		}
		String profile = entity.getPersistentData().getString("mda_profile");
		if (!profile.isEmpty()) {
			sb.append("Profile:     ").append(profile).append("\n");
		}
		String spawnType = entity.getPersistentData().getString("mda_spawn_type");
		if (!spawnType.isEmpty()) {
			sb.append("Spawn type:  ").append(spawnType).append("\n");
		}

		ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
		return 1;
	}

	private static double getBase(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
		var instance = entity.getAttribute(attribute);
		return instance != null ? instance.getBaseValue() : 0;
	}

	private static void appendAttrib(StringBuilder sb, String label, double value) {
		if (value > 0) {
			sb.append(String.format("%-12s %.2f\n", label + ":", value));
		}
	}
}
