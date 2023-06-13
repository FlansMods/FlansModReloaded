package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunContext;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class AimDownSightAction extends Action
{
	protected boolean shouldStop = false;

	public AimDownSightAction(ActionStack stack, ActionDefinition def, InteractionHand hand)
	{
		super(stack, def, hand);
	}

	@Override
	public boolean CanStart(GunContext context)
	{
		// TODO: 2H check
		return true;
	}

	@Override
	public void OnStartClient(GunContext context)
	{
		super.OnStartClient(context);

		int numScopeActions = 0;
		for(Action action : stack.GetActions())
		{
			if(action instanceof ScopeAction)
				numScopeActions++;
		}
		if(numScopeActions > 1)
		{
			for(Action action : stack.GetActions())
			{
				if(action instanceof ScopeAction scopeAction)
					scopeAction.shouldStop = true;
			}
		}
	}

	@Override
	public boolean Finished()
	{
		return shouldStop;
	}
}
