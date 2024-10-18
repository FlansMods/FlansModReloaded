package com.flansmod.common.types.parts.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.MinecraftHelpers;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class EngineDefinition
{
	@JsonField
	public float maxAcceleration = 1.0f;
	@JsonField
	public float maxDeceleration = 1.0f;

	@JsonField
	public float maxSpeed = 1.0f;


	// Fuel settings
	@JsonField
	public EFuelType fuelType = EFuelType.Creative;

	@JsonField
	public float fuelConsumptionIdle = 0.0f;
	@JsonField
	public float fuelConsumptionFull = 1.0f;
	@JsonField
	public int solidFuelSlots = 0;
	@JsonField(Docs = "In millibuckets")
	public int liquidFuelCapacity = 1000;
	@JsonField
	public int batterySlots = 0;
	@JsonField
	public int FECapacity = 0;


	// For solid fuel engines
	public int GetCoalPerHourIdle() { return Maths.ceil(fuelConsumptionIdle * (20 * 60 * 60) / 800f); }
	public int GetCoalPerHourFull() { return Maths.ceil(fuelConsumptionFull * (20 * 60 * 60) / 800f); }
	// For liquid engines
	public int GetMilliBucketsPerTickIdle() { return Maths.ceil(fuelConsumptionIdle); }
	public int GetMilliBucketsPerTickFull() { return Maths.ceil(fuelConsumptionFull); }
	// For electric engines
	public int GetForgeEnergyPerTickIdle() { return Maths.ceil(fuelConsumptionIdle); }
	public int GetForgeEnergyPerTickFull() { return Maths.ceil(fuelConsumptionFull); }

	@Nonnull
	public Component GetFuelConsumptionTooltip()
	{
		switch(fuelType)
		{
			case Smeltable, Smokable, Blastable -> {
				int cphIdle = GetCoalPerHourIdle();
				int cphFull = GetCoalPerHourFull();
				if(cphIdle == cphFull)
					return Component.translatable("tooltip.format.engine_solid_fuel_consumption", cphIdle);
				return Component.translatable("tooltip.format.engine_solid_fuel_consumption.range", cphIdle, cphFull);
			}
			case Liquid -> {
				int mbptIdle = GetMilliBucketsPerTickIdle();
				int mbptFull = GetMilliBucketsPerTickFull();
				if(mbptIdle == mbptFull)
					return Component.translatable("tooltip.format.engine_liquid_fuel_consumption", mbptIdle);
				return Component.translatable("tooltip.format.engine_liquid_fuel_consumption.range", mbptIdle, mbptFull);
			}
			case FE -> {
				int feptIdle = GetForgeEnergyPerTickIdle();
				int feptFull = GetForgeEnergyPerTickFull();
				if(feptIdle == feptFull)
					return Component.translatable("tooltip.format.engine_electric_fuel_consumption", feptIdle);
				return Component.translatable("tooltip.format.engine_electric_fuel_consumption.range", feptIdle, feptFull);
			}
			case Creative -> {
				return Component.translatable("tooltip.format.engine_creative_consumption");
			}
		}
		return Component.empty();
	}

	@Nonnull
	public Component GetFuelStorageTooltip()
	{
		switch(fuelType)
		{
			case Smeltable, Smokable, Blastable -> {
				return Component.translatable("tooltip.format.engine_solid_fuel_storage", solidFuelSlots);
			}
			case Liquid -> {
				return Component.translatable("tooltip.format.engine_liquid_fuel_storage", liquidFuelCapacity, solidFuelSlots);
			}
			case FE -> {
				return Component.translatable("tooltip.format.engine_electric_fuel_storage", MinecraftHelpers.getFEString(FECapacity), batterySlots);
			}
			case Creative -> {
				return Component.translatable("tooltip.format.engine_creative_storage");
			}
		}
		return Component.empty();
	}


}
