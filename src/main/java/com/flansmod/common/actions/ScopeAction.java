package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.gunshots.GunContext;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ScopeAction extends AimDownSightAction
{
	public ScopeAction(@Nonnull ActionGroup group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	public boolean ApplyOverlay()
	{
		return true;
	}

	public ResourceLocation GetOverlayLocation()
	{
		return new ResourceLocation("flansmod", "textures/scopes/" + Def.scopeOverlay + ".png");
	}

	@Override
	public boolean ShouldRender(GunContext context)
	{
		return false;
	}
}
