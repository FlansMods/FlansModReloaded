package com.flansmod.client.render.decals;

import com.flansmod.client.render.guns.ShotRenderer;
import com.flansmod.util.Maths;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;

public class DecalRenderer
{
	private HashMap<ResourceLocation, ArrayList<DecalInfo>> DecalsByTexture = new HashMap<>();
	private static TextureManager textureManager;
	private final static int MAX_DECALS_PER_TEXTURE = 512;

	public DecalRenderer()
	{
		textureManager = Minecraft.getInstance().textureManager;

		MinecraftForge.EVENT_BUS.addListener(this::RenderTick);
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
	}

	public void AddDecal(ResourceLocation texture, Vec3 position, Direction direction, float yaw, int lifetime)
	{
		AddDecal(texture,
			position,
			new Vec3(direction.getNormal().getX(),
				direction.getNormal().getY(),
				direction.getNormal().getZ()),
			yaw,
			lifetime);
	}

	public void AddDecal(ResourceLocation texture, Vec3 position, Vec3 normal, float yaw, int lifetime)
	{
		if(!DecalsByTexture.containsKey(texture))
			DecalsByTexture.put(texture, new ArrayList<>());

		if(DecalsByTexture.get(texture).size() >= MAX_DECALS_PER_TEXTURE)
		{
			DecalsByTexture.get(texture).remove(0);
		}
		DecalsByTexture.get(texture).add(new DecalInfo(position, normal, yaw, lifetime));
	}

	public void ClientTick(TickEvent.ClientTickEvent event)
	{
		for(var list : DecalsByTexture.values())
		{
			for (int i = list.size() - 1; i >= 0; i--)
			{
				list.get(i).Update();
				if (list.get(i).Finished())
					//shots.get(i).ticksExisted = 0;
					list.remove(i);
			}
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
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			//RenderSystem.disableDepthTest();

			for(var kvp : DecalsByTexture.entrySet())
			{
				if(kvp.getKey() != null && kvp.getValue().size() > 0)
				{
					//textureManager.bindForSetup(kvp.getKey());
					RenderSystem.setShaderTexture(0, kvp.getKey());
					RenderSystem.enableTexture();
				}


				RenderSystem.enableBlend();
				//RenderSystem.blendEquation(GlConst.GL_FUNC_ADD);
				RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				//RenderSystem.disableDepthTest();
				//RenderSystem.depthFunc(GlConst.GL_LEQUAL);

				for(DecalInfo decal : kvp.getValue())
					decal.Render(tesselator, poseStack, pos, event.getPartialTick());
			}
		}
	}

	private static class DecalInfo
	{
		private final Vec3 Position;
		private final Vec3 Normal;
		private final float Yaw;
		private final int Lifetime;
		private int TicksExisted = 0;

		public DecalInfo(Vec3 pos, Vec3 norm, float yaw, int life)
		{
			Position = pos;
			Normal = norm;
			Yaw = yaw;
			Lifetime = life;
		}

		public void Update()
		{
			TicksExisted++;
		}

		public boolean Finished()
		{
			return TicksExisted >= Lifetime;
		}

		public void Render(Tesselator tesselator, PoseStack poseStack, Vec3 cameraPos, float dt)
		{
			poseStack.pushPose();
			Vec3 toCamera = Maths.Sub(Position, cameraPos);
			poseStack.translate(toCamera.x, toCamera.y, toCamera.z);
			poseStack.mulPose(new Quaternionf().rotateAxis(Yaw, Normal.toVector3f()));

			Vec3 xAxis = Maths.Cross(Normal, new Vec3(1d, 0d, 0d));
			if(xAxis.lengthSqr() < 0.01d)
				xAxis = Maths.Cross(Normal, new Vec3(0d, 0d, 1d));

			Vec3 yAxis = Maths.Cross(xAxis, Normal);

			BufferBuilder buf = tesselator.getBuilder();
			buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

			xAxis = xAxis.normalize().scale(0.25d);
			yAxis = yAxis.normalize().scale(0.25d);

			Vec3 smolNormal = Normal.normalize().scale(0.0001d * toCamera.lengthSqr());

			Vec3 v0 = Maths.Add(smolNormal, Maths.Add(xAxis, yAxis));
			Vec3 v1 = Maths.Add(smolNormal, Maths.Sub(xAxis, yAxis));
			Vec3 v2 = Maths.Add(smolNormal, Maths.Sub(xAxis.scale(-1d), yAxis));
			Vec3 v3 = Maths.Add(smolNormal, Maths.Add(xAxis.scale(-1d), yAxis));

			buf.vertex(poseStack.last().pose(), (float)v0.x, (float)v0.y, (float)v0.z)
				.uv(0f, 0f)
				.endVertex();
			buf.vertex(poseStack.last().pose(), (float)v1.x, (float)v1.y, (float)v1.z)
				.uv(0f, 1f)
				.endVertex();
			buf.vertex(poseStack.last().pose(), (float)v2.x, (float)v2.y, (float)v2.z)
				.uv(1f, 1f)
				.endVertex();
			buf.vertex(poseStack.last().pose(), (float)v3.x, (float)v3.y, (float)v3.z)
				.uv(1f, 0f)
				.endVertex();

			tesselator.end();
			poseStack.popPose();
		}
	}
}
