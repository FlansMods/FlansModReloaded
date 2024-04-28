package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.SoundDefinition;
import net.minecraft.world.phys.Vec3;

public class MountedGunDefinition
{
	public static final MountedGunDefinition INVALID = new MountedGunDefinition();

	@JsonField
	public String name = "default";
	@JsonField
	public ActionDefinition[] primaryActions = new ActionDefinition[0];

	@JsonField
	public Vec3 recoil = Vec3.ZERO;
	@JsonField
	public String attachedTo = "body";

	@JsonField
	public Vec3 shootPointOffset = Vec3.ZERO;
	@JsonField
	public float minYaw = -360f;
	@JsonField
	public float maxYaw = 360f;
	@JsonField
	public float minPitch = -90;
	@JsonField
	public float maxPitch = 90f;
	@JsonField
	public float aimingSpeed = 1.0f;
	@JsonField
	public boolean lockSeatToGunAngles = false;

	@JsonField(Docs = "If set true, this turret has to line up its yaw, then its pitch, one at a time")
	public boolean traveseIndependently = false;
	@JsonField
	public SoundDefinition yawSound = new SoundDefinition();
	@JsonField
	public SoundDefinition pitchSound = new SoundDefinition();
}
