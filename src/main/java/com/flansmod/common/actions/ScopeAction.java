package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class ScopeAction extends Action
{
	public ScopeAction(ActionDefinition def, InteractionHand hand)
	{
		super(def, hand);
	}

	public boolean CanStart(GunContext context)
	{
		// TODO: 2H check
		return true;
	}

	@Override
	public boolean Finished()
	{
		return false;
	}

	public boolean ApplyOverlay()
	{
		return true;
	}

	public ResourceLocation GetOverlayLocation()
	{
		return new ResourceLocation("flansmod", "scopes/" + actionDef.scopeOverlay);
	}
}
