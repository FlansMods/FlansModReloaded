package com.flansmod.common.actions.nodes;

import com.flansmod.common.FlansModConfig;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.EActionResult;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.physics.common.util.Maths;

import javax.annotation.Nonnull;

public class LaserAction extends ActionInstance
{
	protected boolean ShouldStop = false;

	public LaserAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public EActionResult CanStart()
	{
		// Server config hook
		if(!FlansModConfig.AllowLaserActions.get())
			return EActionResult.TryNextAction;
		// ------------------

		return EActionResult.CanProcess;
	}

	@Override
	public void OnTriggerClient(int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{

	}

	public float Red() { return Maths.Clamp(GetFloat(Constants.STAT_LASER_RED), 0f, 1f); }
	public float Green() { return Maths.Clamp(GetFloat(Constants.STAT_LASER_GREEN), 0f, 1f); }
	public float Blue() { return Maths.Clamp(GetFloat(Constants.STAT_LASER_BLUE), 0f, 1f); }
	public String LaserOrigin() { return ModifyString(Constants.STAT_LASER_ORIGIN, "laser_origin"); }
	public boolean FixedLaserDirection() { return Group.Context.GetBoolean(Constants.MODAL_FIXED_LASER_DIRECTION); }
}
