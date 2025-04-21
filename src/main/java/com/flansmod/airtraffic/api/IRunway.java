package com.flansmod.airtraffic.api;

import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

public interface IRunway extends IRunwayConst
{
	void setName(@Nonnull String newName);

	// Return true if we invalidated the test results
	boolean setBlockPos(@Nonnull BlockPos newBlockPos);
	boolean setRotation(@Nonnull Rotation8 newRotation);
	boolean setConfiguredWidth(int newWidth);
	boolean setConfiguredHieght(int newHeight);
	boolean setConfiguredLength(int newLength);
	boolean setConfiguredEntryIncline(float newEntryIncline);
	boolean setConfiguredExitIncline(float newExitIncline);

	boolean setTestResults(@Nonnull RunwayCheckResult result);
}
