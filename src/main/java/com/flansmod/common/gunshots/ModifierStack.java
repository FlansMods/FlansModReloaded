package com.flansmod.common.gunshots;

import com.flansmod.common.types.elements.ModifierDefinition;

public class ModifierStack
{
	private final String Stat;
	private final String GroupPath;
	private float Add = 0.0f;
	private float Multiply = 1.0f;
	private String SetValue = "";

	public ModifierStack(String stat, String groupPath)
	{
		GroupPath = groupPath;
		Stat = stat;
	}

	public ModifierStack Modify(ModifierDefinition modifier, float multiplier)
	{
		if(modifier.AppliesTo(Stat, GroupPath))
		{
			Add += (modifier.Add * multiplier);
			Multiply += ((modifier.Multiply - 1.0f) * multiplier);
			if (!modifier.SetValue.isEmpty())
				SetValue = modifier.SetValue;
		}
		return this;
	}

	public boolean ApplyTo(boolean input)
	{
		return SetValue.isEmpty() ? input : Boolean.parseBoolean(SetValue);
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
