package com.flansmod.common.types.guns.elements;

import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.*;

import javax.annotation.Nonnull;

public class AbilityDefinition
{
	@JsonField
	public AbilityTriggerDefinition[] startTriggers = new AbilityTriggerDefinition[0];
	@JsonField
	public AbilityTriggerDefinition[] endTriggers = new AbilityTriggerDefinition[0];
	@JsonField
	public AbilityTargetDefinition[] targets = new AbilityTargetDefinition[0];
	@JsonField
	public AbilityEffectDefinition[] effects = new AbilityEffectDefinition[0];


	@JsonField
	public AbilityStackingDefinition stacking = new AbilityStackingDefinition();

	public boolean IsValid()
	{
		return startTriggers.length > 0
			&& targets.length > 0
			&& effects.length > 0;
	}
	public boolean IsStackable()
	{
		return stacking.IsStackable();
	}

	public boolean MatchTrigger(@Nonnull EAbilityTrigger checkTriggerType, @Nonnull TriggerContext triggerContext)
	{
		for(AbilityTriggerDefinition triggerDef : startTriggers)
			if(triggerDef.Matches(checkTriggerType, triggerContext))
				return true;
		return false;
	}
	@Nonnull
	public TargetsContext MatchTargets(@Nonnull TriggerContext triggerContext)
	{
		TargetsContext targetsContext = new TargetsContext();
		for(AbilityTargetDefinition targetDef : targets)
			targetDef.ApplyTo(triggerContext, targetsContext);
		return targetsContext;
	}
}
