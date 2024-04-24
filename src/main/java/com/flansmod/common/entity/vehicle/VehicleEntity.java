package com.flansmod.common.entity.vehicle;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.damage.VehicleDamageModule;
import com.flansmod.common.entity.vehicle.guns.VehicleGunModule;
import com.flansmod.common.entity.vehicle.guns.VehicleGunSaveState;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleHierarchyModule;
import com.flansmod.common.entity.vehicle.seats.VehicleSeatSaveState;
import com.flansmod.common.entity.vehicle.seats.VehicleSeatsModule;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;

public class VehicleEntity extends Entity
{
	@Nonnull private final LazyDefinition<VehicleDefinition> DefRef;
	@Nonnull private final Lazy<VehicleDamageModule> LazyDamage = Lazy.of(this::CreateDamageModule);
	@Nonnull private final Lazy<VehicleHierarchyModule> LazyHierarchy = Lazy.of(this::CreateHierarchyModule);
	@Nonnull private final Lazy<VehicleGunModule> LazyGuns = Lazy.of(this::CreateGunModule);
	@Nonnull private final Lazy<VehicleSeatsModule> LazySeats = Lazy.of(this::CreateSeatsModule);

	@Nonnull private final Lazy<VehicleInventory> LazyInventory = Lazy.of(this::CreateInventory);

	// Module getters
	@Nonnull public VehicleDamageModule Damage() { return LazyDamage.get(); }
	@Nonnull public VehicleHierarchyModule Hierarchy() { return LazyHierarchy.get(); }
	@Nonnull public VehicleGunModule Guns() { return LazyGuns.get(); }
	@Nonnull public VehicleSeatsModule Seats() { return LazySeats.get(); }


	@Nonnull public VehicleInventory Inventory() { return LazyInventory.get(); }

	@Nonnull public VehicleDefinition Def() { return DefRef.DefGetter().get(); }
	@Nonnull public ResourceLocation Loc() { return DefRef.Loc(); }



	public VehicleEntity(@Nonnull EntityType<? extends VehicleEntity> type, @Nonnull Level world)
	{
		super(type, world);
		DefRef = LazyDefinition.of(new ResourceLocation("TODO"), FlansMod.VEHICLES);
	}

	public boolean InitFromDefinition()
	{
		VehicleDefinition def = Def();
		if(!def.IsValid())
			return false;

		return true;
	}

	@Override
	protected void defineSynchedData()
	{

	}

	@Override
	protected void readAdditionalSaveData(@Nonnull CompoundTag tags)
	{
		LoadModules(tags);
	}

	@Override
	protected void addAdditionalSaveData(@Nonnull CompoundTag tags)
	{
		SaveModules(tags);
	}

	@Override
	public void tick()
	{
		super.tick();
		TickModules();
	}

	// ---------------------------------------------------------------------------------------------------------
	// MODULES
	// ---------------------------------------------------------------------------------------------------------
	@Nonnull
	private VehicleHierarchyModule CreateHierarchyModule()
	{
		return new VehicleHierarchyModule(Def().AsHierarchy.get(), this);
	}
	@Nonnull
	private VehicleDamageModule CreateDamageModule()
	{
		return new VehicleDamageModule(Def().AsHierarchy.get(), this);
	}
	@Nonnull
	private VehicleGunModule CreateGunModule()
	{
		return new VehicleGunModule(Def().AsHierarchy.get(), this);
	}
	@Nonnull
	private VehicleSeatsModule CreateSeatsModule()
	{
		return new VehicleSeatsModule(Def().AsHierarchy.get(), this);
	}
	private void TickModules()
	{
		Damage().Tick(this);
		Hierarchy().Tick(this);
		Guns().Tick(this);
		Seats().Tick(this);
	}
	private void SaveModules(@Nonnull CompoundTag tags)
	{
		tags.put("damage", Damage().Save());
		tags.put("articulation", Hierarchy().Save());
		tags.put("guns", Guns().Save());
		tags.put("seats", Seats().Save());
	}
	private void LoadModules(@Nonnull CompoundTag tags)
	{
		if(tags.contains("damage"))
			Damage().Load(tags.getCompound("damage"));
		if(tags.contains("articulation"))
			Hierarchy().Load(tags.getCompound("articulation"));
		if(tags.contains("guns"))
			Guns().Load(tags.getCompound("guns"));
		if(tags.contains("seats"))
			Seats().Load(tags.getCompound("seats"));
	}
	// ---------------------------------------------------------------------------------------------------------


	// Inventory
	@Nonnull
	private VehicleInventory CreateInventory()
	{
		VehicleDefinition def = Def();
		// TODO
		return new VehicleInventory(1, 1, 1);
	}
}
