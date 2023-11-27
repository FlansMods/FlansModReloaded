package com.flansmod.client.render.bullets;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class BulletItemRenderer extends FlanItemModelRenderer
{
	public BulletItemRenderer()
	{
		super(false);
	}

	@Override
	protected void DoRender(Entity heldByEntity, ItemStack stack, RenderContext renderContext)
	{
		// Find our skin
		ResourceLocation skin = GetSkin(stack);

		RenderPartIteratively(
			renderContext,
			"body",
			(partName) -> { return skin; }, // Texture-Func
			(partName, innerRenderContext) -> { return true; }, // Pre-Func
			(partName, innerRenderContext) -> {} // Post-Func
		);
	}
}