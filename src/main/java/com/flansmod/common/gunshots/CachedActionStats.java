package com.flansmod.common.gunshots;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.ShotDefinition;
import com.flansmod.common.types.guns.ERepeatMode;
import com.flansmod.util.Maths;

import java.util.List;

public class CachedActionStats
{
	public final ERepeatMode RepeatMode;
	public final int RepeatCount;
	public final float RepeatDelay;
	public final float SpinUpDuration;
	public final float Duration;
	public final float FOVFactor;
	public final float ToolLevel;
	public final float HarvestSpeed;
	public final float Reach;

	public int RoundsPerMinute()
	{
		return RepeatDelay <= 0.00001f ? 0 : Maths.Ceil(60.0f / RepeatDelay);
	}

	public CachedActionStats(ActionDefinition actionDef, List<ModifierDefinition> modifiers)
	{
		RepeatMode = ModifyMode(actionDef.repeatMode, "repeatMode", modifiers);
		RepeatDelay = ModifyFloat(actionDef.repeatDelay, "repeatDelay", modifiers);
		RepeatCount = Maths.Ceil(ModifyFloat(actionDef.repeatCount, "repeatCount", modifiers));
		SpinUpDuration = ModifyFloat(actionDef.spinUpDuration, "spinUpDuration", modifiers);
		Duration = ModifyFloat(actionDef.duration, "duration", modifiers);
		FOVFactor = ModifyFloat(actionDef.fovFactor, "fovFactor", modifiers);
		ToolLevel = ModifyFloat(actionDef.toolLevel, "toolLevel", modifiers);
		HarvestSpeed = ModifyFloat(actionDef.harvestSpeed, "harvestSpeed", modifiers);
		Reach = ModifyFloat(actionDef.reach, "reach", modifiers);
	}

	private ERepeatMode ModifyMode(ERepeatMode baseMode, String parameterName, List<ModifierDefinition> modifiers)
	{
		for(ModifierDefinition mod : modifiers)
		{
			if(mod.Stat.equals(parameterName))
			{
				if(!mod.SetValue.isEmpty())
				{
					try { return ERepeatMode.valueOf(mod.SetValue); }
					catch(IllegalArgumentException ignored) { }
				}
			}
		}
		return baseMode;
	}

	private float ModifyFloat(float base, String parameterName, List<ModifierDefinition> modifiers)
	{
		float additives = 0.0f;
		float multiplicatives = 1.0f;

		for(ModifierDefinition mod : modifiers)
		{
			additives += mod.Add;
			multiplicatives *= mod.Multiply;
		}

		return (base + additives) * multiplicatives;
	}
}
