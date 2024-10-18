package com.flansmod.client.render;

import com.flansmod.physics.client.DebugRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.nodes.AimDownSightAction;
import com.flansmod.common.actions.nodes.ScopeAction;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.effects.FlansMobEffect;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.bullets.elements.ProjectileDefinition;
import com.flansmod.common.types.guns.elements.ModeDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
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
	public void OnRenderFirstPersonHands(RenderHandEvent event)
	{

	}

	@SubscribeEvent
	public void OnComputeFOV(ComputeFovModifierEvent event)
	{
		ShooterContext shooterContext = ShooterContext.of(MinecraftHelpers.GetClient().player);
		if(!shooterContext.IsValid())
			return;

		float totalFOVModifier = 0.0f;
		int FOVModifierCount = 0;

		GunContext[] gunContexts = shooterContext.GetAllGunContexts();
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
	public void OnRenderOverlay(RenderGuiOverlayEvent event)
	{
		GuiGraphics graphics = event.getGuiGraphics();
		Player player = MinecraftHelpers.GetClient().player;
		ShooterContext shooterContext = ShooterContext.of(player);
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
					RenderPlayerAmmoOverlay(event.getGuiGraphics());
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
	private static boolean isFatal = false;
	private static boolean isMLG = false;
	private static final ArrayList<Vec2> MLGPositions = new ArrayList<>();
	public void ApplyHitMarker(float duration, boolean fatal, boolean MLG)
	{
		HitMarkerDurationRemaining = Maths.max(HitMarkerDurationRemaining, duration);
		MC.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_HURT, 1.0f));
		isMLG = MLG;
		isFatal = fatal;
	}

	private void UpdateHitMarkers()
	{
		if(isMLG)
		{
			MC.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_HURT, 1.0f));
			for(int i = 0; i < Maths.ceil(HitMarkerDurationRemaining * 0.5f); i++)
			{
				MLGPositions.add(new Vec2((float)RNG.nextGaussian(), (float)RNG.nextGaussian()));
			}
			for(int i = 0; i < Maths.ceil(MLGPositions.size() * 0.25f); i++)
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
		float uMin = isFatal ? 16f : 0f;
		float vMin = 0f;

		float scale = 1f;
		float x = pos.x * 64.0f, y = pos.y * 64.0f;

		RenderQuad(i*0.5f + x - 0.5f*HIT_MARKER_SIZE*scale, j*0.5f + y - 0.5f*HIT_MARKER_SIZE*scale,
			HIT_MARKER_SIZE*scale, HIT_MARKER_SIZE*scale,
			uMin, vMin,
			32, 16);
	}

	private void RenderPlayerAmmoOverlay(@Nonnull GuiGraphics graphics)
	{
		Player player = Minecraft.getInstance().player;

		int screenX = MinecraftHelpers.GetClient().getWindow().getGuiScaledWidth();
		int screenY = MinecraftHelpers.GetClient().getWindow().getGuiScaledHeight();

		int anchorX = screenX / 2;
		int anchorY = screenY;

		ShooterContext shooterContext = ShooterContext.of(player);
		if(player == null || !shooterContext.IsValid())
			return;

		GunContext[] gunContexts = shooterContext.GetAllGunContexts();
		GunContext mainContext = gunContexts[0];
		GunContext offContext = gunContexts[1];

		if(gunContexts[0].IsValid())
		{
			GunItem item = (GunItem)mainContext.Stack.getItem();
			int a = 0x80808080;
			int b = 0xffffff;
			int c = 0x3f808080;
			Vector4f lockCol = new Vector4f(128f/255f,128f/255f,128f/255f,128f/255f);
			if(item.lockTime > 0){
				a = 0x80fec710;
				b = 0xfffec710;
				c = 0x80fec710;
				lockCol = new Vector4f(254f/255f,199f/255f,16f/255f,1);
			}
			if(item.lockTime > item.lockTimeMax){
				a = 0x80fe1010;
				b = 0xfffe1010;
				c = 0x80fe1010;
				lockCol = new Vector4f(254f/255f,16f/255f,16f/255f,1);
			}

			ProjectileDefinition def = item.GetChamberProjectile(mainContext.Stack,mainContext);

			if(def != null){
				if(def.HasLockOn()){
					int i = MinecraftHelpers.GetClient().getWindow().getGuiScaledWidth();
					int j = MinecraftHelpers.GetClient().getWindow().getGuiScaledHeight();
					double size = ((def.lockCone/ Minecraft.getInstance().options.fov().get())*i)/2d;
					RenderUntexturedCircle(i*0.5f, j*0.5f ,
							32, (float)size,

							(float)size-1, c);

					if(item.LockedOnTarget != null){
						Transform t = Transform.fromPos(item.LockedOnTarget.position());
						float rot = Minecraft.getInstance().cameraEntity.getYRot();
						t = t.rotateYaw(rot);
						t = t.rotatePitch(-Minecraft.getInstance().cameraEntity.getXRot());
						t = t.rotateRoll(45f*Math.min(item.lockTime/item.lockTimeMax,1f));

						Vector3f bounds = new Vector3f((float) item.LockedOnTarget.getBoundingBox().getSize(), (float) item.LockedOnTarget.getBoundingBox().getSize(),0);
						t = t.translated(new Vec3(0,item.LockedOnTarget.getBoundingBox().getSize()/2,0));
						DebugRenderer.renderCube(t,1,lockCol,bounds.mul(2f-Math.min(item.lockTime/item.lockTimeMax,1f)));
						//Transform t2 = t;
						//Vector3f bounds2 = bounds;
						//DebugRenderer.RenderCube(t2,2,lockCol,bounds2.mul(0.5f));
					}
				}
			}


			RenderUntexturedQuad(anchorX + 94, anchorY - 20, 300, 18, a);

			RenderItem(graphics, mainContext.GetItemStack(), anchorX + 95, anchorY - 19, false);

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
									RenderItem(graphics, bulletStacks[stackIndex],  x, y, false);
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
						RenderItem(graphics, bulletStack, x, y, true);
						x += 16;
					}
				}
			}


			RenderString(graphics, anchorX + 96, anchorY - 29, mainContext.GetItemStack().getHoverName(), b);

			// Render stacks of effects
			int xOffset = 0;
			for(MobEffectInstance mobEffect : player.getActiveEffects())
			{
				if(mobEffect.getEffect() instanceof FlansMobEffect flansMobEffect)
				{
					TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(flansMobEffect);
					RenderSystem.setShaderTexture(0, sprite.atlasLocation());
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
					RenderSprite(graphics, anchorX + 100 + xOffset, anchorY - 49, 18, 18, sprite);



					String stacksString = Integer.toString(mobEffect.getAmplifier() + 1);
					int stacksStringWidth = Minecraft.getInstance().font.width(stacksString);
					RenderString(graphics, anchorX + 118 + xOffset - stacksStringWidth, anchorY - 49, Component.literal(stacksString), 0xffffff);

					String timeRemaining = ".".repeat(Math.max(0, Maths.min(mobEffect.getDuration() / 20, 5)));
					RenderString(graphics, anchorX + 102 + xOffset, anchorY - 39, Component.literal(timeRemaining), 0xffffff);


					xOffset += 20;
				}
			}

			if(mainHandPrimaryContext.Def.twoHanded && !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty())
			{
				RenderString(graphics, anchorX + 96, anchorY - 39, Component.translatable("tooltip.dual_wielding_two_handed"), 0xb0b0b0);
			}
			else if(mainContext.GetAllModeDefs().length > 0)
			{
				for (int i = 0; i < mainContext.GetAllModeDefs().length; i++)
				{
					ModeDefinition modeDef = mainContext.GetAllModeDefs()[i];
					String value = mainContext.GetModeValue(modeDef.key);
					RenderString(graphics, anchorX + 96, anchorY - 39 - 10 * i, Component.translatable("tooltip.mode."+modeDef.key+"."+value), 0xb0b0b0);
				}
			}

			// TODO: If alternate ammo?
		}

		if(gunContexts[1].IsValid())
		{
			GunItem item = (GunItem)offContext.Stack.getItem();
			int a = 0x80808080;
			int b = 0xffffff;
			int c = 0x3f808080;
			Vector4f lockCol = new Vector4f(128f/255f,128f/255f,128f/255f,128f/255f);
			if(item.lockTime > 0){
				a = 0x80fec710;
				b = 0xfffec710;
				c = 0x80fec710;
				lockCol = new Vector4f(254f/255f,199f/255f,16f/255f,1);
			}
			if(item.lockTime > item.lockTimeMax){
				a = 0x80fe1010;
				b = 0xfffe1010;
				c = 0x80fe1010;
				lockCol = new Vector4f(254f/255f,16f/255f,16f/255f,1);
			}

			ProjectileDefinition def = item.GetChamberProjectile(offContext.Stack,mainContext);

			if(def != null){
				if(def.HasLockOn()){
					int i = MinecraftHelpers.GetClient().getWindow().getGuiScaledWidth();
					int j = MinecraftHelpers.GetClient().getWindow().getGuiScaledHeight();
					double size = ((def.lockCone/ Minecraft.getInstance().options.fov().get())*i)/2d;
					RenderUntexturedCircle(i*0.5f, j*0.5f ,
							32, (float)size,

							(float)size-1, c);

					if(item.LockedOnTarget != null){
						Vector3f view = new Vector3f(offContext.GetShooter().Entity().getLookAngle().toVector3f());
						Transform t = Transform.fromPos(item.LockedOnTarget.position());
						float rot = Minecraft.getInstance().cameraEntity.getYRot();
						t = t.rotateYaw(rot);
						t = t.rotatePitch(-Minecraft.getInstance().cameraEntity.getXRot());
						t = t.rotateRoll(45f*Math.min(item.lockTime/item.lockTimeMax,1f));

						Vector3f bounds = new Vector3f((float) item.LockedOnTarget.getBoundingBox().getSize(), (float) item.LockedOnTarget.getBoundingBox().getSize(),0);
						t = t.translated(new Vec3(0,item.LockedOnTarget.getBoundingBox().getSize()/2,0));
						DebugRenderer.renderCube(t,1,lockCol,bounds.mul(2f-Math.min(item.lockTime/item.lockTimeMax,1f)));
					}
				}
			}

			RenderSystem.enableBlend();
			RenderUntexturedQuad(anchorX - 94 - 308, anchorY - 20, 300, 18, a);

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
									RenderItem(graphics, bulletStacks[stackIndex], x, y, false);
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
						RenderItem(graphics, bulletStack, x, y, true);
						x -= 16;
					}
				}
			}

			RenderString(graphics,
				anchorX - 98 - Minecraft.getInstance().font.width(offContext.GetItemStack().getHoverName()),
				anchorY - 31,
				offContext.GetItemStack().getHoverName(),
				b);

			if(offHandPrimaryContext.Def.twoHanded && !player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
			{
				Component warningString = Component.translatable("tooltip.dual_wielding_two_handed");
				RenderString(graphics, anchorX - 98 - Minecraft.getInstance().font.width(warningString), anchorY - 39, warningString, 0xb0b0b0);
			}
		}
	}


	private void RenderKillMessageOverlay()
	{

	}

	private void RenderTeamInfoOverlay()
	{

	}

	private void RenderItem(@Nonnull GuiGraphics graphics, @Nonnull ItemStack stack, float x, float y, boolean decorate)
	{
		graphics.renderItem(stack, Maths.floor(x), Maths.floor(y));
		if(decorate)
			graphics.renderItemDecorations(Minecraft.getInstance().font, stack, Maths.floor(x), Maths.floor(y));
	}

	private void RenderSprite(@Nonnull GuiGraphics graphics, float x, float y, float w, float h, @Nonnull TextureAtlasSprite sprite)
	{
		float u0 = sprite.getU0();
		float u1 = sprite.getU1();
		float v0 = sprite.getV0();
		float v1 = sprite.getV1();
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
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		builder.vertex(x, y + h, -90f)		.color(colour)	.endVertex();
		builder.vertex(x + w, y + h, -90f)	.color(colour)	.endVertex();
		builder.vertex(x + w, y, -90f)		.color(colour)	.endVertex();
		builder.vertex(x, y, -90f)			.color(colour)	.endVertex();
		tesselator.end();
	}

	private void RenderUntexturedCircle(float x, float y, int sides, float outerRadius, float innerRadius, int colour)
	{
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder builder = tesselator.getBuilder();
		builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

		ArrayList<Vec2>  pointsList = new ArrayList<Vec2> ();
		ArrayList<Vec2>  outerPoints = GetCircumferencePoints(sides,outerRadius);
		pointsList.addAll(outerPoints);
		ArrayList<Vec2>  innerPoints = GetCircumferencePoints(sides,innerRadius);
		pointsList.addAll(innerPoints);

		//DrawHollowTriangles(pointsList);

		int sides2 = pointsList.size()/2;
		//int triangleAmount = sides*2;

		for(int i = 0; i < sides2;i++){
			Vec2 p = pointsList.get(i);
			//p.add(new Vec2(x,y));
			//pointsList.set(i,p);
		}

		for(int i = 0; i<sides2;i++)
		{
			int outerIndex = i;
			int innerIndex = i+sides;

			//first triangle starting at outer edge i
			//newTriangles.Add(outerIndex);
			//newTriangles.Add(innerIndex);
			//newTriangles.Add((i+1)%sides);

			builder.vertex(pointsList.get(outerIndex).x+x, pointsList.get(outerIndex).y+y, -90f)		.color(colour)	.endVertex();
			builder.vertex(pointsList.get(innerIndex).x+x, pointsList.get(innerIndex).y+y, -90f)	.color(colour)	.endVertex();
			builder.vertex(pointsList.get((i+1)%sides).x+x, pointsList.get((i+1)%sides).y+y, -90f)		.color(colour)	.endVertex();

			//second triangle starting at outer edge i
			//newTriangles.Add(outerIndex);
			//newTriangles.Add(sides+((sides+i-1)%sides));
			//newTriangles.Add(outerIndex+sides);

			builder.vertex(pointsList.get(outerIndex).x+x, pointsList.get(outerIndex).y+y, -90f)		.color(colour)	.endVertex();
			builder.vertex(pointsList.get(sides+((sides+i-1)%sides)).x+x, pointsList.get(sides+((sides+i-1)%sides)).y+y, -90f)	.color(colour)	.endVertex();
			builder.vertex(pointsList.get(outerIndex+sides).x+x, pointsList.get(outerIndex+sides).y+y, -90f)		.color(colour)	.endVertex();

		}

		tesselator.end();
	}

	ArrayList<Vec2> GetCircumferencePoints(int sides, float radius)
	{
		ArrayList<Vec2> points = new ArrayList<Vec2>();
		float circumferenceProgressPerStep = (float)1/sides;
		float TAU = (float) (2*Math.PI);
		float radianProgressPerStep = circumferenceProgressPerStep*TAU;

		for(int i = 0; i<sides; i++)
		{
			float currentRadian = radianProgressPerStep*i;
			points.add(new Vec2((float)Math.cos(currentRadian)*radius, (float)Math.sin(currentRadian)*radius));
		}
		return points;
	}

	private void RenderString(@Nonnull GuiGraphics graphics, float x, float y, @Nonnull Component content, int colour)
	{
		graphics.drawString(Minecraft.getInstance().font, content, Maths.floor(x), Maths.floor(y), colour);
	}
}
