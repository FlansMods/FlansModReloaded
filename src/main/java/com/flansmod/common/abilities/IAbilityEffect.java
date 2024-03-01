package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.stats.StatAccumulator;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.formulae.FloatAccumulation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IAbilityEffect
{
	class StatHolder
	{
		private final String Stat;
		private final StatAccumulator Base;

		public StatHolder(@Nonnull String statId, @Nonnull AbilityEffectDefinition def)
		{
			Stat = statId;
			Base = new StatAccumulator().Stack(def.MatchModifiers(statId));
		}

		public float Get(@Nonnull ActionGroupContext actionGroup, @Nullable AbilityStack stacks)
		{
			// Take our cached accumulator, enter the traitLevel and stackCount
			if(stacks != null)
			{
				return FloatAccumulation.compose(
					Base.CopyWithLevelAndStacks(stacks.Level, stacks.GetStackCount())
						.Calculate(actionGroup.Gun),
					actionGroup.ModifyFloat(Stat)).get();
			}
			else
			{
				return FloatAccumulation.compose(Base.Calculate(actionGroup.Gun),
					actionGroup.ModifyFloat(Stat)).get();
			}
		}
	}

	default void TriggerClient(@Nonnull ActionGroupContext gun,
							   @Nonnull TriggerContext trigger,
							   @Nonnull TargetsContext targets,
							   @Nullable AbilityStack stacks)
	{

	}
	default void TriggerServer(@Nonnull ActionGroupContext gun,
							   @Nonnull TriggerContext trigger,
							   @Nonnull TargetsContext targets,
							   @Nullable AbilityStack stacks)
	{

	}

	default boolean CanBeContinuous() { return false; }
	default void EndClient(@Nonnull GunContext gun,
						   @Nullable AbilityStack stacks)
	{

	}
	default void EndServer(@Nonnull GunContext gun,
						   @Nullable AbilityStack stacks)
	{

	}
	ModifierDefinition[] NO_MODS = new ModifierDefinition[0];
	@Nonnull
	default ModifierDefinition[] GetActiveModifiers() { return NO_MODS; }
}
