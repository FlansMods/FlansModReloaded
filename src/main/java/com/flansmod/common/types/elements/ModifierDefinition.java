package com.flansmod.common.types.elements;

import com.flansmod.common.actions.EActionSet;
import com.flansmod.common.types.JsonField;

public class ModifierDefinition
{
	@JsonField
	public String Stat = "";
	@JsonField(Docs = "Additive modifiers are applied first")
	public float Add = 0.0f;
	@JsonField(Docs = "All multiplys are applied after all adds, so notably a 0x multiplier will always 0 the stat")
	public float Multiply = 1.0f;
	@JsonField(Docs = "For non-numeric values, such as enums, this is a simple override")
	public String SetValue = "";
	@JsonField
	public boolean ApplyToPrimary = true;
	@JsonField
	public boolean ApplyToSecondary = false;


	public boolean AppliesTo(EActionSet actionSet)
	{
		return actionSet == EActionSet.PRIMARY ? ApplyToPrimary : ApplyToSecondary;
	}
	public boolean AppliesTo(String stat, EActionSet actionSet)
	{
		return Stat.equals(stat)
			&& (actionSet == EActionSet.PRIMARY ? ApplyToPrimary : ApplyToSecondary);
	}
}
