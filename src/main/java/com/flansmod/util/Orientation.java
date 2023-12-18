package com.flansmod.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Orientation
{
	public static final Orientation IDENTITY = new Orientation();

	private final Quaternionf Impl;

	public Orientation()
	{
		Impl = new Quaternionf();
	}
	public Orientation(Orientation other)
	{
		Impl = new Quaternionf(other.Impl);
	}
	public Orientation(Quaternionf quat)
	{
		Impl = new Quaternionf(quat);
	}
	public Orientation(Orientation parent, Orientation child)
	{
		Impl = parent.Impl.mul(child.Impl, new Quaternionf());
	}

	// Minecraft co-ordinates, where "forward" is -Z
	public Vec3 Forward() { return new Vec3(Impl.transform(new Vector3f(0f, 0f, -1f))); }
	public Vec3 Right() { return new Vec3(Impl.transform(new Vector3f(1f, 0f, 0f))); }
	public Vec3 Up() { return new Vec3(Impl.transform(new Vector3f(0f, 1f, 0f))); }
	public Vec3 ApplyTo(Vec3 pos) { return new Vec3(Impl.transform(pos.toVector3f())); }

	public Orientation Slerp(Orientation other, float t) { return new Orientation(Impl.slerp(other.Impl, t, new Quaternionf())); }
}
