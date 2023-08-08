package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class PartItem extends FlanItem
{
	public PartDefinition Def() { return FlansMod.PARTS.Get(DefinitionLocation); }

	public PartItem(ResourceLocation defLoc, Properties props)
	{
		super(defLoc, props);
	}

	@Override
	public void appendHoverText(ItemStack stack,
								@Nullable Level level,
								List<Component> tooltips,
								TooltipFlag flags)
	{
		super.appendHoverText(stack, level, tooltips, flags);

		if(Def().materialTier != 0)
		{
			switch(Def().materialType)
			{
				case Misc -> { tooltips.add(Component.translatable("tooltip.format.part_misc_tier", Def().materialTier)); }
				case Wood -> { tooltips.add(Component.translatable("tooltip.format.part_wood_tier", Def().materialTier)); }
				case Glass -> { tooltips.add(Component.translatable("tooltip.format.part_glass_tier", Def().materialTier)); }
				case Metal -> { tooltips.add(Component.translatable("tooltip.format.part_metal_tier", Def().materialTier)); }
				case Composite -> { tooltips.add(Component.translatable("tooltip.format.part_composite_tier", Def().materialTier)); }
				case Electronic -> { tooltips.add(Component.translatable("tooltip.format.part_electronic_tier", Def().materialTier)); }
				case Fabric -> { tooltips.add(Component.translatable("tooltip.format.part_fabric_tier", Def().materialTier)); }
			}
		}

		if(Def().compatiblityTags.length != 3)
		{
			//tooltips.add(Component.translatable("tooltip.format.only_compatible_with_2", Def().compatibilityTags[0], ));
		}

		for(String tag : Def().itemSettings.tags)
		{
			switch(tag)
			{
				case "flansmod:engine":
				{
					// Fuel consumption
					switch (Def().engine.fuelType)
					{
						case Smeltable, Smokable, Blastable -> {
							int coalPerHour = Maths.Ceil(Def().engine.fuelConsumptionRate * (20 * 60 * 60) / 800f);
							tooltips.add(Component.translatable("tooltip.format.engine_solid_fuel_consumption", coalPerHour));
							tooltips.add(Component.translatable("tooltip.format.engine_solid_fuel_storage", Def().engine.solidFuelSlots));
						}
						case Liquid -> {
							int mBPerTick = Maths.Ceil(Def().engine.fuelConsumptionRate);
							tooltips.add(Component.translatable("tooltip.format.engine_liquid_fuel_consumption", mBPerTick));
							tooltips.add(Component.translatable("tooltip.format.engine_liquid_fuel_storage", Def().engine.liquidFuelCapacity, Def().engine.solidFuelSlots));
						}
						case FE -> {
							int FEPerTick = Maths.Ceil(Def().engine.fuelConsumptionRate);
							tooltips.add(Component.translatable("tooltip.format.engine_electric_fuel_consumption", MinecraftHelpers.GetFEString(FEPerTick)));
							tooltips.add(Component.translatable("tooltip.format.engine_electric_fuel_storage", MinecraftHelpers.GetFEString(Def().engine.FECapacity), Def().engine.batterySlots));
						}
					}

					// Max speed
					if(Def().engine.maxSpeed != 1.0f)
						tooltips.add(Component.translatable("tooltip.format.engine_speed_modifier", Maths.Floor(Def().engine.maxSpeed * 100.0f)));

					// Acceleration / Deceleration
					if(Def().engine.maxAcceleration != 1.0f && Def().engine.maxAcceleration == Def().engine.maxDeceleration)
						tooltips.add(Component.translatable("tooltip.format.engine_acceleration_and_deceleration_modifier", Maths.Floor(Def().engine.maxAcceleration * 100.0f)));
					else if(Def().engine.maxAcceleration != 1.0f)
						tooltips.add(Component.translatable("tooltip.format.engine_acceleration_modifier", Maths.Floor(Def().engine.maxAcceleration * 100.0f)));
					else if(Def().engine.maxDeceleration != 1.0f)
						tooltips.add(Component.translatable("tooltip.format.engine_deceleration_modifier", Maths.Floor(Def().engine.maxDeceleration * 100.0f)));

					break;
				}
			}
		}
	}

}
