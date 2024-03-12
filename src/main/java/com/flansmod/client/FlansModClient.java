package com.flansmod.client;

import com.flansmod.client.gui.crafting.*;
import com.flansmod.client.render.ClientRenderHooks;
import com.flansmod.client.input.ClientInputHooks;
import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.client.render.effects.EffectRenderer;
import com.flansmod.client.render.effects.FlashEffectRenderer;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.MagazineTextureAtlas;
import com.flansmod.client.render.animation.FlanimationDefinitions;
import com.flansmod.client.render.bullets.BulletEntityRenderer;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.client.render.effects.DecalRenderer;
import com.flansmod.client.render.bullets.ShotRenderer;
import com.flansmod.client.render.effects.LaserRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = FlansMod.MODID)
public class FlansModClient
{
	public static final ShotRenderer SHOT_RENDERER= new ShotRenderer();
	public static final LaserRenderer LASER_RENDERER = new LaserRenderer();
	public static final FlashEffectRenderer FLASH_RENDERER = new FlashEffectRenderer();
	public static final DebugRenderer DEBUG_RENDERER = new DebugRenderer();
	public static final ClientInputHooks CLIENT_INPUT_HOOKS = new ClientInputHooks();
	public static final ClientRenderHooks CLIENT_OVERLAY_HOOKS = new ClientRenderHooks();
	public static final FlansModelRegistry MODEL_REGISTRATION = new FlansModelRegistry();
	public static final FlanimationDefinitions ANIMATIONS = new FlanimationDefinitions();
	public static final DecalRenderer DECAL_RENDERER = new DecalRenderer();
	public static final MagazineTextureAtlas MAGAZINE_ATLAS = new MagazineTextureAtlas();
	public static final RecoilManager RECOIL = new RecoilManager();

	public static final ClientActionManager ACTIONS_CLIENT = new ClientActionManager();
	public static final ContextCache CONTEXT_CACHE = new ClientContextCache();


	public static long PREV_FRAME_NS = 0L;
	public static long THIS_FRAME_NS = 0L;
	public static float FrameDeltaSeconds() { return (THIS_FRAME_NS - PREV_FRAME_NS) / 1000000000f; }
	public static float FrameAbsoluteSeconds() { return THIS_FRAME_NS / 1000000000f; }

	@Nullable
	private static ShaderInstance GUN_SOLID;
	@Nullable
	private static ShaderInstance GUN_CUTOUT;
	@Nullable
	private static ShaderInstance GUN_EMISSIVE;
	@Nullable
	private static ShaderInstance GUN_TRANSPARENT;


	static
	{
		FlansModelRegistry.PreRegisterRenderer(new ResourceLocation(FlansMod.MODID, "effects/muzzle_flash_small"), new EffectRenderer());
		FlansModelRegistry.PreRegisterRenderer(new ResourceLocation(FlansMod.MODID, "effects/muzzle_flash_medium"), new EffectRenderer());
		FlansModelRegistry.PreRegisterRenderer(new ResourceLocation(FlansMod.MODID, "effects/muzzle_flash_large"), new EffectRenderer());
	}

	@SubscribeEvent
	public static void ClientInit(final FMLClientSetupEvent event)
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ACTIONS_CLIENT.HookClient(modEventBus);
		MODEL_REGISTRATION.Hook(modEventBus);
		modEventBus.register(ANIMATIONS);
		MAGAZINE_ATLAS.Init();
		InitReflection();

		// Screens
		MenuScreens.register(FlansMod.WORKBENCH_MENU_GUN_CRAFTING.get(), WorkbenchScreenTabGunCrafting::new);
		MenuScreens.register(FlansMod.WORKBENCH_MENU_MODIFICATION.get(), WorkbenchScreenTabModification::new);
		MenuScreens.register(FlansMod.WORKBENCH_MENU_PART_CRAFTING.get(), WorkbenchScreenTabPartCrafting::new);
		MenuScreens.register(FlansMod.WORKBENCH_MENU_POWER.get(), WorkbenchScreenTabPower::new);
		MenuScreens.register(FlansMod.WORKBENCH_MENU_MATERIALS.get(), WorkbenchScreenTabMaterials::new);

