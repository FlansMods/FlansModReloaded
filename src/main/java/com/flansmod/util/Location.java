package com.flansmod.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class Location
{
	public static final Location IDENTITY = new Location();

	public final Vec3 Position;
	public final Orientation Facing;
	public final float Scale;

	public Location() {
		Position = Vec3.ZERO; Facing = Orientation.IDENTITY; Scale = 1.0f;
	}
	public Location(Vec3 pos) {
		Position = pos; Facing = Orientation.IDENTITY; Scale = 1.0f;
	}
	public Location(Orientation ori) {
		Position = Vec3.ZERO; Facing = ori; Scale = 1.0f;
	}
	public Location(Vec3 pos, Orientation ori) {
		Position = pos; Facing = ori; Scale = 1.0f;
	}
	public Location(Vec3 pos, Orientation ori, float scale) {
		Position = pos; Facing = ori; Scale = scale;
	}
	public Location(Location other)
	{
		Position = other.Position;
		Facing = new Orientation(other.Facing);
		Scale = other.Scale;
	}


	// Operations
	public Vec3 TransformPosition(Vec3 pos)
	{
		return Position;// + pos*Orientation*Scale;
	}


}
