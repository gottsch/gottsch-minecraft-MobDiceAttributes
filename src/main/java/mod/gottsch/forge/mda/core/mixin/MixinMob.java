package mod.gottsch.forge.mda.core.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 
 * @author Mark Gottschling Feb 8, 2023
 *
 */
@Deprecated
@Mixin(Mob.class)
public class MixinMob {
	
	@Inject(at = @At("RETURN"), method = "net/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;", cancellable = false)
	   private void finalizeSpawn(ServerLevelAccessor serverAccessor, DifficultyInstance difficulty, 
			   MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, 
			   @Nullable CompoundTag tag, CallbackInfoReturnable<SpawnGroupData > callback) {
		   
		AttributeInstance attribute = this.getAttribute(Attributes.MAX_HEALTH);
		if (attribute != null) {
			double newHealth = this.getMaxHealth() + 10;
			attribute.setBaseValue(newHealth);
			this.setHealth(this.getMaxHealth());
		}
	   }

	@Shadow
	public AttributeInstance getAttribute(Attribute a) {
	  throw new IllegalStateException("Mixin failed to shadow getAttribute()");
	}
	
	@Shadow
	public double getMaxHealth() {
		throw new IllegalStateException("Mixin failed to shadow getMaxHealth()");
	}
	
	@Shadow
	public void setHealth(double health) {
		throw new IllegalStateException("Mixin failed to shadow setHealth()");
	}
}
