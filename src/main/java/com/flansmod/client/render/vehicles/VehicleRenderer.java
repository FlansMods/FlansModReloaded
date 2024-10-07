package com.flansmod.client.render.vehicles;

import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.models.*;
import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.physics.common.util.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VehicleRenderer extends EntityRenderer<VehicleEntity> implements ITurboRenderer
{
	@Nonnull
	private final LazyDefinition<VehicleDefinition> Def;
	@Nullable
	private TurboRenderUtility TurboRenderHelper;
	@Nonnull
	public TurboRenderUtility GetTurboRigWrapper()
	{
		if(TurboRenderHelper == null)
		{
			TurboRenderHelper = FlansModelRegistry.GetRigWrapperFor(Def.Loc());
		}
		return TurboRenderHelper;
	}

	public VehicleRenderer(@Nonnull ResourceLocation defLoc,
						   @Nonnull EntityRendererProvider.Context context)
	{
		super(context);
		Def = LazyDefinition.of(defLoc, FlansMod.VEHICLES);
	}

	@Override
	@Nonnull
	public ResourceLocation getTextureLocation(@Nonnull VehicleEntity vehicle)
	{
		return TextureManager.INTENTIONAL_MISSING_TEXTURE;
	}

	private void DoRender(@Nullable VehicleEntity vehicle, @Nonnull RenderContext renderContext, float deltaTick)
	{
		// Minecraft rendering starts upside down for legacy reasons
		renderContext.Transforms.add(Transform.FromEuler(0f, 0f, 180f));
		ResourceLocation skin = vehicle != null ? getTextureLocation(vehicle) : TextureManager.INTENTIONAL_MISSING_TEXTURE;
		if(vehicle != null)
		{
			renderContext.Transforms.add(Transform.ExtractOrientation(vehicle.RootTransform(deltaTick), false, () -> "RootOri"));
		}
		GetTurboRigWrapper().RenderPartIteratively(renderContext,
			"body",
			(partName) -> skin,
			(partName, preRenderContext) -> {
				return true;
			},
			(partName, postRenderContext) -> {

			});
	}

	public void RenderDirect(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
	{
		DoRender(heldByEntity instanceof VehicleEntity veh ? veh : null, renderContext, 0);
	}

	// ItemRenderer
	public void render(@Nonnull VehicleEntity vehicle,
					   float yaw,
					   float dt,
					   @Nonnull PoseStack poseStack,
					   @Nonnull MultiBufferSource buffers,
					   int light)
	{
		RenderContext renderContext = new RenderContext(buffers, ItemDisplayContext.FIXED, poseStack, light, 0);
		DoRender(vehicle, renderContext, dt);
	}
}
