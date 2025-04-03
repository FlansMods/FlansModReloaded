package com.flansmod.common.roads;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Long-distance, detached from TileEntity
public class RunwayStorage
{
	public final UUID runwayID;

	public BlockPos runwayBlockPos;
	public int width = 16;
	public int length = 64;
	public List<ResourceLocation> allowedVehicleClasses = new ArrayList<>();

	public RunwayStorage(@Nonnull UUID id)
	{
		runwayID = id;
	}


}
