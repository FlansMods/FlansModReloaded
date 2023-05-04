package com.flansmod.client;

import com.flansmod.client.input.ClientInputHooks;
import com.flansmod.client.render.FlanModelRegistration;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.client.render.guns.ShotRenderer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class FlansModClient
{
	public static ShotRenderer SHOT_RENDERER;
	public static DebugRenderer DEBUG_RENDERER;
	public static ClientInputHooks CLIENT_INPUT_HOOKS;

	public static void Init()
	{
		SHOT_RENDERER = new ShotRenderer();
		DEBUG_RENDERER = new DebugRenderer();
		CLIENT_INPUT_HOOKS = new ClientInputHooks();

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		new FlanModelRegistration().hook(modEventBus);
	}
}
