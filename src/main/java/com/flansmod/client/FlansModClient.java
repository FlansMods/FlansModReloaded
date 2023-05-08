package com.flansmod.client;

import com.flansmod.client.input.ClientInputHooks;
import com.flansmod.client.render.FlanModelRegistration;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.client.render.guns.ShotRenderer;
import com.flansmod.client.render.models.TurboModel;
import com.flansmod.common.FlansMod;
import com.flansmod.common.item.FlanItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = FlansMod.MODID)
public class FlansModClient
{
	public static ShotRenderer SHOT_RENDERER;
	public static DebugRenderer DEBUG_RENDERER;
	public static ClientInputHooks CLIENT_INPUT_HOOKS;
	public static FlanModelRegistration MODEL_REGISTRATION = new FlanModelRegistration();

	@SubscribeEvent
	public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event)
	{
		event.register("turbomodel", TurboModel.LOADER);
	}

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

	private void onReloadResources(AddReloadListenerEvent event)
	{
		event.addListener(MODEL_REGISTRATION);
	}

	public static void Init()
	{
		SHOT_RENDERER = new ShotRenderer();
		DEBUG_RENDERER = new DebugRenderer();
		CLIENT_INPUT_HOOKS = new ClientInputHooks();

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		MODEL_REGISTRATION.hook(modEventBus);
	}
}
