package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.gunshots.GunContext;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
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

	public ResourceLocation GetOverlayLocation(ActionGroupContext context)
	{
		return new ResourceLocation("flansmod", "textures/scopes/" + ZoomOverlay(context) + ".png");
	}
	public String ZoomOverlay(ActionGroupContext context) { return context.ModifyString(ModifierDefinition.STAT_ZOOM_SCOPE_OVERLAY, Def.scopeOverlay); }

	@Override
	public boolean ShouldRender(GunContext context)
	{
		return false;
	}
}
