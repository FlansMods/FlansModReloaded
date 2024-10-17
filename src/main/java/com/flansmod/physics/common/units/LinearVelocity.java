package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record LinearVelocity(@Nonnull Vec3 Velocity) implements IVelocity
{
	public static final LinearVelocity Zero = new LinearVelocity(Vec3.ZERO);

	@Nonnull
	public static LinearVelocity blocksPerSecond(@Nonnull Vec3 blocksPerSec)
	{
		return new LinearVelocity(Units.Speed.BlocksPerSecond_To_BlocksPerTick(blocksPerSec));
	}
	@Nonnull
	public static LinearVelocity blocksPerTick(@Nonnull Vec3 blocksPerTick)
	{
		return new LinearVelocity(blocksPerTick);
	}

	@Nonnull
	public LinearVelocity add(@Nonnull LinearVelocity other) { return new LinearVelocity(Velocity.add(other.Velocity)); }
	@Nonnull
	public LinearVelocity scale(double scale) { return new LinearVelocity(Velocity.scale(scale)); }
	@Nonnull
	public static LinearVelocity interpolate(LinearVelocity a, LinearVelocity b, double t)
	{
		return new LinearVelocity(a.Velocity.lerp(b.Velocity, t));
	}

	@Nonnull
	public Units.Speed getDefaultUnits() { return Units.Speed.BlocksPerTick; }
	@Nonnull
	public Vec3 convertToUnits(@Nonnull Units.Speed toUnits) { return Units.Speed.Convert(Velocity, Units.Speed.BlocksPerTick, toUnits); }

	@Nonnull
	public Vec3 applyOverTicks(double ticks) { return Velocity.scale(ticks); }
	@Nonnull
	public Vec3 applyOneTick() { return Velocity; }


	@Override @Nonnull
	public LinearVelocity inverse() { return new LinearVelocity(Velocity.scale(-1d)); }
	@Override
	public boolean isApproxZero() { return Velocity.lengthSqr() < Maths.EpsilonSq; }
	@Override
	public boolean hasLinearComponent(@Nonnull Transform actingOn) { return false; }
	@Override @Nonnull
	public LinearVelocity getLinearComponent(@Nonnull Transform actingOn) { return this; }
	@Override
	public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
	@Override @Nonnull
	public AngularVelocity getAngularComponent(@Nonnull Transform actingOn) { return AngularVelocity.Zero; }
	@Override
	public String toString() { return "LinearVelocity ["+Velocity+"]"; }
	@Override @Nonnull
	public Component toFancyString() { return Component.translatable("flansphysicsmod.linear_velocity", Velocity.x, Velocity.y, Velocity.z); }

	public boolean isApprox(@Nonnull LinearVelocity other) { return Maths.approx(other.Velocity, Velocity); }
	public boolean isApprox(@Nonnull LinearVelocity other, double epsilon) { return Maths.approx(other.Velocity, Velocity, epsilon); }
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof LinearVelocity otherLinearV)
			return otherLinearV.Velocity.equals(Velocity);
		return false;
	}
	public void toBuf(@Nonnull FriendlyByteBuf buf)
	{
		buf.writeVector3f(Velocity.toVector3f());
	}
	@Nonnull
	public static LinearVelocity fromBuf(@Nonnull FriendlyByteBuf buf)
	{
		return new LinearVelocity(new Vec3(buf.readVector3f()));
	}
}
