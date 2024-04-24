package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHeirarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleArticulationSaveState;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class VehicleHierarchyModule implements IVehicleModule
{
	@Nonnull
	public final VehicleDefinitionHeirarchy Reference;
	@Nonnull
	public final Map<String, VehicleArticulationSaveState> ArticulationStates = new HashMap<>();
	@Nonnull
	public final Map<String, Supplier<Transform>> Transforms;

	public VehicleHierarchyModule(@Nonnull VehicleDefinitionHeirarchy hierarchy,
								  @Nonnull VehicleEntity vehicle)
	{
		Reference = hierarchy;

		hierarchy.ForEachArticulation((articulationDef) -> {
			ArticulationStates.put(articulationDef.partName, new VehicleArticulationSaveState(articulationDef));
		});

		Transforms = new HashMap<>();

		for(var kvp : Reference.Nodes.entrySet())
		{
			if(kvp.getKey().equals("body"))
			{
				Transforms.put("body", () -> Transform.FromPos(vehicle.position()));
			}
			else if(kvp.getValue().Articulation != null)
			{
				Transforms.put(kvp.getKey(), () -> kvp.getValue().Articulation.Apply(ArticulationStates.get(kvp.getKey()).Parameter));
			}
			else
			{
				Transforms.put(kvp.getKey(), Transform::Identity);
			}
		}
	}


	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{

	}

	@Override
	public void Load(@Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			if(ArticulationStates.containsKey(key))
			{
				ArticulationStates.get(key).Load(tags.getCompound(key));
			}
			else FlansMod.LOGGER.warn("Articulation key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}

	@Nonnull
	@Override
	public CompoundTag Save()
	{
		CompoundTag tags = new CompoundTag();
		for(var kvp : ArticulationStates.entrySet())
		{
			tags.put(kvp.getKey(), kvp.getValue().Save());
		}
		return tags;
	}
}
