package com.flansmod.airtraffic.common.airport;

import com.flansmod.airtraffic.api.IAirport;
import com.flansmod.airtraffic.api.IRunway;
import com.flansmod.airtraffic.api.IRunwayConst;
import com.flansmod.airtraffic.api.RunwayCheckResult;
import com.flansmod.airtraffic.common.runway.RunwayInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

public class AirportInstance implements IAirport
{
	public BlockPos controllerBlockPos;
	public String name = "";
	public RunwayCheckResult largestRunway = null;
	public List<UUID> runwayIDs = new ArrayList<>();
	public UUID ownerID = new UUID(0L, 0L);
	public Map<UUID, RunwayInstance> loadedRunways = new HashMap<>();

	// Const accessors
	@Override @Nonnull
	public String getName() { return name; }
	@Override @Nonnull
	public BlockPos getControllerBlockPos() { return controllerBlockPos; }
	@Override
	public int getNumRunways() { return runwayIDs.size(); }
	@Override @Nonnull
	public UUID getRunwayID(int index) { return runwayIDs.get(index); }
	@Override @Nullable
	public RunwayCheckResult getLargestRunway() { return largestRunway; }
	@Override @Nullable
	public IRunwayConst tryLoadRunway(int index)
	{
		return tryLoadRunwayForEdit(index);
	}
	@Override @Nonnull
	public UUID getOwnerPlayerID() { return ownerID; }

	// Edits
	@Override
	public void setName(@Nonnull String newName)
	{
		name = newName;
	}
	@Override @Nullable
	public IRunway tryLoadRunwayForEdit(int index)
	{
		UUID runwayID = getRunwayID(index);
		if(loadedRunways.containsKey(runwayID))
			return loadedRunways.get(runwayID);
		return null;
	}
	@Override
	public void transferOwnership(@Nonnull UUID playerID)
	{
		ownerID = playerID;
	}


	// Save/Load
	public void saveTo(@Nonnull CompoundTag tags)
	{
		tags.putString("name", name);
		tags.putIntArray("pos", new int[] { controllerBlockPos.getX(), controllerBlockPos.getY(), controllerBlockPos.getZ() });
		tags.putUUID("owner", ownerID);
		ListTag runwaysList = new ListTag();
		for(UUID runwayID : runwayIDs)
			runwaysList.add(NbtUtils.createUUID(runwayID));
		tags.put("runways", runwaysList);
	}
	public void loadFrom(@Nonnull CompoundTag tags)
	{
		name = tags.getString("name");
		int[] posArray = tags.getIntArray("pos");
		if(posArray.length == 3)
			controllerBlockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
		ownerID = tags.getUUID("owner");
		ListTag runwaysList = tags.getList("runways", 10);
		runwayIDs.clear();
		for(Tag tag : runwaysList)
		{
			runwayIDs.add(NbtUtils.loadUUID(tag));
		}
	}

}
