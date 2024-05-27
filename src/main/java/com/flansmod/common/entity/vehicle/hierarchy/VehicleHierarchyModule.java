package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.ITransformChildEntity;
import com.flansmod.common.entity.ITransformPair;
import com.flansmod.common.entity.vehicle.*;
import com.flansmod.common.entity.vehicle.controls.ForceModel;
import com.flansmod.common.entity.vehicle.damage.VehicleDamageModule;
import com.flansmod.common.entity.vehicle.damage.VehicleHitResult;
import com.flansmod.common.types.vehicles.elements.ArticulatedPartDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
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
	public record ArticulationInstance(float Parameter, float Velocity)
	{
		public static final EntityDataSerializer<ArticulationInstance> SERIALIZER = new EntityDataSerializer.ForValueType<>()
		{
			@Override
			public void write(@Nonnull FriendlyByteBuf buf, @Nonnull ArticulationInstance data)
			{
				buf.writeFloat(data.Parameter);
				buf.writeFloat(data.Velocity);
			}
			@Override
			@Nonnull
			public ArticulationInstance read(@Nonnull FriendlyByteBuf buf)
			{
				float param = buf.readFloat();
				float velocity = buf.readFloat();
				return new ArticulationInstance(param, velocity);
			}
		};
	}
	public static final EntityDataSerializer<PerPartMap<ArticulationInstance>> ARTICULATIONS_SERIALIZER =
		PerPartMap.SERIALIZER(ArticulationInstance.SERIALIZER);
	public static final EntityDataAccessor<PerPartMap<ArticulationInstance>> ARTICULATIONS_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, ARTICULATIONS_SERIALIZER);

	public static final EntityDataAccessor<PerPartMap<WheelEntity.WheelSyncData>> WHEELS_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, WheelEntity.WHEELS_SERIALIZER);




	@Nonnull
	public final VehicleDefinitionHierarchy Reference;
	@Nullable
	public final VehicleDefinitionHierarchy.Node RootDefinitionNode;
	@Nonnull
	public final Map<String, ArticulatedPartDefinition> ArticulatedParts = new HashMap<>();
	@Nonnull
	public final Map<String, ITransformChildEntity> ChildEntities = new HashMap<>();
	@Nonnull
	public Transform RootTransform = Transform.Identity();
	@Nonnull
	public Transform RootTransformPrev = Transform.Identity();
	@Nonnull
	private final SynchedEntityData VehicleDataSynchronizer;

	public VehicleHierarchyModule(@Nonnull VehicleDefinitionHierarchy hierarchy,
								  @Nonnull VehicleEntity vehicle)
	{
		Reference = hierarchy;
		RootDefinitionNode = hierarchy.Find("body");

		hierarchy.ForEachArticulation(ArticulatedParts::put);
		VehicleDataSynchronizer = vehicle.getEntityData();
	}

	// Root Transform settings
	public void SetPosition(double x, double y, double z) { RootTransform = RootTransform.WithPosition(x, y, z); }
	public void SetPosition(@Nonnull Vec3 pos) { RootTransform = RootTransform.WithPosition(pos); }
	public void SetYaw(float yaw) { RootTransform = RootTransform.WithYaw(yaw); }
	public void SetPitch(float pitch) { RootTransform = RootTransform.WithPitch(pitch); }
	public void SetRoll(float roll) { RootTransform = RootTransform.WithRoll(roll); }
	public void RotateYaw(float yaw) { RootTransform = RootTransform.RotateYaw(yaw); }
	public void RotatePitch(float pitch) { RootTransform = RootTransform.RotatePitch(pitch); }
	public void RotateRoll(float roll) { RootTransform = RootTransform.RotateRoll(roll); }
	public void SetEulerAngles(float pitch, float yaw, float roll) { RootTransform = RootTransform.WithEulerAngles(pitch, yaw, roll); }
	public void SetCurrentRootTransform(@Nonnull Transform transform) { RootTransform = transform; }
	public void SetPreviousRootTransform(@Nonnull Transform transform) { RootTransformPrev = transform; }


	// -----------------------------------------------------------------------------------------------
	// Synced data maps
	@Nonnull private PerPartMap<ArticulationInstance> GetArticulationMap() { return VehicleDataSynchronizer.get(ARTICULATIONS_ACCESSOR); }
	private void SetArticulationMap(@Nonnull PerPartMap<ArticulationInstance> map) { VehicleDataSynchronizer.set(ARTICULATIONS_ACCESSOR, map); }

	@Nonnull private PerPartMap<WheelEntity.WheelSyncData> GetWheelMap() { return VehicleDataSynchronizer.get(WHEELS_ACCESSOR); }
	private void SetWheelMap(@Nonnull PerPartMap<WheelEntity.WheelSyncData> map) { VehicleDataSynchronizer.set(WHEELS_ACCESSOR, map); }



	// -----------------------------------------------------------------------------------------------
	// Velocity is units per second, NOT per tick
	// Articulation Accessors
	public void SetArticulationParameterByHash(int hash, float parameter)
	{
		PerPartMap<ArticulationInstance> map = GetArticulationMap();
		float existingVelocity = map.Values.containsKey(hash) ? map.Values.get(hash).Velocity : 0.0f;
		map.Values.put(hash, new ArticulationInstance(parameter, existingVelocity));
		SetArticulationMap(map);
	}
	public void SetArticulationVelocityByHash(int hash, float velocity)
	{
		PerPartMap<ArticulationInstance> map = GetArticulationMap();
		float existingParam = map.Values.containsKey(hash) ? map.Values.get(hash).Parameter : 0.0f;
		map.Values.put(hash, new ArticulationInstance(existingParam, velocity));
		SetArticulationMap(map);
	}
	public float GetArticulationParameterByHash(int hash)
	{
		PerPartMap<ArticulationInstance> map = GetArticulationMap();
		return map.Values.containsKey(hash) ? map.Values.get(hash).Parameter : 0.0f;
	}
	public float GetArticulationVelocityByHash(int hash)
	{
		PerPartMap<ArticulationInstance> map = GetArticulationMap();
		return map.Values.containsKey(hash) ? map.Values.get(hash).Velocity : 0.0f;
	}

	public void SetArticulationParameter(@Nonnull String partName, float parameter)
	{
		SetArticulationParameterByHash(partName.hashCode(), parameter);
	}
	public float GetArticulationParameter(@Nonnull String partName)
	{
		return GetArticulationParameterByHash(partName.hashCode());
	}
	public void SetArticulationVelocity(@Nonnull String partName, float velocity)
	{
		SetArticulationVelocityByHash(partName.hashCode(), velocity);
	}
	public float GetArticulationVelocity(@Nonnull String partName)
	{
		return GetArticulationVelocityByHash(partName.hashCode());
	}
	@Nonnull
	public Transform GetArticulationTransform(@Nonnull String partName)
	{
		ArticulatedPartDefinition def = ArticulatedParts.get(partName);
		if (def != null && def.active)
		{
			float parameter = GetArticulationParameter(partName);
			return def.Apply(parameter);
		}
		return Transform.IDENTITY;
	}

	// -----------------------------------------------------------------------------------------------
	// Wheel Accessors
	public void RegisterWheel(int wheelIndex, @Nonnull WheelEntity wheel)
	{
		ChildEntities.put(ForceModel.Wheel(wheelIndex), wheel);
	}
	@Nullable
	public WheelEntity.WheelSyncData GetWheelData(@Nonnull String wheelPath)
	{
		return GetWheelMap().ForPart(wheelPath);
	}
	public void SetWheelData(@Nonnull String wheelPath, @Nonnull WheelEntity.WheelSyncData data)
	{
		PerPartMap<WheelEntity.WheelSyncData> map = GetWheelMap();
		map.Put(wheelPath, data);
		SetWheelMap(map);
	}






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
			float articulationParameter = GetArticulationParameter(node.Def.partName);
			float articulationVelocity = GetArticulationVelocity(node.Def.partName);
			return node.Def.articulation.Apply(articulationParameter - articulationVelocity);
		}
		return node.Def.LocalTransform.get();
	}
	@Nonnull
	public Transform GetPartLocalCurrent(@Nonnull VehicleDefinitionHierarchy.Node node)
	{
		if(node.Def.IsArticulated())
		{
			float articulationParameter = GetArticulationParameter(node.Def.partName);
			return node.Def.articulation.Apply(articulationParameter);
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
		PerPartMap<ArticulationInstance> articulation = GetArticulationMap();
		for(String key : tags.getAllKeys())
		{
			CompoundTag articulationTags = tags.getCompound(key);
			if(articulation.Values.containsKey(key.hashCode()))
			{
				articulation.Values.put(key.hashCode(), new ArticulationInstance(articulationTags.getFloat("param"), articulationTags.getFloat("velocity")));
			}
			else FlansMod.LOGGER.warn("Articulation key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		PerPartMap<ArticulationInstance> map = GetArticulationMap();
		CompoundTag tags = new CompoundTag();
		for(var kvp : ArticulatedParts.entrySet())
		{
			CompoundTag articulationTags = new CompoundTag();
			articulationTags.putFloat("param", GetArticulationParameter(kvp.getKey()));
			articulationTags.putFloat("velocity", GetArticulationVelocity(kvp.getKey()));
			tags.put(kvp.getKey(), articulationTags);
		}
		return tags;
	}
}
