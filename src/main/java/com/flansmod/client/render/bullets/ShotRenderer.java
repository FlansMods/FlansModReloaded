package com.flansmod.client.render.bullets;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.common.gunshots.GunshotContext;
import com.flansmod.common.gunshots.ShooterContext;
import com.flansmod.util.Maths;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

public class ShotRenderer
{
	private ArrayList<ShotRenderInstance> shots = new ArrayList<>(128);
	private static TextureManager textureManager;

	public ShotRenderer()
	{
		textureManager = Minecraft.getInstance().textureManager;

		MinecraftForge.EVENT_BUS.addListener(this::RenderTick);
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
	}

	public int AddLocalPlayerTrail(Vec3 origin, Vec3 endpoint, GunshotContext context)
	{
		if(Minecraft.getInstance().player != null)
		{
			ItemStack gunStack = context.ActionGroup.Gun.GetItemStack();
			FlanItemModelRenderer gunRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(gunStack);
			if(gunRenderer != null)
			{
				// TODO: Offset if barrel attachment used

				Vector3f barrelAP = gunRenderer.GetAttachPoint("barrel");
				Vec3 firstPersonRelative = new Vec3(2f - barrelAP.x, barrelAP.y - 6f, 13f + barrelAP.z);
				firstPersonRelative = firstPersonRelative.scale(1f/16f);

				Vec3 playerPos = Minecraft.getInstance().player.getEyePosition();
				float playerYaw = Minecraft.getInstance().player.getYHeadRot();
				float playerPitch = Minecraft.getInstance().player.getXRot();

				Vec3 globalOrigin = firstPersonRelative.xRot(-playerPitch * Maths.DegToRadF);
				globalOrigin = globalOrigin.yRot(-playerYaw * Maths.DegToRadF);
				globalOrigin = globalOrigin.add(playerPos);

				ShotRenderInstance shot = new ShotRenderInstance(globalOrigin, endpoint, 0.05f, 10.0f, 10.0f, null, false);
				shots.add(shot);
				return shot.GetLifetime();
			}
		}

		ShotRenderInstance shot = new ShotRenderInstance(origin, endpoint, 0.05f, 10.0f, 10.0f, null, false);
		shots.add(shot);
		return shot.GetLifetime();
	}

	public int AddTrail(Vec3 origin, Vec3 endpoint)
	{
		ShotRenderInstance shot = new ShotRenderInstance(origin, endpoint, 0.05f, 10.0f, 10.0f, null, false);
		shots.add(shot);
		return shot.GetLifetime();
	}

	public int AddTrail(Vec3 origin, Vec3 endpoint, ResourceLocation texture)
	{
		ShotRenderInstance shot = new ShotRenderInstance(origin, endpoint, 0.05f, 10.0f, 10.0f, texture, false);
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
		}
	}

	private static class ShotRenderInstance
	{
		// If true, the origin provided will be in local camera space
		private final boolean isFromLocalPlayer;
		private final Vec3 origin;
		private final Vec3 endPoint;
		private final float width;
		private final float length;
		private final ResourceLocation trailTexture;

		private final double bulletSpeed;
		private final double distanceToTarget;

		private int ticksExisted = 0;
		private final int lifetime;
		public int GetLifetime() { return lifetime; }

		public ShotRenderInstance(Vec3 start, Vec3 end, float w, float l, float speed, ResourceLocation trail, boolean local)
		{
			origin = start;
			endPoint = end;
			width = w;
			length = l;
			bulletSpeed = speed;// * 0.001d; <- Do this if you want to test trails
			trailTexture = trail;
			distanceToTarget = start.distanceTo(end);
			lifetime = bulletSpeed <= 0.0001d ? 1 : Maths.Floor((distanceToTarget - length) / bulletSpeed);
			isFromLocalPlayer = local;
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

		private Vec3 GetStartPoint()
		{
			if(isFromLocalPlayer && Minecraft.getInstance().player != null)
			{
				Vec3 playerPos = Minecraft.getInstance().player.getEyePosition();
				float playerYaw = Minecraft.getInstance().player.getYHeadRot();
				float playerPitch = Minecraft.getInstance().player.getXRot();

				Vec3 globalOrigin = origin.xRot(-playerPitch * Maths.DegToRadF);
				globalOrigin = globalOrigin.yRot(-playerYaw * Maths.DegToRadF);
				return playerPos.add(globalOrigin);
			}
			return origin;
		}

		public void Render(Tesselator tesselator, PoseStack poseStack, Vec3 cameraPos, float dt)
		{
			Vector4f colour = WHITE_COLOUR;
			if(trailTexture != null)
			{
				textureManager.bindForSetup(trailTexture);
				RenderSystem.enableTexture();
			}
			else
			{
				colour = DEFAULT_TRAIL_COLOUR;
				RenderSystem.disableTexture();
			}

			Vec3 startPos = GetStartPoint();

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
	}


}
