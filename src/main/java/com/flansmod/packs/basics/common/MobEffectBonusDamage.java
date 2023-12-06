package com.flansmod.packs.basics.common;

import com.flansmod.common.effects.FlansMobEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import org.jetbrains.annotations.NotNull;

public class MobEffectBonusDamage extends FlansMobEffect
{
	public MobEffectBonusDamage(@NotNull ResourceLocation abilityLoc, @NotNull MobEffectCategory category, int colour)
	{
		super(abilityLoc, category, colour);
	}


}
