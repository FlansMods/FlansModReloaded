package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectBreakBlock implements IAbilityEffect
{
	public final float HarvestLevel;

	public AbilityEffectBreakBlock(@Nonnull AbilityEffectDefinition def)
	{
		HarvestLevel = def.ModifyFloat(ModifierDefinition.STAT_TOOL_HARVEST_LEVEL, 1.0f);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		Level level = gun.GetLevel();
		if(level != null)
		{
			float toolLevel = ToolLevel(gun, stacks);
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
	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}

	public float ToolLevel(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		float toolLevel = gun.ModifyFloat(ModifierDefinition.STAT_TOOL_HARVEST_LEVEL, 1.0f);
		if(stacks != null)
		{
			toolLevel *= stacks.GetIntensity();
		}
		return toolLevel;
	}
}
