package com.flansmod.client.render.vehicles;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.common.item.VehicleItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VehicleItemRenderer extends FlanItemModelRenderer
{
	public VehicleItemRenderer(@Nullable VehicleItem vehicleItem)
	{
		super(vehicleItem, false);
	}

	@Override
	protected void DoRender(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
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

