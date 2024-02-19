package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.abilities.CraftingTraitDefinition;
import com.flansmod.common.types.abilities.elements.CraftingTraitProviderDefinition;
import com.flansmod.common.types.crafting.MaterialDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class PartItem extends FlanItem
{
	public PartDefinition Def() { return FlansMod.PARTS.Get(DefinitionLocation); }

	public PartItem(ResourceLocation defLoc, Properties props)
	{
		super(defLoc, props);
	}

	@Override
	public boolean ShouldRenderAsIcon(@Nonnull ItemDisplayContext transformType) { return true; }
	@Override
	public boolean CanBeCraftedFromParts() { return false; }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack,
								@Nullable Level level,
								@Nonnull List<Component> tooltips,
								@Nonnull TooltipFlag flags)
	{
		super.appendHoverText(stack, level, tooltips, flags);

		MaterialDefinition material = Def().GetMaterial();
		if(material.IsValid())
		{
			Component colourCode = Component.translatable("tooltip.tier_colour."+ material.craftingTier);
			switch(material.materialType)
			{
				case Misc -> { tooltips.add(Component.translatable("tooltip.format.part_misc_tier", colourCode, material.craftingTier)); }
				case Wood -> { tooltips.add(Component.translatable("tooltip.format.part_wood_tier", colourCode, material.craftingTier)); }
				case Glass -> { tooltips.add(Component.translatable("tooltip.format.part_glass_tier", colourCode, material.craftingTier)); }
				case Metal -> { tooltips.add(Component.translatable("tooltip.format.part_metal_tier", colourCode, material.craftingTier)); }
				case Composite -> { tooltips.add(Component.translatable("tooltip.format.part_composite_tier", colourCode, material.craftingTier)); }
				case Electronic -> { tooltips.add(Component.translatable("tooltip.format.part_electronic_tier", colourCode, material.craftingTier)); }
				case Fabric -> { tooltips.add(Component.translatable("tooltip.format.part_fabric_tier", colourCode, material.craftingTier)); }
			}
		}

		if(Def().compatiblityTags.length != 3)
		{
			//tooltips.add(Component.translatable("tooltip.format.only_compatible_with_2", Def().compatibilityTags[0], ));
		}

		for(ResourceLocation tag : Def().itemSettings.tags)
		{
			switch(tag.toString())
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

	@Override
	protected void CollectAbilities(@Nonnull ItemStack stack, @Nonnull Map<CraftingTraitDefinition, Integer> abilityMap)
	{
		for(CraftingTraitProviderDefinition provider : Def().traits)
			abilityMap.put(provider.GetAbility(), provider.level);
	}
}
