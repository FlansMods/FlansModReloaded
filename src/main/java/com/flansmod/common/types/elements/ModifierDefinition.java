package com.flansmod.common.types.elements;

import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.EAccumulationSource;
import com.flansmod.common.types.abilities.elements.StatAccumulatorDefinition;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ModifierDefinition
{
	@JsonField
	public String stat = "";
	@JsonField
	public String[] matchGroupPaths = new String[0];
	@JsonField
	public StatAccumulatorDefinition[] accumulators = new StatAccumulatorDefinition[0];
	@JsonField
	public String setValue = "";

	public boolean AppliesTo(@Nonnull String groupPath)
	{
		if(matchGroupPaths.length == 0)
			return true;
		for(String filter : matchGroupPaths)
			if(groupPath.contains(filter))
				return true;
		return false;
	}
	public boolean AppliesTo(@Nonnull String check, @Nonnull String groupPath)
	{
		return stat.equals(check) && AppliesTo(groupPath);
	}


	@Nonnull
	public List<Component> GetModifierStrings()
	{
		List<Component> componentList = new ArrayList<>();
		Component statName = Component.translatable("modifier.stat_name." + stat);

		Component groupPathComponent = switch(matchGroupPaths.length)
			{
				case 0 -> Component.empty();
				case 1 -> Component.translatable("comma_list.brackets", Component.translatable("action.group_path." + matchGroupPaths[0]));
				default -> Component.translatable("comma_list.brackets", FlanItem.ListOf("action.group_path.", matchGroupPaths));
			};

		for(StatAccumulatorDefinition accumulator : accumulators)
		{
			Component valueComponent = switch(accumulator.operation)
			{
				case BaseAdd -> Component.literal("+ "+PrintIntOrFloat(accumulator.value));
				case StackablePercentage -> Component.literal("+ "+PrintIntOrFloat(accumulator.value)+"%");
				case IndependentPercentage -> Component.literal("* "+PrintIntOrFloat(accumulator.value)+"%");
				case FinalAdd -> Component.literal("+(After %)"+PrintIntOrFloat(accumulator.value));
			};

			for(EAccumulationSource source : accumulator.multiplyPer)
			{
				switch(source)
				{
					case PerStacks -> 		valueComponent = Component.translatable("modifier.scalar.stacks", statName, valueComponent);
					case PerLevel -> 		valueComponent = Component.translatable("modifier.scalar.level", statName, valueComponent);
					case PerAttachment -> 	valueComponent = Component.translatable("modifier.scalar.attachment", statName, valueComponent);
					case PerMagFullness -> 	valueComponent = Component.translatable("modifier.scalar.mag_full", statName, valueComponent);
					case PerMagEmptiness -> valueComponent = Component.translatable("modifier.scalar.mag_empty", statName, valueComponent);
				}
			}
			componentList.add(Component.translatable("%s%s %s", statName, groupPathComponent, valueComponent));
		}
		return componentList;
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
