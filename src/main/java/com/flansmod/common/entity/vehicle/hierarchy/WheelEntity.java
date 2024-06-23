package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.entity.ITransformChildEntity;
import com.flansmod.common.entity.ITransformPair;
import com.flansmod.common.entity.vehicle.ITransformEntity;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.ForceModel;
import com.flansmod.common.network.FlansEntityDataSerializers;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.common.types.vehicles.elements.WheelDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WheelEntity extends Entity implements ITransformChildEntity
{
	private static final int INVALID_WHEEL_INDEX = -1;

	private WheelDefinition Def;
	public EntityDimensions Size;
	private Optional<VehicleEntity> Vehicle = Optional.empty();

	public static EntityDataAccessor<Integer> VEHICLE_ENTITY_ID = SynchedEntityData.defineId(WheelEntity.class, EntityDataSerializers.INT);
	public static EntityDataAccessor<VehicleDefinition> VEHICLE_DEF = SynchedEntityData.defineId(WheelEntity.class, FlansEntityDataSerializers.VEHICLE_DEF);
	public static EntityDataAccessor<Integer> WHEEL_INDEX = SynchedEntityData.defineId(WheelEntity.class, EntityDataSerializers.INT);
	public static EntityDataAccessor<Integer> WHEEL_PATH = SynchedEntityData.defineId(WheelEntity.class, EntityDataSerializers.INT);
	public static EntityDataAccessor<Float> ANGULAR_VELOCITY = SynchedEntityData.defineId(WheelEntity.class, EntityDataSerializers.FLOAT);

	// From -1 to 1, this is a raw input from the controller
	// The WheelDefinition will tell what to do with this value
	public float TargetYawParameter = 0.0f;
	public float YawParameterPrevious = 0.0f;
	public float YawParameterCurrent = 0.0f;

	public float TargetTorqueParameter = 0.0f;
	public float TorqueParameterPrevious = 0.0f;
	public float TorqueParameterCurrent = 0.0f;

	// In radians
	public float AngularVelocityPrevious = 0.0f;
	public float AngularVelocityCurrent = 0.0f;



	public WheelEntity(@Nonnull EntityType<? extends WheelEntity> type,
					   @Nonnull Level world)
	{
		super(type, world);
		Size = EntityDimensions.fixed(0.5f, 0.5f);
		refreshDimensions();
		blocksBuilding = true;
	}

	// Synced Data Fields
	@Override
	protected void defineSynchedData()
	{
		entityData.define(VEHICLE_DEF, VehicleDefinition.INVALID);
		entityData.define(VEHICLE_ENTITY_ID, 0);
		entityData.define(WHEEL_INDEX, 0);
		entityData.define(WHEEL_PATH, 0);
		entityData.define(ANGULAR_VELOCITY, 0.0f);
	}

	public void SetLinkToVehicle(@Nonnull VehicleEntity vehicle, int wheelIndex)
	{
		SetVehicleDef(vehicle.Def());
		SetVehicleEntityID(vehicle.getId());
		SetWheelIndex(wheelIndex);

		WheelDefinition def = GetWheelDef();
		Size = EntityDimensions.fixed(def.radius, def.radius);
		Vehicle = Optional.of(vehicle);
		setPos(vehicle.GetWorldToAP(GetWheelPath().Part()).GetCurrent().PositionVec3());
	}
	private void SetVehicleDef(@Nonnull VehicleDefinition def) { entityData.set(VEHICLE_DEF, def); }
	private void SetVehicleEntityID(int entityID) { entityData.set(VEHICLE_ENTITY_ID, entityID); }
	private void SetWheelIndex(int index) { entityData.set(WHEEL_INDEX, index); }
	private void SetAngularVelocity(float angularVelocity) { entityData.set(ANGULAR_VELOCITY, angularVelocity); }

	@Nonnull public VehicleDefinition GetVehicleDef() { return entityData.get(VEHICLE_DEF); }
	public int GetVehicleID() { return entityData.get(VEHICLE_ENTITY_ID); }
	@Nullable public VehicleEntity GetVehicle()
	{
		if(Vehicle.isEmpty())
		{
			if(level().getEntity(GetVehicleID()) instanceof VehicleEntity parent)
			{
				Vehicle = Optional.of(parent);
			}
		}
		return Vehicle.get();
	}
	public int GetWheelIndex() { return entityData.get(WHEEL_INDEX); }
	@Nonnull
	public VehicleComponentPath GetWheelPath()
	{
		return VehicleComponentPath.of(entityData.get(WHEEL_PATH), EPartDefComponent.Wheel, entityData.get(WHEEL_INDEX));
	}
	public float GetAngularVelocity() { return entityData.get(ANGULAR_VELOCITY); }
	@Nonnull
	public WheelDefinition GetWheelDef()
	{
		if(Def == null)
		{
			int wheelIndex = GetWheelIndex();
			if(wheelIndex != INVALID_WHEEL_INDEX)
			{
				VehicleDefinition vehicleDef = GetVehicleDef();
				if (vehicleDef.IsValid())
				{
					Def = vehicleDef.AsHierarchy().FindWheel(GetWheelPath()).orElse(WheelDefinition.INVALID);
				}
			}
		}
		return Def != null ? Def : WheelDefinition.INVALID;
	}
	public boolean IsLinked()
	{
		return GetVehicleDef().IsValid() && GetWheelIndex() != INVALID_WHEEL_INDEX;
	}


	// Some misc overrides
	@Override @Nonnull public EntityDimensions getDimensions(@Nonnull Pose pose) {
		return Size;
	}
	public static boolean canVehicleCollide(@Nonnull Entity a, @Nonnull Entity b)
	{
		return (b.canBeCollidedWith() || b.isPushable()) && !a.isPassengerOfSameVehicle(b);
	}
	@Override public boolean canCollideWith(@Nonnull Entity other) {
		return canVehicleCollide(this, other);
	}
	@Override public boolean canBeCollidedWith() {
		return true;
	}
	@Override public boolean isPushable() {
		return true;
	}
	public boolean Is(@Nonnull EControlLogicHint hint) { return Def.IsHintedAs(hint); }

	@Override
	public boolean shouldBeSaved() {
		return false;
	}
	@Override
	public boolean is(@Nonnull Entity other) {
		return this == other || GetVehicle() == other;
	}
	@Override
	@Nullable
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean isPickable() {
		return true;
	}
	@Override
	@Nullable
	public ItemStack getPickResult()
	{
		VehicleEntity vehicle = GetVehicle();
		return vehicle != null ? vehicle.getPickResult() : ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand)
	{
		VehicleEntity vehicle = GetVehicle();
		return vehicle != null ? vehicle.interact(player, hand) : InteractionResult.PASS;
	}

	@Nonnull
	public Transform GetWorldTransformPrevious() { return Transform.FromPos(getPosition(0f)); }
	@Nonnull
	public Transform GetWorldTransformCurrent() { return Transform.FromPos(getPosition(1f)); }
	@Nonnull
	@Override
	public ITransformPair GetWorldTransform()
	{
		return ITransformPair.of(this::GetWorldTransformPrevious, this::GetWorldTransformCurrent);
	}
	@Override
	public void SetWorldTransform(@Nonnull Transform currentWorldTransform)
	{
		setPos(currentWorldTransform.PositionVec3());
	}

	@Override
	protected void readAdditionalSaveData(@Nonnull CompoundTag tags)
	{

	}

	@Override
	protected void addAdditionalSaveData(@Nonnull CompoundTag tags)
	{

	}

	public void StartTick(@Nonnull VehicleEntity parent)
	{
		// Copy current to previous
		setOldPosAndRot();
		YawParameterPrevious = YawParameterCurrent;
		AngularVelocityPrevious = AngularVelocityCurrent;
		TorqueParameterPrevious = TorqueParameterCurrent;
	}

	public void PhysicsTick(@Nonnull VehicleEntity vehicle, int wheelIndex, @Nonnull ForceModel forces)
	{
		if (!VehicleEntity.PAUSE_PHYSICS)
		{
			// Move using the Minecraft body, so we can process collisions
			Vec3 motion = GetVelocity();
			motion = forces.ApplyLinearForcesToWheel(motion, wheelIndex, GetWorldTransformCurrent(), GetWheelDef().mass);
			motion = forces.ApplySpringForcesToWheel(motion, wheelIndex, GetWorldTransformCurrent(), GetWheelDef().mass, vehicle::GetWorldToPartCurrent);
			motion = forces.ApplyDampeningToWheel(wheelIndex, motion);
			SetVelocity(motion);
			ApplyVelocity();

			SyncEntityToTransform();
		}
	}

	public void EndTick(@Nonnull VehicleEntity parent)
	{
		if(parent.IsAuthority())
		{
			// Lerp our parameters to the player requested parameters
			TorqueParameterCurrent = Maths.LerpF(TorqueParameterCurrent, TargetTorqueParameter, GetWheelDef().TorqueLerpRate());
			YawParameterCurrent = Maths.LerpF(YawParameterCurrent, TargetYawParameter, GetWheelDef().YawLerpRate());

		}
		else
		{
			//Vec3 lerpTarget = GetRemotePosition();
			//lerpTo(lerpTarget.x, lerpTarget.y, lerpTarget.z);
			//LocalOffsetFromAPCurrent = LocalOffsetFromAPCurrent.lerp(lerpTarget, 1f);
		}

		SyncTransformToEntity();
	}



	private void CheckCollisions()
	{
		Level level = level();
		List<Entity> list = level.getEntities(this, getBoundingBox().inflate((double)0.2F, (double)-0.01F, (double)0.2F), EntitySelector.pushableBy(this));
		if (!list.isEmpty()) {
			//boolean flag = !level.isClientSide && !(this.getControllingPassenger() instanceof Player);

			for(int j = 0; j < list.size(); ++j) {
				Entity entity = list.get(j);
				if (!entity.hasPassenger(this)) {
					push(entity);
				}
			}
		}
	}

	/*
	public void PhysicsTick(@Nonnull VehicleEntity vehicle, int wheelIndex, @Nonnull ForceModel forces)
	{
		if(!VehiclePhysicsModule.PAUSE)
		{
			Vec3 motion = GetVelocity();
			motion = forces.ApplyLinearForcesToWheel(motion, wheelIndex, GetWorldToEntity().GetCurrent(), Def.mass);
			motion = forces.ApplySpringForcesToWheel(motion, wheelIndex, GetWorldToEntity().GetCurrent(), Def.mass, vehicle.Hierarchy()::GetWorldToPartCurrent);
			motion = forces.ApplyDampeningToWheel(wheelIndex, motion);
			SetVelocity(motion);
			ApplyVelocity();
		}

		SyncEntityToTransform();

		/*
		Vec3 motion = getDeltaMovement();
		Vec3 localMotion = LocalTransform1.GlobalToLocalDirection(motion);

		// Let's get the stack from the vehicle, but remember, this could be any fully 3D rotation
		// We want wheels to move as Minecraft AABB entities and then
		// report back their findings to our more complex rotation model
		// So a wheel cares about
		//  - Its absolute position (use Entity.position()), which it uses to calculate a relative position
		//  - Its relative steering value (YawParameter)
		// It does not care about
		//  - Entity.rotYaw, rotPitch

		Transform vehicleRoot = parent.Hierarchy().GetRootTransformPrevious();
		Transform rootToAP = parent.Hierarchy().GetArticulationPrevious(Def.attachedTo);
		TransformStack worldToAP = TransformStack.of(vehicleRoot, rootToAP);

		// Now we know how offset the wheel is from its intended position in vehicle space
		Transform restPosAPRelative = Transform.FromPos(Def.physicsOffset);
		Transform wheelPosAPRelative = worldToAP.GlobalToLocalTransform(Transform.FromPos(position()));
		Vec3 returnToRestAPRelative = restPosAPRelative.PositionVec3().subtract(wheelPosAPRelative.PositionVec3());
		Vec3 returnToRestWorld = worldToAP.LocalToGlobalDirection(returnToRestAPRelative);

		// Add a spring force (undampened)
		motion = motion.add(returnToRestWorld.scale(Def.springStrength * 1/20d));
		// Add a gravity force
		motion = motion.add(0d, Def.gravityScale, 0d);
		// Add frictional forces???


		setDeltaMovement(motion);

		// Apply the actual movement, then see what happens with ground checks etc
		move(MoverType.SELF, motion);
		if(onGround())
		{

		}



		// https://en.wikipedia.org/wiki/Slip_(vehicle_dynamics)#Longitudinal_slip
		double longitudinalSpeed = -localMotion.z();
		double spinSpeed = AngularSpeed1 * Def.radius;
		double longitudinalSlip = (spinSpeed - longitudinalSpeed) / longitudinalSpeed;

		double lateralSpeed = localMotion.x();
		double slipAngle = Maths.Atan2(Maths.Abs(longitudinalSpeed), lateralSpeed);

		// uhhh physic

	*/

	public void SetYawParameter(float target)
	{
		TargetYawParameter = target;
	}
	public void SetTorqueParameter(float target)
	{
		TargetTorqueParameter = target;
	}

	// ---------------------------------------------------------------------------------------------------
	// ITransformChildEntity
	@Override @Nonnull public ITransformEntity GetParent() { return GetVehicle(); }
	@Override public int GetIndex() { return GetWheelIndex(); }
	public void SyncTransformToEntity()
	{
		Transform worldRoot = GetWorldTransformCurrent();
		Vector3f euler = worldRoot.Euler();
		setPos(worldRoot.PositionVec3());
		setYRot(euler.y);
		setXRot(euler.x);
	}
	public void SyncEntityToTransform()
	{
		SetWorldTransform(Transform.FromPosAndEuler(getPosition(1f), getXRot(), getYRot(), 0f));
	}
	@Nonnull
	public Vec3 GetVelocity() { return getDeltaMovement().scale(20f); }
	public void SetVelocity(@Nonnull Vec3 velocityMetersPerSecond) { setDeltaMovement(velocityMetersPerSecond.scale(1f/20f)); }
	public void ApplyVelocity()
	{
		// Stash pre-values
		Vec3 prePos = position();
		Vec3 expectedMovement = getDeltaMovement();

		// Apply the movement
		move(MoverType.SELF, getDeltaMovement());
		CheckCollisions();

		// Check where we ended up
		Vec3 postPos = position();
		Vec3 actualMovement = postPos.subtract(prePos);

		// If we collided, add a normal reaction "force".
		// This should not actually be applied in the force model (we are after application now),
		// but it is useful to render it
		if(verticalCollision || horizontalCollision)
		{
			VehicleEntity parent = GetVehicle();
			if(parent != null)
			{
				parent.ForcesLastFrame.AddGlobalForce(ForceModel.Wheel(GetWheelIndex()),
					actualMovement.subtract(expectedMovement).scale(20f * 20f * Def.mass),
					() -> "Normal reaction force");
			}
		}
	}
	// ---------------------------------------------------------------------------------------------------

}
