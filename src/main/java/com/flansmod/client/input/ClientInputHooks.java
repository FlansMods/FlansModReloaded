package com.flansmod.client.input;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.vehicles.EVehicleAxis;
import com.flansmod.common.types.elements.EPlayerInput;
import com.flansmod.util.Maths;
import com.flansmod.util.collision.ColliderHandle;
import com.flansmod.util.collision.OBBCollisionSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import javax.annotation.Nonnull;

public class ClientInputHooks
{
	private static class HoldableInput
	{
		public boolean HeldThisFrame = false;
		public boolean HeldLastFrame = false;
		public int FramesSinceToggled = 0;

		public boolean IsPressed() { return HeldThisFrame && !HeldLastFrame; }
		public boolean IsHeld() { return HeldThisFrame; }
		public boolean IsReleased() { return !HeldThisFrame && HeldLastFrame; }
		public int TicksSinceToggled() { return FramesSinceToggled; }

		public void Tick(boolean pressed)
		{
			HeldLastFrame = HeldThisFrame;
			HeldThisFrame = pressed;
			if(HeldThisFrame != HeldLastFrame)
				FramesSinceToggled = 0;
			else
				FramesSinceToggled++;
		}
	}

	private static class HoldableAxis
	{
		public float Value = 0.0f;
		public boolean IsActive = false;
		public Lazy<GLFWGamepadState> GamepadState = Lazy.of(GLFWGamepadState::create);
		public void Tick(@Nonnull KeyMappings.AxisMapping axis)
		{
			switch(axis.mappingType())
			{
				case GamepadAxis -> {
					if(axis.axisID() >= 0 && GLFW.glfwGetGamepadState(0, GamepadState.get()))
					{
						IsActive = true;
						Value = GamepadState.get().axes(axis.axisID());
					}
					else
					{
						IsActive = false;
						Value = 0.0f;
					}
				}
				case MouseAxis -> {
					switch(axis.axisID())
					{
						case KeyMappings.MouseX -> {
							IsActive = true;
							Value = (float)Minecraft.getInstance().mouseHandler.getXVelocity();
						}
						case KeyMappings.MouseY -> {
							IsActive = true;
							Value = (float)Minecraft.getInstance().mouseHandler.getYVelocity();
						}
						default -> {
							IsActive = false;
							Value = 0;
						}
					}
				}
			}
		}
	}

	public static float Positive(@Nonnull HoldableInput input) { return input.IsHeld() ? 1.0f : 0.0f; }
	public static float Negative(@Nonnull HoldableInput input) { return input.IsHeld() ? -1.0f : 0.0f; }
	// Right is positive, left is negative
	public static float GetYaw() { return Maths.Clamp(MouseYaw.Value
													+ GamepadYaw.Value
													+ Positive(YawRight)
													+ Negative(YawLeft), -1f, 1f); }
	// Up is positive, down is negative
	public static float GetPitch() { return Maths.Clamp(MousePitch.Value
													+ GamepadPitch.Value
													+ Positive(PitchUp)
													+ Negative(PitchDown), -1f, 1f); }
	// Right is positive, left is negative
	public static float GetRoll() { return Maths.Clamp(MouseRoll.Value
													+ GamepadRoll.Value
													+ Positive(RollRight)
													+ Negative(RollLeft), -1f, 1f); }
	public static float GetMoveForward() { return Maths.Clamp(GamepadForward.Value
													+ Positive(MoveForward)
													+ Negative(MoveBack), -1f, 1f); }
	public static float GetMoveRight() { return Maths.Clamp(GamepadRight.Value
													+ Positive(MoveRight)
													+ Negative(MoveLeft), -1f, 1f); }
	public static float GetMoveUp() { return Maths.Clamp(GamepadUp.Value
													+ Positive(MoveUp)
													+ Negative(MoveDown), -1f, 1f); }


