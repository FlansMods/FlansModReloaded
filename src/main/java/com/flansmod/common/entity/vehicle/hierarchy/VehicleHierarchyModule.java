package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHierarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.damage.VehicleHitResult;
import com.flansmod.common.types.vehicles.elements.DamageablePartDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleHierarchyModule implements IVehicleModule
{
	@Nonnull
	public final VehicleDefinitionHierarchy Reference;
	@Nullable
	public final VehicleDefinitionHierarchy.Node RootDefinitionNode;
	@Nonnull
	public final Map<String, VehicleArticulationSaveState> ArticulationStates = new HashMap<>();
	@Nonnull
	public Transform RootTransform0 = Transform.Identity();
	@Nonnull
	public Transform RootTransform1 = Transform.Identity();

	public VehicleHierarchyModule(@Nonnull VehicleDefinitionHierarchy hierarchy,
								  @Nonnull VehicleEntity vehicle)
	{
		Reference = hierarchy;
		RootDefinitionNode = hierarchy.Find("body");

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
		VehicleDefinitionHierarchy.Node node = Reference.Find(vehiclePart);
		if(node != null)
		{
			// Apply parent transform iteratively first
			if (node.Parent != null)
				TransformRootToPart(node.Parent.Key, stack, dt);

			// Then apply child
			stack.add(GetArticulationTransformLocal(vehiclePart, dt));
		}
	}

	// Start and end should be relative to the root node
	public void Raycast(@Nonnull VehicleEntity vehicle,
						@Nonnull Vec3 start,
						@Nonnull Vec3 end,
						@Nonnull List<HitResult> results,
						float dt)
	{
		if(RootDefinitionNode != null)
		{
			TransformStack stack = TransformStack.of(GetRootTransform(dt));
			Raycast(vehicle, stack, RootDefinitionNode, start, end, results, dt);
		}
	}
	private void Raycast(@Nonnull VehicleEntity vehicle, @Nonnull TransformStack stack, @Nonnull VehicleDefinitionHierarchy.Node node, @Nonnull Vec3 start, @Nonnull Vec3 end, @Nonnull List<HitResult> results, float dt)
	{
		stack.PushSaveState();

		// If this piece is articulated, add a transform
		Transform articulation = GetArticulationTransformLocal(node.Key, dt);
		if(!articulation.IsIdentity())
		{
			start = articulation.GlobalToLocalPosition(start);
			end = articulation.GlobalToLocalPosition(end);
			stack.add(articulation);
		}

		for(VehicleDefinitionHierarchy.Node child : node.Children)
		{
			Raycast(vehicle, stack, child, start, end, results, dt);
		}

		// If this piece has a hitbox (needs damageable), cast against it
		DamageablePartDefinition damageable = Reference.GetDamageable(node.Key);
		if(damageable != null)
		{
			stack.PushSaveState();
			stack.add(Transform.FromPos(damageable.hitboxCenter));
			Vector3d hitPos = new Vector3d();
			if(Maths.RayBoxIntersect(start, end, stack.Top(), damageable.hitboxHalfExtents.toVector3f(), hitPos))
			{
				results.add(new VehicleHitResult(vehicle, node.Key));
			}
			stack.PopSaveState();
		}

		stack.PopSaveState();
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
