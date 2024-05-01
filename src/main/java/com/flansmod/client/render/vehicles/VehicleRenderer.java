package com.flansmod.client.render.vehicles;

import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.models.ETurboRenderMaterial;
import com.flansmod.client.render.models.TurboRenderUtility;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleHierarchyModule;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class VehicleRenderer extends EntityRenderer<VehicleEntity>
{
	@Nonnull
	public TurboRenderUtility TurboRenderer = TurboRenderUtility.of();

	public VehicleRenderer(@Nonnull EntityRendererProvider.Context context)
	{
		super(context);
	}


	// -------------------------------------------------------------------------------------------
	// Events
	public void OnUnbakedLoaded(@Nonnull UnbakedModel unbaked)
	{
		if(unbaked instanceof BlockModel blockModel)
			if(blockModel.customData.hasCustomGeometry())
				if(blockModel.customData.getCustomGeometry() instanceof TurboRig unbakedRig)
				{
					TurboRenderer = TurboRenderer.with(unbakedRig);
				}
	}
	public void OnBakedLoaded(@Nonnull BakedModel baked)
	{
		if (baked instanceof TurboRig.Baked turboBakedModel)
			TurboRenderer = TurboRenderer.with(turboBakedModel);
	}
	// -------------------------------------------------------------------------------------------

	@Override
	@Nonnull
	public ResourceLocation getTextureLocation(@Nonnull VehicleEntity vehicle)
	{
		return TextureManager.INTENTIONAL_MISSING_TEXTURE;
	}

	public void render(@Nonnull VehicleEntity vehicle,
					   float yaw,
					   float dt,
					   @Nonnull PoseStack poseStack,
					   @Nonnull MultiBufferSource buffers,
					   int light)
	{
		RenderContext renderContext = new RenderContext(buffers, ItemDisplayContext.FIXED, poseStack, light, 0);
		TurboRenderer.RenderPartIteratively(renderContext,
			"body",
			(partName) -> getTextureLocation(vehicle),
			(partName, preRenderContext) -> {
				return true;
			},
			(partName, postRenderContext) -> {

			});
	}
}
