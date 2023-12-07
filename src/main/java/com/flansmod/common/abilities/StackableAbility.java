package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.abilities.AbilityDefinition;
import com.flansmod.util.Maths;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class StackableAbility extends AbilityInstance
{
	private float Intensity = 0.0f;
	private float Duration = 0.0f;

	public StackableAbility(AbilityDefinition def, int level)
	{
		super(def, level);
	}

	public float GetIntensity() { return Intensity; }
	public float GetDurationSeconds() { return Duration; }
	public int GetDurationTicks() { return Maths.Ceil(Duration * 20.0f); }

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nullable HitResult hit)
	{
		if(Def.stackAmount)
		{
			Intensity += Def.CalculateIntensity(Level, gun);
			Intensity = Maths.Clamp(Intensity, 0f, Def.maxAmount);
		}
		else
			Intensity = Def.CalculateIntensity(Level, gun);
		if(Def.stackDuration)
		{
			Duration += Def.GetDuration(Level);
			Duration = Maths.Clamp(Duration, 0f, Def.maxDuration);
		}
		else
			Duration = Def.GetDuration(Level);
	}

	@Override
	public void End(@Nonnull GunContext gun)
	{
		Intensity = 0.0f;
		Duration = 0.0f;
	}

	@Override
	public void Tick()
	{
		Duration -= 1f/20f;
		Duration = Maths.Clamp(Duration, 0, 9999);
		if(Duration <= 0.0f)
			Intensity = 0.0f;
	}
}
