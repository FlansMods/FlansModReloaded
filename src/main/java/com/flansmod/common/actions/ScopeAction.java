package com.flansmod.common.actions;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.gunshots.GunshotManager;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class ScopeAction extends AimDownSightAction
{
	public ScopeAction(ActionStack stack, ActionDefinition def, InteractionHand hand)
	{
		super(stack, def, hand);
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
