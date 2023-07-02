package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.gunshots.GunContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class ScopeAction extends AimDownSightAction
{
	public ScopeAction(ActionDefinition def, EActionInput inputType)
	{
		super(def, inputType);
	}

	public boolean ApplyOverlay()
	{
		return true;
	}

	public ResourceLocation GetOverlayLocation()
	{
		return new ResourceLocation("flansmod", "textures/scopes/" + actionDef.scopeOverlay + ".png");
	}

	@Override
	public boolean ShouldRender(GunContext context)
	{
		return false;
	}
}
