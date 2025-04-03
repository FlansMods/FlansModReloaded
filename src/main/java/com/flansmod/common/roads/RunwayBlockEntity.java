package com.flansmod.common.roads;

import com.flansmod.common.FlansMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RunwayBlockEntity extends BlockEntity
{
	public static final UUID UnassignedRunwayID = new UUID(0L, 0L);
	public UUID runwayID = UnassignedRunwayID;
	public RunwayStorage runwayStorage = null;

	public RunwayBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		super(FlansMod.RUNWAY_MARKER_TILE_ENTITY.get(), pos, state);
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.putUUID("runwayID", runwayID);
	}
	@Override
	public void load(@Nonnull CompoundTag tags)
	{
		super.load(tags);
		runwayID = tags.getUUID("runwayID");
	}

}
