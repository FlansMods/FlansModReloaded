package com.flansmod.client;

import com.flansmod.client.gui.crafting.WorkbenchScreen;
import com.flansmod.client.render.ClientRenderHooks;
import com.flansmod.client.input.ClientInputHooks;
import com.flansmod.client.render.FlanModelRegistration;
import com.flansmod.client.render.animation.AnimationDefinitions;
import com.flansmod.client.render.debug.DebugModelPoser;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.client.render.decals.DecalRenderer;
import com.flansmod.client.render.guns.ShotRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.ActionManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.io.IOException;


@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = FlansMod.MODID)
public class FlansModClient
{
	public static ShotRenderer SHOT_RENDERER= new ShotRenderer();
	public static DebugRenderer DEBUG_RENDERER = new DebugRenderer();
	public static ClientInputHooks CLIENT_INPUT_HOOKS = new ClientInputHooks();
	public static ClientRenderHooks CLIENT_OVERLAY_HOOKS = new ClientRenderHooks();
	public static FlanModelRegistration MODEL_REGISTRATION = new FlanModelRegistration();
	public static AnimationDefinitions ANIMATIONS = new AnimationDefinitions();
	public static DecalRenderer DECAL_RENDERER = new DecalRenderer();
	public static ActionManager ACTIONS_CLIENT = new ActionManager(true);

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

		// Screens
		MenuScreens.register(FlansMod.WORKBENCH_MENU.get(), WorkbenchScreen::new);
	}

	public static void RegisterClientReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(ANIMATIONS);
	}
}
