package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.abilities.Abilities;
import com.flansmod.common.abilities.IAbilityEffect;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AbilityEffectDefinition
{
	// -- Effect --
	@JsonField
	public EAbilityEffect effectType = EAbilityEffect.Nothing;
	@JsonField(Docs = "The modifiers to add when the effect is active")
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];

	@Nonnull
	public ModifierDefinition[] MatchModifiers(@Nonnull String stat)
	{
		List<ModifierDefinition> matches = new ArrayList<>();
		for(ModifierDefinition modifier : modifiers)
		{
			if(modifier.stat.equals(stat))
				matches.add(modifier);
		}
		return matches.toArray(new ModifierDefinition[0]);
	}
	@Nonnull
	public String ModifyString(@Nonnull String stat, @Nonnull String defaultValue)
	{
		for(ModifierDefinition modifier : modifiers)
		{
			if(modifier.stat.equals(stat) && !modifier.setValue.isEmpty())
				defaultValue = modifier.setValue;
		}
		return defaultValue;
	}
	public boolean ModifyBoolean(@Nonnull String stat, boolean defaultValue)
	{
		for(ModifierDefinition modifier : modifiers)
		{
			if(modifier.stat.equals(stat) && !modifier.setValue.isEmpty())
				return Boolean.parseBoolean(modifier.setValue);
		}
		return defaultValue;
	}

	@Nullable
	private IAbilityEffect EffectProcessor = null;
	@Nonnull
	public IAbilityEffect GetEffectProcessor()
	{
		if(EffectProcessor == null)
			EffectProcessor = Abilities.CreateEffectProcessor(this);
		return EffectProcessor;
	}

	@Nonnull
	public Component GetTooltip(boolean expanded)
	{
		//expanded ? "trigger.expanded." : "trigger.icon."
		String localisationKey = "effect.expanded." + effectType.toString().toLowerCase();
		switch(effectType)
		{
			//case Owner, Shooter, ShotEntity, SplashedEntities -> {
			//	for(ResourceLocation id : matchIDs)
			//	{
			//		//if(condition.conditionType == ETriggerConditionType.CheckActionGroupPath)
			//		//	return Component.translatable(localisationKey, FlanItem.ListOf("action.group_path.", condition.allowedValues));
			//	}
			//	return Component.translatable(localisationKey, Component.translatable("target.any"));
			//}
			default -> {
				return Component.translatable(localisationKey);
			}
		}
	}
}
