package com.flansmod.airtraffic.common.runway;

import com.flansmod.airtraffic.api.IRunway;
import com.flansmod.airtraffic.api.Rotation8;
import com.flansmod.airtraffic.api.RunwayCheckResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.RotationSegment;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Long-distance, detached from TileEntity
public class RunwayInstance implements IRunway
{
	public final UUID runwayID;

	public String name;
	public BlockPos runwayBlockPos;
	public Rotation8 rotation;
	public int width = 16;
	public int height = 32;
	public int length = 64;
	public float entryIncline = 1.0f;
	public float exitIncline = 1.0f;
	public RunwayCheckResult validation = null;
	public List<ResourceLocation> allowedVehicleClasses = new ArrayList<>();

	public RunwayInstance(@Nonnull UUID id)
	{
		runwayID = id;
	}



	@Override @Nonnull
	public UUID getID() { return runwayID; }
	@Override @Nonnull
	public String getName() { return name; }
	@Override @Nonnull
	public BlockPos getBlockPos() { return runwayBlockPos; }
	@Override @Nonnull
	public Rotation8 getRotation() { return rotation; }

	@Override public int getConfiguredWidth() { return width; }
	@Override public int getConfiguredHeight() { return height; }
	@Override public int getConfiguredLength() { return length; }
	@Override public float getConfiguredEntryIncline() { return entryIncline; }
	@Override public float getConfiguredExitIncline() { return exitIncline; }

	@Override public int getLastKnownClearedWidth() { return validation != null ? validation.width() : 0; }
	@Override public int getLastKnownClearedHeight() { return validation != null ? validation.height() : 0; }
	@Override public int getLastKnownClearedLength() { return validation != null ? validation.length() : 0; }
	@Override public float getLastKnownBestEntryIncline() { return validation != null ? validation.minEntryIncline() : 0; }
	@Override public float getLastKnownBestExitIncline() { return validation != null ? validation.minExitIncline() : 0; }

	// Return true if we invalidated the test results
	@Override
	public boolean setBlockPos(@Nonnull BlockPos newBlockPos)
	{
		boolean needTest = !runwayBlockPos.equals(newBlockPos);
		runwayBlockPos = newBlockPos;
		return needTest;
	}
	@Override
	public boolean setRotation(@Nonnull Rotation8 newRotation)
	{
		boolean needTest = !rotation.equals(newRotation);
		rotation = newRotation;
		return needTest;
	}
	@Override
	public boolean setConfiguredWidth(int newWidth)
	{
		boolean needTest = newWidth > width;
		width = newWidth;
		return needTest;
	}
	@Override
	public boolean setConfiguredHieght(int newHeight)
	{
		boolean needTest = newHeight > height;
		height = newHeight;
		return needTest;
	}
	@Override
	public boolean setConfiguredLength(int newLength)
	{
		boolean needTest = newLength > length;
		length = newLength;
		return needTest;
	}
	@Override
	public boolean setConfiguredEntryIncline(float newEntryIncline)
	{
		boolean needTest = newEntryIncline < entryIncline;
		entryIncline = newEntryIncline;
		return needTest;
	}
	@Override
	public boolean setConfiguredExitIncline(float newExitIncline)
	{
		boolean needTest = newExitIncline < exitIncline;
		exitIncline = newExitIncline;
		return needTest;
	}

	@Override
	public boolean setTestResults(@Nonnull RunwayCheckResult result)
	{
		validation = result;
		return true;
	}
	@Override
	public void setName(@Nonnull String newName)
	{
		name = newName;
	}

	// Save/Load
	public void saveTo(@Nonnull CompoundTag tags)
	{
		tags.putString("name", name);
		tags.putIntArray("pos", new int[] { runwayBlockPos.getX(), runwayBlockPos.getY(), runwayBlockPos.getZ() });
	}
	public void loadFrom(@Nonnull CompoundTag tags)
	{
		name = tags.getString("name");
		int[] posArray = tags.getIntArray("pos");
		if(posArray.length == 3)
			runwayBlockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
	}

}
