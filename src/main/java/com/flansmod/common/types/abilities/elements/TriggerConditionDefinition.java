package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.JsonField;

import javax.annotation.Nonnull;

public class TriggerConditionDefinition
{
	@JsonField
	public ETriggerConditionType conditionType = ETriggerConditionType.AlwaysAllowed;
	@JsonField
	public String[] allowedValues = new String[0];

	public boolean Matches(@Nonnull TriggerContext triggerContext)
	{
		return true;
	}
}

