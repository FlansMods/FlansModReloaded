package com.flansmod.common.effects;

import com.flansmod.common.abilities.AbilityStack;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EffectDeleteEntity implements IEffect
{
	public EffectDeleteEntity(@Nonnull AbilityEffectDefinition def)
	{

	}

	@Override
	public void TriggerServer(@Nonnull ActionGroupContext gun, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		targets.ForEachEntity(Entity::kill);
	}
}
