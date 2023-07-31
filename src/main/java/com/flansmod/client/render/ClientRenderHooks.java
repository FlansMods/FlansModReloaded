package com.flansmod.client.render;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.gunshots.GunContext;
import com.flansmod.common.gunshots.ShooterContext;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
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
		ShooterContext shooterContext = ShooterContext.CreateFrom(MinecraftHelpers.GetClient().player);
		if(!shooterContext.IsValid())
			return;

		float totalFOVModifier = 0.0f;
		int FOVModifierCount = 0;

		GunContext[] gunContexts = shooterContext.GetAllGunContexts();
		for(GunContext gunContext : gunContexts)
		{
			if(!gunContext.IsValid())
				continue;

			for (Action action : gunContext.GetActionStack().GetActions())
			{
				if (action instanceof AimDownSightAction adsAction)
				{
					totalFOVModifier += action.actionDef.fovFactor;
					FOVModifierCount++;

				}
			}
		}

		if(FOVModifierCount > 0)
		{
			event.setNewFovModifier(event.getNewFovModifier() / (totalFOVModifier / FOVModifierCount));
		}
	}

	@SubscribeEvent
	public void OnRenderHands(RenderHandEvent event)
	{
		FlanItemModelRenderer renderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(event.getItemStack());
		if(renderer != null && renderer.ShouldRenderWhenHeld)
		{
			renderer.RenderFirstPerson(
				Minecraft.getInstance().player,
				event.getItemStack(),
				MinecraftHelpers.GetArm(event.getHand()),
				event.getHand() == InteractionHand.OFF_HAND ? ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND : ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND,
				event.getPoseStack(),
				event.getMultiBufferSource(),
				event.getPackedLight(),
				0,
				event.getEquipProgress());

			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void OnRenderOverlay(RenderGuiOverlayEvent event)
	{
		int i = MinecraftHelpers.GetClient().getWindow().getWidth();
		int j = MinecraftHelpers.GetClient().getWindow().getHeight();
		Tesselator tesselator = Tesselator.getInstance();
		Player player = MinecraftHelpers.GetClient().player;
		ShooterContext shooterContext = ShooterContext.CreateFrom(player);
		if(!shooterContext.IsValid())
			return;

		GunContext[] gunContexts = shooterContext.GetAllGunContexts();
		GunContext mainContext = gunContexts[0];
		GunContext offContext = gunContexts[1];
		if (event instanceof RenderGuiOverlayEvent.Pre)
		{
			switch (event.getOverlay().id().getPath())
			{
				case "helmet":
				{

					break;
				}
				case "crosshair":
				{
					RenderHitMarkerOverlay();
					if (RenderScopeOverlay(mainContext, offContext))
					{
						event.setCanceled(true);
					}
					break;
				}
				case "hotbar":
				{
					RenderPlayerAmmoOverlay(event.getPoseStack());
					RenderKillMessageOverlay();
					RenderTeamInfoOverlay();

					break;
				}
			}
		}
	}

	private boolean RenderScopeOverlay(GunContext main, GunContext off)
	{
		int i = MinecraftHelpers.GetClient().getWindow().getGuiScaledWidth();
		int j = MinecraftHelpers.GetClient().getWindow().getGuiScaledHeight();

		for(GunContext gunContext : new GunContext[] { main, off})
		{
			if(!gunContext.IsValid())
				continue;

			for (Action action : gunContext.GetActionStack().GetActions())
			{
				if (action instanceof ScopeAction scopeAction)
				{
					if (scopeAction.ApplyOverlay())
					{
						ResourceLocation overlayLocation = scopeAction.GetOverlayLocation();
						if (overlayLocation != null)
						{
							RenderSystem.setShader(GameRenderer::getPositionTexShader);
							RenderSystem.enableBlend();
							RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
							RenderSystem.enableTexture();
							RenderSystem.setShaderTexture(0, overlayLocation);

							Tesselator tesselator = Tesselator.getInstance();
							BufferBuilder builder = tesselator.getBuilder();
							builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
							builder.vertex(i * 0.5f - 2 * j, j, -90f).uv(0f, 1f).endVertex();
							builder.vertex(i * 0.5f + 2 * j, j, -90f).uv(1f, 1f).endVertex();
							builder.vertex(i * 0.5f + 2 * j, 0f, -90f).uv(1f, 0f).endVertex();
							builder.vertex(i * 0.5f - 2 * j, 0f, -90f).uv(0f, 0f).endVertex();
							tesselator.end();
						}
						return true;
					}
				}
			}
		}

		return false;
	}

	private static final ResourceLocation HIT_MARKER_TEXTURE = new ResourceLocation(FlansMod.MODID, "textures/gui/hitmarker.png");
	private static final float HIT_MARKER_SIZE = 9f;
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
		float uvMin = 0f, uvMax = 9f / 16f;
		float scale = 1f;
		float x = pos.x * 64.0f, y = pos.y * 64.0f;

		RenderQuad(i*0.5f + x - 0.5f*HIT_MARKER_SIZE*scale, j*0.5f + y - 0.5f*HIT_MARKER_SIZE*scale,
			HIT_MARKER_SIZE*scale, HIT_MARKER_SIZE*scale,
			uvMin, uvMin,
			16, 16);
	}

	private void RenderPlayerAmmoOverlay(PoseStack poseStack)
	{
		int screenX = MinecraftHelpers.GetClient().getWindow().getGuiScaledWidth();
		int screenY = MinecraftHelpers.GetClient().getWindow().getGuiScaledHeight();

		int anchorX = screenX / 2;
		int anchorY = screenY;

		ShooterContext shooterContext = ShooterContext.CreateFrom(Minecraft.getInstance().player);
		if(!shooterContext.IsValid())
			return;

		GunContext[] gunContexts = shooterContext.GetAllGunContexts();
		GunContext mainContext = gunContexts[0];
		GunContext offContext = gunContexts[1];
		if(gunContexts[0].IsValid())
		{
			RenderUntexturedQuad(anchorX + 94, anchorY - 41, 300, 18, 0x80808080);

			Minecraft.getInstance().getItemRenderer().renderGuiItem(mainContext.GetItemStack(), anchorX + 95, anchorY - 40);

			int x = anchorX + 113;
			for(int i = 0; i < mainContext.GetNumBulletStacks(EActionInput.PRIMARY); i++)
			{
				ItemStack bulletStack = mainContext.GetBulletStack(EActionInput.PRIMARY, i);
				if(!bulletStack.isEmpty())
				{
					int y = anchorY - 41 + (i % 2 == 0 ? 2 : 0);
					Minecraft.getInstance().getItemRenderer().renderGuiItem(bulletStack, x, y);
					if(bulletStack.isDamageableItem())
					{
						int countRemaining = bulletStack.getMaxDamage() - bulletStack.getDamageValue();
						RenderString(poseStack, x + 16, y + 4,  Component.literal(countRemaining + "/" + bulletStack.getMaxDamage()), 0xffffff);
						x += 32;
					}
				}
				x += 12;
			}

			RenderString(poseStack, anchorX + 96, anchorY - 50, mainContext.GetItemStack().getHoverName(), 0xffffff);

			// TODO: If alternate ammo?
		}

		if(gunContexts[1].IsValid())
		{
			RenderSystem.enableBlend();
			RenderUntexturedQuad(anchorX - 94 - 300, anchorY - 41, 300, 18, 0x80808080);

			Minecraft.getInstance().getItemRenderer().renderGuiItem(offContext.GetItemStack(), anchorX - 95 - 16, anchorY - 40);

			int x = anchorX - 113 - 16;
			for(int i = 0; i < offContext.GetNumBulletStacks(EActionInput.PRIMARY); i++)
			{
				ItemStack bulletStack = offContext.GetBulletStack(EActionInput.PRIMARY, i);
				if(!bulletStack.isEmpty())
				{
					int y = anchorY - 41 + (i % 2 == 0 ? 2 : 0);
					Minecraft.getInstance().getItemRenderer().renderGuiItem(bulletStack, x, y);
					if(bulletStack.isDamageableItem())
					{
						int countRemaining = bulletStack.getMaxDamage() - bulletStack.getDamageValue();
						RenderString(poseStack, x - 12 - 16, y + 4,  Component.literal(countRemaining + "/" + bulletStack.getMaxDamage()), 0xffffff);
						x -= 32;
					}
				}
				x -= 12;
			}

			RenderString(poseStack,
				anchorX - 96 - Minecraft.getInstance().font.width(offContext.GetItemStack().getHoverName()),
				anchorY - 50,
				offContext.GetItemStack().getHoverName(),
				0xffffff);
		}
	}


	private void RenderKillMessageOverlay()
	{

	}

	private void RenderTeamInfoOverlay()
	{

	}

	private void RenderQuad(float x, float y, float w, float h, float u0, float v0, float texW, float texH)
	{
		RenderSystem.enableTexture();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		builder.vertex(x, y + h, -90f)		.uv(u0 / texW, (v0 + h) / texH).endVertex();
		builder.vertex(x + w, y + h, -90f)	.uv((u0 + w) / texW, (v0 + h) / texH).endVertex();
		builder.vertex(x + w, y, -90f)		.uv((u0 + w) / texW, v0 / texH).endVertex();
		builder.vertex(x, y, -90f)			.uv(u0 / texW, v0 / texH).endVertex();
		tesselator.end();
	}

	private void RenderUntexturedQuad(float x, float y, float w, float h, int colour)
	{
		RenderSystem.disableTexture();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		builder.vertex(x, y + h, -90f)		.color(colour)	.endVertex();
		builder.vertex(x + w, y + h, -90f)	.color(colour)	.endVertex();
		builder.vertex(x + w, y, -90f)		.color(colour)	.endVertex();
		builder.vertex(x, y, -90f)			.color(colour)	.endVertex();
		tesselator.end();
		RenderSystem.enableTexture();
	}

	private void RenderString(PoseStack poseStack, float x, float y, Component content, int colour)
	{
		Minecraft.getInstance().font.draw(poseStack, content, x, y, colour);
	}

}
