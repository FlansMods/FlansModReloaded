package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;

public class ArticulatedPartDefinition
{
	@JsonField
	public String partName = "";
	@JsonField
	public String attachedToPart = "body";

	@JsonField(Min = -360f, Max = 360f)
	public float minYaw = 0f;
	@JsonField(Min = -360f, Max = 360f)
	public float maxYaw = 0f;
	@JsonField(Min = -90f, Max = 90f)
	public float minPitch = 0f;
	@JsonField(Min = -90f, Max = 90f)
	public float maxPitch = 0f;
	@JsonField(Min = -360f, Max = 360f)
	public float minRoll = 0f;
	@JsonField(Min = -360f, Max = 360f)
	public float maxRoll = 0f;

	@JsonField
	public Vec3 minOffset = Vec3.ZERO;
	@JsonField
	public Vec3 maxOffset = Vec3.ZERO;

	@JsonField(Docs = "How fast does the part turn? Set to 0 or negative to make it move at player look speed.")
	public float rotateSpeed = 1.0f;
}
