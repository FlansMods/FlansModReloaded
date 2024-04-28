package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.entity.vehicle.ITransformEntity;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.common.types.vehicles.elements.WheelDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
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

	public void ParentTick(@Nonnull VehicleEntity vehicle)
	{
		// Stash last frame's parameters
		TorqueParameter0 = TorqueParameter1;
		YawParameter0 = YawParameter1;

		// Lerp our parameters to the player requested parameters
		TorqueParameter1 = Maths.LerpF(TorqueParameter1, TargetTorqueParameter, Def.TorqueLerpRate());
		YawParameter1 = Maths.LerpF(YawParameter1, TargetYawParameter, Def.YawLerpRate());


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
