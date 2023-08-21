package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.gunshots.GunContext;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import net.minecraft.resources.ResourceLocation;

public class ScopeAction extends AimDownSightAction
{
	public ScopeAction(ActionGroupDefinition groupDef, ActionDefinition def, EActionInput inputType)
	{
		super(groupDef, def, inputType);
	}

	public boolean ApplyOverlay()
	{
		return true;
	}

	public ResourceLocation GetOverlayLocation()
	{
		return new ResourceLocation("flansmod", "textures/scopes/" + ActionDef.scopeOverlay + ".png");
	}

	@Override
	public boolean ShouldRender(GunContext context)
	{
		return false;
	}
}