		// Entity Renderers
		EntityRenderers.register(FlansMod.ENT_TYPE_BULLET.get(), BulletEntityRenderer::new);

		MinecraftForge.EVENT_BUS.addListener(FlansModClient::RenderTick);
		MinecraftForge.EVENT_BUS.addListener(FlansModClient::OnLevelLoad);
	}

	public static void OnLevelLoad(LevelEvent.Load event)
	{
		if(event.getLevel().isClientSide())
			new Raytracer(event.getLevel()).hook();
	}
	public static void OnLevelUnload(LevelEvent.Unload event)
	{
		if(event.getLevel().isClientSide())
			CONTEXT_CACHE.OnLevelUnloaded(ACTIONS_CLIENT);
	}

	@SubscribeEvent
	public static void OnRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) { MODEL_REGISTRATION.OnRegisterGeometryLoaders(event); }

	@SubscribeEvent
	public static void ModelRegistryEvent(ModelEvent.RegisterAdditional event)
	{
		ItemModelShaper shaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();

		for (var entry : FlansMod.ITEMS.getEntries())
		{
			event.register(new ModelResourceLocation(entry.getId(), "inventory"));
			shaper.register(entry.get(), new ModelResourceLocation(entry.getId(), "inventory"));
		}

		for (ResourceLocation gunLoc : FlansMod.GUNS.getIds())
		{
			
		}
	}

	@Nullable
	public static ShaderInstance GetGunSolidShader() { return GUN_SOLID; }
	@Nullable
	public static ShaderInstance GetGunCutoutShader() { return GUN_CUTOUT; }
	@Nullable
	public static ShaderInstance GetGunEmissiveShader() { return GUN_EMISSIVE; }
	@Nullable
	public static ShaderInstance GetGunTransparentShader() { return GUN_TRANSPARENT; }

	@SubscribeEvent
	public static void ShaderRegistryEvent(RegisterShadersEvent event)
	{
		try
		{
			event.registerShader(new ShaderInstance(event.getResourceProvider(), "flansmod:rendertype_flans_gun_solid", DefaultVertexFormat.BLOCK), (shader) -> {
				GUN_SOLID = shader;
			});
			event.registerShader(new ShaderInstance(event.getResourceProvider(), "flansmod:rendertype_flans_gun_cutout", DefaultVertexFormat.BLOCK), (shader) -> {
				GUN_CUTOUT = shader;
			});
			event.registerShader(new ShaderInstance(event.getResourceProvider(), "flansmod:rendertype_flans_gun_emissive", DefaultVertexFormat.BLOCK), (shader) -> {
				GUN_EMISSIVE = shader;
			});
			event.registerShader(new ShaderInstance(event.getResourceProvider(), "flansmod:rendertype_flans_gun_transparent", DefaultVertexFormat.BLOCK), (shader) -> {
				GUN_TRANSPARENT = shader;
			});
		}
		catch(Exception ignored)
		{

		}
	}

	public static void RenderTick(TickEvent.RenderTickEvent event)
	{
		PREV_FRAME_NS = THIS_FRAME_NS;
		THIS_FRAME_NS = Util.getNanos();

		FirstPersonManager.RenderTick();
	}

	@SubscribeEvent
	public static void RegisterClientReloadListeners(RegisterClientReloadListenersEvent event)
	{
		event.registerReloadListener(MAGAZINE_ATLAS);
		event.registerReloadListener(ANIMATIONS);

		FlansMod.RegisterCommonReloadListeners(event::registerReloadListener);
	}

	// ---------------------------
	// REFLECTION
	// ---------------------------
	private static final Field MINECRAFT_MISS_TIME = ObfuscationReflectionHelper.findField(Minecraft.class, "f_91078_");
	private static final Field ITEM_IN_HAND_RENDERER_MAIN_HAND_HEIGHT = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109302_");
	private static final Field ITEM_IN_HAND_RENDERER_O_MAIN_HAND_HEIGHT = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109303_");
	private static final Field ITEM_IN_HAND_RENDERER_OFF_HAND_HEIGHT = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109304_");
	private static final Field ITEM_IN_HAND_RENDERER_O_OFF_HAND_HEIGHT = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109305_");
	private static final Method GET_FOV = ObfuscationReflectionHelper.findMethod(GameRenderer.class, "m_109141_", Camera.class, Float.TYPE, Boolean.TYPE);
	private static void InitReflection()
	{
		MINECRAFT_MISS_TIME.setAccessible(true);
		ITEM_IN_HAND_RENDERER_MAIN_HAND_HEIGHT.setAccessible(true);
		ITEM_IN_HAND_RENDERER_O_MAIN_HAND_HEIGHT.setAccessible(true);
		ITEM_IN_HAND_RENDERER_OFF_HAND_HEIGHT.setAccessible(true);
		ITEM_IN_HAND_RENDERER_O_OFF_HAND_HEIGHT.setAccessible(true);
		GET_FOV.setAccessible(true);
	}
	public static double GetFOV(Camera camera, float dt, boolean applyFOVSetting)
	{
		try
		{
			return (double) GET_FOV.invoke(Minecraft.getInstance().gameRenderer, camera, dt, applyFOVSetting);
		}
		catch(Exception e)
		{
			FlansMod.LOGGER.error("Failed to GetFOV due to " + e);
		}
		return 1.0d;
	}
	public static float GetHandHeight(InteractionHand hand, float dt)
	{
		try
		{
			ItemInHandRenderer iihr = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
			switch (hand)
			{
				case MAIN_HAND -> {
					return
						Maths.LerpF((float)ITEM_IN_HAND_RENDERER_O_MAIN_HAND_HEIGHT.get(iihr),
									(float)ITEM_IN_HAND_RENDERER_MAIN_HAND_HEIGHT.get(iihr),
								    dt);
				}
				case OFF_HAND -> {
					return
						Maths.LerpF((float)ITEM_IN_HAND_RENDERER_O_OFF_HAND_HEIGHT.get(iihr),
							(float)ITEM_IN_HAND_RENDERER_OFF_HAND_HEIGHT.get(iihr),
							dt);
				}
			}
		}
		catch(Exception e)
		{
			FlansMod.LOGGER.error("Failed to GetHandHeight due to " + e);
		}
		return 0.0f;
	}
	public static void SetMissTime(int missTime)
	{
		try
		{
			MINECRAFT_MISS_TIME.set(Minecraft.getInstance(), missTime);
		}
		catch (Exception e)
		{
			FlansMod.LOGGER.error("Failed to SetMissTime due to " + e);
		}
	}

	//Clean this up later
	public static void SpawnLocalMuzzleParticles(Vec3 origin, GunshotContext gunshotContext,int count){
		if(Minecraft.getInstance().player != null && gunshotContext.ActionGroup.Gun instanceof GunContextPlayer playerGunContext) {
			//Transform shootOrigin = FirstPersonManager.GetWorldSpaceAPTransform(gunshotContext.ActionGroup.Gun, MinecraftHelpers.GetFirstPersonTransformType(playerGunContext.GetHand()), "shoot_origin");
			for (int i = 0; i < gunshotContext.ActionGroup.Gun.Def.particleCount; i++) {
				if (playerGunContext.GetShooter() != ShooterContext.INVALID) {

					GunContext gunContext = gunshotContext.ActionGroup.Gun;
					ItemDisplayContext transformType = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
					Transform eyeOrigin = gunContext.GetShootOrigin(Minecraft.getInstance().getPartialTick());

					if(!Minecraft.getInstance().options.getCameraType().isFirstPerson() || !gunContext.GetShooter().IsLocalPlayerOwner())
					{
						transformType = MinecraftHelpers.GetThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
					}
					else
					{
						transformType = MinecraftHelpers.GetFirstPersonTransformType(playerGunContext.GetHand());
					}
					Transform shootOrigin = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath("shoot_origin"));

					Vec3 look = playerGunContext.GetShooter().Entity().getLookAngle();
					if(count > 1)
						Minecraft.getInstance().level.addParticle(ParticleTypes.POOF, shootOrigin.PositionVec3().x() + look.x * 0.1f, shootOrigin.PositionVec3().y() + look.y * 0.1f, shootOrigin.PositionVec3().z() + look.z * 0.1f, (look.x() * 0.3) + random( count), (look.y() * 0.3) + random( count), (look.z() * 0.3) + random( count));
					else {
						Minecraft.getInstance().level.addParticle(ParticleTypes.SMOKE, shootOrigin.PositionVec3().x() + look.x * 0.1f, shootOrigin.PositionVec3().y() + look.y * 0.1f, shootOrigin.PositionVec3().z() + look.z * 0.1f, (look.x() * 0.3) + random( count), (look.y() * 0.3) + random( count), (look.z() * 0.3) + random( count));
					}
					if (i == 1) {
						Minecraft.getInstance().level.addParticle(ParticleTypes.FLAME, shootOrigin.PositionVec3().x(), shootOrigin.PositionVec3().y(), shootOrigin.PositionVec3().z(), look.x, look.y, look.z);
					}
				}
			}
		}
	}

	public static void SpawnMuzzleParticles(Vec3 origin, GunshotContext gunshotContext, int count){
		for (int i = 0; i < count; i++) {
			if (gunshotContext.ActionGroup.Gun.GetShooter() != ShooterContext.INVALID) {

				GunContext gunContext = gunshotContext.ActionGroup.Gun;
				ItemDisplayContext transformType = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
				Transform eyeOrigin = gunContext.GetShootOrigin(Minecraft.getInstance().getPartialTick());
				if(gunContext instanceof GunContextPlayer playerGunContext)
				{
					if(!Minecraft.getInstance().options.getCameraType().isFirstPerson() || !gunContext.GetShooter().IsLocalPlayerOwner())
					{
						transformType = MinecraftHelpers.GetThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
					}
					else
					{
						transformType = MinecraftHelpers.GetFirstPersonTransformType(playerGunContext.GetHand());
					}

				}
				Transform laserOrigin = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath("shoot_origin"));
				origin = laserOrigin.PositionVec3();
				Vec3 look = gunshotContext.ActionGroup.Gun.GetShooter().Entity().getLookAngle();
				if(count > 1)
					Minecraft.getInstance().level.addParticle(ParticleTypes.POOF, origin.x() + look.x * 0.1f, origin.y() + look.y * 0.1f, origin.z() + look.z * 0.1f, (look.x() * 0.3) + random( count), (look.y() * 0.3) + random( count), (look.z() * 0.3) + random( count));
				else {
					Minecraft.getInstance().level.addParticle(ParticleTypes.SMOKE, origin.x() + look.x * 0.1f, origin.y() + look.y * 0.1f, origin.z() + look.z * 0.1f, (look.x() * 0.3) + random( count), (look.y() * 0.3) + random( count), (look.z() * 0.3) + random( count));
				}				if (i == 1) {
					Minecraft.getInstance().level.addParticle(ParticleTypes.FLAME, origin.x(), origin.y(), origin.z(), look.x, look.y, look.z);
				}
			}
		}
	}


	private static float random(int count){
		return (((float)Math.random() * 0.6f)-0.3f)*(float)count/20f;
	}

}
