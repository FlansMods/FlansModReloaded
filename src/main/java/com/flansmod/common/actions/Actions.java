package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import net.minecraft.world.InteractionHand;

public class Actions
{
	public static Action CreateAction(ActionDefinition def, EActionInput inputType)
	{
		switch(def.actionType)
		{
			case Animation			-> { return new AnimationAction(def, inputType); }
			case Shoot 				-> { return new ShootAction(def, inputType); }
			case Scope				-> { return new ScopeAction(def, inputType); }
			case AimDownSights 		-> { return new AimDownSightAction(def, inputType); }
			case PlaySound  		-> { return new PlaySoundAction(def, inputType); }
			default					-> { return null; }
		}
	}
}
