package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

// Internally, always store as Blocks/Tick^2
public record LinearAcceleration(@Nonnull Vec3 Acceleration) implements IAcceleration
{
	public static final LinearAcceleration Zero = new LinearAcceleration(Vec3.ZERO);

	@Nonnull
	public static LinearAcceleration blocksPerSecondSq(@Nonnull Vec3 blocksPerSecSq)
	{
		return new LinearAcceleration(Units.Acceleration.BlocksPerSecondSq_To_BlocksPerTickSq(blocksPerSecSq));
	}
	@Nonnull
	public static LinearAcceleration blocksPerTickSq(@Nonnull Vec3 blocksPerTickSq)
	{
		return new LinearAcceleration(blocksPerTickSq);
	}
	@Nonnull
	public static LinearAcceleration fromUtoVinSeconds(@Nonnull LinearVelocity u, @Nonnull LinearVelocity v, double seconds)
	{
		if(Maths.Approx(seconds, 0d))
			return Zero;

		// v = u+at, a=(v-u)/t
		Vec3 uPerS = u.convertToUnits(Units.Speed.BlocksPerSecond);
		Vec3 vPerS = v.convertToUnits(Units.Speed.BlocksPerSecond);
		return blocksPerSecondSq(vPerS.subtract(uPerS).scale(1d/seconds));
	}
	@Nonnull
	public static LinearAcceleration fromUtoVinTicks(@Nonnull LinearVelocity u, @Nonnull LinearVelocity v, int ticks)
	{
		if(ticks == 0)
			return Zero;

		// v = u+at, a=(v-u)/t
		Vec3 uPerT = u.convertToUnits(Units.Speed.BlocksPerTick);
		Vec3 vPerT = v.convertToUnits(Units.Speed.BlocksPerTick);
		return blocksPerTickSq(vPerT.subtract(uPerT).scale(1d/ticks));
	}
	@Nonnull
	public static LinearAcceleration reaction(@Nonnull LinearVelocity inputV, @Nonnull LinearVelocity resultV)
	{
		return fromUtoVinTicks(inputV, resultV, 1);
	}

	@Nonnull
	public Vec3 getTicksPerSecondSq() { return Acceleration; }
	@Nonnull
	public Vec3 getBlocksPerSecondSq() { return getInUnits(Units.Acceleration.BlocksPerSecondSquared); }

	@Nonnull
	public LinearForce multiplyBy(double mass) { return new LinearForce(Acceleration.scale(mass)); }
	@Nonnull
	public Units.Acceleration getDefaultUnits() { return Units.Acceleration.BlocksPerTickSquared; }
	@Nonnull
	public Vec3 getInUnits(@Nonnull Units.Acceleration toUnit)
	{
		return Units.Acceleration.Convert(Acceleration, Units.Acceleration.BlocksPerTickSquared, toUnit);
	}
	@Nonnull
	public LinearVelocity applyOverTicks(double ticks) { return new LinearVelocity(Acceleration.scale(ticks)); }
	@Nonnull
	public LinearVelocity applyOneTick() { return new LinearVelocity(Acceleration); }

	@Override @Nonnull
	public LinearAcceleration inverse() { return new LinearAcceleration(Acceleration.scale(-1d)); }
	@Override
	public boolean isApproxZero() { return Acceleration.lengthSqr() < Maths.EpsilonSq; }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public LinearAcceleration getLinearComponent(@Nonnull Transform actingOn) { return this; }
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public AngularAcceleration getAngularComponent(@Nonnull Transform actingOn) { return AngularAcceleration.Zero; }
	@Override
	public String toString() { return "LinearAcceleration ["+Acceleration+"] blocks/tick^2"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysicsmod.linear_acceleration", Acceleration.x, Acceleration.y, Acceleration.z); }
}
