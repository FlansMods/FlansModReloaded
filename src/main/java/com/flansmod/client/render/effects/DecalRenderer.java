package com.flansmod.client.render.effects;

import com.flansmod.physics.common.util.Maths;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DecalRenderer
{
	private final HashMap<ResourceLocation, DecalLayerInfo> DecalsByTexture = new HashMap<>();
	private static TextureManager textureManager;
	private final static int MAX_DECALS_PER_TEXTURE = 512;

	public DecalRenderer()
	{
		textureManager = Minecraft.getInstance().textureManager;

		MinecraftForge.EVENT_BUS.addListener(this::RenderTick);
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
	}

	public void AddDecal(@Nonnull ResourceLocation texture,
						 @Nonnull Vec3 position,
						 @Nonnull Direction direction,
						 float yaw,
						 int lifetime)
	{
		AddDecal(texture,
			position,
			new Vec3(direction.getNormal().getX(),
				direction.getNormal().getY(),
				direction.getNormal().getZ()),
			yaw,
			lifetime);
	}

	public void AddDecal(@Nonnull ResourceLocation texture,
						 @Nonnull Vec3 position,
						 @Nonnull Vec3 normal,
						 float yaw,
						 int lifetime)
	{
		if(!DecalsByTexture.containsKey(texture))
			DecalsByTexture.put(texture, new DecalLayerInfo());

		DecalsByTexture.get(texture).AddDecal(position, normal, yaw, lifetime);
	}

	public void AddOrUpdateDecal(@Nonnull ResourceLocation texture,
								 @Nullable UUID uuid,
								 @Nonnull Vec3 position,
								 @Nonnull Vec3 normal,
								 float yaw,
								 int lifetime)
	{
		if(!DecalsByTexture.containsKey(texture))
			DecalsByTexture.put(texture, new DecalLayerInfo());

		DecalsByTexture.get(texture).AddOrUpdateDecal(uuid, position, normal, yaw, lifetime);
	}
	public void AddOrUpdateDecal(@Nonnull ResourceLocation texture,
								 @Nullable UUID uuid,
								 @Nonnull Vec3 position,
								 @Nonnull Vec3 normal,
								 @Nonnull Vector4f colour,
								 float yaw,
								 int lifetime)
	{
		if(!DecalsByTexture.containsKey(texture))
			DecalsByTexture.put(texture, new DecalLayerInfo());

		DecalsByTexture.get(texture).AddOrUpdateDecal(uuid, position, normal, colour, yaw, lifetime);
	}

	public void ClientTick(@Nonnull TickEvent.ClientTickEvent event)
	{
		if(event.phase == TickEvent.Phase.START)
			for(var list : DecalsByTexture.values())
				list.ClientTick();
	}

	public void RenderTick(@Nonnull RenderLevelStageEvent event)
	{
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
		{
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			for(var kvp : DecalsByTexture.entrySet())
			{
				if(kvp.getKey() != null && kvp.getValue().HasAnyDecals())
				{
					RenderSystem.setShaderTexture(0, kvp.getKey());
					kvp.getValue().RenderTick(
						Tesselator.getInstance(),
						event.getPoseStack(),
						event.getCamera().getPosition(),
						event.getPartialTick());
				}
			}
		}
	}

	public static class DecalLayerInfo
	{
		public final ArrayList<DecalInfo> StaticDecals = new ArrayList<>();
		public final HashMap<UUID, DecalInfo> DynamicDecals = new HashMap<UUID, DecalInfo>();

		public boolean HasAnyDecals()
		{
			return StaticDecals.size() + DynamicDecals.size() > 0;
		}

		public void AddDecal(@Nonnull Vec3 position, @Nonnull Vec3 normal, float yaw, int lifetime)
		{
			if(StaticDecals.size() >= MAX_DECALS_PER_TEXTURE)
			{
				StaticDecals.remove(0);
			}
			StaticDecals.add(new DecalInfo(position, normal, yaw, lifetime));
		}

		public void AddOrUpdateDecal(@Nullable UUID uuid, @Nonnull Vec3 position, @Nonnull Vec3 normal, float yaw, int lifetime)
		{
			if(uuid != null)
				DynamicDecals.put(uuid, new DecalInfo(position, normal, yaw, lifetime));
			else
				StaticDecals.add(new DecalInfo(position, normal, yaw, lifetime));
		}
		public void AddOrUpdateDecal(@Nullable UUID uuid, @Nonnull Vec3 position, @Nonnull Vec3 normal, @Nonnull Vector4f colour, float yaw, int lifetime)
		{
			if(uuid != null)
				DynamicDecals.put(uuid, new DecalInfo(position, normal, colour, yaw, lifetime));
			else
				StaticDecals.add(new DecalInfo(position, normal, colour, yaw, lifetime));
		}

		public void ClientTick()
		{
			for (int i = StaticDecals.size() - 1; i >= 0; i--)
			{
				StaticDecals.get(i).Update();
				if (StaticDecals.get(i).Finished())
					StaticDecals.remove(i);
			}
			List<UUID> toRemove = new ArrayList<>();
			for(var kvp : DynamicDecals.entrySet())
			{
				kvp.getValue().Update();
				if (kvp.getValue().Finished())
					toRemove.add(kvp.getKey());
			}
			for(UUID remove : toRemove)
				DynamicDecals.remove(remove);
		}

		public void RenderTick(@Nonnull Tesselator tesselator, @Nonnull PoseStack poseStack, @Nonnull Vec3 pos, float partialTick)
		{
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			for(DecalInfo decal : StaticDecals)
				decal.Render(tesselator, poseStack, pos, partialTick);
			for(DecalInfo decal : DynamicDecals.values())
				decal.Render(tesselator, poseStack, pos, partialTick);
		}
	}

	public static class DecalInfo
	{
		@Nonnull
		private final Vec3 Position;
		@Nonnull
		private final Vec3 Normal;
		@Nonnull
		private final Vector4f Colour;
		private final float Yaw;
		private final int Lifetime;
		private int TicksExisted = 0;

		public DecalInfo(@Nonnull Vec3 pos, @Nonnull Vec3 norm, @Nonnull Vector4f colour, float yaw, int life)
		{
			Position = pos;
			Normal = norm;
			Colour = colour;
			Yaw = yaw;
			Lifetime = life;
		}
		public DecalInfo(@Nonnull Vec3 pos, @Nonnull Vec3 norm, float yaw, int life)
		{
			this(pos, norm, new Vector4f(1f, 1f, 1f, 1f), yaw, life);
		}

		public void Update()
		{
			TicksExisted++;
		}

		public boolean Finished()
		{
			return TicksExisted >= Lifetime;
		}

		public void Render(@Nonnull Tesselator tesselator, @Nonnull PoseStack poseStack, @Nonnull Vec3 cameraPos, float dt)
		{
			poseStack.pushPose();
			Vec3 toCamera = Maths.sub(Position, cameraPos);
			poseStack.translate(toCamera.x, toCamera.y, toCamera.z);
			poseStack.mulPose(new Quaternionf().rotateAxis(Yaw, Normal.toVector3f()));

			Vec3 xAxis = Maths.cross(Normal, new Vec3(1d, 0d, 0d));
			if(xAxis.lengthSqr() < 0.01d)
				xAxis = Maths.cross(Normal, new Vec3(0d, 0d, 1d));

			Vec3 yAxis = Maths.cross(xAxis, Normal);

			BufferBuilder buf = tesselator.getBuilder();
			buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

			xAxis = xAxis.normalize().scale(0.25d);
			yAxis = yAxis.normalize().scale(0.25d);

			Vec3 smolNormal = Normal.normalize().scale(0.001d * toCamera.length());

			Vec3 v0 = Maths.add(smolNormal, Maths.add(xAxis, yAxis));
			Vec3 v1 = Maths.add(smolNormal, Maths.sub(xAxis, yAxis));
			Vec3 v2 = Maths.add(smolNormal, Maths.sub(xAxis.scale(-1d), yAxis));
			Vec3 v3 = Maths.add(smolNormal, Maths.add(xAxis.scale(-1d), yAxis));

			buf.vertex(poseStack.last().pose(), (float)v0.x, (float)v0.y, (float)v0.z)
				.uv(0f, 0f)
				.color(Colour.x, Colour.y, Colour.z, Colour.w)
				.endVertex();
			buf.vertex(poseStack.last().pose(), (float)v1.x, (float)v1.y, (float)v1.z)
				.uv(0f, 1f)
				.color(Colour.x, Colour.y, Colour.z, Colour.w)
				.endVertex();
			buf.vertex(poseStack.last().pose(), (float)v2.x, (float)v2.y, (float)v2.z)
				.uv(1f, 1f)
				.color(Colour.x, Colour.y, Colour.z, Colour.w)
				.endVertex();
			buf.vertex(poseStack.last().pose(), (float)v3.x, (float)v3.y, (float)v3.z)
				.uv(1f, 0f)
				.color(Colour.x, Colour.y, Colour.z, Colour.w)
				.endVertex();

			tesselator.end();
			poseStack.popPose();
		}
	}
}
