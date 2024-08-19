package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.util.Maths;
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
	@JsonField(Docs = "Leave as zero to auto-calculate")
	public Vec3 momentOfInertia = Vec3.ZERO;
	@JsonField(Docs = "Legacy only, use new axis bindings")
	public float maxThrottle = 1.0f;

	@JsonField
	public CollisionPointDefinition[] collisionPoints = new CollisionPointDefinition[0];

	private Vec3 _CalculatedMoment = null;
	public Vec3 MomentOfInertia() {
		if(_CalculatedMoment == null)
		{
			if(momentOfInertia.distanceToSqr(Vec3.ZERO) < Maths.Epsilon)
			{
				_CalculatedMoment = new Vec3(mass, mass, mass);
			}
			else
			{
				_CalculatedMoment = momentOfInertia;
			}
		}
		return _CalculatedMoment;
	}
}
