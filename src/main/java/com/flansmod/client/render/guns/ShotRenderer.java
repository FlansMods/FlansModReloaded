package com.flansmod.client.render.guns;

import com.flansmod.util.Maths;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
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

	public int AddTrail(Vec3 origin, Vec3 endpoint)
	{
		ShotRenderInstance shot = new ShotRenderInstance(origin, endpoint, 0.05f, 10.0f, 10.0f, null);
		shots.add(shot);
		return shot.GetLifetime();
	}

	public int AddTrail(Vec3 origin, Vec3 endpoint, ResourceLocation texture)
	{
		ShotRenderInstance shot = new ShotRenderInstance(origin, endpoint, 0.05f, 10.0f, 10.0f, texture);
		shots.add(shot);
		return shot.GetLifetime();
	}

	public void ClientTick(TickEvent.ClientTickEvent event)
	{
		for(int i = shots.size() - 1; i >= 0; i--)
		{
			shots.get(i).Update();
			if(shots.get(i).Finished())
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

			for(ShotRenderInstance shot : shots)
			{
				shot.Render(tesselator, poseStack, pos, event.getPartialTick());
			}
		}
	}

	private static class ShotRenderInstance
	{
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

		public ShotRenderInstance(Vec3 start, Vec3 end, float w, float l, float speed, ResourceLocation trail)
		{
			origin = start;
			endPoint = end;
			width = w;
			length = l;
			bulletSpeed = speed;
			trailTexture = trail;
			distanceToTarget = start.distanceTo(end);
			lifetime = bulletSpeed <= 0.0001d ? 1 : Maths.Floor((distanceToTarget - length) / bulletSpeed);
		}

		public boolean Finished()
		{
			return ticksExisted >= lifetime;
		}

		public void Update()
		{
			ticksExisted++;
		}

		private static final Vector4f WHITE_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 0.0f);
		private static final Vector4f DEFAULT_TRAIL_COLOUR = new Vector4f(1.0f, 1.0f, 0.5f, 0.0f);

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

			double centerT = (ticksExisted + dt) * bulletSpeed;
			Vec3 centerPos = origin.lerp(endPoint, centerT);
			Vec3 bulletDirection = endPoint.subtract(origin).normalize();
			Vec3 cameraToTrailDirection = centerPos.subtract(cameraPos).normalize();

			Vec3 trailYAxis = bulletDirection.cross(cameraToTrailDirection).normalize().scale(width * 0.5f);
			Vec3 trailXAxis = bulletDirection.scale(length * 0.5f);
			Vec3 trailNormal = trailXAxis.cross(trailYAxis).normalize();


			poseStack.pushPose();
			poseStack.translate(centerPos.x - cameraPos.x, centerPos.y - cameraPos.y, centerPos.z - cameraPos.z);

			BufferBuilder buf = tesselator.getBuilder();
			buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR_NORMAL);

			buf.vertex(poseStack.last().pose(),
				(float)(trailXAxis.x + trailYAxis.x),
				(float)(trailXAxis.y + trailYAxis.y),
			    (float)(trailXAxis.z + trailYAxis.z))
				.color(colour.x, colour.y, colour.z, colour.w)
				.normal((float)trailNormal.x, (float)trailNormal.y, (float)trailNormal.z)
				.endVertex();
			buf.vertex(poseStack.last().pose(),
					(float)(trailXAxis.x - trailYAxis.x),
					(float)(trailXAxis.y - trailYAxis.y),
					(float)(trailXAxis.z - trailYAxis.z))
				.color(colour.x, colour.y, colour.z, colour.w)
				.normal((float)trailNormal.x, (float)trailNormal.y, (float)trailNormal.z)
				.endVertex();
			buf.vertex(poseStack.last().pose(),
					(float)(-trailXAxis.x - trailYAxis.x),
					(float)(-trailXAxis.y - trailYAxis.y),
					(float)(-trailXAxis.z - trailYAxis.z))
				.color(colour.x, colour.y, colour.z, colour.w)
				.normal((float)trailNormal.x, (float)trailNormal.y, (float)trailNormal.z)
				.endVertex();
			buf.vertex(poseStack.last().pose(),
					(float)(-trailXAxis.x + trailYAxis.x),
					(float)(-trailXAxis.y + trailYAxis.y),
					(float)(-trailXAxis.z + trailYAxis.z))
				.color(colour.x, colour.y, colour.z, colour.w)
				.normal((float)trailNormal.x, (float)trailNormal.y, (float)trailNormal.z)
				.endVertex();

			tesselator.end();

			poseStack.popPose();
		}
	}


}
