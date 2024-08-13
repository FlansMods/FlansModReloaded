package com.flansmod.util.physics;

import com.flansmod.util.Maths;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;

public class Units
{
	public enum Time
	{
		Ticks,
		Seconds;

		public static final double Ticks_To_Seconds = 20d;
		public static final double Seconds_To_Ticks = 1d/20d;
		public static final double PerTick_To_PerSecond = 1d / Ticks_To_Seconds;
		public static final double PerSecond_To_PerTick = 1d / Seconds_To_Ticks;
		public static final double PerTickSq_To_PerSecondSq = PerTick_To_PerSecond * PerTick_To_PerSecond;
		public static final double PerSecondSq_To_PerTickSq = PerSecond_To_PerTick * PerSecond_To_PerTick;

		public static double Convert(double in, @Nonnull Time from, @Nonnull Time to) {
			if(from == to)
				return in;
			if(from == Time.Ticks && to == Time.Seconds)
				return in * Ticks_To_Seconds;
			if(from == Time.Seconds && to == Time.Ticks)
				return in * Seconds_To_Ticks;
			throw new NotImplementedException();
		}
		public static double ConvertInverse(double in, @Nonnull Time from, @Nonnull Time to) {
			return Convert(in, to, from);
		}
	}

	public enum Distance
	{
		Blocks;



	}

	public enum Speed
	{
		BlocksPerTick,
		BlocksPerSecond;

		public static final double BlocksPerTick_To_BlocksPerSecond = Time.PerTick_To_PerSecond;
		public static final double BlocksPerSecond_To_BlocksPerTick = Time.PerSecond_To_PerTick;
		public static double BlocksPerTick_To_BlocksPerSecond(double in) { return in * BlocksPerTick_To_BlocksPerSecond; }
		public static double BlocksPerSecond_To_BlocksPerTick(double in) { return in * BlocksPerSecond_To_BlocksPerTick; }
		@Nonnull public static Vec3 BlocksPerTick_To_BlocksPerSecond(@Nonnull Vec3 in) { return in.scale(BlocksPerTick_To_BlocksPerSecond); }
		@Nonnull public static Vec3 BlocksPerSecond_To_BlocksPerTick(@Nonnull Vec3 in) { return in.scale(BlocksPerSecond_To_BlocksPerTick); }

		public static double Convert(double in, @Nonnull Speed from, @Nonnull Speed to) {
			if(from == to)
				return in;
			if(from == Speed.BlocksPerSecond && to == Speed.BlocksPerTick)
				return in * BlocksPerSecond_To_BlocksPerTick;
			if(from == Speed.BlocksPerTick && to == Speed.BlocksPerSecond)
				return in * BlocksPerTick_To_BlocksPerSecond;
			throw new NotImplementedException();
		}
		@Nonnull
		public static Vec3 Convert(@Nonnull Vec3 in, @Nonnull Speed from, @Nonnull Speed to) {
			if(from == to)
				return in;
			if(from == Speed.BlocksPerSecond && to == Speed.BlocksPerTick)
				return in.scale(BlocksPerSecond_To_BlocksPerTick);
			if(from == Speed.BlocksPerTick && to == Speed.BlocksPerSecond)
				return in.scale(BlocksPerTick_To_BlocksPerSecond);
			throw new NotImplementedException();
		}
	}
	public enum Acceleration
	{
		BlocksPerTickSquared,
		BlocksPerSecondSquared;

		public static final double BlocksPerTickSq_To_BlocksPerSecondSq = Time.PerTickSq_To_PerSecondSq;
		public static final double BlocksPerSecondSq_To_BlocksPerTickSq = Time.PerSecondSq_To_PerTickSq;

