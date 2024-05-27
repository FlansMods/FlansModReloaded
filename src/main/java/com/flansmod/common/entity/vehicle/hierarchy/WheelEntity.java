package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.entity.ITransformChildEntity;
import com.flansmod.common.entity.ITransformPair;
import com.flansmod.common.entity.vehicle.ITransformEntity;
import com.flansmod.common.entity.vehicle.PerPartMap;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.ForceModel;
import com.flansmod.common.entity.vehicle.physics.VehiclePhysicsModule;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.common.types.vehicles.elements.WheelDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataSerializer;
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

	public record WheelSyncData(@Nonnull Vec3 WorldPos, float YawParameter, float AngularVelocity)
	{
		private static final WheelSyncData INVALID = new WheelSyncData(Vec3.ZERO, 0, 0);
		public static final EntityDataSerializer<WheelSyncData> SERIALIZER = new EntityDataSerializer.ForValueType<>()
		{
			@Override
			public void write(@Nonnull FriendlyByteBuf buf, @Nonnull WheelSyncData data)
			{
				buf.writeDouble(data.WorldPos.x);
				buf.writeFloat((float)data.WorldPos.y);
				buf.writeDouble(data.WorldPos.z);
				buf.writeFloat(data.YawParameter);
				buf.writeFloat(data.AngularVelocity);
			}
			@Override
			@Nonnull
			public WheelSyncData read(@Nonnull FriendlyByteBuf buf)
			{
				double x = buf.readDouble();
				double y = buf.readFloat();
				double z = buf.readDouble();
				float yawParam = buf.readFloat();
				float angularVelocity = buf.readFloat();
				return new WheelSyncData(new Vec3(x, y, z), yawParam, angularVelocity);
			}
		};
	}
	public static final EntityDataSerializer<PerPartMap<WheelSyncData>> WHEELS_SERIALIZER =
		PerPartMap.SERIALIZER(WheelSyncData.SERIALIZER);


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



	public WheelEntity(@Nonnull VehicleEntity parent, @Nonnull String wheelPath, @Nonnull WheelDefinition def)
	{
		super(parent);
		Def = def;
		WheelPath = wheelPath;
		Size = EntityDimensions.fixed(def.radius, def.radius);
		refreshDimensions();
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
	@Nonnull
	public Transform GetLocalTransformPrevious()
	{

		return Transform.FromPosAndEuler(LocalOffsetFromAPPrevious, 0f, YawParameterPrevious, 0f);
	}
	@Nonnull
	public Transform GetLocalTransformCurrent() { return Transform.FromPosAndEuler(LocalOffsetFromAPCurrent, 0f, YawParameterCurrent, 0f); }
	@Nonnull
	public ITransformPair GetLocalTransform() { return ITransformPair.of(this::GetLocalTransformPrevious, this::GetLocalTransformCurrent); }

	// Synced data
	@Nonnull
	public Vec3 GetRemotePosition() { return GetSyncData().WorldPos; }
	public float GetRemoteYaw() { return GetSyncData().YawParameter; }
	public float GetRemoteVelocity() { return GetSyncData().AngularVelocity; }
	@Nonnull
	public WheelSyncData GetSyncData()
	{
		WheelSyncData data = getParent().Hierarchy().GetWheelData(WheelPath);
		return data != null ? data : WheelSyncData.INVALID;
	}
	public void SetSyncData(@Nonnull WheelSyncData data)
	{
		getParent().Hierarchy().SetWheelData(WheelPath, data);
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
		if (!VehiclePhysicsModule.PAUSE)
		{
			// Move using the Minecraft body, so we can process collisions
			Vec3 motion = GetVelocity();
			motion = forces.ApplyLinearForcesToWheel(motion, wheelIndex, GetWorldToEntity().GetCurrent(), Def.mass);
			motion = forces.ApplySpringForcesToWheel(motion, wheelIndex, GetWorldToEntity().GetCurrent(), Def.mass, vehicle.Hierarchy()::GetWorldToPartCurrent);
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
			TorqueParameterCurrent = Maths.LerpF(TorqueParameterCurrent, TargetTorqueParameter, Def.TorqueLerpRate());
			YawParameterCurrent = Maths.LerpF(YawParameterCurrent, TargetYawParameter, Def.YawLerpRate());
			SetSyncData(new WheelSyncData(LocalOffsetFromAPCurrent, YawParameterCurrent, AngularVelocityCurrent));
		}
		else
		{
			Vec3 lerpTarget = GetRemotePosition();
			lerpTo(lerpTarget.x, lerpTarget.y, lerpTarget.z);
			LocalOffsetFromAPCurrent = LocalOffsetFromAPCurrent.lerp(lerpTarget, 1f);
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
	@Override @Nonnull public ITransformEntity GetParent() { return getParent(); }
	@Override @Nonnull public String GetPathInHierarchy() { return WheelPath; }
	@Override @Nonnull public ITransformPair GetOffsetFromAP() { return ITransformPair.of(this::GetLocalRootPrevious, this::GetLocalRootCurrent); }
	@Nonnull public Transform GetLocalRootPrevious() { return GetLocalTransformPrevious(); }
	@Nonnull public Transform GetLocalRootCurrent() { return GetLocalTransformCurrent(); }
	public void SetOffsetFromAP(@Nonnull Transform currentTransform)
	{
		LocalOffsetFromAPCurrent = currentTransform.PositionVec3();
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
