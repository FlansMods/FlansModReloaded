package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.abilities.AbilityDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Subtly different to ApplyEffect ability, this does not show up in the HUD as a mob effect
// Use for things that are more constant and less trigger based
public class ApplyModifierAbility extends InstantAbility
{
	public boolean IsActive = false;

	public ApplyModifierAbility(AbilityDefinition def, int level)
	{
		super(def, level);
	}

	public ModifierDefinition[] GetModifiers()
	{
		return Def.modifiers;
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nullable HitResult hit)
	{
		super.Trigger(gun, hit);
		IsActive = true;
	}

	@Override
	public void End(@Nonnull GunContext gun)
	{
		super.End(gun);
		IsActive = false;
	}
}
