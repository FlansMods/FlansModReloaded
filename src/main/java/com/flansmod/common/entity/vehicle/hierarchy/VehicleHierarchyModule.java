package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHeirarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class VehicleHierarchyModule implements IVehicleModule
{
	@Nonnull
	public final VehicleDefinitionHeirarchy Reference;
	@Nonnull
	public final Map<String, VehicleArticulationSaveState> ArticulationStates = new HashMap<>();
	@Nonnull
	public Transform RootTransform0 = Transform.Identity();
	@Nonnull
	public Transform RootTransform1 = Transform.Identity();

	public VehicleHierarchyModule(@Nonnull VehicleDefinitionHeirarchy hierarchy,
								  @Nonnull VehicleEntity vehicle)
	{
		Reference = hierarchy;

		hierarchy.ForEachArticulation((articulationDef) -> {
			ArticulationStates.put(articulationDef.partName, new VehicleArticulationSaveState(articulationDef));
		});
	}


	// Velocity is units per second, NOT per tick
	public void SetArticulationVelocity(@Nonnull String key, float velocity)
	{
		if(ArticulationStates.containsKey(key))
			ArticulationStates.get(key).SetVelocity(velocity);
	}
	public float GetArticulationVelocity(@Nonnull String key)
	{
		if(ArticulationStates.containsKey(key))
			return ArticulationStates.get(key).GetVelocityUnitsPerSecond();
		return 0.0f;
	}
	@Nonnull
	public Transform GetRootTransform(float dt)
	{
		return Transform.Interpolate(RootTransform0, RootTransform1, dt);
	}
	@Nonnull
	public Transform GetArticulationTransformLocal(@Nonnull String key, float dt)
	{
		if(ArticulationStates.containsKey(key))
			return ArticulationStates.get(key).GetLocalTransform(dt);

		return Transform.Identity();
	}

	public void TransformWorldToPart(@Nonnull String vehiclePart, @Nonnull TransformStack stack, float dt)
	{
		TransformWorldToRoot(stack, dt);
		TransformRootToPart(vehiclePart, stack, dt);
	}
	public void TransformWorldToRoot(@Nonnull TransformStack stack, float dt)
	{
		stack.add(GetRootTransform(dt));
	}
	public void TransformRootToPart(@Nonnull String vehiclePart, @Nonnull TransformStack stack, float dt)
	{
		VehicleDefinitionHeirarchy.Node node = Reference.Find(vehiclePart);
		if(node != null)
		{
			// Apply parent transform iteratively first
			if (node.Parent != null)
				TransformRootToPart(node.Parent.Key, stack, dt);

			// Then apply child
			stack.add(GetArticulationTransformLocal(vehiclePart, dt));
		}
	}


	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{

	}

	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			if(ArticulationStates.containsKey(key))
			{
				ArticulationStates.get(key).Load(vehicle, tags.getCompound(key));
			}
			else FlansMod.LOGGER.warn("Articulation key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		CompoundTag tags = new CompoundTag();
		for(var kvp : ArticulationStates.entrySet())
		{
			tags.put(kvp.getKey(), kvp.getValue().Save(vehicle));
		}
		return tags;
	}
}
