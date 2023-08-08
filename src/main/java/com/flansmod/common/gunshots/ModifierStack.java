package com.flansmod.common.gunshots;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.elements.ModifierDefinition;

public class ModifierStack
{
	private final String Stat;
	private final EActionInput InputType;
	private float Add = 0.0f;
	private float Multiply = 1.0f;
	private String SetValue = "";

	public ModifierStack(String stat, EActionInput inputType)
	{
		InputType = inputType;
		Stat = stat;
	}

	public ModifierStack Apply(ModifierDefinition modifier)
	{
		if((modifier.ApplyToPrimary && InputType.IsPrimary())
		|| (modifier.ApplyToSecondary && InputType.IsSecondary()))
		{
			if (modifier.Stat.equals(Stat))
			{
				Add += modifier.Add;
				Multiply += (modifier.Multiply - 1.0f);
				if (!modifier.SetValue.isEmpty())
					SetValue = modifier.SetValue;
			}
		}
		return this;
	}

	public float ApplyTo(float input)
	{
		return (input + Add) * Multiply;
	}

	public String ApplyTo(String input)
	{
		return SetValue.isEmpty() ? input : SetValue;
	}
}
