package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import net.minecraft.world.InteractionHand;

public class Actions
{
	public static Action CreateAction(ActionGroupDefinition groupDef, ActionDefinition def, EActionInput inputType)
	{
		switch(def.actionType)
		{
			case Animation			-> { return new AnimationAction(groupDef, def, inputType); }
			case Shoot 				-> { return new ShootAction(groupDef, def, inputType); }
			case Scope				-> { return new ScopeAction(groupDef, def, inputType); }
			case AimDownSights 		-> { return new AimDownSightAction(groupDef, def, inputType); }
			case PlaySound  		-> { return new PlaySoundAction(groupDef, def, inputType); }
			default					-> { return null; }
		}
	}
}
