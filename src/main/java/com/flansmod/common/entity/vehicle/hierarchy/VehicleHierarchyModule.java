package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.ITransformChildEntity;
import com.flansmod.common.entity.ITransformPair;
import com.flansmod.common.entity.vehicle.ITransformEntity;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHierarchy;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.ForceModel;
import com.flansmod.common.entity.vehicle.damage.VehicleHitResult;
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
	public final Map<String, ITransformChildEntity> ChildEntities = new HashMap<>();
	@Nonnull
	public Transform RootTransform = Transform.Identity();
	@Nonnull
	public Transform RootTransformPrev = Transform.Identity();

	public VehicleHierarchyModule(@Nonnull VehicleDefinitionHierarchy hierarchy,
								  @Nonnull VehicleEntity vehicle)
	{
		Reference = hierarchy;
		RootDefinitionNode = hierarchy.Find("body");

		hierarchy.ForEachArticulation((nodePath, articulationDef) -> {
			ArticulationStates.put(nodePath, new VehicleArticulationSaveState(articulationDef));
		});
	}

	public void SetPosition(double x, double y, double z) { RootTransform = RootTransform.WithPosition(x, y, z); }
	public void SetPosition(@Nonnull Vec3 pos) { RootTransform = RootTransform.WithPosition(pos); }
	public void SetYaw(float yaw) { RootTransform = RootTransform.WithYaw(yaw); }
	public void SetPitch(float pitch) { RootTransform = RootTransform.WithPitch(pitch); }
	public void SetRoll(float roll) { RootTransform = RootTransform.WithRoll(roll); }
	public void RotateYaw(float yaw) { RootTransform = RootTransform.RotateYaw(yaw); }
	public void RotatePitch(float pitch) { RootTransform = RootTransform.RotatePitch(pitch); }
	public void RotateRoll(float roll) { RootTransform = RootTransform.RotateRoll(roll); }
	public void SetEulerAngles(float pitch, float yaw, float roll) { RootTransform = RootTransform.WithEulerAngles(pitch, yaw, roll); }

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
	public void RegisterWheel(int wheelIndex, @Nonnull WheelEntity wheel)
	{
		ChildEntities.put(ForceModel.Wheel(wheelIndex), wheel);
	}

	public void SetCurrentRootTransform(@Nonnull Transform transform) { RootTransform = transform; }
	public void SetPreviousRootTransform(@Nonnull Transform transform) { RootTransformPrev = transform; }

	// ----------------------------------------------------------------------------------------------------------------
	// World to Part
	@Nonnull public ITransformPair GetWorldToPart(@Nonnull String apPath) { return ITransformPair.compose(GetWorldToRoot(), GetRootToPart(apPath)); }
	@Nonnull public Transform GetWorldToPartPrevious(@Nonnull String apPath) { return GetWorldToPart(apPath).GetPrevious(); }
	@Nonnull public Transform GetWorldToPartCurrent(@Nonnull String apPath) { return GetWorldToPart(apPath).GetCurrent(); }

	// ----------------------------------------------------------------------------------------------------------------
	// World to Root
	@Nonnull public ITransformPair GetWorldToRoot() { return ITransformPair.of(this::GetWorldToRootPrevious, this::GetWorldToRootCurrent); }
	@Nonnull public Transform GetWorldToRootPrevious() { return RootTransformPrev; }
	@Nonnull public Transform GetWorldToRootCurrent() { return RootTransform; }

	// ----------------------------------------------------------------------------------------------------------------
	// Root to Part
	@Nonnull public ITransformPair GetRootToPart(@Nonnull String vehiclePart) {
		return ITransformPair.of(() -> GetRootToPartPrevious(vehiclePart), () -> GetRootToPartCurrent(vehiclePart));
	}
	@Nonnull public Transform GetRootToPartPrevious(@Nonnull String vehiclePart) {
		TransformStack stack = new TransformStack();
		TransformRootToPartPrevious(vehiclePart, stack);
		return stack.Top();
	}
	@Nonnull public Transform GetRootToPartCurrent(@Nonnull String vehiclePart) {
		TransformStack stack = new TransformStack();
		TransformRootToPartCurrent(vehiclePart, stack);
		return stack.Top();
	}
	public void TransformRootToPartPrevious(@Nonnull String vehiclePart, @Nonnull TransformStack stack) {
		Reference.Traverse(vehiclePart, (node) -> { stack.add(GetPartLocalPrevious(node)); });
	}
	public void TransformRootToPartCurrent(@Nonnull String vehiclePart, @Nonnull TransformStack stack) {
		Reference.Traverse(vehiclePart, (node) -> { stack.add(GetPartLocalCurrent(node)); });
	}

	// ----------------------------------------------------------------------------------------------------------------
	// Part to Part
	@Nonnull public ITransformPair GetPartLocal(@Nonnull VehicleDefinitionHierarchy.Node node) {
		return ITransformPair.of(() -> GetPartLocalPrevious(node), () -> GetPartLocalCurrent(node));
	}
	@Nonnull
	public Transform GetPartLocalPrevious(@Nonnull VehicleDefinitionHierarchy.Node node)
	{
		if(node.Def.IsArticulated())
		{
			VehicleArticulationSaveState articulation = ArticulationStates.get(node.Def.partName);
			return articulation.GetPartLocalPrevious();
		}
		return node.Def.LocalTransform.get();
	}
	@Nonnull
	public Transform GetPartLocalCurrent(@Nonnull VehicleDefinitionHierarchy.Node node)
	{
		if(node.Def.IsArticulated())
		{
			VehicleArticulationSaveState articulation = ArticulationStates.get(node.Def.partName);
			return articulation.GetPartLocalCurrent();
		}
		return node.Def.LocalTransform.get();
	}


	// ---------------------------------------------------------------------------------------------------------
	// Raycasts
	// Start and end should be relative to the root node
	public void Raycast(@Nonnull VehicleEntity vehicle,
						@Nonnull Vec3 start,
						@Nonnull Vec3 end,
						@Nonnull List<HitResult> results,
						float dt)
	{
		if(RootDefinitionNode != null)
		{
			TransformStack stack = TransformStack.of(GetWorldToRoot().GetDelta(dt));
			Raycast(vehicle, stack, RootDefinitionNode, start, end, results, dt);
		}
	}
	private void Raycast(@Nonnull VehicleEntity vehicle, @Nonnull TransformStack stack, @Nonnull VehicleDefinitionHierarchy.Node node, @Nonnull Vec3 start, @Nonnull Vec3 end, @Nonnull List<HitResult> results, float dt)
	{
		stack.PushSaveState();

		// If this piece is articulated, add a transform
		if(node.Def.IsArticulated())
		{
			Transform articulation = GetPartLocal(node).GetDelta(dt);
			if(!articulation.IsIdentity())
			{
				start = articulation.GlobalToLocalPosition(start);
				end = articulation.GlobalToLocalPosition(end);
				stack.add(articulation);
			}
		}

		// Cast against children nodes
		for(VehicleDefinitionHierarchy.Node child : node.Children.values())
		{
			Raycast(vehicle, stack, child, start, end, results, dt);
		}

		// If this piece has a hitbox (needs damageable), cast against it
		if(node.Def.IsDamageable())
		{
			stack.PushSaveState();
			stack.add(Transform.FromPos(node.Def.damage.hitboxCenter));
			Vector3d hitPos = new Vector3d();
			if(Maths.RayBoxIntersect(start, end, stack.Top(), node.Def.damage.hitboxHalfExtents.toVector3f(), hitPos))
			{
				results.add(new VehicleHitResult(vehicle, node.Path()));
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
