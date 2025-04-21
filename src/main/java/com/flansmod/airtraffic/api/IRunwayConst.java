package com.flansmod.airtraffic.api;

import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface IRunwayConst
{
	@Nonnull UUID getID();
	@Nonnull String getName();

	@Nonnull BlockPos getBlockPos();
	@Nonnull Rotation8 getRotation();

	int getConfiguredWidth();
	int getConfiguredHeight();
	int getConfiguredLength();
	float getConfiguredEntryIncline();
	float getConfiguredExitIncline();

	int getLastKnownClearedWidth();
	int getLastKnownClearedHeight();
	int getLastKnownClearedLength();
	float getLastKnownBestEntryIncline();
	float getLastKnownBestExitIncline();
}
