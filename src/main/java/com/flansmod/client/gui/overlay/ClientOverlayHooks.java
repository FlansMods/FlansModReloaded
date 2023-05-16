package com.flansmod.client.gui.overlay;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Action;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.ScopeAction;
import com.flansmod.common.types.guns.GunContext;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientOverlayHooks
{
	public ClientOverlayHooks()
	{
		MinecraftForge.EVENT_BUS.register(this);
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
		int i = MinecraftHelpers.GetClient().getWindow().getWidth();
		int j = MinecraftHelpers.GetClient().getWindow().getHeight();

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

	private void RenderHitMarkerOverlay()
	{

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
