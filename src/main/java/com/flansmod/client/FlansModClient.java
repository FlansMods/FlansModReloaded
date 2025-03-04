package com.flansmod.client;

import com.flansmod.client.gui.crafting.*;
import com.flansmod.client.gui.turret.TurretScreen;
import com.flansmod.client.render.ClientRenderHooks;
import com.flansmod.client.input.ClientInputHooks;
import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.client.render.bullets.*;
import com.flansmod.client.render.effects.EffectRenderer;
import com.flansmod.client.render.effects.FlashEffectRenderer;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.MagazineTextureAtlas;
import com.flansmod.client.render.animation.FlanimationDefinitions;
import com.flansmod.client.render.effects.DecalRenderer;
import com.flansmod.client.render.effects.LaserRenderer;
import com.flansmod.client.render.vehicles.VehicleDebugRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.actions.nodes.SpawnParticleAction;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.guns.elements.ESpreadPattern;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
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
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = FlansMod.MODID)
public class FlansModClient
{
	public static final ClientInventoryManager INVENTORY_MANAGER = new ClientInventoryManager();
	public static final VehicleDebugRenderer VEHICLE_DEBUG_RENDERER = new VehicleDebugRenderer();

	public static final ShotRenderer SHOT_RENDERER= new ShotRenderer();
	public static final LaserRenderer LASER_RENDERER = new LaserRenderer();
	public static final FlashEffectRenderer FLASH_RENDERER = new FlashEffectRenderer();
	public static final CasingEntityItemRenderer CASING_RENDERER = new CasingEntityItemRenderer();
	public static final ClientInputHooks CLIENT_INPUT_HOOKS = new ClientInputHooks();
	public static final ClientRenderHooks CLIENT_OVERLAY_HOOKS = new ClientRenderHooks();
	public static final FlansModelRegistry MODEL_REGISTRATION = new FlansModelRegistry();
	public static final FlanimationDefinitions ANIMATIONS = new FlanimationDefinitions();
	public static final DecalRenderer DECAL_RENDERER = new DecalRenderer();
	public static final MagazineTextureAtlas MAGAZINE_ATLAS = new MagazineTextureAtlas();
	public static final RecoilManager RECOIL = new RecoilManager();

	public static final ClientActionManager ACTIONS_CLIENT = new ClientActionManager();
	public static final ContextCache CONTEXT_CACHE = new ClientContextCache();
	public static final ClientLongDistanceEntitySystem CLIENT_LONG_DISTANCE = new ClientLongDistanceEntitySystem();


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
		ResourceLocation smallMuzzleFlash = new ResourceLocation(FlansMod.MODID, "effects/muzzle_flash_small");
		ResourceLocation mediumMuzzleFlash = new ResourceLocation(FlansMod.MODID, "effects/muzzle_flash_medium");
		ResourceLocation largeMuzzleFlash = new ResourceLocation(FlansMod.MODID, "effects/muzzle_flash_large");
		FlansModelRegistry.PreRegisterRenderer(smallMuzzleFlash, new EffectRenderer(smallMuzzleFlash));
		FlansModelRegistry.PreRegisterRenderer(mediumMuzzleFlash, new EffectRenderer(mediumMuzzleFlash));
		FlansModelRegistry.PreRegisterRenderer(largeMuzzleFlash, new EffectRenderer(largeMuzzleFlash));

		ResourceLocation rifleCasing = new ResourceLocation(FlansMod.MODID, "effects/casing_rifle");
		FlansModelRegistry.PreRegisterRenderer(rifleCasing, new CasingRenderer(rifleCasing));
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
		MenuScreens.register(FlansMod.WORKBENCH_MENU_PAINTING.get(), WorkbenchScreenTabPainting::new);
		MenuScreens.register(FlansMod.WORKBENCH_MENU_PART_CRAFTING.get(), WorkbenchScreenTabPartCrafting::new);
		MenuScreens.register(FlansMod.WORKBENCH_MENU_POWER.get(), WorkbenchScreenTabPower::new);
		MenuScreens.register(FlansMod.WORKBENCH_MENU_MATERIALS.get(), WorkbenchScreenTabMaterials::new);
		MenuScreens.register(FlansMod.TURRET_MENU.get(), TurretScreen::new);

