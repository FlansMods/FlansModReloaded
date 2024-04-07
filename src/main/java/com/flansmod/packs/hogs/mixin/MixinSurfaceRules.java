package com.flansmod.packs.hogs.mixin;

import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SurfaceRuleData.class)
public class MixinSurfaceRules
{

	private static final SurfaceRules.RuleSource ROAD_SURFACE =  SurfaceRules.state(Blocks.BASALT.defaultBlockState());


	@Inject(
		method = "nether()Lnet/minecraft/world/level/levelgen/SurfaceRules$RuleSource;",
		at = @At("RETURN"))
	private void nether(CallbackInfoReturnable<SurfaceRules.RuleSource> callback)
	{
		SurfaceRules.RuleSource existingSequence = callback.getReturnValue();
		SurfaceRules.RuleSource appendedSequence = SurfaceRules.sequence(existingSequence, SurfaceRules.ifTrue(SurfaceRules.hole(), ROAD_SURFACE));
		callback.setReturnValue(appendedSequence);
	}
}