		public static double BlocksPerTickSq_To_BlocksPerSecondSq(double in) { return in * BlocksPerTickSq_To_BlocksPerSecondSq; }
		public static double BlocksPerSecondSq_To_BlocksPerTickSq(double in) { return in * BlocksPerSecondSq_To_BlocksPerTickSq; }
		@Nonnull public static Vec3 BlocksPerTickSq_To_BlocksPerSecondSq(@Nonnull Vec3 in) { return in.scale(BlocksPerTickSq_To_BlocksPerSecondSq); }
		@Nonnull public static Vec3 BlocksPerSecondSq_To_BlocksPerTickSq(@Nonnull Vec3 in) { return in.scale(BlocksPerSecondSq_To_BlocksPerTickSq); }
		public static double Convert(double in, @Nonnull Acceleration from, @Nonnull Acceleration to) {
			if(from == to)
				return in;
			if(from == Acceleration.BlocksPerSecondSquared && to == Acceleration.BlocksPerTickSquared)
				return in * BlocksPerSecondSq_To_BlocksPerTickSq;
			if(from == Acceleration.BlocksPerTickSquared && to == Acceleration.BlocksPerSecondSquared)
				return in * BlocksPerTickSq_To_BlocksPerSecondSq;
			throw new NotImplementedException();
		}
		@Nonnull
		public static Vec3 Convert(@Nonnull Vec3 in, @Nonnull Acceleration from, @Nonnull Acceleration to) {
			if(from == to)
				return in;
			if(from == Acceleration.BlocksPerSecondSquared && to == Acceleration.BlocksPerTickSquared)
				return in.scale(BlocksPerSecondSq_To_BlocksPerTickSq);
			if(from == Acceleration.BlocksPerTickSquared && to == Acceleration.BlocksPerSecondSquared)
				return in.scale(BlocksPerTickSq_To_BlocksPerSecondSq);
			throw new NotImplementedException();
		}
	}

	public enum Force
	{
		KgBlocksPerTickSquared,		// Like Minecraft-Newtons?
		KgBlocksPerSecondSquared;	// Like regular Newtons

		public static final double KgBlocksPerTickSq_To_KgBlocksPerSecondSq = Time.PerTickSq_To_PerSecondSq;
		public static final double KgBlocksPerSecondSq_To_KgBlocksPerTickSq = Time.PerSecondSq_To_PerTickSq;

		public static double KgBlocksPerTickSq_To_KgBlocksPerSecondSq(double in) { return in * KgBlocksPerTickSq_To_KgBlocksPerSecondSq; }
		public static double KgBlocksPerSecondSq_To_KgBlocksPerTickSq(double in) { return in * KgBlocksPerSecondSq_To_KgBlocksPerTickSq; }
		@Nonnull public static Vec3 KgBlocksPerTickSq_To_KgBlocksPerSecondSq(@Nonnull Vec3 in) { return in.scale(KgBlocksPerTickSq_To_KgBlocksPerSecondSq); }
		@Nonnull public static Vec3 KgBlocksPerSecondSq_To_KgBlocksPerTickSq(@Nonnull Vec3 in) { return in.scale(KgBlocksPerSecondSq_To_KgBlocksPerTickSq); }
		public static double Convert(double in, @Nonnull Force from, @Nonnull Force to) {
			if(from == to)
				return in;
			if(from == Force.KgBlocksPerSecondSquared && to == Force.KgBlocksPerTickSquared)
				return in * KgBlocksPerSecondSq_To_KgBlocksPerTickSq;
			if(from == Force.KgBlocksPerTickSquared && to == Force.KgBlocksPerSecondSquared)
				return in * KgBlocksPerTickSq_To_KgBlocksPerSecondSq;
			throw new NotImplementedException();
		}
		@Nonnull
		public static Vec3 Convert(@Nonnull Vec3 in, @Nonnull Force from, @Nonnull Force to) {
			if(from == to)
				return in;
			if(from == Force.KgBlocksPerSecondSquared && to == Force.KgBlocksPerTickSquared)
				return in.scale(KgBlocksPerSecondSq_To_KgBlocksPerTickSq);
			if(from == Force.KgBlocksPerTickSquared && to == Force.KgBlocksPerSecondSquared)
				return in.scale(KgBlocksPerTickSq_To_KgBlocksPerSecondSq);
			throw new NotImplementedException();
		}
	}

	public enum Angle
	{
		Radians,
		Degrees;

		public static final double Radians_To_Degrees = Maths.RadToDeg;
		public static final double Degrees_To_Radians = Maths.DegToRad;

