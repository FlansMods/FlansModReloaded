package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;

public class LegsDefinition
{
	@JsonField
	public String attachedTo = "body";
	@JsonField
	public Vec3 visualOffset = Vec3.ZERO;
	@JsonField
	public Vec3 physicsOffset = Vec3.ZERO;
	@JsonField
	public float stepHeight = 1.0f;
	@JsonField
	public float jumpHeight = 1.0f;
	@JsonField
	public float jumpVelocity = 1.0f;
	@JsonField
	public float rotateSpeed = 10.0f;
	@JsonField(Docs = "Once the player turns beyond this limit, legs engage and rotate")
	public float bodyMinYaw = -90f;
	@JsonField(Docs = "Once the player turns beyond this limit, legs engage and rotate")
	public float bodyMaxYaw = 90f;




	@JsonField
	public float legLength = 1.0f;


	@JsonField
	public float negateFallDamageRatio = 0.0f;
	@JsonField
	public float transferFallDamageIntoEnvironmentRatio = 0.0f;
}
