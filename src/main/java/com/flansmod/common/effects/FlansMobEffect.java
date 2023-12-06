package com.flansmod.common.effects;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.abilities.AbilityDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import javax.annotation.Nonnull;

public abstract class FlansMobEffect extends MobEffect
{
	@Nonnull
	public final ResourceLocation AbilityLocation;
	public AbilityDefinition Def() { return FlansMod.ABILITIES.Get(AbilityLocation); };

	protected FlansMobEffect(@Nonnull ResourceLocation abilityLoc, @Nonnull MobEffectCategory category, int colour)
	{
		super(category, colour);
		AbilityLocation = abilityLoc;
	}
}
