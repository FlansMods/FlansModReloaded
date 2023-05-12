package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import net.minecraft.world.InteractionHand;

public class Actions
{
	public static Action CreateAction(ActionDefinition def, InteractionHand hand)
	{
		switch(def.ActionType)
		{
			case Animation	-> { return new AnimationAction(def, hand); }
			case Shoot 		-> { return new ShootAction(def, hand); }
			default			-> { return null; }
		}
	}
}
