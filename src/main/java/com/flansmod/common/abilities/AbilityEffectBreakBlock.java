package com.flansmod.common.abilities;

import com.flansmod.common.FlansModConfig;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectBreakBlock implements IAbilityEffect
{
	private final StatHolder HarvestLevel;

	public AbilityEffectBreakBlock(@Nonnull AbilityEffectDefinition def)
	{
		HarvestLevel = new StatHolder(Constants.STAT_TOOL_HARVEST_LEVEL, def);
	}

	@Override
	public void TriggerServer(@Nonnull ActionGroupContext actionGroup, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		// Server config hook
		if(!FlansModConfig.AllowBulletsBreakBlocks.get())
			return;
		// ------------------

		Level level = actionGroup.Gun.GetLevel();
		if(level != null)
		{
			float toolLevel = HarvestLevel.Get(actionGroup, stacks);
			targets.ForEachBlock((blockPos, blockState) ->
			{
				if (blockState.canEntityDestroy(level, blockPos, actionGroup.Gun.GetShooter().Owner()))
				{
					if (blockState.getBlock().defaultDestroyTime() <= toolLevel)
						level.destroyBlock(blockPos, true, actionGroup.Gun.GetShooter().Owner());
				}
			});
		}
	}
}
