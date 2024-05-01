package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.elements.InputDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SeatDefinition
{
	public static final SeatDefinition INVALID = new SeatDefinition();

	@JsonField
	public String attachedTo = "body";
	@JsonField
	public Vec3 offsetFromAttachPoint = Vec3.ZERO;
	@JsonField
	public float minYaw = -360f;
	@JsonField
	public float maxYaw = 360f;
	@JsonField
	public float minPitch = -90f;
	@JsonField
	public float maxPitch = 90f;

	@JsonField
	public boolean gyroStabilised = false;

	@JsonField
	public VehicleControlOptionDefinition[] controllerOptions = new VehicleControlOptionDefinition[0];
	@JsonField
	public InputDefinition[] inputs = new InputDefinition[0];

	public Lazy<Map<String, ControlSchemeDefinition>> Controllers = Lazy.of(() -> {
		Map<String, ControlSchemeDefinition> map = new HashMap<>();
		for(VehicleControlOptionDefinition controllerOption : controllerOptions)
		{
			ControlSchemeDefinition controlScheme = FlansMod.CONTROL_SCHEMES.Get(controllerOption.controlScheme);
			if(controlScheme.IsValid())
				map.put(controllerOption.key, controlScheme);
		}
		return map;
	});
}
