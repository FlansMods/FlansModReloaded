package com.flansmod.client.input;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.item.GunItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

public class ClientInputHooks
{
	public static final Lazy<KeyMapping> LOOK_AT_MAPPING = Lazy.of(() -> { return new KeyMapping(
		"key.flansmod.look_at",
		KeyConflictContext.IN_GAME,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_L,
		"key.categories.flansmod"); });

	public ClientInputHooks()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::OnKeyMappings);

		MinecraftForge.EVENT_BUS.addListener(this::OnClickInput);
		MinecraftForge.EVENT_BUS.addListener(this::OnClientTick);
	}

	public void OnKeyMappings(RegisterKeyMappingsEvent event)
	{
		event.register(LOOK_AT_MAPPING.get());
	}

	public void OnClickInput(InputEvent.InteractionKeyMappingTriggered event)
	{
		Player player = Minecraft.getInstance().player;

		ItemStack stack = player.getItemInHand(event.getHand());
		if(stack.getItem() instanceof GunItem gun)
		{
			gun.ClientHandleMouse(player, stack, event);
		}
	}

	public void OnClientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase == TickEvent.Phase.END)
		{
			while(LOOK_AT_MAPPING.get().consumeClick())
			{
				Player player = Minecraft.getInstance().player;
				ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
				if(stack.getItem() instanceof GunItem gun)
				{
					FlansModClient.GUNSHOTS_CLIENT.ClientLookAt(player, InteractionHand.MAIN_HAND);
				}

				stack = player.getItemInHand(InteractionHand.OFF_HAND);
				if(stack.getItem() instanceof GunItem gun)
				{
					FlansModClient.GUNSHOTS_CLIENT.ClientLookAt(player, InteractionHand.OFF_HAND);
				}
			}
		}
	}
}
