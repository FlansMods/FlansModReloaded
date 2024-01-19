package com.flansmod.client.render.decals;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.actions.nodes.LaserAction;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class LaserRenderer
{
	public LaserRenderer()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void RenderTick(RenderLevelStageEvent event)
	{
		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
		{
			if (Minecraft.getInstance().player != null)
			{
				ShooterContext playerContext = ShooterContext.of(Minecraft.getInstance().player);
				if (playerContext.IsValid())
				{
					for (GunContext gunContext : playerContext.GetAllGunContexts(true))
					{
						if (gunContext.IsValid())
						{
							for (ActionGroupInstance actionGroup : gunContext.GetActionStack().GetActiveActionGroups())
							{
								for (ActionInstance actionInstance : actionGroup.GetActions())
								{
									if (actionInstance instanceof LaserAction laserAction)
									{
										if (actionGroup.Context.IsAttachment())
										{
											RenderLaserFirstPerson(event.getPoseStack(),
												event.getCamera(),
												gunContext,
												actionGroup.Context.GetAttachmentType(),
												actionGroup.Context.GetAttachmentIndex(),
												laserAction.LaserOrigin(),
												new Vector4f(laserAction.Red(), laserAction.Green(), laserAction.Blue(), 1f));
										} else
										{
											RenderLaserFirstPerson(event.getPoseStack(),
												event.getCamera(),
												gunContext,
												EAttachmentType.Generic,
												-1,
												laserAction.LaserOrigin(),
												new Vector4f(laserAction.Red(), laserAction.Green(), laserAction.Blue(), 1f));
										}
									}
								}
							}
						}
					}
				}
			}
		}

	}
	@SubscribeEvent
	public void OnRenderFirstPersonHands(RenderHandEvent event)
	{

	}

	private static final ResourceLocation LaserTexture = new ResourceLocation(FlansMod.MODID, "textures/effects/laser_point.png");
	public void RenderLaserFirstPerson(@Nonnull PoseStack poseStack, @Nonnull Camera camera, @Nonnull GunContext gunContext, @Nonnull EAttachmentType attachType, int attachIndex, @Nonnull String apName, @Nonnull Vector4f colour)
	{
		ItemDisplayContext transformType = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
		if(gunContext instanceof GunContextPlayer playerGunContext)
			transformType = MinecraftHelpers.GetFirstPersonTransformType(playerGunContext.GetHand());

		Transform origin = FirstPersonManager.GetWorldSpaceAPTransform(
			gunContext,
			transformType,
			ActionGroupContext.CreateGroupPath(attachType, attachIndex, apName));

		Level level = Minecraft.getInstance().level;
		if(level != null)
		{
			Raytracer raytracer = Raytracer.ForLevel(level);
			if(raytracer != null)
			{
				List<HitResult> hits = new ArrayList<>();
				Vec3 ray = origin.ForwardVec3().scale(100f);
				raytracer.CastBullet(Minecraft.getInstance().player,
					origin.PositionVec3(),
					ray,
					0.0f,
					0.0f,
					hits);
				if(hits.size() > 0)
				{
					Vec3 normal = origin.ForwardVec3().scale(-1f);
					if(hits.get(0) instanceof BlockHitResult blockHit)
						normal = new Vec3(blockHit.getDirection().getNormal().getX(),
							blockHit.getDirection().getNormal().getY(),
							blockHit.getDirection().getNormal().getZ());

					RenderLaser(poseStack,
						camera,
						origin.PositionVec3(),
						origin.ForwardVec3(),
						hits.get(0).getLocation(),
						normal,
						colour);

					FlansModClient.DECAL_RENDERER.AddOrUpdateDecal(
						LaserTexture,
						gunContext.GetUUID(),
						hits.get(0).getLocation(),
						normal,
						colour,
						0.0f,
						2);
				}
				else
				{
					RenderLaser(poseStack,
						camera,
						origin.PositionVec3(),
						origin.ForwardVec3(),
						origin.PositionVec3().add(ray),
						origin.ForwardVec3(),
						colour);
				}
			}
		}
	}

	public void RenderLaser(@Nonnull PoseStack poseStack, @Nonnull Camera camera, @Nonnull Vec3 startPos, @Nonnull Vec3 startNormal, Vec3 endPos, Vec3 endNormal, @Nonnull Vector4f colour)
	{
		Tesselator tesselator = Tesselator.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		Vec3 cameraPos = camera.getPosition();
		Vec3 centerPos = startPos.add(endPos).scale(0.5d);
		double length = endPos.distanceTo(startPos);
		Vec3 laserDirection = endPos.subtract(startPos).normalize();
		Vec3 cameraToTrailDirection = centerPos.subtract(cameraPos).normalize();
		Vec3 trailYAxis = laserDirection.cross(cameraToTrailDirection).normalize();
		Vec3 trailXAxis = laserDirection.scale(length * 0.5f);
		trailYAxis = trailYAxis.scale(0.01f);

		poseStack.pushPose();
		poseStack.translate(centerPos.x - cameraPos.x, centerPos.y - cameraPos.y, centerPos.z - cameraPos.z);

		BufferBuilder buf = tesselator.getBuilder();
		buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		Vec3 v0 = trailYAxis.add(trailXAxis);
		Vec3 v1 = trailYAxis.subtract(trailXAxis);
		Vec3 v2 = trailYAxis.scale(-1d).subtract(trailXAxis);
		Vec3 v3 = trailYAxis.scale(-1d).add(trailXAxis);

		buf.vertex(poseStack.last().pose(), (float)v0.x, (float)v0.y, (float)v0.z)
			.color(colour.x, colour.y, colour.z, colour.w)
			.endVertex();
		buf.vertex(poseStack.last().pose(), (float)v1.x, (float)v1.y, (float)v1.z)
			.color(colour.x, colour.y, colour.z, colour.w)
			.endVertex();
		buf.vertex(poseStack.last().pose(), (float)v2.x, (float)v2.y, (float)v2.z)
			.color(colour.x, colour.y, colour.z, colour.w)
			.endVertex();
		buf.vertex(poseStack.last().pose(), (float)v3.x, (float)v3.y, (float)v3.z)
			.color(colour.x, colour.y, colour.z, colour.w)
			.endVertex();

		tesselator.end();
		RenderSystem.disableBlend();
		poseStack.popPose();
	}
}
