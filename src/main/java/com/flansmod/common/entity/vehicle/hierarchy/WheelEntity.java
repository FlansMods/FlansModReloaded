package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.entity.vehicle.ITransformEntity;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.common.types.vehicles.elements.WheelDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WheelEntity extends PartEntity<VehicleEntity> implements ITransformEntity
{
	@Nonnull
	public Transform LocalTransform0;
	@Nonnull
	public Transform LocalTransform1;

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

	@Nonnull
	@Override
	public Transform GetLocal0() { return LocalTransform0; }
	@Nonnull
	@Override
	public Transform GetLocal(float dt) { return Transform.Interpolate(LocalTransform0, LocalTransform1, dt); }
	@Nullable
	@Override
	public ITransformEntity GetParent() { return getParent(); }

	@Nonnull
	public final WheelDefinition Def;

	public WheelEntity(@Nonnull VehicleEntity parent, @Nonnull WheelDefinition def)
	{
		super(parent);
		Def = def;
		LocalTransform0 = Transform.Identity();
		LocalTransform1 = Transform.Identity();
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
		// Stash last frame's parameters
		TorqueParameter0 = TorqueParameter1;
		YawParameter0 = YawParameter1;

		// Lerp our parameters to the player requested parameters
		TorqueParameter1 = Maths.LerpF(TorqueParameter1, TargetTorqueParameter, Def.TorqueLerpRate());
		YawParameter1 = Maths.LerpF(YawParameter1, TargetYawParameter, Def.YawLerpRate());

		AngularSpeed0 = AngularSpeed1;

		PhysicsTick(parent);
	}

	private void PhysicsTick(@Nonnull VehicleEntity parent)
	{
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

		Transform vehicleRoot = parent.Hierarchy().GetRootTransform(0f);
		Transform rootToAP = parent.Hierarchy().GetArticulationTransformLocal(Def.attachedTo, 0f);
		TransformStack worldToAP = TransformStack.of(vehicleRoot, rootToAP);

		// Now we know how offset the wheel is from its intended position in vehicle space
		Transform restPosAPRelative = Transform.FromPos(Def.physicsOffset);
		Transform wheelPosAPRelative = worldToAP.GlobalToLocalTransform(Transform.FromPos(position()));
		Vec3 returnToRestAPRelative = restPosAPRelative.PositionVec3().subtract(wheelPosAPRelative.PositionVec3());
		Vec3 returnToRestWorld = worldToAP.LocalToGlobalDirection(returnToRestAPRelative);

		// Add a spring force (undampened)
		motion = motion.add(returnToRestWorld.normalize().scale(Def.springStrength));
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


	}

	public void SetYawParameter(float target)
	{
		TargetYawParameter = target;
	}
	public void SetTorqueParameter(float target)
	{
		TargetTorqueParameter = target;
	}
}
