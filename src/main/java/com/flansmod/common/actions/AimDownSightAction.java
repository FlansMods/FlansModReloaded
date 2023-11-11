package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;

import javax.annotation.Nonnull;

public class AimDownSightAction extends Action
{
	protected boolean ShouldStop = false;

	public float Magnitude = 0.0f;

	public AimDownSightAction(@Nonnull ActionGroup group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}
	@Override
	public void OnTriggerClient(ActionGroupContext context, int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(ActionGroupContext context, int triggerIndex)
	{

	}

	@Override
	public void OnTickClient(ActionGroupContext context)
	{
		Magnitude = Maths.Lerp(Magnitude, 1.0f, 1f/20f);
	}

	public float FOVFactor(ActionGroupContext context) { return context.ModifyFloat(ModifierDefinition.STAT_ZOOM_FOV_FACTOR, Def.fovFactor); }

}
