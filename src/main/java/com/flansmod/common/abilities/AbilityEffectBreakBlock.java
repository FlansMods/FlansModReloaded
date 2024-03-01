package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.world.level.Level;

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
	public void TriggerServer(@Nonnull GunContext gun, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		Level level = gun.GetLevel();
		if(level != null)
		{
			float toolLevel = HarvestLevel.Get(gun, stacks);
			targets.ForEachBlock((blockPos, blockState) ->
			{
				if (blockState.canEntityDestroy(level, blockPos, gun.GetShooter().Owner()))
				{
					if (blockState.getBlock().defaultDestroyTime() <= toolLevel)
						level.destroyBlock(blockPos, true, gun.GetShooter().Owner());
				}
			});
		}
	}
}
