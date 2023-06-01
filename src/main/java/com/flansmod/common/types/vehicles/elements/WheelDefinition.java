package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;

public class WheelDefinition
{
	@JsonField
	public String attachedTo = "body";
	@JsonField
	public Vec3 visualOffset = Vec3.ZERO;
	@JsonField
	public Vec3 physicsOffset = Vec3.ZERO;
	@JsonField
	public float springStrength = 1.0f;
	@JsonField
	public float stepHeight = 1.0f;

}
