package com.flansmod.client.input;

import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import javax.annotation.Nonnull;

public class KeyMappings
{
	public static class KeyConflictContextHoldingGun implements IKeyConflictContext
	{
		@Override
		public boolean isActive()
		{
			ShooterContext context = ShooterContext.of(Minecraft.getInstance().player);
			return context.IsValid() && context.GetNumValidContexts() > 0;
		}

		@Override
		public boolean conflicts(@Nonnull IKeyConflictContext other)
		{
			return this == other;
		}
	}
	public static class KeyConflictContextInVehicle implements IKeyConflictContext
	{
		@Override
		public boolean isActive()
		{
			return Minecraft.getInstance().player != null
				&& Minecraft.getInstance().player.getVehicle() instanceof VehicleEntity;
		}

		@Override
		public boolean conflicts(@Nonnull IKeyConflictContext other)
		{
			return this == other;
		}
	}
	public static final IKeyConflictContext HOLDING_GUN = new KeyConflictContextHoldingGun();
	public static final IKeyConflictContext IN_VEHICLE = new KeyConflictContextInVehicle();

	public static final Lazy<KeyMapping> DEBUG_PAUSE_PHYSICS = Lazy.of(() -> new KeyMapping(
		"key.flansmod.debug.pause_physics",
		KeyConflictContext.UNIVERSAL,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_F10,
		"key.categories.flansmod.debug"
	));
	public static final Lazy<KeyMapping> DEBUG_CYCLE_PHYSICS_INSPECT = Lazy.of(() -> new KeyMapping(
		"key.flansmod.debug.cycle_inspect",
		KeyConflictContext.UNIVERSAL,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_F9,
		"key.categories.flansmod.debug"
	));


	public static final Lazy<KeyMapping> LOOK_AT_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.look_at",
		HOLDING_GUN,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_L,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> MODE_TOGGLE_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.mode_toggle",
		HOLDING_GUN,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_C,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> MANUAL_RELOAD_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.manual_reload",
		HOLDING_GUN,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_R,
		"key.categories.flansmod"));

	public static final Lazy<KeyMapping> GEAR_UP_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.gear_down",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_HOME,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> GEAR_DOWN_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.gear_up",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_END,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> STRAFE_UP_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.strafe_down",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_PAGE_UP,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> STRAFE_DOWN_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.strafe_up",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_PAGE_DOWN,
		"key.categories.flansmod"));

	public static final Lazy<KeyMapping> YAW_RIGHT_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.yaw_right",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_D,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> YAW_LEFT_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.yaw_left",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_A,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> PITCH_UP_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.pitch_up",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_DOWN,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> PITCH_DOWN_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.pitch_down",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_UP,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> ROLL_LEFT_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.roll_left",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_LEFT,
		"key.categories.flansmod"));
	public static final Lazy<KeyMapping> ROLL_RIGHT_MAPPING = Lazy.of(() -> new KeyMapping(
		"key.flansmod.roll_right",
		IN_VEHICLE,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_RIGHT,
		"key.categories.flansmod"));


	public enum AxisMappingType
	{
		MouseAxis,
		GamepadAxis,
	}

	public static final int MouseX = 0;
	public static final int MouseY = 1;
	public static final int MouseScrollWheel = 2;

	public record AxisMapping(@Nonnull String name,
							  @Nonnull IKeyConflictContext context,
							  @Nonnull AxisMappingType mappingType,
							  int axisID,
							  @Nonnull String category)
	{
	}

	public static final Lazy<AxisMapping> YAW_MOUSE_AXIS = Lazy.of(() -> new AxisMapping(
		"axis.flansmod.yaw",
		IN_VEHICLE,
		AxisMappingType.MouseAxis,
		-1,
		"key.categories.flansmod"
	));
	public static final Lazy<AxisMapping> ROLL_MOUSE_AXIS = Lazy.of(() -> new AxisMapping(
		"axis.flansmod.roll",
		IN_VEHICLE,
		AxisMappingType.MouseAxis,
		MouseX,
		"key.categories.flansmod"
	));
	public static final Lazy<AxisMapping> PITCH_MOUSE_AXIS = Lazy.of(() -> new AxisMapping(
		"axis.flansmod.pitch",
		IN_VEHICLE,
		AxisMappingType.MouseAxis,
		MouseY,
		"key.categories.flansmod"
	));
	public static final Lazy<AxisMapping> ROLL_GAMEPAD_AXIS = Lazy.of(() -> new AxisMapping(
		"axis.flansmod.roll",
		IN_VEHICLE,
		AxisMappingType.GamepadAxis,
		GLFW.GLFW_GAMEPAD_AXIS_LEFT_X,
		"key.categories.flansmod"
	));
	public static final Lazy<AxisMapping> PITCH_GAMEPAD_AXIS = Lazy.of(() -> new AxisMapping(
		"axis.flansmod.pitch",
		IN_VEHICLE,
		AxisMappingType.GamepadAxis,
		GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y,
		"key.categories.flansmod"
	));
	public static final Lazy<AxisMapping> YAW_GAMEPAD_AXIS = Lazy.of(() -> new AxisMapping(
		"axis.flansmod.yaw",
		IN_VEHICLE,
		AxisMappingType.GamepadAxis,
		GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X,
		"key.categories.flansmod"
	));
	public static final Lazy<AxisMapping> FORWARD_GAMEPAD_AXIS = Lazy.of(() -> new AxisMapping(
		"axis.flansmod.forward",
		IN_VEHICLE,
		AxisMappingType.GamepadAxis,
		GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER,
		"key.categories.flansmod"
	));
	public static final Lazy<AxisMapping> STRAFE_RIGHT_GAMEPAD_AXIS = Lazy.of(() -> new AxisMapping(
		"axis.flansmod.right",
		IN_VEHICLE,
		AxisMappingType.GamepadAxis,
		-1,
		"key.categories.flansmod"
	));
	public static final Lazy<AxisMapping> STRAFE_UP_GAMEPAD_AXIS = Lazy.of(() -> new AxisMapping(
		"axis.flansmod.up",
		IN_VEHICLE,
		AxisMappingType.GamepadAxis,
		GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y,
		"key.categories.flansmod"
	));
}
