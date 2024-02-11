package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.JsonField;

import javax.annotation.Nonnull;

public class AbilityTriggerDefinition
{
	// --
	@JsonField
	public EAbilityTrigger triggerType = EAbilityTrigger.Instant;
	@JsonField
	public TriggerConditionDefinition[] triggerConditions = new TriggerConditionDefinition[0];

	public boolean Matches(@Nonnull EAbilityTrigger checkTriggerType)
	{
		return triggerType == checkTriggerType;
	}
	public boolean Matches(@Nonnull EAbilityTrigger checkTriggerType, @Nonnull TriggerContext triggerContext)
	{
		if(triggerType != checkTriggerType)
			return false;
		for(TriggerConditionDefinition conditionDef : triggerConditions)
			if(!conditionDef.Matches(triggerContext))
				return false;

		return true;
	}
}
