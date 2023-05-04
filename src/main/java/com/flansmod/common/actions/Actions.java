package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;

public class Actions
{
	public static Action CreateAction(ActionDefinition def)
	{
		switch(def.ActionType)
		{
			case Animation	-> { return new AnimationAction(def); }
			default			-> { return null; }
		}
	}
}
