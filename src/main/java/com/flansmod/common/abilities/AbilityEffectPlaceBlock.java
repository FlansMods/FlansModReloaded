package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectPlaceBlock implements IAbilityEffect
{
	@Nullable
	public final BlockState BlockToPlace;

	public AbilityEffectPlaceBlock(@Nonnull AbilityEffectDefinition def)
	{
		String blockID = def.ModifyString(ModifierDefinition.STAT_BLOCK_ID, "");
		if(!blockID.isEmpty())
		{
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockID));
			if(block != null)
			{
				BlockToPlace = block.defaultBlockState();
			}
			else
				BlockToPlace = null;
		}
		else
			BlockToPlace = null;

		// TODO: BlockStates
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		if(BlockToPlace != null)
		{
			Level level = gun.GetLevel();
			if (level != null)
			{
				targets.ForEachPosition((pos) ->
				{
					BlockPos blockPos = BlockPos.containing(pos);
					BlockState existingState = level.getBlockState(blockPos);
					if (existingState.isAir())
					{
						level.setBlockAndUpdate(blockPos, BlockToPlace);
					}
				});
			}
		}
	}
	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}
}
