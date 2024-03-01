package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectExplode implements IAbilityEffect
{
	private final StatHolder ExplosionRadius;
	private final boolean BreakBlocks;

	public AbilityEffectExplode(@Nonnull AbilityEffectDefinition def)
	{
		ExplosionRadius = new StatHolder(Constants.STAT_IMPACT_EXPLOSION_RADIUS, def);
		BreakBlocks = def.ModifyBoolean(Constants.STAT_EXPLOSION_BREAKS_BLOCKS, true);
	}

	@Override
	public void TriggerServer(@Nonnull ActionGroupContext actionGroup, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		Level level = actionGroup.Gun.GetLevel();
		if(level != null)
		{
			DamageSource dmgSource = actionGroup.Gun.CreateDamageSource();
			float radius = ExplosionRadius.Get(actionGroup, stacks);
			targets.ForEachPosition((triggerAt) ->
			{
				level.explode(null, dmgSource, null,triggerAt,  radius, BreakBlocks, Level.ExplosionInteraction.TNT);
			});
		}
	}
}
