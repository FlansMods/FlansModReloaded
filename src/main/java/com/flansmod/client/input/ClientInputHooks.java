package com.flansmod.client.input;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.vehicles.EPlayerInput;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

public class ClientInputHooks
{
	private static boolean UseHeldThisFrame = false;
	private static boolean UseHeldLastFrame = false;
	private static boolean AttackHeldThisFrame = false;
	private static boolean AttackHeldLastFrame = false;
	private static int FramesSinceUseToggled = 0;
	private static int FramesSinceAttackToggled = 0;

	public static boolean IsUsePressed() { return UseHeldThisFrame && !UseHeldLastFrame; }
	public static boolean IsUseHeld() { return UseHeldThisFrame; }
	public static boolean IsUseReleased() { return UseHeldLastFrame && !UseHeldThisFrame; }
	public static int TicksSinceUseToggled() { return FramesSinceUseToggled; }
	public static boolean IsAttackPressed() { return AttackHeldThisFrame && !AttackHeldLastFrame; }
	public static boolean IsAttackHeld() { return AttackHeldThisFrame; }
	public static boolean IsAttackReleased() { return AttackHeldLastFrame && !AttackHeldThisFrame; }
	public static int TicksSinceAttackToggled() { return FramesSinceAttackToggled; }

	public static final Lazy<KeyMapping> LOOK_AT_MAPPING = Lazy.of(() -> { return new KeyMapping(
		"key.flansmod.look_at",
		KeyConflictContext.IN_GAME,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_L,
		"key.categories.flansmod"); });
	public static final Lazy<KeyMapping> MODE_TOGGLE_MAPPING = Lazy.of(() -> { return new KeyMapping(
		"key.flansmod.mode_toggle",
		KeyConflictContext.IN_GAME,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_C,
		"key.categories.flansmod"); });

	public static final Lazy<KeyMapping> MANUAL_RELOAD_MAPPING = Lazy.of(() -> { return new KeyMapping(
		"key.flansmod.manual_reload",
		KeyConflictContext.IN_GAME,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_R,
		"key.categories.flansmod"); });

	public ClientInputHooks()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::OnKeyMappings);

		MinecraftForge.EVENT_BUS.addListener(this::OnClickInput);
		MinecraftForge.EVENT_BUS.addListener(this::OnClientTick);
		MinecraftForge.EVENT_BUS.addListener(this::OnUseItemTick);
	}

	public void OnKeyMappings(RegisterKeyMappingsEvent event)
	{
		event.register(LOOK_AT_MAPPING.get());
		event.register(MANUAL_RELOAD_MAPPING.get());
		event.register(MODE_TOGGLE_MAPPING.get());
	}

	public void OnClickInput(InputEvent.InteractionKeyMappingTriggered event)
	{
		Player player = Minecraft.getInstance().player;
		ItemStack stack = player.getItemInHand(event.getHand());
		if(stack.getItem() instanceof GunItem gun)
		{
			//gun.ClientHandleMouse(player, stack, event);
		}
	}

	public void OnUseItemTick(LivingEntityUseItemEvent.Tick event)
	{
		Player player = Minecraft.getInstance().player;
		if(event.getItem().getItem() instanceof GunItem gun)
		{
			gun.ClientUpdateUsing(player, event.getItem(), event);
		}
	}

	public void OnClientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase == TickEvent.Phase.END)
		{
			Player player = Minecraft.getInstance().player;
			while(LOOK_AT_MAPPING.get().consumeClick())
			{
				FlansModClient.ACTIONS_CLIENT.ClientKeyPressed(player, EPlayerInput.SpecialKey1);
			}
			while(MODE_TOGGLE_MAPPING.get().consumeClick())
			{
				FlansModClient.ACTIONS_CLIENT.ClientKeyPressed(player, EPlayerInput.SpecialKey2);
			}

			while(MANUAL_RELOAD_MAPPING.get().consumeClick())
			{
				FlansModClient.ACTIONS_CLIENT.ClientKeyPressed(player, EPlayerInput.Reload1);
			}

			UseHeldLastFrame = UseHeldThisFrame;
			AttackHeldLastFrame = AttackHeldThisFrame;

			UseHeldThisFrame = Minecraft.getInstance().options.keyUse.isDown();
			AttackHeldThisFrame = Minecraft.getInstance().options.keyAttack.isDown();

			FramesSinceUseToggled++;
			FramesSinceAttackToggled++;

			if(IsAttackPressed())
				FlansModClient.ACTIONS_CLIENT.ClientKeyPressed(player, EPlayerInput.Fire1);
			if(IsAttackHeld())
				FlansModClient.ACTIONS_CLIENT.ClientKeyHeld(player, EPlayerInput.Fire1);
			if(IsAttackReleased())
				FlansModClient.ACTIONS_CLIENT.ClientKeyReleased(player, EPlayerInput.Fire1, FramesSinceAttackToggled);

			if(IsUsePressed())
				FlansModClient.ACTIONS_CLIENT.ClientKeyPressed(player, EPlayerInput.Fire2);
			if(IsUseHeld())
				FlansModClient.ACTIONS_CLIENT.ClientKeyHeld(player, EPlayerInput.Fire2);
			if(IsUseReleased())
				FlansModClient.ACTIONS_CLIENT.ClientKeyReleased(player, EPlayerInput.Fire2, FramesSinceUseToggled);

			if(UseHeldThisFrame != UseHeldLastFrame)
				FramesSinceUseToggled = 0;
			if(AttackHeldThisFrame != AttackHeldLastFrame)
				FramesSinceAttackToggled = 0;
		}
	}
}
