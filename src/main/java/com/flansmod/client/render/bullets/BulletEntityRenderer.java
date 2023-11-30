package com.flansmod.client.render.bullets;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.guns.AttachmentItemRenderer;
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
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class BulletEntityRenderer extends EntityRenderer<BulletEntity>
{
	private final HashMap<ResourceLocation, ModelPart> LoadedModels = new HashMap<>();

	public BulletEntityRenderer(EntityRendererProvider.Context context)
	{
		super(context);
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
		Item item = ForgeRegistries.ITEMS.getValue(bullet.Def.Location);
		if(item != null)
		{
			FlanItemModelRenderer bulletRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(new ItemStack(item));
			if(bulletRenderer != null)
			{
				pose.pushPose();
				pose.translate(-0.5f, -0.5f, -0.5f);
				bulletRenderer.RenderDirect(
					bullet,
					new ItemStack(item),
					new RenderContext(
						buffers,
						ItemTransforms.TransformType.FIXED,
						pose,
						light,
						0));
				pose.popPose();
			}
			else FlansMod.LOGGER.warn("Could not find bullet renderer for " + item);
		}
		else FlansMod.LOGGER.warn("Could not find item for bullet def " + bullet.Context.Bullet.Location);
	}
}