	public static boolean CheckPositive(float f) { return f > 0.01f; }
	public static boolean CheckNegative(float f) { return f < -0.01f; }
	public static boolean GetInput(@Nonnull EPlayerInput additionalKey)
	{
		return switch (additionalKey) {
			case MoveForward -> 	CheckPositive(GetMoveForward());
			case MoveBackward -> 	CheckNegative(GetMoveForward());
			case MoveLeft -> 		CheckNegative(GetMoveRight());
			case MoveRight -> 		CheckPositive(GetMoveRight());

			case YawLeft -> 		CheckNegative(GetYaw());
			case YawRight -> 		CheckPositive(GetYaw());
			case RollLeft -> 		CheckNegative(GetRoll());
			case RollRight -> 		CheckPositive(GetRoll());
			case PitchUp -> 		CheckPositive(GetPitch());
			case PitchDown -> 		CheckNegative(GetPitch());

			case Fire1 -> 			Attack.IsPressed();
			case Fire2 -> 			Use.IsPressed();
			case Fire3 -> 			false;
			case Reload1 -> 		ManualReload.IsPressed();
			case Reload2 -> 		false;
			case Reload3 ->			false;

			case Jump -> 			Jump.IsPressed();
			case Sprint -> 			Sprint.IsPressed();

			case SpecialKey1 -> 	LookAt.IsPressed();
			case SpecialKey2 -> 	ModeToggle.IsPressed();

			case GearUp -> 			GearUp.IsPressed();
			case GearDown -> 		GearDown.IsPressed();
		};
	}
	public static float GetInput(@Nonnull EVehicleAxis control)
	{
		return switch (control) {
			case Roll -> GetRoll();
			case Yaw -> GetYaw();
			case Pitch -> GetPitch();
			case MoveX -> GetMoveRight();
			case MoveY -> GetMoveUp();
			case MoveZ -> GetMoveForward();
			case Accelerator -> GetMoveForward();
			default -> 0.0f;
		};
	}

	// These ones piggyback off the Minecraft settings
	private static final HoldableInput Use = new HoldableInput();
	private static final HoldableInput Attack = new HoldableInput();
	private static final HoldableInput MoveRight = new HoldableInput();
	private static final HoldableInput MoveForward = new HoldableInput();
	private static final HoldableInput MoveBack = new HoldableInput();
	private static final HoldableInput MoveLeft = new HoldableInput();
	private static final HoldableInput MoveUp = new HoldableInput();
	private static final HoldableInput MoveDown = new HoldableInput();
	private static final HoldableInput Jump = new HoldableInput();
	private static final HoldableInput Sneak = new HoldableInput();
	private static final HoldableInput Sprint = new HoldableInput();
	private static final HoldableInput GearUp = new HoldableInput();
	private static final HoldableInput GearDown = new HoldableInput();

	// These are custom bound, because inferring from MC doesn't make sense in 6-axis space

	private static final HoldableInput YawLeft = new HoldableInput();
	private static final HoldableInput YawRight = new HoldableInput();
	private static final HoldableInput PitchUp = new HoldableInput();
	private static final HoldableInput PitchDown = new HoldableInput();
	private static final HoldableInput RollLeft = new HoldableInput();
	private static final HoldableInput RollRight = new HoldableInput();

	private static final HoldableAxis MouseYaw = new HoldableAxis();
	private static final HoldableAxis MousePitch = new HoldableAxis();
	private static final HoldableAxis MouseRoll = new HoldableAxis();

	private static final HoldableAxis GamepadYaw = new HoldableAxis();
	private static final HoldableAxis GamepadPitch = new HoldableAxis();
	private static final HoldableAxis GamepadRoll = new HoldableAxis();
	private static final HoldableAxis GamepadForward = new HoldableAxis();
	private static final HoldableAxis GamepadRight = new HoldableAxis();
	private static final HoldableAxis GamepadUp = new HoldableAxis();


	private static final HoldableInput LookAt = new HoldableInput();
	private static final HoldableInput ModeToggle = new HoldableInput();
	private static final HoldableInput ManualReload = new HoldableInput();

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
		event.register(KeyMappings.LOOK_AT_MAPPING.get());
		event.register(KeyMappings.MODE_TOGGLE_MAPPING.get());
		event.register(KeyMappings.MANUAL_RELOAD_MAPPING.get());
		event.register(KeyMappings.STRAFE_UP_MAPPING.get());
		event.register(KeyMappings.STRAFE_DOWN_MAPPING.get());
		event.register(KeyMappings.YAW_RIGHT_MAPPING.get());
		event.register(KeyMappings.YAW_LEFT_MAPPING.get());
		event.register(KeyMappings.PITCH_UP_MAPPING.get());
		event.register(KeyMappings.PITCH_DOWN_MAPPING.get());
		event.register(KeyMappings.ROLL_LEFT_MAPPING.get());
		event.register(KeyMappings.ROLL_RIGHT_MAPPING.get());
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

	private void ProcessPress(@Nonnull Player player,
							  @Nonnull HoldableInput holdable,
							  @Nonnull EPlayerInput input)
	{
		if(holdable.IsPressed())
			FlansModClient.ACTIONS_CLIENT.ClientKeyPressed(player, input);
	}
	private void ProcessPressHoldRelease(@Nonnull Player player,
										 @Nonnull HoldableInput holdable,
										 @Nonnull EPlayerInput input)
	{
		if(holdable.IsPressed())
			FlansModClient.ACTIONS_CLIENT.ClientKeyPressed(player, input);
		if(holdable.IsHeld())
			FlansModClient.ACTIONS_CLIENT.ClientKeyHeld(player, input);
		if(holdable.IsReleased())
			FlansModClient.ACTIONS_CLIENT.ClientKeyReleased(player, input, holdable.TicksSinceToggled());
	}

