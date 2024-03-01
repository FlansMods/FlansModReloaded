package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.ActionDefinition;

import javax.annotation.Nonnull;

public class LaserAction extends ActionInstance
{
	protected boolean ShouldStop = false;

	public LaserAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
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

	public float Red() { return ModifyFloat(Constants.STAT_LASER_RED, 1.0f); }
	public float Green() { return ModifyFloat(Constants.STAT_LASER_GREEN, 1.0f); }
	public float Blue() { return ModifyFloat(Constants.STAT_LASER_BLUE, 1.0f); }
	public String LaserOrigin() { return ModifyString(Constants.STAT_LASER_ORIGIN, "laser_origin"); }
	public boolean FixedLaserDirection() { return Group.Context.GetBoolean(Constants.MODAL_FIXED_LASER_DIRECTION); }
}