		public static double Convert(double in, @Nonnull Angle from, @Nonnull Angle to) {
			if(from == to)
				return in;
			if(from == Angle.Radians && to == Angle.Degrees)
				return in * Radians_To_Degrees;
			if(from == Angle.Degrees && to == Angle.Radians)
				return in * Degrees_To_Radians;
			throw new NotImplementedException();
		}
	}

	public enum AngularSpeed
	{
		RadiansPerTick,
		RadiansPerSecond,
		DegreesPerTick,
		DegreesPerSecond;

		@Nonnull
		public Angle AngleUnit()
		{
			return switch(this)
			{
				case RadiansPerTick, RadiansPerSecond -> Angle.Radians;
				case DegreesPerTick, DegreesPerSecond -> Angle.Degrees;
			};
		}
		@Nonnull
		public Time TimeUnit()
		{
			return switch(this)
				{
					case DegreesPerSecond, RadiansPerSecond -> Time.Seconds;
					case DegreesPerTick, RadiansPerTick -> Time.Ticks;
				};
		}


		public static final double RadiansPerTick_To_RadiansPerSecond = Time.PerTick_To_PerSecond;
		public static final double RadiansPerSecond_To_RadiansPerTick = Time.PerSecond_To_PerTick;
		public static double RadiansPerTick_To_RadiansPerSecond(double in) { return in * Time.PerTick_To_PerSecond; }
		public static double RadiansPerSecond_To_RadiansPerTick(double in) { return in * Time.PerSecond_To_PerTick; }

		public static double Convert(double in, @Nonnull AngularSpeed from, @Nonnull AngularSpeed to)
		{
			in = Angle.Convert(in, from.AngleUnit(), to.AngleUnit());
			in = Time.ConvertInverse(in, from.TimeUnit(), to.TimeUnit());
			return in;
		}
	}
	public enum AngularAcceleration
	{
		RadiansPerTickSq,
		RadiansPerSecondSq,
		DegreesPerTickSq,
		DegreesPerSecondSq;

		@Nonnull
		public Angle AngleUnit()
		{
			return switch(this)
				{
					case RadiansPerTickSq, RadiansPerSecondSq -> Angle.Radians;
					case DegreesPerTickSq, DegreesPerSecondSq -> Angle.Degrees;
				};
		}
		@Nonnull
		public Time TimeUnit()
		{
			return switch(this)
				{
					case DegreesPerSecondSq, RadiansPerSecondSq -> Time.Seconds;
					case DegreesPerTickSq, RadiansPerTickSq -> Time.Ticks;
				};
		}

		public static final double RadiansPerTickSq_To_RadiansPerSecondSq = Time.PerTickSq_To_PerSecondSq;
		public static final double RadiansPerSecondSq_To_RadiansPerTickSq = Time.PerSecondSq_To_PerTickSq;

		public static double Convert(double in, @Nonnull AngularAcceleration from, @Nonnull AngularAcceleration to)
		{
			in = Angle.Convert(in, from.AngleUnit(), to.AngleUnit());
			in = Time.ConvertInverse(in, from.TimeUnit(), to.TimeUnit());
			in = Time.ConvertInverse(in, from.TimeUnit(), to.TimeUnit());
			return in;
		}
	}
	public enum Torque
	{
		KgBlocksSqPerTickSq,	// Minecraft-Newton-Blocks
		KgBlocksSqPerSecondSq;	// Newton-metres

		@Nonnull
		public Time TimeUnit()
		{
			return switch(this) {
				case KgBlocksSqPerSecondSq -> Time.Seconds;
				case KgBlocksSqPerTickSq -> Time.Ticks;
			};
		}

		public static final double KgBlocksSqPerTickSq_To_KgBlocksSqPerSecondSq = Time.PerTickSq_To_PerSecondSq;
		public static final double KgBlocksSqPerSecondSq_To_KgBlocksSqPerTickSq = Time.PerSecondSq_To_PerTickSq;

		public static double Convert(double in, @Nonnull Torque from, @Nonnull Torque to)
		{
			in = Time.ConvertInverse(in, from.TimeUnit(), to.TimeUnit());
			in = Time.ConvertInverse(in, from.TimeUnit(), to.TimeUnit());
			return in;
		}
	}
}
