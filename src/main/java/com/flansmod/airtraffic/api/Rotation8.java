package com.flansmod.airtraffic.api;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public enum Rotation8
{
	N, NE, E, SE, S, SW, W, NW;

	public static final float SEGMENT_DEG = 360f / 8f;
	public static final float HALF_SEGMENT_DEG = SEGMENT_DEG / 2f;


	public static int count() { return 8; }

	@Nonnull
	public static Rotation8 fromAngle(float yRot)
	{
		// We do +Half, then Floor, which is equivalent to Round
		// But in doing so before modulo, we move the range that would map to [337.5,360) to [0,22.5)
		yRot += HALF_SEGMENT_DEG;
		yRot %= 360f;
		if(yRot < 0f)
			yRot += 360f;

		// Should be in the range [0, 360) now
		int segmentIndex = (int)Math.floor(yRot / SEGMENT_DEG);
		return Rotation8.values()[segmentIndex];
	}
}
