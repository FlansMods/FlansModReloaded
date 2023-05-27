package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.vehicles.elements.InputDefinition;
import net.minecraft.world.phys.Vec3;


public class SeatDefinition
{
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
	public InputDefinition[] inputs = new InputDefinition[0];
}
