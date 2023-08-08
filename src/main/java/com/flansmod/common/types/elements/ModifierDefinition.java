package com.flansmod.common.types.elements;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.JsonField;
import com.flansmod.util.Maths;
import com.mojang.datafixers.kinds.IdF;
import net.minecraft.network.chat.Component;

public class ModifierDefinition
{
	@JsonField
	public String Stat = "";
	@JsonField
	public String Filter = "";
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


	public boolean AppliesTo(EActionInput actionSet)
	{
		return actionSet == EActionInput.PRIMARY ? ApplyToPrimary : ApplyToSecondary;
	}
	public boolean AppliesTo(String stat, EActionInput actionSet)
	{
		return Stat.equals(stat)
			&& (actionSet == EActionInput.PRIMARY ? ApplyToPrimary : ApplyToSecondary);
	}
	public Component GetModifierString()
	{
		Component statName = Component.translatable("modifier.stat_name." + Stat);
		if(Add != 0.0f && Multiply != 1.0f)
		{
			return Component.translatable("modifier.unknown");
		}
		else if(Add != 0.0f)
		{
			if(Add > 0.0f)
				return Component.translatable("modifier.increase_stat", PrintIntOrFloat(Add), statName);
			else
				return Component.translatable("modifier.decrease_stat", PrintIntOrFloat(-Add), statName);
		}
		else if(Multiply != 1.0f)
		{
			if(Multiply > 1.0f)
			{
				float percentIncrease = (Multiply - 1.0f) * 100.0f;
				return Component.translatable("modifier.increase_stat_percent", PrintIntOrFloat(percentIncrease), statName);
			}
			else
			{
				float percentDecrease = (1.0f - Multiply) * 100.0f;
				return Component.translatable("modifier.decrease_stat_percent", PrintIntOrFloat(percentDecrease), statName);
			}
		}
		else if(!SetValue.isEmpty())
		{
			return Component.translatable("modifier.override_value", statName, SetValue);
		}
		else
		{
			return Component.translatable("modifier.unknown");
		}
	}
	private String PrintIntOrFloat(float value)
	{
		if(Maths.Approx(value, Maths.Round(value)))
		{
			return Integer.toString(Maths.Round(value));
		}
		return Float.toString(value);
	}
}
