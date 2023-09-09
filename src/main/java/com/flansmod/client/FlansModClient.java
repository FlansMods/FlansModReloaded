package com.flansmod.client;

import com.flansmod.client.gui.crafting.WorkbenchScreen;
import com.flansmod.client.render.ClientRenderHooks;
import com.flansmod.client.input.ClientInputHooks;
import com.flansmod.client.render.FlanModelRegistration;
import com.flansmod.client.render.MagazineTextureAtlas;
import com.flansmod.client.render.animation.AnimationDefinitions;
import com.flansmod.client.render.bullets.BulletEntityRenderer;
import com.flansmod.client.render.debug.DebugModelPoser;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.client.render.decals.DecalRenderer;
import com.flansmod.client.render.bullets.ShotRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.ActionManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import cpw.mods.modlauncher.api.INameMappingService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;


@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = FlansMod.MODID)
public class FlansModClient
{
	public static final ShotRenderer SHOT_RENDERER= new ShotRenderer();
	public static final DebugRenderer DEBUG_RENDERER = new DebugRenderer();
	public static final ClientInputHooks CLIENT_INPUT_HOOKS = new ClientInputHooks();
	public static final ClientRenderHooks CLIENT_OVERLAY_HOOKS = new ClientRenderHooks();
	public static final FlanModelRegistration MODEL_REGISTRATION = new FlanModelRegistration();
	public static final AnimationDefinitions ANIMATIONS = new AnimationDefinitions();
	public static final DecalRenderer DECAL_RENDERER = new DecalRenderer();
	public static final ActionManager ACTIONS_CLIENT = new ActionManager(true);
	public static final MagazineTextureAtlas MAGAZINE_ATLAS = new MagazineTextureAtlas();
	public static final RecoilManager RECOIL = new RecoilManager();

	@Nullable
	private static ShaderInstance GUN_CUTOUT;

	@SubscribeEvent
	public static void OnRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) { MODEL_REGISTRATION.OnRegisterGeometryLoaders(event); }

	@SubscribeEvent
	public static void ModelRegistryEvent(ModelEvent.RegisterAdditional event)
	{
		ItemModelShaper shaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();

		for(var entry : FlansMod.ITEMS.getEntries())
		{
			event.register(new ModelResourceLocation(entry.getId(), "inventory"));
			shaper.register(entry.get(), new ModelResourceLocation(entry.getId(), "inventory"));
		}
	}

	public static ShaderInstance GetGunCutoutShader() { return GUN_CUTOUT; }

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

	@SubscribeEvent
	public static void ShaderRegistryEvent(RegisterShadersEvent event)
	{
		try
		{
			event.registerShader(new ShaderInstance(event.getResourceProvider(), "flansmod:rendertype_flans_gun_cutout", DefaultVertexFormat.BLOCK), (shader) -> {
				GUN_CUTOUT = shader;
			});
		}
		catch(Exception ignored)
		{

		}
	}

	public static void Init()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ACTIONS_CLIENT.HookClient(modEventBus);
		new DebugModelPoser().Init();
		MODEL_REGISTRATION.hook(modEventBus);
		modEventBus.register(ANIMATIONS);
		MAGAZINE_ATLAS.Init();
		InitReflection();

		// Screens
		MenuScreens.register(FlansMod.WORKBENCH_MENU.get(), WorkbenchScreen::new);

		// Entity Renderers
		EntityRenderers.register(FlansMod.ENT_TYPE_BULLET.get(), BulletEntityRenderer::new);
	}

	@SubscribeEvent
	public static void RegisterClientReloadListeners(RegisterClientReloadListenersEvent event)
	{
		event.registerReloadListener(MAGAZINE_ATLAS);
	}

	public static void RegisterClientDataReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(ANIMATIONS);
	}


	// ---------------------------
	// REFLECTION
	// ---------------------------
	private static final Field MINECRAFT_MISS_TIME = ObfuscationReflectionHelper.findField(Minecraft.class, "f_91078_");
	private static void InitReflection()
	{
		MINECRAFT_MISS_TIME.setAccessible(true);
	}

}
