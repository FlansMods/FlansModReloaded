package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.damage.VehicleDamageModule;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleHierarchyModule;
import com.flansmod.common.entity.vehicle.hierarchy.WheelEntity;
import com.flansmod.common.entity.vehicle.physics.VehiclePhysicsModule;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.EVehicleAxis;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.CollisionPointDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nonnull;

public class LegacyVehicleControlLogic extends ControlLogic
{
	private static final String SingleEngineKey = "main";

	public final boolean Tank;

	public float wheelsYaw;

	public LegacyVehicleControlLogic(@Nonnull ControlSchemeDefinition def, boolean tank)
	{
		super(def);
		Tank = tank;
	}

	@Override
	public boolean CanControl(@Nonnull VehicleDefinition vehicleDef)
	{
		int numWheels = vehicleDef.physics.wheels.length;
		if(numWheels != 4)
			return false;

		return true;
	}

	// This is an attempt to replicate legacy 1.12.2 behaviour in FM:Reloaded
	// Reference is here:
	// https://github.com/FlansMods/FlansMod/blob/1.12.2/src/main/java/com/flansmod/common/driveables/EntityVehicle.java

	@Override
	public void TickAuthoritative(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs)
	{
		TickShared(vehicle, inputs);
	}

	@Override
	public void TickRemote(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs)
	{
		TickShared(vehicle, inputs);
	}
	private void TickShared(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs)
	{
		//Return the wheels to their resting position
		wheelsYaw *= 0.9F;

		//Limit wheel angles
		if(wheelsYaw > 20)
			wheelsYaw = 20;
		if(wheelsYaw < -20)
			wheelsYaw = -20;

		Player driver = (vehicle.getControllingPassenger() instanceof Player player) ? player : null;
		Vec3 amountToMoveCar = Vec3.ZERO;

		// We get this from the new input system. Will need a bit of fiddling to get it matching
		// TODO: Weave in (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle)
		float throttle = inputs.GetValue(EVehicleAxis.Accelerator);
		// TODO: Weave in (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier)
		float steering = inputs.GetValue(EVehicleAxis.Yaw);

		float engineSpeed = vehicle.Engine().GetEngine(SingleEngineKey).maxSpeed;
		// -------------------------------------------------------------------------------------

		WheelEntity[] wheels = vehicle.Physics().AllWheels().toArray(new WheelEntity[0]);
		for(int wheelID = 0; wheelID < wheels.length; wheelID++)
		{
			WheelEntity wheel = wheels[wheelID];
			if(wheel == null)
				continue;

			// Hacky way of forcing the car to step up blocks
			vehicle.setOnGround(true);
			wheel.setOnGround(true);

			//Update angles
			wheel.setYRot(vehicle.RootTransform0().Yaw());
			//Front wheels
			if(!Tank && (wheelID == 2 || wheelID == 3))
			{
				wheel.setYRot(wheel.getYRot() + wheelsYaw);
			}

			Vec3 wheelMotion = wheel.getDeltaMovement();
			wheelMotion = wheelMotion.scale(0.9f);

			//Apply gravity
			wheelMotion = wheelMotion.add(0f, -0.98f / 20f, 0f);

			//Apply velocity
			if(vehicle.Engine().CanThrust(driver, SingleEngineKey)) // TODO: Fuel module
			{
				if(Tank)
				{
					boolean left = wheelID == 0 || wheelID == 3;

					float turningDrag = 0.02F;

					float xzMotionDampen = 1F - (Math.abs(wheelsYaw) * turningDrag);
					wheelMotion = wheelMotion.multiply(xzMotionDampen, 1f, xzMotionDampen);

					float velocityScale = 0.04F * throttle * engineSpeed;
					float steeringScale = 0.1F * steering;

					float effectiveWheelSpeed =
						(throttle + (wheelsYaw * (left ? 1 : -1) * steeringScale)) * velocityScale;

					wheelMotion = wheelMotion.add(
						effectiveWheelSpeed * Math.cos(wheel.yRotO * Maths.DegToRadF),
						0f,
					 	effectiveWheelSpeed * Math.sin(wheel.yRotO * Maths.DegToRadF));

				}
				else
				{
					//if(getVehicleType().fourWheelDrive || wheel.ID == 0 || wheel.ID == 1)
					{
						float velocityScale = 0.1F * throttle * engineSpeed;
						wheelMotion = wheelMotion.add(
							Math.cos(wheel.yRotO * Maths.DegToRadF) * velocityScale,
							0f,
							Math.sin(wheel.yRotO * Maths.DegToRadF) * velocityScale);
					}

					//Apply steering
					if(wheelID == 2 || wheelID == 3)
					{
						float velocityScale = 0.01F * steering * (throttle > 0 ? 1 : -1);
						double xzSpeed = Maths.Sqrt(wheelMotion.x * wheelMotion.x + wheelMotion.z * wheelMotion.z);

						wheelMotion = wheelMotion.add(
							-xzSpeed * Math.sin(wheel.yRotO * Maths.DegToRadF) * velocityScale * wheelsYaw,
							0f,
							xzSpeed * Math.cos(wheel.yRotO * Maths.DegToRadF) * velocityScale * wheelsYaw);
					}
					else
					{
						wheelMotion = wheelMotion.scale(0.9f);
					}
				}
			}

			if(wheel.Def.floatOnWater && vehicle.level().containsAnyLiquid(wheel.getBoundingBox()))
			{
				wheelMotion = wheelMotion.add(0f, wheel.Def.buoyancy, 0f);
			}

			wheel.setDeltaMovement(wheelMotion);
			wheel.move(MoverType.PLAYER, wheelMotion);

			//Pull wheels towards car
			// TODO: Make sure the wheel position array (old) gets updated to this hierarchy structure
			Transform expectedWheelPosition = vehicle.Hierarchy().GetWorldToPartPrevious("wheel_" + wheelID);

			Vec3 dPos = expectedWheelPosition.PositionVec3().subtract(wheel.position());
			dPos = dPos.scale(wheel.Def.springStrength);
			if(dPos.lengthSqr() > 0.00001d)
			{
				// Move the wheel by this much
				wheel.move(MoverType.PLAYER, dPos);
				// Pull the car back by _HALF_ as much??
				amountToMoveCar = amountToMoveCar.add(dPos.scale(-0.5d));
			}
		}

		vehicle.move(MoverType.PLAYER, amountToMoveCar);

		// Now we do the oldschool rotation resolution
		// I think its bad, let's just run the code
		if(wheels.length == 4 && wheels[0] != null && wheels[1] != null && wheels[2] != null && wheels[3] != null)
		{
			Vec3 frontAxleCentre = wheels[2].position().lerp(wheels[3].position(), 0.5d);
			Vec3 backAxleCentre = wheels[0].position().lerp(wheels[1].position(), 0.5d);
			Vec3 leftSideCentre = wheels[0].position().lerp(wheels[3].position(), 0.5d);
			Vec3 rightSideCentre = wheels[1].position().lerp(wheels[2].position(), 0.5d);

			double dx = frontAxleCentre.x - backAxleCentre.x;
			double dy = frontAxleCentre.y - backAxleCentre.y;
			double dz = frontAxleCentre.z - backAxleCentre.z;
			double drx = leftSideCentre.x - rightSideCentre.x;
			double dry = leftSideCentre.y - rightSideCentre.y;
			double drz = leftSideCentre.z - rightSideCentre.z;

			double dxz = Maths.Sqrt(dx * dx + dz * dz);
			double drxz = Maths.Sqrt(drx * drx + drz * drz);

			float yaw = (float)Maths.Atan2(dz, dx);
			float pitch = -(float)Maths.Atan2(dy, dxz);
			float roll = 0F;
			//if(type.canRoll) <- TODO: Does this even translate?
			{
				roll = -(float)Math.atan2(dry, drxz);
			}

			if(Tank)
			{
				yaw = (float)Math.atan2(wheels[3].getZ() - wheels[2].getZ(), wheels[3].getX() - wheels[2].getX()) +
					(float)Math.PI / 2F;
			}

			vehicle.SetEulerAngles(yaw, pitch, roll);
		}

		CheckForCollisions(vehicle);

		/*
		int animSpeed = 4;
		//Change animation speed based on our current throttle
		if((throttle > 0.05 && throttle <= 0.33) || (throttle < -0.05 && throttle >= -0.33))
		{
			animSpeed = 3;
		}
		else if((throttle > 0.33 && throttle <= 0.66) || (throttle < -0.33 && throttle >= -0.66))
		{
			animSpeed = 2;
		}
		else if((throttle > 0.66 && throttle <= 0.9) || (throttle < -0.66 && throttle >= -0.9))
		{
			animSpeed = 1;
		}
		else if((throttle > 0.9 && throttle <= 1) || (throttle < -0.9 && throttle >= -1))
		{
			animSpeed = 0;
		}

		if(throttle > 0.05)
		{
			animCount--;
		}
		else if(throttle < -0.05)
		{
			animCount++;
		}

		if(animCount <= 0)
		{
			animCount = animSpeed;
			animFrame++;
		}

		if(throttle < 0)
		{
			if(animCount >= animSpeed)
			{
				animCount = 0;
				animFrame--;
			}
		}
		//Cycle the animation frame, but only if we have anything to cycle
		if(type.animFrames != 0)
		{
			if(animFrame > type.animFrames)
			{
				animFrame = 0;
			}
			if(animFrame < 0)
			{
				animFrame = type.animFrames;
			}
		*/
	}

