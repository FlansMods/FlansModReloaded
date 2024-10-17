package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.SoundDefinition;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;

public class ArticulatedPartDefinition
{
	@JsonField
	public boolean active = false;

	@JsonField
	public float minParameter = 0f;
	@JsonField
	public float maxParameter = 1f;
	@JsonField
	public float startParameter = 0f;
	@JsonField
	public boolean cyclic = false;


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

	@JsonField
	public SoundDefinition traverseSound = new SoundDefinition();
	@JsonField(Docs = "If set true, this turret has to line up its yaw, then its pitch, one at a time")
	public boolean traveseIndependently = false;


	@JsonField(Docs = "If non-empty, the turret will try to align itself to this seat's look vector")
	public String followSeatAtPath = "";
	@JsonField
	public boolean lockSeatToGunAngles = false;

	@JsonField(Docs = "How fast does the part turn? Set to 0 or negative to make it move at player look speed.")
	public float rotateSpeed = 1.0f;


	@Nonnull
	public Lazy<Transform> StartPose = Lazy.of(() -> Transform.fromPosAndEuler(minOffset, minYaw, minPitch, minRoll));
	@Nonnull
	public Lazy<Transform> EndPose = Lazy.of(() -> Transform.fromPosAndEuler(maxOffset, maxYaw, maxPitch, maxRoll));

	@Nonnull
	public Transform Apply(float parameter)
	{
		return Transform.interpolate(StartPose.get(),
							 		 EndPose.get(),
							 		 Maths.clamp(parameter, minParameter, maxParameter));
	}
}
