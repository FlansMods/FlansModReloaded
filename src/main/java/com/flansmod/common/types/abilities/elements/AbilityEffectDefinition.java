package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.abilities.Abilities;
import com.flansmod.common.abilities.IAbilityEffect;
import com.flansmod.common.gunshots.ModifierStack;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ModifierDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectDefinition
{
	// -- Effect --
	@JsonField
	public EAbilityEffect effectType = EAbilityEffect.Nothing;
	@JsonField(Docs = "The modifiers to add when the effect is active")
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];



	public void Apply(@Nonnull ModifierStack modStack)
	{
		for(ModifierDefinition mod : modifiers)
			modStack.Modify(mod, 1.0f);
	}
	public float ModifyFloat(@Nonnull String key, float baseValue)
	{
		ModifierStack stack = new ModifierStack(key, "");
		Apply(stack);
		return stack.ApplyTo(baseValue);
	}
	public String ModifyString(String key, String defaultValue)
	{
		ModifierStack stack = new ModifierStack(key, "");
		Apply(stack);
		return stack.ApplyTo(defaultValue);
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
}
