package com.flansmod.common.types.guns.elements;

import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.*;
import net.minecraft.network.chat.Component;

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

	@Nonnull
	public Component GetTriggerTooltip(boolean expanded)
	{
		Component[] startTriggerTooltips = new Component[startTriggers.length];
		for(int i = 0; i < startTriggers.length; i++)
			startTriggerTooltips[i] = startTriggers[i].GetTooltip(expanded);

		Component fromComponent = FlanItem.ListOf(startTriggerTooltips);
		Component triggerComponent = fromComponent;
		if(endTriggers.length > 0)
		{
			Component[] endTriggerTooltips = new Component[endTriggers.length];
			for(int i = 0; i < endTriggers.length; i++)
				endTriggerTooltips[i] = endTriggers[i].GetTooltip(expanded);

			Component toComponent = FlanItem.ListOf(endTriggerTooltips);
			triggerComponent = Component.translatable("trigger.expanded.to_from", fromComponent, toComponent);
		}

		return triggerComponent;
	}

	@Nonnull
	public Component GetTargetTooltip(boolean expanded)
	{
		Component[] targetTooltips = new Component[targets.length];
		for(int i = 0; i < targets.length; i++)
			targetTooltips[i] = targets[i].GetTooltip(expanded);

		return FlanItem.ListOf(targetTooltips);
	}

	@Nonnull
	public Component GetEffectTooltip(boolean expanded)
	{
		Component[] effectTooltips = new Component[effects.length];
		for(int i = 0; i < effects.length; i++)
			effectTooltips[i] = effects[i].GetTooltip(expanded);

		return FlanItem.ListOf(effectTooltips);
	}

	@Nonnull
	public Component GetTooltip(boolean expanded)
	{
		Component triggers = GetTriggerTooltip(expanded);
		Component targets = GetTargetTooltip(expanded);
		Component effects = GetEffectTooltip(expanded);

		return Component.translatable(expanded ? "trigger.compose.expanded" : "trigger.compose.icon", triggers, targets, effects);
	}
}
