package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

// Internally, always store as Blocks/Tick^2
public record LinearAcceleration(@Nonnull Vec3 Acceleration)
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
		Vec3 uPerS = u.ConvertToUnits(Units.Speed.BlocksPerSecond);
		Vec3 vPerS = v.ConvertToUnits(Units.Speed.BlocksPerSecond);
		return blocksPerSecondSq(vPerS.subtract(uPerS).scale(1d/seconds));
	}
	@Nonnull
	public static LinearAcceleration fromUtoVinTicks(@Nonnull LinearVelocity u, @Nonnull LinearVelocity v, int ticks)
	{
		if(ticks == 0)
			return Zero;

		// v = u+at, a=(v-u)/t
		Vec3 uPerT = u.ConvertToUnits(Units.Speed.BlocksPerTick);
		Vec3 vPerT = v.ConvertToUnits(Units.Speed.BlocksPerTick);
		return blocksPerTickSq(vPerT.subtract(uPerT).scale(1d/ticks));
	}
	@Nonnull
	public static LinearAcceleration reaction(@Nonnull LinearVelocity inputV, @Nonnull LinearVelocity resultV)
	{
		return fromUtoVinTicks(inputV, resultV, 1);
	}

	@Nonnull
	public Vec3 GetTicksPerSecondSq() { return Acceleration; }
	@Nonnull
	public Vec3 GetBlocksPerSecondSq() { return GetInUnits(Units.Acceleration.BlocksPerSecondSquared); }

	@Nonnull
	public LinearForce MultiplyBy(double mass) { return new LinearForce(Acceleration.scale(mass)); }
	@Nonnull
	public Units.Acceleration GetDefaultUnits() { return Units.Acceleration.BlocksPerTickSquared; }
	@Nonnull
	public Vec3 GetInUnits(@Nonnull Units.Acceleration toUnit)
	{
		return Units.Acceleration.Convert(Acceleration, Units.Acceleration.BlocksPerTickSquared, toUnit);
	}
	@Nonnull
	public LinearVelocity ApplyOverTicks(double ticks) { return new LinearVelocity(Acceleration.scale(ticks)); }
	@Nonnull
	public LinearVelocity ApplyOneTick() { return new LinearVelocity(Acceleration); }


	public boolean IsApproxZero() { return Acceleration.lengthSqr() < Maths.EpsilonSq; }


	@Override
	public String toString() { return "LinearAcceleration ["+Acceleration+"] blocks/tick^2"; }
	public Component toFancyString() { return Component.translatable("flansphysicsmod.linear_acceleration", Acceleration.x, Acceleration.y, Acceleration.z); }
}