		// Entity Renderers
		EntityRenderers.register(FlansMod.ENT_TYPE_BULLET.get(), BulletEntityRenderer::new);
		EntityRenderers.register(FlansMod.ENT_TYPE_CASING.get(), CasingEntityRenderer::new);

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
						Maths.lerpF((float)ITEM_IN_HAND_RENDERER_O_MAIN_HAND_HEIGHT.get(iihr),
									(float)ITEM_IN_HAND_RENDERER_MAIN_HAND_HEIGHT.get(iihr),
								    dt);
				}
				case OFF_HAND -> {
					return
						Maths.lerpF((float)ITEM_IN_HAND_RENDERER_O_OFF_HAND_HEIGHT.get(iihr),
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
						transformType = MinecraftHelpers.getThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
					}
					else
					{
						transformType = MinecraftHelpers.getFirstPersonTransformType(playerGunContext.GetHand());
					}
					Transform shootOrigin = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath("shoot_origin"));

					Vec3 look = playerGunContext.GetShooter().Entity().getLookAngle();
					if(count > 1)
						Minecraft.getInstance().level.addParticle(ParticleTypes.POOF, shootOrigin.positionVec3().x() + look.x * 0.1f, shootOrigin.positionVec3().y() + look.y * 0.1f, shootOrigin.positionVec3().z() + look.z * 0.1f, (look.x() * 0.3) + random( count), (look.y() * 0.3) + random( count), (look.z() * 0.3) + random( count));
					else {
						Minecraft.getInstance().level.addParticle(ParticleTypes.SMOKE, shootOrigin.positionVec3().x() + look.x * 0.1f, shootOrigin.positionVec3().y() + look.y * 0.1f, shootOrigin.positionVec3().z() + look.z * 0.1f, (look.x() * 0.3) + random( count), (look.y() * 0.3) + random( count), (look.z() * 0.3) + random( count));
					}
					if (i == 1) {
						Minecraft.getInstance().level.addParticle(ParticleTypes.FLAME, shootOrigin.positionVec3().x(), shootOrigin.positionVec3().y(), shootOrigin.positionVec3().z(), look.x, look.y, look.z);
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
						transformType = MinecraftHelpers.getThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
					}
					else
					{
						transformType = MinecraftHelpers.getFirstPersonTransformType(playerGunContext.GetHand());
					}

				}
				Transform laserOrigin = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath("shoot_origin"));
				origin = laserOrigin.positionVec3();
				Vec3 look = gunshotContext.ActionGroup.Gun.GetShooter().GetShootOrigin(0f).forward();
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

	public static void SpawnLocalParticles(SpawnParticleAction action){
		if(Minecraft.getInstance().player != null && action.Group.Context.Gun instanceof GunContextPlayer playerGunContext) {
			GunContext gunContext = action.Group.Context.Gun;

			ItemDisplayContext transformType;
			if(!Minecraft.getInstance().options.getCameraType().isFirstPerson() || !gunContext.GetShooter().IsLocalPlayerOwner())
			{
				transformType = MinecraftHelpers.getThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
			}
			else
			{
				transformType = MinecraftHelpers.getFirstPersonTransformType(playerGunContext.GetHand());
			}

			Transform shootOrigin = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath(action.AttachPoint()));
			ParticleOptions particle = (ParticleOptions) ForgeRegistries.PARTICLE_TYPES.getValue(action.ParticleType());

			for (int i = 0; i < action.ParticleCount(); i++) {
				if (action.Group.Context.Gun.GetShooter() != ShooterContext.INVALID) {

					RandomSource rand = action.Group.Context.Gun.GetShooter().Level().random;

					TransformStack transformStack = TransformStack.empty();
					transformStack.add(shootOrigin);
					RandomizeVectorDirection(
							transformStack,
							rand,
							action.ParticleSpread(),
							action.SpreadPattern());

					Transform randomizedDirection = transformStack.top();


					Vec3 position = randomizedDirection.positionVec3();
					Vec3 look = randomizedDirection.forward();

					float speed = (float) ((action.ParticleSpeed() - action.ParticleSpeedDispersion()) + (rand.nextFloat()*(action.ParticleSpeedDispersion()*2)));

					if(particle != null) {
                        assert Minecraft.getInstance().level != null;
                        Minecraft.getInstance().level.addParticle(particle, position.x(), position.y(), position.z(), look.x()*speed, look.y()*speed, look.z()*speed);
                    }
				}
			}
		}
	}

	public static void SpawnParticles(SpawnParticleAction action){
		int count = action.ParticleCount();
		//Transform shootOrigin = FirstPersonManager.GetWorldSpaceAPTransform(gunshotContext.ActionGroup.Gun, MinecraftHelpers.GetFirstPersonTransformType(playerGunContext.GetHand()), "shoot_origin");
		for (int i = 0; i < count; i++) {
			if (action.Group.Context.Gun.GetShooter() != ShooterContext.INVALID) {

				GunContext gunContext = action.Group.Context.Gun;
				ItemDisplayContext transformType = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

				if(gunContext instanceof GunContextPlayer playerGunContext)
				{
					if(!Minecraft.getInstance().options.getCameraType().isFirstPerson() || !gunContext.GetShooter().IsLocalPlayerOwner())
					{
						transformType = MinecraftHelpers.getThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
					}
					else
					{
						transformType = MinecraftHelpers.getFirstPersonTransformType(playerGunContext.GetHand());
					}

				}
				Transform shootOrigin = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath(action.AttachPoint()));


				Vec3 direction = shootOrigin.forward();

				float spread = action.ParticleSpread();

				ParticleOptions particle = (ParticleOptions) ForgeRegistries.PARTICLE_TYPES.getValue(action.ParticleType());


				//TO-DO, particle patterns
				if(particle != null)
					Minecraft.getInstance().level.addParticle(particle, shootOrigin.positionVec3().x() + direction.x * spread==0?0:0.1f, shootOrigin.positionVec3().y() + direction.y * spread==0?0:0.1f, shootOrigin.positionVec3().z() + direction.z * spread==0?0:0.1f, (direction.x() * 0.3) + random(spread), (direction.y() * 0.3) + random(spread), (direction.z() * 0.3) + random(spread));
			}
		}
	}

	//TO-DO: Move all this into some particle-specific class
	private static float random(float strength){
		return (((float)Math.random() * 0.6f)-0.3f)*strength/20f;
	}

	private static void RandomizeVectorDirection(@Nonnull TransformStack transformStack,
										  @Nonnull RandomSource rand,
										  float spread,
										  @Nonnull ESpreadPattern spreadPattern)
	{
		float xComponent;
		float yComponent;

		switch (spreadPattern)
		{
			case Circle, FilledCircle ->
			{
				float theta = rand.nextFloat() * Maths.TauF;
				float radius = (spreadPattern == ESpreadPattern.Circle ? 1.0f : rand.nextFloat()) * spread;
				xComponent = radius * Maths.sinF(theta);
				yComponent = radius * Maths.cosF(theta);
			}
			case Horizontal ->
			{
				xComponent = spread * (rand.nextFloat() * 2f - 1f);
				yComponent = 0.0f;
			}
			case Vertical ->
			{
				xComponent = 0.0f;
				yComponent = spread * (rand.nextFloat() * 2f - 1f);
			}
			case Triangle ->
			{
				// Random square, then fold the corners
				xComponent = rand.nextFloat() * 2f - 1f;
				yComponent = rand.nextFloat() * 2f - 1f;

				if (xComponent > 0f)
				{
					if (yComponent > 1.0f - xComponent * 2f)
					{
						yComponent = -yComponent;
						xComponent = 1f - xComponent;
					}
				} else
				{
					if (yComponent > xComponent * 2f + 1f)
					{
						yComponent = -yComponent;
						xComponent = -1f - xComponent;
					}
				}
			}
			default -> {
				xComponent = 0.0f;
				yComponent = 0.0f;
			}
		}

		float yaw = xComponent;
		float pitch = yComponent;

		transformStack.add(Transform.fromEuler(pitch, yaw, 0f));
	}

}
