package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;

import javax.annotation.Nonnull;

public class AimDownSightAction extends ActionInstance
{
	protected boolean ShouldStop = false;

	public float Magnitude = 0.0f;

	public AimDownSightAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}
	@Override
	public void OnTriggerClient(int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{

	}

	@Override
	public void OnTickClient()
	{
		Magnitude = Maths.Lerp(Magnitude, 1.0f, 1f/20f);
	}
	public float FOVFactor() { return Group.Context.ModifyFloat(ModifierDefinition.STAT_ZOOM_FOV_FACTOR, Def.fovFactor); }
}