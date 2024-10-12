package com.flansmod.client.render.bullets;

import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.models.ITurboRenderer;
import com.flansmod.common.actions.contexts.GunContextPlayer;
import com.flansmod.common.actions.contexts.GunshotContext;
import com.flansmod.common.item.FlanItem;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ShotRenderer
{
	private ArrayList<ShotRenderInstance> shots = new ArrayList<>(128);
	private static TextureManager textureManager() { return Minecraft.getInstance().textureManager; }

	public ShotRenderer()
	{
		MinecraftForge.EVENT_BUS.addListener(this::RenderTick);
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
	}

	public int AddLocalPlayerTrail(Vec3 origin, Vec3 endpoint, GunshotContext gunshotContext)
	{
		if(Minecraft.getInstance().player != null && gunshotContext.ActionGroup.Gun instanceof GunContextPlayer playerGunContext)
		{
			Transform shootOrigin = FirstPersonManager.GetWorldSpaceAPTransform(gunshotContext.ActionGroup.Gun, MinecraftHelpers.GetFirstPersonTransformType(playerGunContext.GetHand()), "shoot_origin");
			ShotRenderInstance shot = new ShotRenderInstance(shootOrigin.positionVec3(), endpoint, 0.05f, 10.0f, 10.0f, null, GetBulletRenderer(gunshotContext));
			shots.add(shot);
			return shot.GetLifetime();
		}

		ShotRenderInstance shot = new ShotRenderInstance(origin, endpoint, 0.05f, 10.0f, 10.0f, null, GetBulletRenderer(gunshotContext));
		shots.add(shot);
		return shot.GetLifetime();
	}

	@Nullable
	private BulletItemRenderer GetBulletRenderer(GunshotContext gunshotContext)
	{
		Item item = ForgeRegistries.ITEMS.getValue(gunshotContext.Bullet.Location);
		if(item instanceof FlanItem flanItem)
		{
			ITurboRenderer itemRenderer = FlansModelRegistry.GetItemRenderer(flanItem);
			if(itemRenderer instanceof BulletItemRenderer bulletRenderer)
			{
				return bulletRenderer;
			}
		}
		return null;
	}

	public int AddTrail(Vec3 origin, Vec3 endpoint)
	{
		ShotRenderInstance shot = new ShotRenderInstance(origin, endpoint, 0.05f, 10.0f, 10.0f, null, null);
		shots.add(shot);
		return shot.GetLifetime();
	}

	public int AddTrail(Vec3 origin, Vec3 endpoint, ResourceLocation texture)
	{
		ShotRenderInstance shot = new ShotRenderInstance(origin, endpoint, 0.05f, 10.0f, 10.0f, texture, null);
		shots.add(shot);
		return shot.GetLifetime();
	}

	public void ClientTick(TickEvent.ClientTickEvent event)
	{
		for(int i = shots.size() - 1; i >= 0; i--)
		{
			shots.get(i).Update();
			if(shots.get(i).Finished())
				//shots.get(i).ticksExisted = 0;
				shots.remove(i);
		}
	}

	public void RenderTick(RenderLevelStageEvent event)
	{
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
		{
			PoseStack poseStack = event.getPoseStack();
			Camera camera = event.getCamera();
			Vec3 pos = camera.getPosition();
			Tesselator tesselator = Tesselator.getInstance();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			//RenderSystem.disableDepthTest();

			for(ShotRenderInstance shot : shots)
			{
				shot.Render(tesselator, poseStack, pos, event.getPartialTick());
			}

			//for(ShotRenderInstance shot : shots)
			//{
			//	shot.RenderModel(Minecraft.getInstance().renderBuffers().bufferSource(), poseStack, pos, event.getPartialTick());
			//}
		}
	}

	private static class ShotRenderInstance
	{
		private final Vec3 origin;
		private final Vec3 endPoint;
		private final float width;
		private final float length;
		@Nullable
		private final ResourceLocation trailTexture;
		@Nullable
		private final BulletItemRenderer bulletModel;

		private final double bulletSpeed;
		private final double distanceToTarget;

		private int ticksExisted = 0;
		private final int lifetime;
		public int GetLifetime() { return lifetime; }

		public ShotRenderInstance(Vec3 start, Vec3 end, float w, float l, float speed, @Nullable ResourceLocation trail, @Nullable BulletItemRenderer model)
		{
			origin = start;
			endPoint = end;
			width = w;
			length = l;
			bulletSpeed = speed;// * 0.001d; // <- Do this if you want to test trails
			trailTexture = trail;
			distanceToTarget = start.distanceTo(end);
			lifetime = bulletSpeed <= 0.0001d ? 1 : Maths.Floor((distanceToTarget - length) / bulletSpeed);
			bulletModel = model;
		}

		public boolean Finished()
		{
			return ticksExisted >= lifetime;
		}

		public void Update()
		{
			ticksExisted++;
		}

		private static final Vector4f WHITE_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
		private static final Vector4f DEFAULT_TRAIL_COLOUR = new Vector4f(1.0f, 1.0f, 0.5f, 1.0f);

		public void Render(Tesselator tesselator, PoseStack poseStack, Vec3 cameraPos, float dt)
		{
			Vector4f colour = WHITE_COLOUR;
			if(trailTexture != null)
			{
				textureManager().bindForSetup(trailTexture);
			}
			else
			{
				colour = DEFAULT_TRAIL_COLOUR;
			}

			Vec3 startPos = origin;

			double centerT = length * 0.5f + (ticksExisted + dt) * bulletSpeed;
			Vec3 bulletDirection = endPoint.subtract(startPos).normalize();
			Vec3 centerPos = startPos.add(bulletDirection.scale(centerT));
			Vec3 cameraToTrailDirection = centerPos.subtract(cameraPos).normalize();

			Vec3 trailYAxis = bulletDirection.cross(cameraToTrailDirection).normalize();
			Vec3 trailXAxis = bulletDirection.scale(length * 0.5f);
			Vec3 trailNormal = trailXAxis.cross(trailYAxis).normalize();


			trailYAxis = trailYAxis.scale(width * 0.5f);

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
				//.normal((float)trailNormal.x, (float)trailNormal.y, (float)trailNormal.z)
				.endVertex();
			buf.vertex(poseStack.last().pose(), (float)v1.x, (float)v1.y, (float)v1.z)
				.color(colour.x, colour.y, colour.z, colour.w)
				//.normal((float)trailNormal.x, (float)trailNormal.y, (float)trailNormal.z)
				.endVertex();
			buf.vertex(poseStack.last().pose(), (float)v2.x, (float)v2.y, (float)v2.z)
				.color(colour.x, colour.y, colour.z, colour.w)
				//.normal((float)trailNormal.x, (float)trailNormal.y, (float)trailNormal.z)
				.endVertex();
			buf.vertex(poseStack.last().pose(), (float)v3.x, (float)v3.y, (float)v3.z)
				.color(colour.x, colour.y, colour.z, colour.w)
				//.normal((float)trailNormal.x, (float)trailNormal.y, (float)trailNormal.z)
				.endVertex();

			tesselator.end();

			poseStack.popPose();

		}

		public void RenderModel(MultiBufferSource buffers, PoseStack pose, Vec3 cameraPos, float dt)
		{
			if(bulletModel != null)
			{
				Vec3 startPos = origin;
				//double centerT = length * 0.5f + (ticksExisted + dt) * bulletSpeed;
				double frontT = length + (ticksExisted + dt) * bulletSpeed;
				Vec3 bulletDirection = endPoint.subtract(startPos).normalize();
				Vec3 centerPos = startPos.add(bulletDirection.scale(frontT));

				pose.pushPose();
				pose.translate(centerPos.x - cameraPos.x, centerPos.y - cameraPos.y, centerPos.z - cameraPos.z);
				//pose.translate(-0.5f, -0.5f, -0.5f);
				pose.mulPose(new Quaternionf().lookAlong((float)bulletDirection.z, (float)bulletDirection.y, (float)bulletDirection.x, 0f, 1f, 0f));
				bulletModel.RenderDirect(
					null,
					null,
					new RenderContext(buffers,
						ItemDisplayContext.FIXED,
						pose,
						0xffffff,
						0));
				pose.popPose();
			}
		}
	}


}
