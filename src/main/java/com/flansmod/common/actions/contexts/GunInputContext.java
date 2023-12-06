package com.flansmod.common.actions.contexts;

import com.flansmod.common.types.vehicles.EPlayerInput;

import javax.annotation.Nonnull;

public class GunInputContext
{
	public static final GunInputContext INVALID = new GunInputContext(GunContext.INVALID, EPlayerInput.Fire1);

	public final GunContext Gun;
	public final EPlayerInput InputType;

	public static GunInputContext CreateFrom(GunContext gunContext, EPlayerInput inputType)
	{
		if(gunContext.IsValid())
			return gunContext.GetInputContext(inputType);
		return INVALID;
	}
	protected GunInputContext(@Nonnull GunContext gun, @Nonnull EPlayerInput inputType)
	{
		Gun = gun;
		InputType = inputType;
	}
}
