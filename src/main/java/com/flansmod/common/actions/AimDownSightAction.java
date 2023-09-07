package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;

public class AimDownSightAction extends Action
{
	protected boolean shouldStop = false;

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

	public float FOVFactor(ActionGroupContext context) { return context.ModifyFloat(ModifierDefinition.STAT_ZOOM_FOV_FACTOR, Def.fovFactor); }
	public String ZoomOverlay(ActionGroupContext context) { return context.ModifyString(ModifierDefinition.STAT_ZOOM_SCOPE_OVERLAY, Def.scopeOverlay); }

}
