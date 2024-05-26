package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.physics.VehiclePropellerSaveState;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Collection;

public class LegacyPlaneControlLogic extends ControlLogic
{
	private static final String SingleEngineKey = "main";
	public final boolean Heli;
	public float flapsYaw, flapsPitchLeft, flapsPitchRight;
	public float propAngle;
	public float throttle;

	public LegacyPlaneControlLogic(@Nonnull ControlSchemeDefinition def, boolean heli)
	{
		super(def);
		Heli = heli;
	}

	@Override
	public boolean CanControl(@Nonnull VehicleDefinition vehicleDef)
	{
		return false;
	}

	@Override
	public void TickAuthoritative(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs, @Nonnull ForceModel forces)
	{
		TickShared(vehicle, inputs, forces);
	}

	@Override
	public void TickRemote(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs, @Nonnull ForceModel forces)
	{
		// if(vehicle.level().isClientSide && !thePlayerIsDrivingThis)
		//		{
		//			//The driveable is currently moving towards its server position. Continue doing so.
		//			if(serverPositionTransitionTicker > 0)
		//			{
		//				moveTowardServerPosition();
		//			}
		//			//If the driveable is at its server position and does not have the next update, it should just simulate itself as a server side plane would, so continue
		//
		//		}

		TickShared(vehicle, inputs, forces);


	}
	private static final float g = 0.98F / 10F;

	private void TickShared(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs, @Nonnull ForceModel forces)
	{
		// TODO: if(!readyForUpdates) return;
		Player driver = (vehicle.getControllingPassenger() instanceof Player player) ? player : null;

		//Return the flaps to their resting position
		flapsYaw *= 0.9F;
		flapsPitchLeft *= 0.9F;
		flapsPitchRight *= 0.9F;

		//Limit flap angles
		flapsYaw = Maths.Clamp(flapsYaw, -20, 20);
		flapsPitchLeft = Maths.Clamp(flapsPitchLeft, -20, 20);
		flapsPitchRight = Maths.Clamp(flapsPitchRight, -20, 20);

		// Movement

		//Throttle handling
		//Without a player, default to 0
		//With a player default to 0.5 for helicopters (hover speed)
		//And default to the range 0.25 ~ 0.5 for planes (taxi speed ~ take off speed)
		float throttlePull = 0.99F;
		if(driver != null && Heli && vehicle.Engine().CanThrust(driver, SingleEngineKey))
			throttle = (throttle - 0.5F) * throttlePull + 0.5F;

		//Alter angles
		//Sensitivity function
		float sensitivityAdjust = 2.00677104758f - (float)Math.exp(-2.0f * throttle) / (4.5f * (throttle + 0.1f));
		sensitivityAdjust = Maths.Clamp(sensitivityAdjust, 0.0f, 1.0f);
		//Scalar
		sensitivityAdjust *= 0.125F;

		// TODO: * (flapsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier)
		float yaw = flapsYaw * sensitivityAdjust;

		//if(throttle < 0.2F)
		//	sensitivityAdjust = throttle * 2.5F;
		//Pitch according to the sum of flapsPitchLeft and flapsPitchRight / 2
		float flapsPitch = (flapsPitchLeft + flapsPitchRight) / 2F;
		// TODO: * (flapsPitch > 0 ? type.lookUpModifier : type.lookDownModifier)
		float pitch = flapsPitch * sensitivityAdjust;

		//Roll according to the difference between flapsPitchLeft and flapsPitchRight / 2
		float flapsRoll = (flapsPitchRight - flapsPitchLeft) / 2F;
		// TODO: * (flapsRoll > 0 ? type.rollLeftModifier : type.rollRightModifier)
		float roll = flapsRoll * sensitivityAdjust;

		//Damage modifiers
		if(!Heli)
		{
			if(vehicle.Damage().IsPartDestroyed("tail"))
			{
				yaw = 0;
				pitch = 0;
				roll = 0;
			}
			if(vehicle.Damage().IsPartDestroyed("left_wing"))
				roll -= 7F * vehicle.GetSpeedXZ();
			if(vehicle.Damage().IsPartDestroyed("right_wing"))
				roll += 7F * vehicle.GetSpeedXZ();
		}

		vehicle.Hierarchy().RotateYaw(yaw);
		vehicle.Hierarchy().RotatePitch(pitch);
		vehicle.Hierarchy().RotateRoll(-roll);

		//Some constants

		float drag = 1F - (0.05F * vehicle.Physics().Def.drag);
		float wobbleFactor = 0F;//.005F;

		EngineDefinition engine = vehicle.Engine().GetEngineDef(SingleEngineKey);
		float throttleScaled = 0.01F * (vehicle.Physics().Def.maxThrottle + engine.maxSpeed);

		if(!vehicle.Engine().CanThrust(driver, SingleEngineKey))
			throttleScaled = 0;

		int numPropsWorking = 0;
		int numProps = 0;

		if(Heli)
			HeliPhysics(vehicle, throttleScaled, drag);
		else
			PlanePhysics(vehicle, throttleScaled, drag);

	}

