package com.flansmod.client.render;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.nodes.AimDownSightAction;
import com.flansmod.common.actions.nodes.ScopeAction;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.effects.FlansMobEffect;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.event.*;
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
		if(event.phase == TickEvent.Phase.END)
		{
			UpdateHitMarkers();
		}
	}

	@SubscribeEvent
	public void OnComputeFOV(ComputeFovModifierEvent event)
	{
		ShooterContext shooterContext = ShooterContext.GetOrCreate(MinecraftHelpers.GetClient().player);
		if(!shooterContext.IsValid())
			return;

		float totalFOVModifier = 0.0f;
		int FOVModifierCount = 0;

		GunContext[] gunContexts = shooterContext.GetAllGunContexts(true);
		for(GunContext gunContext : gunContexts)
		{
			if(!gunContext.IsValid())
				continue;

			for (ActionGroupInstance actionGroup : gunContext.GetActionStack().GetActiveActionGroups())
			{
				for(ActionInstance action : actionGroup.GetActions())
				{
					if (action instanceof AimDownSightAction adsAction)
					{
						totalFOVModifier += adsAction.FOVFactor();
						FOVModifierCount++;
					}
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

	//@SubscribeEvent
	//public void OnRenderLiving(RenderLivingEvent<LivingEntity> event)
	//{

	//}

	@SubscribeEvent
	public void OnRenderOverlay(RenderGuiOverlayEvent event)
	{
		int i = MinecraftHelpers.GetClient().getWindow().getWidth();
		int j = MinecraftHelpers.GetClient().getWindow().getHeight();
		Tesselator tesselator = Tesselator.getInstance();
		Player player = MinecraftHelpers.GetClient().player;
		ShooterContext shooterContext = ShooterContext.GetOrCreate(player);
		if(!shooterContext.IsValid())
			return;

		GunContext[] gunContexts = shooterContext.GetAllGunContexts(true);
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
					for(GunContext gunContext : gunContexts)
					{
						if(!gunContext.IsValid())
							continue;

						for(ActionGroupInstance actionGroup : gunContext.GetActionStack().GetActiveActionGroups())
						{
							for (ActionInstance action : actionGroup.GetActions())
							{
								if (action instanceof AimDownSightAction adsAction)
								{
									event.setCanceled(true);
								}
							}
						}
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

			for(ActionGroupInstance actionGroup : gunContext.GetActionStack().GetActiveActionGroups())
			{
				for (ActionInstance action : actionGroup.GetActions())
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
		Player player = Minecraft.getInstance().player;

		int screenX = MinecraftHelpers.GetClient().getWindow().getGuiScaledWidth();
		int screenY = MinecraftHelpers.GetClient().getWindow().getGuiScaledHeight();

		int anchorX = screenX / 2;
		int anchorY = screenY;

		ShooterContext shooterContext = ShooterContext.GetOrCreate(player);
		if(player == null || !shooterContext.IsValid())
			return;

		GunContext[] gunContexts = shooterContext.GetAllGunContexts(true);
		GunContext mainContext = gunContexts[0];
		GunContext offContext = gunContexts[1];
		if(gunContexts[0].IsValid())
		{
			RenderUntexturedQuad(anchorX + 94, anchorY - 20, 300, 18, 0x80808080);

			Minecraft.getInstance().getItemRenderer().renderGuiItem(mainContext.GetItemStack(), anchorX + 95, anchorY - 19);

			int x = anchorX + 113;

			ActionGroupContext mainHandPrimaryContext = ActionGroupContext.CreateFrom(mainContext, Actions.DefaultPrimaryActionKey);
			if(mainHandPrimaryContext.IsShootAction())
			{
				MagazineDefinition magDef = mainHandPrimaryContext.GetMagazineType(0);
				ItemStack[] bulletStacks = mainHandPrimaryContext.GetCombinedBulletStacks(0);

				if(magDef.numRounds <= 32)
				{
					int stackIndex = 0;
					int bulletIndex = 0;
					for(int i = 0; i < magDef.numRounds; i++)
					{
						if(stackIndex < bulletStacks.length)
						{
							if(bulletIndex == bulletStacks[stackIndex].getCount())
							{
								stackIndex++;
								bulletIndex = 0;
							}

							if(stackIndex < bulletStacks.length
							&& bulletIndex < bulletStacks[stackIndex].getCount())
							{
								if(!bulletStacks[stackIndex].isEmpty() && bulletStacks[stackIndex].getItem() != Items.APPLE)
								{
									int y = anchorY - 20 + (i % 4 == 3 ? 2 : (i % 4 == 2 ? 0 : (i % 4 == 1 ? 1 : 3)));
									Minecraft.getInstance().getItemRenderer().renderGuiItem(bulletStacks[stackIndex], x, y);
									x += 5;
								}
								bulletIndex++;
							}
						}
					}
				}
				else
				{
					for (ItemStack bulletStack : bulletStacks)
					{
						if (bulletStack.isEmpty() || bulletStack.getItem() == Items.APPLE)
							continue;
						int y = anchorY - 20;
						Minecraft.getInstance().getItemRenderer().renderGuiItem(bulletStack, x, y);
						Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, bulletStack, x, y);
						x += 16;
					}
				}
			}

			RenderString(poseStack, anchorX + 96, anchorY - 29, mainContext.GetItemStack().getHoverName(), 0xffffff);

			// Render stacks of effects
			int xOffset = 0;
			for(MobEffectInstance mobEffect : player.getActiveEffects())
			{
				if(mobEffect.getEffect() instanceof FlansMobEffect flansMobEffect)
				{
					TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(flansMobEffect);
					RenderSystem.setShaderTexture(0, sprite.atlasLocation());
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
					RenderSprite(anchorX + 100 + xOffset, anchorY - 49, 18, 18, sprite);



					String stacksString = Integer.toString(mobEffect.getAmplifier() + 1);
					int stacksStringWidth = Minecraft.getInstance().font.width(stacksString);
					RenderString(poseStack, anchorX + 118 + xOffset - stacksStringWidth, anchorY - 49, Component.literal(stacksString), 0xffffff);

					String timeRemaining = ".".repeat(Math.max(0, Maths.Min(mobEffect.getDuration() / 20, 5)));
					RenderString(poseStack, anchorX + 102 + xOffset, anchorY - 39, Component.literal(timeRemaining), 0xffffff);


					xOffset += 20;
				}
			}

			if(mainHandPrimaryContext.Def.twoHanded && !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty())
			{
				RenderString(poseStack, anchorX + 96, anchorY - 39, Component.translatable("tooltip.dual_wielding_two_handed"), 0xb0b0b0);
			}

			// TODO: If alternate ammo?
		}

		if(gunContexts[1].IsValid())
		{
			RenderSystem.enableBlend();
			RenderUntexturedQuad(anchorX - 94 - 308, anchorY - 20, 300, 18, 0x80808080);

			//Minecraft.getInstance().getItemRenderer().renderGuiItem(offContext.GetItemStack(), anchorX - 95 - 16, anchorY - 19);

			int x = anchorX - 113 - 22;
			ActionGroupContext offHandPrimaryContext = ActionGroupContext.CreateFrom(offContext, Actions.DefaultPrimaryActionKey);
			if(offHandPrimaryContext.IsShootAction())
			{
				MagazineDefinition magDef = offHandPrimaryContext.GetMagazineType(0);
				ItemStack[] bulletStacks = offHandPrimaryContext.GetCombinedBulletStacks(0);

				if(magDef.numRounds <= 32)
				{
					int stackIndex = 0;
					int bulletIndex = 0;
					for(int i = 0; i < magDef.numRounds; i++)
					{
						if(stackIndex < bulletStacks.length)
						{
							if(bulletIndex == bulletStacks[stackIndex].getCount())
							{
								stackIndex++;
								bulletIndex = 0;
							}

							if(stackIndex < bulletStacks.length
								&& bulletIndex < bulletStacks[stackIndex].getCount())
							{
								if(!bulletStacks[stackIndex].isEmpty() && bulletStacks[stackIndex].getItem() != Items.APPLE)
								{
									int y = anchorY - 20 + (i % 4 == 3 ? 2 : (i % 4 == 2 ? 0 : (i % 4 == 1 ? 1 : 3)));
									Minecraft.getInstance().getItemRenderer().renderGuiItem(bulletStacks[stackIndex], x, y);
									x -= 5;
								}
								bulletIndex++;
							}
						}
					}
				}
				else
				{
					for (ItemStack bulletStack : bulletStacks)
					{
						if (bulletStack.isEmpty() || bulletStack.getItem() == Items.APPLE)
							continue;
						int y = anchorY - 20;
						Minecraft.getInstance().getItemRenderer().renderGuiItem(bulletStack, x, y);
						Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, bulletStack, x, y);
						x -= 16;
					}
				}
			}

			RenderString(poseStack,
				anchorX - 98 - Minecraft.getInstance().font.width(offContext.GetItemStack().getHoverName()),
				anchorY - 31,
				offContext.GetItemStack().getHoverName(),
				0xffffff);

			if(offHandPrimaryContext.Def.twoHanded && !player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
			{
				Component warningString = Component.translatable("tooltip.dual_wielding_two_handed");
				RenderString(poseStack, anchorX - 98 - Minecraft.getInstance().font.width(warningString), anchorY - 39, warningString, 0xb0b0b0);
			}
		}
	}


	private void RenderKillMessageOverlay()
	{

	}

	private void RenderTeamInfoOverlay()
	{

	}

	private void RenderSprite(float x, float y, float w, float h, TextureAtlasSprite sprite)
	{
		float u0 = sprite.getU0();
		float u1 = sprite.getU1();
		float v0 = sprite.getV0();
		float v1 = sprite.getV1();
		RenderSystem.enableTexture();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		builder.vertex(x, y + h, -90f)		.uv(u0, v1).endVertex();
		builder.vertex(x + w, y + h, -90f)	.uv(u1, v1).endVertex();
		builder.vertex(x + w, y, -90f)		.uv(u1, v0).endVertex();
		builder.vertex(x, y, -90f)			.uv(u0, v0).endVertex();
		tesselator.end();
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
