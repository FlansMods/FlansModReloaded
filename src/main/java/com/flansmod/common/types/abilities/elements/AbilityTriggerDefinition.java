package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.JsonField;
import net.minecraft.network.chat.Component;

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

	@Nonnull
	public Component GetTooltip(boolean expanded)
	{
		//expanded ? "trigger.expanded." : "trigger.icon."
		String localisationKey = "trigger.expanded." + triggerType.toString().toLowerCase();
		switch(triggerType)
		{
			case StartActionGroup, TriggerActionGroup, EndActionGroup -> {
				for(TriggerConditionDefinition condition : triggerConditions)
				{
					if(condition.conditionType == ETriggerConditionType.CheckActionGroupPath)
						return Component.translatable(localisationKey, FlanItem.ListOf("action.group_path.", condition.allowedValues));
				}
				return Component.translatable(localisationKey, Component.translatable("trigger.any"));
			}
			default -> {
				return Component.translatable(localisationKey);
			}
		}
	}
}