	private void HeliPhysics(@Nonnull VehicleEntity vehicle, float throttleScaled, float drag)
	{
		Collection<VehiclePropellerSaveState> liftPropellers = vehicle.Physics().PropsThatMatch(EControlLogicHint.LiftPropeller);
		Collection<VehiclePropellerSaveState> tailPropellers = vehicle.Physics().PropsThatMatch(EControlLogicHint.TailPropeller);

		//Count the number of working propellers
		int numProps = liftPropellers.size();
		int numPropsWorking = numProps;
		for(VehiclePropellerSaveState prop : liftPropellers)
			if(vehicle.Damage().IsPartDestroyed(prop.Def.attachedTo))
				numPropsWorking--;


		Transform root = vehicle.GetWorldToEntity().GetCurrent();
		Vec3 up = root.UpVec3();

		throttleScaled *= numProps == 0 ? 0 : (float)numPropsWorking / numProps * 2F;

		float upwardsForce = throttle * throttleScaled + (g - throttleScaled / 2F);
		if(throttle < 0.5F)
			upwardsForce = g * throttle * 2F;

		if(vehicle.Damage().IsPartDestroyed("blades"))
		{
			upwardsForce = 0F;
		}

		Vec3 motion = vehicle.getDeltaMovement();

		//Move up
		//Apply gravity
		//Throttle - 0.5 means that the positive throttle scales from -0.5 to +0.5. Thus it accounts for gravity-ish
		motion = motion.add(
			upwardsForce * up.x * 0.5F,
			upwardsForce * up.y - g,
			upwardsForce * up.z * 0.5F);

		//Apply wobble
		//motionX += rand.nextGaussian() * wobbleFactor;
		//motionY += rand.nextGaussian() * wobbleFactor;
		//motionZ += rand.nextGaussian() * wobbleFactor;

		//Apply drag
		motion = motion.scale(drag);

		vehicle.setDeltaMovement(motion);

		vehicle.Engine().SetThrottle(SingleEngineKey, upwardsForce * 2f);
	}

	private void PlanePhysics(@Nonnull VehicleEntity vehicle, float throttleScaled, float drag)
	{
		Collection<VehiclePropellerSaveState> forwardPropellers = vehicle.Physics().PropsThatMatch(EControlLogicHint.ForwardPropeller);

		//Count the number of working propellers
		int numProps = forwardPropellers.size();
		int numPropsWorking = numProps;
		for(VehiclePropellerSaveState prop : forwardPropellers)
			if(vehicle.Damage().IsPartDestroyed(prop.Def.attachedTo))
				numPropsWorking--;

		float throttleTemp = throttle * (numProps == 0 ? 0 : (float)numPropsWorking / numProps * 2F);

		//Apply forces

		Transform root = vehicle.GetWorldToEntity().GetCurrent();
		Vec3 forwards = root.ForwardVec3();


		//Get the speed of the plane
		float lastTickSpeed = (float)vehicle.GetSpeed();

		//Sanity limiter
		if(lastTickSpeed > 2F)
			lastTickSpeed = 2F;

		float newSpeed = lastTickSpeed + throttleScaled * 2F;

		//Calculate the amount to alter motion by
		float proportionOfMotionToCorrect = 2F * throttleTemp - 0.5F;
		if(proportionOfMotionToCorrect < throttle * 0.25f)
			proportionOfMotionToCorrect = throttle * 0.25f;
		if(proportionOfMotionToCorrect > 0.6F)
			proportionOfMotionToCorrect = 0.6F;


		Vec3 motion = vehicle.getDeltaMovement();

		//Apply gravity
		motion = motion.add(0f, -g, 0f);

		//Apply lift
		int numWingsIntact = 2;
		if(vehicle.Damage().IsPartDestroyed("right_wing")) numWingsIntact--;
		if(vehicle.Damage().IsPartDestroyed("left_wing")) numWingsIntact--;

		float amountOfLift = 2F * g * throttleTemp * numWingsIntact / 2F;
		if(amountOfLift > g)
			amountOfLift = g;

		if(vehicle.Damage().IsPartDestroyed("tail"))
			amountOfLift *= 0.75F;

		motion = motion.add(0f, amountOfLift, 0f);

		//Cut out some motion for correction
		motion = motion.scale(1F - proportionOfMotionToCorrect);

		//Add the corrected motion
		motion = motion.add(forwards.scale(proportionOfMotionToCorrect * newSpeed));

		//Apply drag
		motion = motion.scale(drag);

		vehicle.setDeltaMovement(motion);

		vehicle.Engine().SetThrottle(SingleEngineKey, Math.abs(throttle) * throttleScaled * 10f);
	}
}
