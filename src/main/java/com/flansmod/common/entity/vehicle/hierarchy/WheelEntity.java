package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.entity.ITransformChildEntity;
import com.flansmod.common.entity.ITransformPair;
import com.flansmod.common.entity.vehicle.ITransformEntity;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.ForceModel;
import com.flansmod.common.entity.vehicle.physics.VehiclePhysicsModule;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.common.types.vehicles.elements.WheelDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class WheelEntity extends PartEntity<VehicleEntity> implements ITransformChildEntity
{
	@Nonnull public final WheelDefinition Def;
	@Nonnull public final String WheelPath;
	@Nonnull public final EntityDimensions Size;

	@Nonnull
	public Transform LocalTransformPrevious;
	@Nonnull
	public Transform LocalTransformCurrent;

	// From -1 to 1, this is a raw input from the controller
	// The WheelDefinition will tell what to do with this value
	public float TargetYawParameter = 0.0f;
	public float YawParameter0 = 0.0f;
	public float YawParameter1 = 0.0f;

	public float TargetTorqueParameter = 0.0f;
	public float TorqueParameter0 = 0.0f;
	public float TorqueParameter1 = 0.0f;

	// In radians
	public float AngularSpeed0 = 0.0f;
	public float AngularSpeed1 = 0.0f;



	public WheelEntity(@Nonnull VehicleEntity parent, @Nonnull String wheelPath, @Nonnull WheelDefinition def)
	{
		super(parent);
		Def = def;
		WheelPath = wheelPath;
		Size = EntityDimensions.fixed(def.radius, def.radius);
		refreshDimensions();
		LocalTransformPrevious = Transform.Identity();
		LocalTransformCurrent = Transform.Identity();
		blocksBuilding = true;
		SyncTransformToEntity();
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
		return this == other || getParent() == other;
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
	public ItemStack getPickResult() {
		return getParent().getPickResult();
	}

	@Nonnull
	@Override
	public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand)
	{
		return getParent().interact(player, hand);
	}

	@Override
	protected void defineSynchedData()
	{

	}

	@Override
	protected void readAdditionalSaveData(@Nonnull CompoundTag tags)
	{

	}

	@Override
	protected void addAdditionalSaveData(@Nonnull CompoundTag tags)
	{

	}

	public void ParentTick(@Nonnull VehicleEntity parent)
	{
		LocalTransformPrevious = LocalTransformCurrent;
		setOldPosAndRot();

		// Stash last frame's parameters
		TorqueParameter0 = TorqueParameter1;
		YawParameter0 = YawParameter1;

		// Lerp our parameters to the player requested parameters
		TorqueParameter1 = Maths.LerpF(TorqueParameter1, TargetTorqueParameter, Def.TorqueLerpRate());
		YawParameter1 = Maths.LerpF(YawParameter1, TargetYawParameter, Def.YawLerpRate());

		AngularSpeed0 = AngularSpeed1;

		//PhysicsTick(parent);
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

	}

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
	@Override @Nonnull public ITransformEntity GetParent() { return getParent(); }
	@Override @Nonnull public String GetPathInHierarchy() { return WheelPath; }
	@Override @Nonnull public ITransformPair GetOffsetFromAP() { return ITransformPair.of(this::GetLocalRootPrevious, this::GetLocalRootCurrent); }
	@Nonnull public Transform GetLocalRootPrevious() { return LocalTransformPrevious; }
	@Nonnull public Transform GetLocalRootCurrent() { return LocalTransformCurrent; }
	public void SetOffsetFromAP(@Nonnull Transform currentTransform)
	{
		LocalTransformCurrent = currentTransform;
	}
	public void SyncTransformToEntity()
	{
		Transform worldRoot = GetWorldToEntity().GetCurrent();
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
		move(MoverType.SELF, getDeltaMovement());
		CheckCollisions();
		SyncEntityToTransform();
	}
	// ---------------------------------------------------------------------------------------------------

}
