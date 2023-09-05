package com.flansmod.client.render.bullets;

import com.flansmod.common.FlansMod;
import com.flansmod.common.projectiles.BulletEntity;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class BulletEntityRenderer extends EntityRenderer<BulletEntity>
{
	private final HashMap<ResourceLocation, ModelPart> LoadedModels = new HashMap<>();
	private final EntityModelSet EntityModels;

	public BulletEntityRenderer(EntityRendererProvider.Context context)
	{
		super(context);
		EntityModels = context.getModelSet();
	}

	@Override
	@Nonnull
	public ResourceLocation getTextureLocation(@Nonnull BulletEntity bullet)
	{
		return bullet.Def.Location;
	}

	@Override
	public void render(@Nonnull BulletEntity bullet, float yaw, float partialTick, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffers, int light)
	{
		// Note: Don't render super, this will never have a nametag
		if(bullet.Def.IsValid())
		{
			ModelPart model = LoadedModels.get(bullet.Def.Location);
			if (model == null)
			{
				try
				{
					model = EntityModels.bakeLayer(new ModelLayerLocation(bullet.Def.Location.withPrefix("entity/"), "main"));
					LoadedModels.put(bullet.Def.Location, model);
				}
				catch(Exception e)
				{
					FlansMod.LOGGER.warn("Could not load bullet model " + bullet.Def.Location);
					return;
				}
			}

			pose.pushPose();
			VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(bullet.Def.Location));
			model.render(pose, vc, light, OverlayTexture.NO_OVERLAY);
			pose.popPose();
		}
	}
}
