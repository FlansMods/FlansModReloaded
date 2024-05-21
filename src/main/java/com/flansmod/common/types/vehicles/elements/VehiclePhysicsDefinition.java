package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;

public class VehiclePhysicsDefinition
{
	// Rest Pose
	@JsonField
	public Vec3 restingEulerAngles = Vec3.ZERO;
	@JsonField(Min = 0f, Max = 1f, Docs = "How much speed to lose per second")
	public float drag = 0.05f;
	@JsonField(Min = 0.0001f)
	public float mass = 10.0f;
	@JsonField(Docs = "Legacy only, use new axis bindings")
	public float maxThrottle = 1.0f;

	@JsonField
	public CollisionPointDefinition[] collisionPoints = new CollisionPointDefinition[0];
}
