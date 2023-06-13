package com.flansmod.client.render;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Action;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.AimDownSightAction;
import com.flansmod.common.actions.ScopeAction;
import com.flansmod.common.types.guns.GunContext;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

public class ClientRenderHooks
{
	private Minecraft MC;
	private RandomSource RNG;
	public ClientRenderHooks()
	{
		MC = Minecraft.getInstance();
		RNG = new LegacyRandomSource(0x19393939292L);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void OnClientTick(TickEvent.ClientTickEvent event)
	{
		UpdateHitMarkers();
	}

	@SubscribeEvent
	public void OnComputeFOV(ComputeFovModifierEvent event)
	{
		Player player = MinecraftHelpers.GetClient().player;

		if(player != null)
		{
			GunContext mainContext = GunContext.CreateFromPlayer(player, InteractionHand.MAIN_HAND);
			GunContext offContext = GunContext.CreateFromPlayer(player, InteractionHand.OFF_HAND);

			ActionStack stack = FlansModClient.GUNSHOTS_CLIENT.GetActionStack(player);
			if(stack == null)
				return;

			for(Action action : stack.GetActions())
			{
				if (action instanceof AimDownSightAction adsAction)
				{
					event.setNewFovModifier(event.getNewFovModifier() / action.actionDef.fovFactor);
				}
			}
		}
	}

	@SubscribeEvent
	public void OnRenderOverlay(RenderGuiOverlayEvent event)
	{
		int i = MinecraftHelpers.GetClient().getWindow().getWidth();
		int j = MinecraftHelpers.GetClient().getWindow().getHeight();
		Tesselator tesselator = Tesselator.getInstance();
		Player player = MinecraftHelpers.GetClient().player;

		if(player != null)
		{
			GunContext mainContext = GunContext.CreateFromPlayer(player, InteractionHand.MAIN_HAND);
			GunContext offContext = GunContext.CreateFromPlayer(player, InteractionHand.OFF_HAND);

			if (event instanceof RenderGuiOverlayEvent.Pre)
			{
				switch (event.getOverlay().id().getPath())
				{
					case "helmet":
					{
						RenderScopeOverlay(player, mainContext, offContext);
						//event.setCanceled(true);
						break;
					}
					case "crosshair":
					{
						RenderHitMarkerOverlay();

						//event.setCanceled(true);
						break;
					}
					case "hotbar":
					{
						RenderPlayerAmmoOverlay();
						RenderKillMessageOverlay();
						RenderTeamInfoOverlay();

						//event.setCanceled(true);
						break;
					}
				}
			}
		}
	}

	private void RenderScopeOverlay(Player player, GunContext main, GunContext off)
	{
		int i = MinecraftHelpers.GetClient().getWindow().getGuiScaledWidth();
		int j = MinecraftHelpers.GetClient().getWindow().getGuiScaledHeight();

		ActionStack stack = FlansModClient.GUNSHOTS_CLIENT.GetActionStack(player);
		if(stack == null)
			return;

		for(Action action : stack.GetActions())
		{
			if(action instanceof ScopeAction scopeAction)
			{
				if(scopeAction.ApplyOverlay())
				{
					ResourceLocation overlayLocation = scopeAction.GetOverlayLocation();
					if(overlayLocation != null)
					{
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						RenderSystem.enableBlend();
						RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
						RenderSystem.enableTexture();
						RenderSystem.setShaderTexture(0, overlayLocation);

						Tesselator tesselator = Tesselator.getInstance();
						BufferBuilder builder = tesselator.getBuilder();
						builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
						builder.vertex(i*0.5f - 2*j, j, -90f).uv(0f, 1f).endVertex();
						builder.vertex(i*0.5f + 2*j, j, -90f).uv(1f, 1f).endVertex();
						builder.vertex(i*0.5f + 2*j, 0f, -90f).uv(1f, 0f).endVertex();
						builder.vertex(i*0.5f - 2*j, 0f, -90f).uv(0f, 0f).endVertex();
						tesselator.end();
					}
				}
			}
		}
	}

	private static final ResourceLocation HIT_MARKER_TEXTURE = new ResourceLocation(FlansMod.MODID, "textures/gui/hitmarker.png");
	private static final float HIT_MARKER_SIZE = 4f;
	private static float HitMarkerDurationRemaining = 0.0f;
	private static boolean isMLG = false;
	private static ArrayList<Vec2> MLGPositions = new ArrayList<>();
	public void ApplyHitMarker(float duration, boolean MLG)
	{
		HitMarkerDurationRemaining = Maths.Max(HitMarkerDurationRemaining, duration);
		MC.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_HURT, 1.0f));
		isMLG = MLG;
	}

	private void UpdateHitMarkers()
	{
		if(isMLG)
		{
			MC.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_HURT, 1.0f));
			for(int i = 0; i < Maths.Ceil(HitMarkerDurationRemaining * 0.5f); i++)
			{
				MLGPositions.add(new Vec2((float)RNG.nextGaussian(), (float)RNG.nextGaussian()));
			}
			for(int i = 0; i < Maths.Ceil(MLGPositions.size() * 0.25f); i++)
			{
				MLGPositions.remove(RNG.nextInt(i+1));
			}
			if(MLGPositions.size() == 0)
				isMLG = false;
		}
		HitMarkerDurationRemaining--;
	}

	private void RenderHitMarkerOverlay()
	{
		int i = MinecraftHelpers.GetClient().getWindow().getGuiScaledWidth();
		int j = MinecraftHelpers.GetClient().getWindow().getGuiScaledHeight();

		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, HIT_MARKER_TEXTURE);

		if(isMLG)
		{
			for(Vec2 v : MLGPositions)
				RenderHitMarker(i, j, v);
		}
		else if(HitMarkerDurationRemaining > 0.0f)
		{
			RenderHitMarker(i, j, Vec2.ZERO);
		}
	}

	private void RenderHitMarker(int i, int j, Vec2 pos)
	{
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		float uvMin = 0f, uvMax = 9f / 16f;
		float scale = 1f;
		float x = pos.x * 64.0f, y = pos.y * 64.0f;
		builder.vertex(i*0.5f + x - HIT_MARKER_SIZE*scale, j*0.5f + y + HIT_MARKER_SIZE*scale, -90f).uv(uvMin, uvMax).endVertex();
		builder.vertex(i*0.5f + x + HIT_MARKER_SIZE*scale, j*0.5f + y + HIT_MARKER_SIZE*scale, -90f).uv(uvMax, uvMax).endVertex();
		builder.vertex(i*0.5f + x + HIT_MARKER_SIZE*scale, j*0.5f + y - HIT_MARKER_SIZE*scale, -90f).uv(uvMax, uvMin).endVertex();
		builder.vertex(i*0.5f + x - HIT_MARKER_SIZE*scale, j*0.5f + y - HIT_MARKER_SIZE*scale, -90f).uv(uvMin, uvMin).endVertex();
		tesselator.end();
	}

	private void RenderPlayerAmmoOverlay()
	{

	}

	private void RenderKillMessageOverlay()
	{

	}

	private void RenderTeamInfoOverlay()
	{

	}
}
