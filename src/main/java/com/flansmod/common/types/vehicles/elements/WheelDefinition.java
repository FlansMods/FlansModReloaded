package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class WheelDefinition
{
	public static final WheelDefinition INVALID = new WheelDefinition();

	@JsonField
	public Vec3 visualOffset = Vec3.ZERO;
	@JsonField
	public float springStrength = 1.0f;
	@JsonField
	public float stepHeight = 1.0f;
	@JsonField
	public float gravityScale = 1.0f;

	@JsonField(Min = 0.0f)
	public float maxForwardTorque = 1.0f;
	@JsonField(Min = 0.0f)
	public float maxReverseTorque = 1.0f;
	@JsonField(Min = 0.01f)
	public float radius = 0.25f;

	@JsonField
	public boolean floatOnWater = false;
	@JsonField
	public float buoyancy = 1.0f;
	@JsonField(Min = 0.0001f)
	public float mass = 1.0f;
	@JsonField(Docs = "Leave as zero to auto-calculate")
	public Vec3 momentOfInertia = Vec3.ZERO;

	@JsonField(Min = 0.0f, Docs = "Roughly how many seconds it takes for player changes to torque to be applied")
	public float torqueResponsiveness = 0.1f;
	@JsonField(Min = 0.0f, Docs = "Roughly how many seconds it takes for player changes to yaw (steering) to be applied")
	public float yawResponsiveness = 0.1f;

	@JsonField
	public EControlLogicHint[] controlHints = new EControlLogicHint[0];

	public boolean IsHintedAs(@Nonnull EControlLogicHint check)
	{
		for(EControlLogicHint hintType : controlHints)
			if(hintType == check)
				return true;
		return false;
	}
	private static final float TICK_RATE = 1f/20f;
	public float TorqueLerpRate()
	{
		if(torqueResponsiveness < 0.01f)
			return 1f;
		return TICK_RATE / torqueResponsiveness;
	}
	public float YawLerpRate()
	{
		if(yawResponsiveness < 0.01f)
			return 1f;
		return TICK_RATE / yawResponsiveness;
	}

	private Vec3 _CalculatedMoment = null;
	public Vec3 MomentOfInertia() {
		if(_CalculatedMoment == null)
		{
			if(momentOfInertia.distanceToSqr(Vec3.ZERO) < Maths.Epsilon)
			{
				// https://en.wikipedia.org/wiki/Moment_of_inertia#Examples_2
				// Like a sphere?
				double inertiaX = 0.4f * mass * radius * radius;
				// idk, you can rotate wheels yaw
				double inertiaY = inertiaX * 0.5f;
				// But not camber???
				double inertiaZ = inertiaX * 0.1f;
				_CalculatedMoment = new Vec3(inertiaX, inertiaY, inertiaZ);
			}
			else
			{
				_CalculatedMoment = momentOfInertia;
			}
		}
		return _CalculatedMoment;
	}
}
