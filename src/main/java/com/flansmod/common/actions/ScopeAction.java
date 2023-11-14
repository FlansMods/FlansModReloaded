package com.flansmod.common.actions;

import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ScopeAction extends AimDownSightAction
{
	public ScopeAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	public boolean ApplyOverlay()
	{
		return true;
	}

	public ResourceLocation GetOverlayLocation()
	{
		return new ResourceLocation("flansmod", "textures/scopes/" + ZoomOverlay() + ".png");
	}
	public String ZoomOverlay() { return Group.Context.ModifyString(ModifierDefinition.STAT_ZOOM_SCOPE_OVERLAY, Def.scopeOverlay); }

	@Override
	public boolean ShouldRender(GunContext context)
	{
		return false;
	}
}
