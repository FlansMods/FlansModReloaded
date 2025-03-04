package com.flansmod.client.render.bullets;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CasingRenderer extends FlanItemModelRenderer
{
	public CasingRenderer(ResourceLocation loc)
	{
		super(null, true, loc);
	}

	@Override
	protected void doRender(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
	{
		ResourceLocation skin = getSkin(stack);
		//renderContext.Buffers.getBuffer(RenderType.entityCutout(skin)).color(1,1,1,1);
		//renderContext.Buffers.getBuffer(RenderType.cutout()).color(1,1,1,1);
		renderContext.Transforms.push();
		renderSectionIteratively(renderContext,
				"body",
				(partName) -> skin, 						// Texture func
				(partName, innerRenderContext) -> true, 	// Pre-func
				(partName, innerRenderContext) -> { } 		// Post-func
		);
		renderContext.Transforms.pop();
	}
}