	private static final float unbreakableBlockDamage = 100.0f;
	private static final float collisionForce = 30f;
	private void CheckForCollisions(@Nonnull VehicleEntity vehicle)
	{
		boolean crashInWater = false;
		double speed = vehicle.getDeltaMovement().length();

		VehicleHierarchyModule hierarchy = vehicle.Hierarchy();
		VehiclePhysicsModule physics = vehicle.Physics();
		VehicleDamageModule damage = vehicle.Damage();
		Level level = vehicle.level();
		for(CollisionPointDefinition point : physics.Def.collisionPoints)
		{
			if(damage.IsPartDestroyed(point.attachedTo))
				continue;

			Transform prevPointPos = hierarchy.GetAttachmentPrevious(point.attachedTo, point.offset);
			Transform currentPointPos = hierarchy.GetAttachmentCurrent(point.attachedTo, point.offset);

			if(FlansMod.DEBUG && level.isClientSide)
			{
				//world.spawnEntity(new EntityDebugVector(world, new Vector3f(lastPos),
				//	Vector3f.sub(currentRelPos, lastRelPos, null), 10, 1F, 0F, 0F));
			}


			// Use level raycast against blocks only, not a full bullet cast
			//Raytracer raytracer = Raytracer.ForLevel(level);
			//raytracer.CastBullet()

			BlockHitResult blockHit = level.clip(new ClipContext(
				prevPointPos.PositionVec3(),
				currentPointPos.PositionVec3(),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, // TODO: crashInWater -> change context
				vehicle));
			if(blockHit.getType() != HitResult.Type.MISS)
			{
				BlockPos pos = blockHit.getBlockPos();
				BlockState state = level.getBlockState(pos);

				// TODO: Is this even right?
				float blockHardness = state.getBlock().getExplosionResistance();
				float impactDamage = (float)speed;

				// unbreakable block
				if(blockHardness < 0F)
				{
					// TODO: Is any of this accurate? Probably not
					float unbreakableBlockDamage = 5.0f;

					impactDamage *= unbreakableBlockDamage * unbreakableBlockDamage;
				}
				else
				{
					impactDamage *= blockHardness * blockHardness;
				}

				// Attack the part
				float damageDealt = damage.ApplyDamageTo(point.attachedTo, level.damageSources().inWall(), impactDamage);
				if(!damage.IsPartDestroyed(point.attachedTo)) // && TeamsManager.driveablesBreakBlocks)
				{
					// And if it didn't die from the attack, break the block
					// TODO: [1.12] Heck
					// playAuxSFXAtEntity(null, 2001, pos, Block.getStateId(state));

					if(!level.isClientSide && blockHardness <= collisionForce)
					{
						level.destroyBlock(pos, true, vehicle.Seats().GetPassengerInSeat(0));
					}
				}
				else
				{
					// The part died!
					level.explode(
						vehicle,
						currentPointPos.PositionVec3().x,
						currentPointPos.PositionVec3().y,
						currentPointPos.PositionVec3().z,
						1F,
						Level.ExplosionInteraction.MOB);
				}
			}

		}
	}
}