	public void OnClientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase == TickEvent.Phase.END)
		{
			// Gather Minecraft inputs
			Options options = Minecraft.getInstance().options;
			Use.Tick(options.keyUse.isDown());
			Attack.Tick(options.keyAttack.isDown());
			MoveForward.Tick(options.keyUp.isDown());
			MoveBack.Tick(options.keyDown.isDown());
			MoveLeft.Tick(options.keyLeft.isDown());
			MoveRight.Tick(options.keyRight.isDown());
			Jump.Tick(options.keyJump.isDown());
			Sneak.Tick(options.keyShift.isDown()); // Not sure we need this because it will be "exit vehicle"
			Sprint.Tick(options.keySprint.isDown());

			// And our own inputs
			YawLeft.Tick(KeyMappings.YAW_LEFT_MAPPING.get().isDown());
			YawRight.Tick(KeyMappings.YAW_RIGHT_MAPPING.get().isDown());
			PitchUp.Tick(KeyMappings.PITCH_UP_MAPPING.get().isDown());
			PitchDown.Tick(KeyMappings.PITCH_DOWN_MAPPING.get().isDown());
			RollLeft.Tick(KeyMappings.ROLL_LEFT_MAPPING.get().isDown());
			RollRight.Tick(KeyMappings.ROLL_RIGHT_MAPPING.get().isDown());
			LookAt.Tick(KeyMappings.LOOK_AT_MAPPING.get().isDown());
			ModeToggle.Tick(KeyMappings.MODE_TOGGLE_MAPPING.get().isDown());
			ManualReload.Tick(KeyMappings.MANUAL_RELOAD_MAPPING.get().isDown());
			MoveUp.Tick(KeyMappings.STRAFE_UP_MAPPING.get().isDown());
			MoveDown.Tick(KeyMappings.STRAFE_DOWN_MAPPING.get().isDown());
			GearUp.Tick(KeyMappings.GEAR_UP_MAPPING.get().isDown());
			GearDown.Tick(KeyMappings.GEAR_DOWN_MAPPING.get().isDown());

			MouseYaw.Tick(KeyMappings.YAW_MOUSE_AXIS.get());
			MousePitch.Tick(KeyMappings.PITCH_MOUSE_AXIS.get());
			MouseRoll.Tick(KeyMappings.ROLL_MOUSE_AXIS.get());
			GamepadYaw.Tick(KeyMappings.YAW_GAMEPAD_AXIS.get());
			GamepadPitch.Tick(KeyMappings.PITCH_GAMEPAD_AXIS.get());
			GamepadRoll.Tick(KeyMappings.ROLL_GAMEPAD_AXIS.get());
			GamepadForward.Tick(KeyMappings.FORWARD_GAMEPAD_AXIS.get());
			GamepadRight.Tick(KeyMappings.STRAFE_RIGHT_GAMEPAD_AXIS.get());
			GamepadUp.Tick(KeyMappings.STRAFE_UP_GAMEPAD_AXIS.get());

			Player player = Minecraft.getInstance().player;
			if(player != null)
			{
				ProcessPress(player, LookAt, EPlayerInput.SpecialKey1);
				ProcessPress(player, ModeToggle, EPlayerInput.SpecialKey2);
				ProcessPress(player, ManualReload, EPlayerInput.Reload1);
				ProcessPress(player, Jump, EPlayerInput.Jump);
				ProcessPress(player, Sprint, EPlayerInput.Sprint);
				ProcessPress(player, GearUp, EPlayerInput.GearUp);
				ProcessPress(player, GearDown, EPlayerInput.GearDown);

				ProcessPressHoldRelease(player, Attack, EPlayerInput.Fire1);
				ProcessPressHoldRelease(player, Use, EPlayerInput.Fire2);

			}

			while(KeyMappings.DEBUG_PAUSE_PHYSICS.get().consumeClick())
			{
				VehicleEntity.PAUSE_PHYSICS = !VehicleEntity.PAUSE_PHYSICS;
				if(VehicleEntity.PAUSE_PHYSICS)
					Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("flansmod.debug.physics_pause.on"), false);
				else
					Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("flansmod.debug.physics_pause.off"), false);
			}
			while(KeyMappings.DEBUG_CYCLE_PHYSICS_INSPECT.get().consumeClick())
			{
				if(Minecraft.getInstance().level != null)
				{
					ColliderHandle handle = OBBCollisionSystem.CycleDebugHandle(Minecraft.getInstance().level);
					if(handle.Handle() == 0L)
						Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("flansmod.debug.physics_handle_inspect.off"), false);
					else
						Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(""+handle.Handle()), false);
				}
			}
		}
	}
}
