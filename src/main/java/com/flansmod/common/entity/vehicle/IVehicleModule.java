package com.flansmod.common.entity.vehicle;

import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IVehicleModule extends IVehicleSaveNode
{
	void Tick(@Nonnull VehicleEntity vehicle);
	@Nullable default Container AsInventory() { return null; }


	interface IInterpolator {
		@Nonnull Transform apply(@Nonnull Transform t0, @Nonnull Transform t1);
		float apply(float t0, float t1);
	}
	static IInterpolator Previous = new IInterpolator()
	{
		@Nonnull @Override public Transform apply(@Nonnull Transform t0, @Nonnull Transform t1) { return t0; }
		@Override public float apply(float t0, float t1) { return t0; }
	};
	static IInterpolator Delta(final float dt)
	{
		return new IInterpolator()
		{
			@Nonnull @Override public Transform apply(@Nonnull Transform t0, @Nonnull Transform t1) { return Transform.Interpolate(t0, t1, dt); }
			@Override public float apply(float t0, float t1) { return Maths.LerpF(t0, t1, dt); }
		};
	};
	static IInterpolator Current = new IInterpolator()
	{
		@Nonnull @Override public Transform apply(@Nonnull Transform t0, @Nonnull Transform t1) { return t1; }
		@Override public float apply(float t0, float t1) { return t1; }
	};
}
