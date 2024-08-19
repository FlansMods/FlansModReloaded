package com.flansmod.common.entity.vehicle;

import com.flansmod.client.input.ClientInputHooks;
import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.ITransformPair;
import com.flansmod.common.entity.vehicle.controls.ControlLogic;
import com.flansmod.common.entity.vehicle.controls.ControlLogics;
import com.flansmod.common.entity.vehicle.controls.VehicleInputState;
import com.flansmod.common.entity.vehicle.modules.IVehicleEngineModule;
import com.flansmod.common.entity.vehicle.modules.IVehicleTransformHelpers;
import com.flansmod.common.entity.vehicle.physics.ForcesOnPart;
import com.flansmod.common.entity.vehicle.physics.VehiclePartPhysics;
import com.flansmod.common.entity.vehicle.save.*;
import com.flansmod.common.entity.vehicle.modules.IVehicleDamageHelper;
import com.flansmod.common.entity.vehicle.hierarchy.*;
import com.flansmod.common.entity.vehicle.modules.IVehicleSeatHelper;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.network.FlansEntityDataSerializers;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.*;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.flansmod.util.collision.*;
import com.flansmod.util.physics.AngularAcceleration;
import com.flansmod.util.physics.LinearAcceleration;
import com.flansmod.util.physics.LinearForce;
import com.flansmod.util.physics.LinearVelocity;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class VehicleEntity extends Entity implements
	ITransformEntity,
	IVehicleEngineModule,
	IVehicleTransformHelpers,
	IVehicleSeatHelper,
	IVehicleDamageHelper

{
	// Data Synchronizer Accessors
	public static final EntityDataAccessor<PerPartMap<EngineSyncState>> ENGINES_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, FlansEntityDataSerializers.ENGINE_MAP);
	public static final EntityDataAccessor<PerPartMap<ArticulationSyncState>> ARTICULATIONS_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, FlansEntityDataSerializers.ARTICULATION_MAP);
	public static final EntityDataAccessor<PerPartMap<SeatSyncState>> SEATS_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, FlansEntityDataSerializers.SEAT_MAP);
	public static final EntityDataAccessor<PerPartMap<DamageSyncState>> DAMAGE_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, FlansEntityDataSerializers.DAMAGE_MAP);
	public static final EntityDataAccessor<PerPartMap<GunSyncState>> GUNS_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, FlansEntityDataSerializers.GUN_MAP);
	public static final EntityDataAccessor<PerPartMap<WheelSyncState>> WHEELS_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, FlansEntityDataSerializers.WHEEL_MAP);



	public static final int INVALID_SEAT_INDEX = -1;
	public static final String INVALID_SEAT_PATH = "body/seat_-1";

	// Location and sync
	//@Nonnull public Transform RootTransform = Transform.Identity();
	//@Nonnull public Transform RootTransformPrev = Transform.Identity();

	// Definition / ID Access
	@Nonnull private final LazyDefinition<VehicleDefinition> DefRef;
	@Nonnull public VehicleDefinition Def() { return DefRef.DefGetter().get(); }
	@Nonnull public ResourceLocation Loc() { return DefRef.Loc(); }


	// Inventory (sort of module-ey)
	private final Lazy<VehicleInventory> LazyInventory = Lazy.of(this::CreateInventory);
	@Nonnull public VehicleInventory Inventory() { return LazyInventory.get(); }

	// Useful Lookups
	// Part lookups
	public final MultiLookup<EControlLogicHint, VehiclePartPath> Articulations = new MultiLookup<>();
	//public final MultiLookup<EControlLogicHint, WheelEntity> Wheels = new MultiLookup<>();
	public final MultiLookup<EControlLogicHint, VehiclePropellerSaveState> Propellers = new MultiLookup<>();
	public final Map<ResourceLocation, ControlLogic> Controllers = new HashMap<>();
	@Nonnull
	public final List<VehicleComponentPath> SeatOrdering = new ArrayList<>();
	@Nonnull
	public final List<VehicleComponentPath> GunOrdering = new ArrayList<>();
	@Nonnull
	private ResourceLocation SelectedControllerLocation = new ResourceLocation(FlansMod.MODID, "control_schemes/null");

	// Physics
	//@Nullable
	//public ColliderHandle CorePhsyicsHandle = null;
	@Nonnull
	public final Map<VehicleComponentPath, VehiclePartPhysics> PhysicsParts = new HashMap<>();


	//@Nonnull
	//public ColliderHandle[] WheelPhysicsHandles = new ColliderHandle[0];

	// Other misc fields
	@Nonnull public ResourceLocation SelectedSkin;
	@Nonnull public final Map<String, VehicleInputState> InputStates = new HashMap<>();
	@Nonnull public final Map<String, String> ModalStates = new HashMap<>();

	public VehicleEntity(@Nonnull EntityType<? extends Entity> type, @Nonnull ResourceLocation defLoc, @Nonnull Level world)
	{
		super(type, world);
		DefRef = LazyDefinition.of(defLoc, FlansMod.VEHICLES);
		SelectedSkin = defLoc;

		blocksBuilding = true;
		InitFromDefinition();
	}

	@Override
	public void remove(@Nonnull Entity.RemovalReason reason)
	{
		super.remove(reason);
		StopPhysics();
	}

	public boolean InitFromDefinition()
	{
		VehicleDefinition def = Def();
		if(!def.IsValid())
			return false;


		SelectedControllerLocation = def.defaultControlScheme;
		//InitPhysics();

		return true;
	}

	// Some misc overrides
	public static boolean canVehicleCollide(@Nonnull Entity a, @Nonnull Entity b)
	{
		return (b.canBeCollidedWith() || b.isPushable()) && !a.isPassengerOfSameVehicle(b);
	}
	@Override public boolean canCollideWith(@Nonnull Entity other) {
		return canVehicleCollide(this, other);
	}
	@Override public boolean canBeCollidedWith() {
		return true;
	}
	@Override public boolean isPushable() {
		return true;
	}
	@Override
	protected void positionRider(@Nonnull Entity rider, @Nonnull Entity.MoveFunction moveFunc)
	{
		if (hasPassenger(rider))
		{
			int seatIndex = GetSeatIndexOf(rider);

		}
	}
	@Override
	@Nonnull
	public Vec3 getDismountLocationForPassenger(@Nonnull LivingEntity p_38357_)
	{
		Vec3 vec3 = getCollisionHorizontalEscapeVector((double) (this.getBbWidth() * Mth.SQRT_OF_TWO), (double) p_38357_.getBbWidth(), p_38357_.getYRot());
		return vec3;
	}

	// -------------------------------------------------------------------------------------------
	// Transform and some vanilla overrides. We want to use Quaternions, pleassse Minecraft
	@Override public float getYRot() { return GetWorldToEntity().GetCurrent().Yaw(); }
	@Override public float getXRot() { return GetWorldToEntity().GetCurrent().Pitch(); }
	@Override public void setYRot(float yaw) { SetYaw(yaw); }
	@Override public void setXRot(float pitch) { SetPitch(pitch); }
	//@Override public void setPos(double x, double y, double z) { SetPosition(x, y, z); }
	@Override
	public void moveTo(double x, double y, double z, float xRot, float yRot) {
		super.moveTo(x, y, z, xRot, yRot);

		CheckInitPhysics();
		SetAllPositionsFromEntity();
	}
	// This is the default entity movement packet
	@Override
	public void lerpTo(double x, double y, double z, float yaw, float pitch, int i, boolean flag)
	{
		super.lerpTo(x, y, z, yaw, pitch, i, flag);
		xo = x;
		yo = y;
		zo = z;
		xRotO = pitch;
		yRotO = yaw;
		SyncEntityToTransform();
	}
	@Override
	protected void reapplyPosition()
	{
		super.reapplyPosition();
		SetAllPositionsFromEntity();
	}
	private void SetAllPositionsFromEntity()
	{
		SyncEntityToTransform();
		//for(WheelEntity wheel : Wheels.All())
		//	wheel.setPos(GetWorldToAP(wheel.GetWheelPath().Part()).GetCurrent().PositionVec3());
	}

	public void SetEulerAngles(float pitch, float yaw, float roll) { SetEulerAngles(pitch, yaw, roll); }
	@Nonnull public Transform RootTransformCurrent() { return GetWorldToRoot().GetCurrent(); }
	@Nonnull public Transform RootTransformPrevious() { return GetWorldToRoot().GetPrevious(); }
	@Nonnull public Transform RootTransform(float dt) { return GetWorldToRoot().GetDelta(dt); }
	// -------------------------------------------------------------------------------------------

	@Nonnull
	private VehicleInputState GetInputStateFor(@Nonnull String key)
	{
		VehicleInputState inputState = InputStates.get(key);
		if(inputState == null)
		{
			inputState = new VehicleInputState();
			InputStates.put(key, inputState);
		}
		return inputState;
	}
	@Nonnull
	public VehicleInputState GetMiscInputState() { return GetInputStateFor("misc"); }
	@Nonnull
	public VehicleInputState GetInputStateFor(@Nonnull ControlSchemeDefinition controlScheme)
	{
		return GetInputStateFor(controlScheme.Location.toString());
	}
	@Nonnull
	public VehicleInputState GetInputStateFor(@Nonnull ControlLogic controller)
	{
		return GetInputStateFor(controller.Def.Location.toString());
	}

	public boolean IsAuthority() { return isControlledByLocalInstance(); }
	public boolean IsValidator() { return !level().isClientSide; }

	// ---------------------------------------------------------------------------------------------------------
	// Entity overrides
	// ---------------------------------------------------------------------------------------------------------
	// Data Syncing
	@Override
	protected void defineSynchedData()
	{
		entityData.define(ENGINES_ACCESSOR, new PerPartMap<>());
		entityData.define(ARTICULATIONS_ACCESSOR, new PerPartMap<>());
		entityData.define(SEATS_ACCESSOR, new PerPartMap<>());
		entityData.define(GUNS_ACCESSOR, new PerPartMap<>());
		entityData.define(DAMAGE_ACCESSOR, new PerPartMap<>());
		entityData.define(WHEELS_ACCESSOR, new PerPartMap<>());
	}

	@Override
	protected void readAdditionalSaveData(@Nonnull CompoundTag tags)
	{
		if(tags.contains("engine"))
			LoadEngineData(tags.getCompound("engine"));
		if(tags.contains("articulation"))
			LoadArticulation(tags.getCompound("articulation"));
		if(tags.contains("seats"))
			LoadSeatState(tags.getCompound("seats"));
		if(tags.contains("damage"))
			LoadDamageState(tags.getCompound("damage"));
		if(tags.contains("guns"))
			LoadGunState(tags.getCompound("guns"));
		if(tags.contains("wheels"))
			LoadWheelState(tags.getCompound("wheels"));
		//if(tags.contains("physics"))
		//	LoadPhysicsState(tags.getCompound("physics"));
	}

	@Override
	protected void addAdditionalSaveData(@Nonnull CompoundTag tags)
	{
		tags.put("engine", SaveEngineData());
		tags.put("articulation", SaveArticulation());
		tags.put("seats", SaveSeatState());
		tags.put("damage", SaveDamageState());
		tags.put("guns", SaveGunState());
		tags.put("wheels", SaveWheelState());
		//tags.put("physics", SavePhysicsState());
	}

	@Override @Nonnull
	protected AABB makeBoundingBox()
	{
		return super.makeBoundingBox();
		//return new OBB(GetRootTransformCurrent(), -getBbWidth() / 2f, -getBbHeight() / 2f, -getBbWidth() / 2f, getBbWidth() / 2f, getBbHeight() / 2f, getBbWidth() / 2f);
	}

	@Override
	public void tick()
	{
		super.tick();

		TickControlSchemes();
		TickPhysics();


	}
	@Override
	protected boolean canAddPassenger(@Nonnull Entity entity)
	{
		// TODO: Locking module (Do you have the car keys?)

		int seatIndex = GetSeatIndexForNewPassenger(entity);

		return seatIndex != INVALID_SEAT_INDEX;
	}
	@Nullable
	@Override
	public LivingEntity getControllingPassenger()
	{
		if (GetControllingPassenger(this) instanceof LivingEntity living)
			return living;
		return null;
	}
	@Nonnull
	@Override
	public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand)
	{
		if (player.isSecondaryUseActive())
		{
			return InteractionResult.PASS;
		}
		else
		{
			if (!level().isClientSide)
			{
				return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
			}
			else
			{
				return InteractionResult.SUCCESS;
			}
		}
	}




	// ---------------------------------------------------------------------------------------------------------
	// ARTICULATION MODULE
	@Nonnull private PerPartMap<ArticulationSyncState> GetArticulationMap() { return entityData.get(ARTICULATIONS_ACCESSOR); }
	private void SetArticulationMap(@Nonnull PerPartMap<ArticulationSyncState> map) { entityData.set(ARTICULATIONS_ACCESSOR, map); }

	// -----------------------------------------------------------------------------------------------
	// Velocity is units per second, NOT per tick
	// Articulation Accessors
	public void SetArticulationParameter(@Nonnull VehicleComponentPath componentPath, float parameter)
	{
		PerPartMap<ArticulationSyncState> map = GetArticulationMap();
		map.ApplyTo(componentPath, (state) -> state.SetParameter(parameter));
		SetArticulationMap(map);
	}
	public float GetArticulationParameter(@Nonnull VehicleComponentPath componentPath)
	{
		return GetArticulationMap().ApplyOrDefault(componentPath, ArticulationSyncState::GetParameter, 0.0f);
	}
	public void SetArticulationVelocity(@Nonnull VehicleComponentPath componentPath, float velocity)
	{
		PerPartMap<ArticulationSyncState> map = GetArticulationMap();
		map.ApplyTo(componentPath, (state) -> state.SetVelocity(velocity));
		SetArticulationMap(map);
	}
	public float GetArticulationVelocity(@Nonnull VehicleComponentPath componentPath)
	{
		return GetArticulationMap().ApplyOrDefault(componentPath, ArticulationSyncState::GetVelocity, 0.0f);
	}
	@Nonnull
	public Transform GetArticulationTransformPrevious(@Nonnull VehicleComponentPath partName)
	{
		Optional<Transform> result = GetHierarchy().IfArticulated(partName, (def) ->
		{
			if (def != null && def.active)
			{
				float parameter = GetArticulationParameter(partName);
				float velocity = GetArticulationVelocity(partName);
				return def.Apply(parameter - velocity);
			}
			return null;
		});
		return result.orElse(Transform.IDENTITY);
	}
	@Nonnull
	public Transform GetArticulationTransformPrevious(@Nonnull VehiclePartPath partPath)
	{
		return GetArticulationTransformPrevious(partPath.Articulation());
	}
	@Nonnull
	public Transform GetArticulationTransformCurrent(@Nonnull VehicleComponentPath partName)
	{
		Optional<Transform> result = GetHierarchy().IfArticulated(partName, (def) ->
		{
			if (def != null && def.active)
			{
				float parameter = GetArticulationParameter(partName);
				return def.Apply(parameter);
			}
			return null;
		});
		return result.orElse(Transform.IDENTITY);
	}
	@Nonnull
	public Transform GetArticulationTransformCurrent(@Nonnull VehiclePartPath partPath) { return GetArticulationTransformCurrent(partPath.Articulation()); }
	public boolean IsArticulated(@Nonnull VehiclePartPath part)
	{
		return GetArticulationMap().TryGet(part.Articulation()).isPresent();
	}

	@Override @Nonnull
	public VehicleDefinitionHierarchy GetHierarchy() { return Def().AsHierarchy(); }
	@Override @Nonnull
	public Transform GetRootTransformCurrent() { return GetCorePhysics().LocationCurrent; }
	@Override @Nonnull
	public Transform GetRootTransformPrevious() { return GetCorePhysics().LocationPrev; }
	@Override
	public void SetRootTransformCurrent(@Nonnull Transform transform) { TeleportTo(transform); }
	@Override
	public void ApplyWorldToRootPrevious(@Nonnull TransformStack stack)
	{
		stack.add(GetCorePhysics().LocationCurrent);
	}
	@Override
	public void ApplyWorldToRootCurrent(@Nonnull TransformStack stack)
	{
		stack.add(GetCorePhysics().LocationPrev);
	}

	@Override
	public void ApplyPartToPartPrevious(@Nonnull VehiclePartPath partPath, @Nonnull TransformStack stack)
	{
		if(IsArticulated(partPath))
		{
			stack.add(GetArticulationTransformPrevious(partPath));
		}
		else
		{
			stack.add(GetPartDef(partPath).LocalTransform.get());
		}
	}
	@Override
	public void ApplyPartToPartCurrent(@Nonnull VehiclePartPath partPath, @Nonnull TransformStack stack)
	{
		if(IsArticulated(partPath))
		{
			stack.add(GetArticulationTransformCurrent(partPath));
		}
		else
		{
			stack.add(GetPartDef(partPath).LocalTransform.get());
		}
	}
	@Override
	public void ApplyPartToComponentPrevious(@Nonnull VehicleComponentPath componentPath, @Nonnull TransformStack stack)
	{
		if(componentPath.Type() == EPartDefComponent.Wheel)
		{
			stack.add(GetWorldToPartPrevious(componentPath.Part()).Inverse());
			stack.add(GetPartPhysics(componentPath).LocationPrev);
		}
	}
	@Override
	public void ApplyPartToComponentCurrent(@Nonnull VehicleComponentPath componentPath, @Nonnull TransformStack stack)
	{
		if(componentPath.Type() == EPartDefComponent.Wheel)
		{
			stack.add(GetWorldToPartCurrent(componentPath.Part()).Inverse());
			stack.add(GetPartPhysics(componentPath).LocationCurrent);
		}
	}


	//@Override
	//public void ApplyPartToPartPrevious(@Nonnull VehicleDefinitionHierarchy.VehicleNode node, @Nonnull TransformStack stack)
	//{
	//	if(node.Def.IsArticulated())
	//	{
	//		float articulationParameter = GetArticulationParameter(node.GetPath().Articulation());
	//		float articulationVelocity = GetArticulationVelocity(node.GetPath().Articulation());
	//		stack.add(node.Def.articulation.Apply(articulationParameter - articulationVelocity));
	//	}
	//	else
	//		stack.add(node.Def.LocalTransform.get());
	//}
	//@Override
	//public void ApplyPartToPartCurrent(@Nonnull VehicleDefinitionHierarchy.VehicleNode node, @Nonnull TransformStack stack)
	//{
	//	if(node.Def.IsArticulated())
	//	{
	//		float articulationParameter = GetArticulationParameter(node.GetPath().Articulation());
	//		stack.add(node.Def.articulation.Apply(articulationParameter));
	//	}
	//	else
	//		stack.add(node.Def.LocalTransform.get());
	//}
	public void Raycast(@Nonnull Vec3 start,
						@Nonnull Vec3 end,
						float dt,
						@Nonnull List<HitResult> results)
	{
		Raycast(start, end, dt, (partPath, hitPos) -> {
			results.add(new VehicleHitResult(this, partPath));
		});
	}
	protected void LoadArticulation(@Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			CompoundTag articulationTags = tags.getCompound(key);
			VehiclePartPath path = VehiclePartPath.of(key);
			SetArticulationParameter(path.Articulation(), articulationTags.getFloat("param"));
			SetArticulationVelocity(path.Articulation(), articulationTags.getFloat("velocity"));
		}
	}
	@Nonnull
	protected CompoundTag SaveArticulation()
	{
		PerPartMap<ArticulationSyncState> map = GetArticulationMap();
		CompoundTag tags = new CompoundTag();
		GetHierarchy().ForEachArticulatedPart((path, def) -> {
			CompoundTag articulationTags = new CompoundTag();
			articulationTags.putFloat("param", GetArticulationParameter(path));
			articulationTags.putFloat("velocity", GetArticulationVelocity(path));
			tags.put(path.toString(), articulationTags);
		});
		return tags;
	}

	// ---------------------------------------------------------------------------------------------------------
	// DAMAGE MODULE

	@Nonnull public PerPartMap<DamageSyncState> GetDamageMap() { return entityData.get(DAMAGE_ACCESSOR); }
	public void SetDamageMap(@Nonnull PerPartMap<DamageSyncState> map) { entityData.set(DAMAGE_ACCESSOR, map); }
	@Nonnull public DamageablePartDefinition GetDef(@Nonnull VehicleComponentPath partPath) {
		return GetHierarchy().FindDamageable(partPath).orElse(DamageablePartDefinition.INVALID);
	}
	@Nonnull public VehiclePartDefinition GetPartDef(@Nonnull VehiclePartPath partPath)
	{
		return GetHierarchy().FindNode(partPath).flatMap(node -> Optional.of(node.Def)).orElse(VehiclePartDefinition.INVALID);
	}
	protected void LoadDamageState(@Nonnull CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			CompoundTag partTags = tags.getCompound(key);
			VehicleComponentPath path = VehicleComponentPath.of(key);
			if(HasDamageablePart(path))
			{
				SetHealthOf(path, partTags.getFloat("hp"));
			}
			else FlansMod.LOGGER.warn("Damage key " + key + " was stored in vehicle save data, but this vehicle doesn't have that part");
		}
	}
	@Nonnull
	protected CompoundTag SaveDamageState()
	{
		CompoundTag tags = new CompoundTag();
		GetHierarchy().ForEachDamageable((partPath, damageDef) ->
		{
			CompoundTag partTags = new CompoundTag();
			partTags.putFloat("hp", GetHealthOf(partPath));
			tags.put(partPath.toString(), partTags);
		});
		return tags;
	}


	// ---------------------------------------------------------------------------------------------------------
	// PHYSICS MODULE
	@Nonnull
	public VehiclePartPhysics GetPartPhysics(@Nonnull VehicleComponentPath path)
	{
		return PhysicsParts.getOrDefault(path, VehiclePartPhysics.invalid);
	}
	@Nonnull
	public VehiclePartPhysics GetCorePhysics()
	{
		return PhysicsParts.getOrDefault(VehicleComponentPath.coreArticulation, VehiclePartPhysics.invalid);
	}
	@Nonnull
	public ForcesOnPart GetCoreForces() { return GetCorePhysics().Forces; }
	@Nonnull
	public ForcesOnPart GetForcesOn(@Nonnull VehicleComponentPath path) { return GetPartPhysics(path).Forces; }
	@Nonnull
	public ColliderHandle GetCorePhsyicsHandle() { return GetCorePhysics().PhysicsHandle; }
	@Nonnull
	public ColliderHandle GetPhysicsHandle(@Nonnull VehicleComponentPath path) { return GetPartPhysics(path).PhysicsHandle; }
	public void AddForceTo(@Nonnull VehicleComponentPath path)
	{

	}

	protected void CheckInitPhysics()
	{
		if(PhysicsParts.isEmpty())
			InitPhysics();
	}
	protected void InitPhysics()
	{
		Map<VehiclePartPath, ImmutableList<AABB>> bbLists = GatherBBs();
		OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());

		for(var kvp : bbLists.entrySet())
		{
			if(kvp.getKey().IsRoot())
			{
				Transform spawnedTransform = Transform.FromEntity(this);
				ColliderHandle handle = physics.RegisterDynamic(kvp.getValue(), spawnedTransform);
				PhysicsParts.put(VehicleComponentPath.coreArticulation, new VehiclePartPhysics(spawnedTransform, handle));
			}
			else
			{
				Transform t = GetWorldToPartCurrent(kvp.getKey());
				ColliderHandle handle = physics.RegisterDynamic(kvp.getValue(), t);
				PhysicsParts.put(kvp.getKey().Articulation(), new VehiclePartPhysics(t, handle));
			}
		}

		Def().AsHierarchy().ForEachWheel((wheelPath, wheelDef) ->
		{
			//WheelEntity wheel = new WheelEntity(FlansMod.ENT_TYPE_WHEEL.get(), level());
			//int wheelIndex = Wheels.Add(wheel, wheelPath, wheelDef.controlHints);
			//wheel.SetLinkToVehicle(this, wheelPath);
			Transform t = GetWorldToPartCurrent(wheelPath);
			ColliderHandle handle = physics.RegisterDynamic(List.of(new AABB(-wheelDef.radius, -wheelDef.radius, -wheelDef.radius, wheelDef.radius, wheelDef.radius, wheelDef.radius)), t);
			PhysicsParts.put(wheelPath, new VehiclePartPhysics(t, handle));
		});
	}
	protected void StopPhysics()
	{
		OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());
		for(VehiclePartPhysics part : PhysicsParts.values())
		{
			if(part.PhysicsHandle.IsValid())
			{
				physics.UnregisterDynamic(part.PhysicsHandle);
				part.PhysicsHandle = ColliderHandle.invalid;
			}
		}
		PhysicsParts.clear();
	}
	public void TeleportTo(@Nonnull Transform transform)
	{
		//ColliderHandle corePhysicsHandle = GetCorePhsyicsHandle();
		//if(corePhysicsHandle.IsValid())
		//{
		//	OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());
		//	physics.Teleport(corePhysicsHandle, transform);
//
		//	for(var kvp : PhysicsParts.entrySet())
		//	{
		//		if(!kvp.getKey().Part().IsRoot())
		//		{
		//			Transform relativePos = GetRootToPart(kvp.getKey().Part()).GetCurrent();
		//			physics.Teleport(kvp.getValue().PhysicsHandle, Transform.Compose(transform, relativePos));
		//		}
		//	}
		//}
	}

	protected void CopyResponsesToForceModel(@Nonnull OBBCollisionSystem physics, @Nullable ControlLogic controller)
	{
		if(OBBCollisionSystem.PAUSE_PHYSICS)
			return;

		for(var kvp : PhysicsParts.entrySet())
		{
			VehicleComponentPath path = kvp.getKey();
			VehiclePartPhysics part = kvp.getValue();

			part.LocationPrev = part.LocationCurrent;

			// Sum all the non-reactionary forces of the last frame
			LinearForce impactForce = part.Forces.SumLinearForces(part.LocationCurrent, false);
			part.Forces.EndFrame();


			if(part.PhysicsHandle.IsValid())
			{
				// Do we need to add these?
				// TODO: If debug, show them?
				AtomicReference<Boolean> collidedX = new AtomicReference<>(false),
					collidedY = new AtomicReference<>(false),
					collidedZ = new AtomicReference<>(false);

				Transform newPos =  physics.ProcessEvents(part.PhysicsHandle,
					(collision) -> {
						if(!Maths.Approx(collision.ContactNormal().x, 0d))
							collidedX.set(true);
						if(!Maths.Approx(collision.ContactNormal().y, 0d))
							collidedY.set(true);
						if(!Maths.Approx(collision.ContactNormal().z, 0d))
							collidedZ.set(true);

					},
					(collision) -> {
						if(!Maths.Approx(collision.ContactNormal().x, 0d))
							collidedX.set(true);
						if(!Maths.Approx(collision.ContactNormal().y, 0d))
							collidedY.set(true);
						if(!Maths.Approx(collision.ContactNormal().z, 0d))
							collidedZ.set(true);
					});
				//LinearVelocity v1 = physics.GetLinearVelocity(part.PhysicsHandle);
				//LinearVelocity v2 = new LinearVelocity(new Vec3(
				//	collidedX.get() ? 0.0d : v1.Velocity().x,
				//	collidedY.get() ? 0.0d : v1.Velocity().y,
				//	collidedZ.get() ? 0.0d : v1.Velocity().z));
				//physics.SetLinearVelocity(part.PhysicsHandle, v2);
				part.Forces.AddReactionForce(new LinearForce(new Vec3(
					collidedX.get() ? -impactForce.Force().x : 0.0d,
					collidedY.get() ? -impactForce.Force().y : 0.0d,
					collidedZ.get() ? -impactForce.Force().z : 0.0d)));

				part.LocationCurrent = newPos;
			}
		}
	}
	protected void TickController(@Nullable ControlLogic controller)
	{
		if(OBBCollisionSystem.PAUSE_PHYSICS)
			return;

		if(controller != null)
		{
			VehicleInputState inputs = GetInputStateFor(controller);
			if(IsAuthority())
			{
				controller.TickAuthoritative(this, inputs);
			}
			else
			{
				controller.TickRemote(this, inputs);
			}

			// We should make sure the controller didn't go wrong!

			if(Double.isNaN(position().x)
				|| Double.isNaN(position().y)
				|| Double.isNaN(position().z))
			{
				FlansMod.LOGGER.error("Vehicle went to NaNsville. Reverting one frame");
				setPos(xOld, yOld, zOld);
			}
		}
		else
		{
			LinearForce coreGravity = LinearForce.kgBlocksPerSecondSq(new Vec3(0f, -9.81f * Def().physics.mass, 0f));

			GetCorePhysics().Forces.AddForce(coreGravity);
			GetCorePhysics().Forces.AddDampener(0.1f);
			GetHierarchy().ForEachWheel((path, def) -> {
				LinearForce wheelGravity = LinearForce.kgBlocksPerSecondSq(new Vec3(0f, -9.81f * def.mass, 0f));
				GetPartPhysics(path).Forces.AddForce(wheelGravity);
				GetPartPhysics(path).Forces.AddDampener(0.1f);
			});
		}
	}
	protected void SendNewForcesToPhysics(@Nonnull OBBCollisionSystem physics)
	{
		if(OBBCollisionSystem.PAUSE_PHYSICS)
			return;

		{
			VehiclePartPhysics corePhysics = GetCorePhysics();
			LinearAcceleration linear = corePhysics.Forces.SumLinearAcceleration(corePhysics.LocationCurrent, Def().physics.mass, true);
			AngularAcceleration angular = corePhysics.Forces.SumAngularAcceleration(corePhysics.LocationCurrent, Def().physics.MomentOfInertia(), true);
			//float dampening = 1.0f;//part.Forces.GetDampeningRatio();
			//if(dampening < 1.0f)
			//{
			//	linear = linear.subtract(part.GetVelocityMS().scale(1.0f - dampening));
			//}

			physics.AddLinearAcceleration(corePhysics.PhysicsHandle, linear);
			physics.AddAngularAcceleration(corePhysics.PhysicsHandle, angular);
		}

		GetHierarchy().ForEachWheel((path, def) ->
		{
			VehiclePartPhysics wheelPhysics = GetPartPhysics(path);

			LinearAcceleration linear = wheelPhysics.Forces.SumLinearAcceleration(GetWorldToPartCurrent(path), def.mass, true);
			AngularAcceleration angular = wheelPhysics.Forces.SumAngularAcceleration(GetWorldToPartCurrent(path), def.MomentOfInertia(), true);
			//float dampening = 1.0f;//part.Forces.GetDampeningRatio();
			//if(dampening < 1.0f)
			//{
			//	linear = linear.subtract(part.GetVelocityMS().scale(1.0f - dampening));
			//}

			physics.AddLinearAcceleration(wheelPhysics.PhysicsHandle, linear);
			physics.AddAngularAcceleration(wheelPhysics.PhysicsHandle, angular);
		});
	}
	protected void TickPhysics()
	{
		// Check for missing wheels
		//Wheels.ForEachWithRemoval((wheel -> {
		//	if(wheel == null || !wheel.isAlive())
		//	{
		//		FlansMod.LOGGER.warn(this + ": Did not expect our wheel to die");
		//		return true;
		//	}
		//	return false;
		//}));



//
		//Wheels.ForEach(wheel -> wheel.StartTick(this));



		// Create a force model and load it up with forces for debug rendering
		ControlLogic controller = CurrentController();
		OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());

		// Check to see if the physics system killed us
		AABB physicsBounds = null;
		for(VehiclePartPhysics part : PhysicsParts.values())
		{
			if(part.PhysicsHandle.IsValid())
			{
				if (physics.IsHandleInvalidated(part.PhysicsHandle))
				{
					part.PhysicsHandle = ColliderHandle.invalid;
				}
				else if(physicsBounds == null)
				{
					physicsBounds = new AABB(part.LocationCurrent.PositionVec3(), part.LocationCurrent.PositionVec3());
				}
				else
				{
					physicsBounds = physicsBounds.expandTowards(part.LocationCurrent.PositionVec3());
				}

			}
		}
		if(physicsBounds != null &&
			(physicsBounds.getXsize() > 1024d
			|| physicsBounds.getYsize() > 1024d
			|| physicsBounds.getZsize() > 1024d))
		{
			StopPhysics();
			return;
		}

		// Respond to collision events from last frame, and update our location
		CopyResponsesToForceModel(physics, controller);
		SyncTransformToEntity();

		// Apply one frame of control logic
		TickController(controller);

		// Now send the results of the Force Model to the collision engine
		SendNewForcesToPhysics(physics);

		// Wheels need to move too
		//for(int i = 0; i < Wheels.All().size(); i++)
		//{
		//	WheelEntity wheel = Wheels.ByIndex(i);
		//	if(wheel != null)
		//		wheel.PhysicsTick(this, i);
		//}

		// End of Tick
		if(IsAuthority())
		{

		}
		else
		{
			//SyncEntityToTransform();
		}

		//Wheels.ForEach(wheel -> wheel.EndTick(this));
	}
	//protected void ProcessStaticCollision(@Nonnull VehiclePartPath onPart, @Nullable ControlLogic controller, @Nonnull ForceModel forces, @Nonnull StaticCollisionEvent collision)
	//{
	//	// Controller can override collision events
	//	if(controller != null && !controller.OnCollide(onPart, collision, forces))
	//		return;
//
	//	//forces.AddGlobalOffsetForce(onPart, collision.ContactPoint(), collision.ContactNormal(), () -> "Reaction force against static geometry at " + collision.ContactPoint());
	//}
	//protected void ProcessDynamicCollision(@Nonnull VehiclePartPath onPart, @Nullable ControlLogic controller, @Nonnull ForceModel forces, @Nonnull DynamicCollisionEvent collision)
	//{
	//	// Controller can override collision events
	//	if(controller != null && !controller.OnCollide(onPart, collision, forces))
	//		return;
//
	//	//forces.AddGlobalOffsetForce(onPart, collision.ContactPoint(), collision.ContactNormal(), () -> "Reaction force against dynamic object at " + collision.ContactPoint());
//
	//}
	public void SetWheelSaveData(@Nonnull PerPartMap<WheelSyncState> map) { entityData.set(WHEELS_ACCESSOR, map); }
	@Nonnull
	public PerPartMap<WheelSyncState> GetWheelSaveData() { return entityData.get(WHEELS_ACCESSOR); }
	protected void LoadWheelState(@Nonnull CompoundTag tags)
	{
		PerPartMap<WheelSyncState> map = GetWheelSaveData();
		for(String key : tags.getAllKeys())
		{
			VehicleComponentPath path = VehicleComponentPath.of(key);
			WheelSyncState wheelState = new WheelSyncState();
			wheelState.Load(this, tags.getCompound(key));
			map.Put(path, wheelState);
		}
		SetWheelSaveData(map);
	}

	@Nonnull
	protected CompoundTag SaveWheelState()
	{
		PerPartMap<WheelSyncState> map = GetWheelSaveData();
		CompoundTag tags = new CompoundTag();
		GetHierarchy().ForEachWheel((partPath, wheelDef) ->
		{
			map.TryGet(partPath).ifPresent((wheelState) ->
			{
				tags.put(partPath.toString(), wheelState.Save(this));
			});
		});
		return tags;
	}

	@Nonnull
	private Map<VehiclePartPath, ImmutableList<AABB>> GatherBBs()
	{
		Map<VehiclePartPath, ImmutableList<AABB>> completedLists = new HashMap<>();
		ImmutableList.Builder<AABB> rootObject = new ImmutableList.Builder<>();
		VehicleDefinitionHierarchy.VehicleNode rootNode = GetHierarchy().RootNode;
		GatherBBs(rootNode, rootObject, completedLists);
		completedLists.put(rootNode.GetPath(), rootObject.build());
		return completedLists;
	}
	private void GatherBBs(@Nonnull VehicleDefinitionHierarchy.VehicleNode node,
						   @Nonnull ImmutableList.Builder<AABB> aabbListBuilder,
						   @Nonnull Map<VehiclePartPath, ImmutableList<AABB>> completedLists)
	{
		if(node.Def.IsArticulated())
		{
			// If this object is articulated, it has a separate physics object
			ImmutableList.Builder<AABB> articulatedObject = new ImmutableList.Builder<>();
			if(node.Def.IsDamageable())
			{
				articulatedObject.add(node.Def.damage.Hitbox.get());
			}
			for(VehicleDefinitionHierarchy.VehicleNode child : node.ChildNodes.values())
			{
				GatherBBs(child, articulatedObject, completedLists);
			}
			completedLists.put(node.GetPath(), articulatedObject.build());
		}
		else
		{
			if(node.Def.IsDamageable())
			{
				aabbListBuilder.add(node.Def.damage.Hitbox.get());
			}
			for(VehicleDefinitionHierarchy.VehicleNode child : node.ChildNodes.values())
			{
				GatherBBs(child, aabbListBuilder, completedLists);
			}
		}
	}



	// ---------------------------------------------------------------------------------------------------------
	// ENGINE MODULE

	@Nonnull
	@Override
	public EngineDefinition GetDefaultEngine() { return Def().defaultEngine; }
	@Nonnull
	@Override
	public PerPartMap<EngineSyncState> GetEngineSaveData() { return entityData.get(ENGINES_ACCESSOR); }
	@Override
	public void SetEngineSaveData(@Nonnull PerPartMap<EngineSyncState> map) { entityData.set(ENGINES_ACCESSOR, map); }
	@Override
	public void ModifyEngineSaveData(@Nonnull VehicleComponentPath enginePath, @Nonnull Consumer<EngineSyncState> func)
	{
		PerPartMap<EngineSyncState> map = GetEngineSaveData();
		map.CreateAndApply(enginePath,
			EngineSyncState::new,
			func);
		SetEngineSaveData(map);
	}
	protected void LoadEngineData(@Nonnull CompoundTag tags)
	{
		PerPartMap<EngineSyncState> engineMap = GetEngineSaveData();
		for(String key : tags.getAllKeys())
		{
			VehicleComponentPath path = VehicleComponentPath.of(key);
			engineMap.CreateAndApply(path,
				EngineSyncState::new,
				(state) -> state.Load(this, tags.getCompound(key)));
		}
		SetEngineSaveData(engineMap);
	}
	@Nonnull
	protected CompoundTag SaveEngineData()
	{
		PerPartMap<EngineSyncState> map = GetEngineSaveData();
		CompoundTag tags = new CompoundTag();
		// tODO:
		//GetHierarchy().ForEachEngine((path, def) -> {
		//	int hash = path.hashCode();
		//	CompoundTag engineTags = new CompoundTag();
		//	if(map.Values.containsKey(hash))
		//		tags.put(hash, map.Values.get(hash).Save(this));
		//	tags.put(path.toString(), engineTags);
		//});
		return tags;
	}
	// ---------------------------------------------------------------------------------------------------------


	// ---------------------------------------------------------------------------------------------------------
	// SEATS MODULE
	@Override @Nonnull
	public List<VehicleComponentPath> GetSeatOrdering() { return SeatOrdering; }
	@Override @Nonnull
	public PerPartMap<SeatSyncState> GetSeatSaveData() { return entityData.get(SEATS_ACCESSOR); }
	@Override
	public void SetSeatSaveData(@Nonnull PerPartMap<SeatSyncState> map) { entityData.set(SEATS_ACCESSOR, map); }
	public void LoadSeatState(@Nonnull CompoundTag tags)
	{
		PerPartMap<SeatSyncState> seatMap = GetSeatSaveData();
		for(String key : tags.getAllKeys())
		{
			VehicleComponentPath path = VehicleComponentPath.of(key);

			seatMap.CreateAndApply(path,
				SeatSyncState::new,
				(state) -> state.Load(this, tags.getCompound(key)));
		}
		SetSeatSaveData(seatMap);
	}
	@Nonnull
	public CompoundTag SaveSeatState()
	{
		PerPartMap<SeatSyncState> map = GetSeatSaveData();
		CompoundTag tags = new CompoundTag();
		GetHierarchy().ForEachSeat((seatPath, seatDef) ->
		{
			map.TryGet(seatPath).ifPresent((state) ->
			{
				tags.put(seatPath.toString(), state.Save(this));
			});
		});
		return tags;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// CONTROLLER MODULE
	@OnlyIn(Dist.CLIENT)
	public void Client_GetLocalPlayerInputs(@Nonnull List<ControlSchemeDefinition> controlSchemes,
											@Nonnull InputDefinition[] additionalInputs)
	{
		// Process any control schemes that are active (e.g. CarController, 1AxisTurretController)
		for(ControlSchemeDefinition scheme : controlSchemes)
		{
			VehicleInputState inputs = GetInputStateFor(scheme);
			for(ControlSchemeAxisDefinition axis : scheme.axes)
			{
				float value = ClientInputHooks.GetInput(axis.axisType);
				inputs.SetInput(axis.axisType, value);
			}
		}
		for(InputDefinition input : additionalInputs)
		{
			VehicleInputState miscInputs = GetMiscInputState();
			miscInputs.SetInput(input.key, ClientInputHooks.GetInput(input.key));
		}
	}
	protected void TickControlSchemes()
	{
		Map<ControlSchemeDefinition, VehicleInputState> statesToTick = new HashMap<>();
		for(int i = 0; i < SeatOrdering.size(); i++)
		{
			VehicleComponentPath seatPath = SeatOrdering.get(i);
			GetHierarchy().IfSeatExists(seatPath, (seatDef) ->
			{
				// If there is someone in this seat, process inputs
				Entity passenger = GetPassengerInSeat(seatPath, getPassengers());
				if (passenger != null) // TODO: && passenger.canDriveVehicle
				{
					if (passenger instanceof Player player && player.isLocalPlayer())
					{
						List<ControlSchemeDefinition> controlSchemes = GetActiveControllersForSeat(seatPath, ModalStates);

						Client_GetLocalPlayerInputs(controlSchemes, seatDef.inputs);

						for (ControlSchemeDefinition scheme : controlSchemes)
						{
							statesToTick.put(scheme, GetInputStateFor(scheme));
						}
					}
					//else if(isControlledByAI())
					//{
//
					//}

				}
			});
		}

		for(var kvp : statesToTick.entrySet())
		{
			kvp.getValue().Tick(kvp.getKey());
		}
	}
	public void SelectController(@Nonnull ControlSchemeDefinition controllerDef)
	{
		// TODO: Send events for unselected, letting vehicles play anims etc

		if(!Controllers.containsKey(controllerDef.GetLocation()))
		{
			ControlLogic controller = ControlLogics.InstanceControlLogic(controllerDef);
			if(controller != null)
			{
				Controllers.put(controllerDef.GetLocation(), controller);
				SelectedControllerLocation = controllerDef.GetLocation();

				// TODO: Send events for selected
			}
			else
			{
				FlansMod.LOGGER.warn(this + ": Could not create control logic for '" + controllerDef.GetLocation() + "'");
			}
		}
	}
	@Nullable
	public ControlLogic CurrentController()
	{
		return Controllers.get(GetActiveControllerDef().GetLocation());
	}

	@Nonnull
	public Collection<ControlSchemeDefinition> GetValidControllersForSeat(@Nonnull VehicleComponentPath seatName)
	{
		Optional<Collection<ControlSchemeDefinition>> result = GetHierarchy().IfSeatExists(seatName, (seatDef) ->
		{
			return seatDef.Controllers.get().values();
		});
		return result.orElse(List.of());
	}
	@Nonnull
	public List<ControlSchemeDefinition> GetActiveControllersForSeat(@Nonnull VehicleComponentPath seatName,
																	  @Nonnull Map<String, String> modes)
	{
		Optional<List<ControlSchemeDefinition>> result = GetHierarchy().IfSeatExists(seatName, (seatDef) ->
		{
			List<ControlSchemeDefinition> matches = new ArrayList<>();
			for(VehicleControlOptionDefinition option : seatDef.controllerOptions)
			{
				if(option.Passes(modes))
					matches.add(seatDef.Controllers.get().get(option.key));
			}
			return matches;
		});
		return result.orElse(List.of());
	}
	@Nonnull
	public List<ControlSchemeDefinition> GetAllActiveControllers(@Nonnull Map<String, String> modes)
	{
		List<ControlSchemeDefinition> matches = new ArrayList<>();
		GetHierarchy().ForEachSeat((seatPath, seatDef) ->
		{
			for(VehicleControlOptionDefinition option : seatDef.controllerOptions)
			{
				if(option.Passes(modes))
				{
					ControlSchemeDefinition scheme = seatDef.Controllers.get().get(option.key);
					if(scheme != null && !matches.contains(scheme))
						matches.add(scheme);
				}
			}
		});
		return matches;
	}
	@Nullable
	public ControlSchemeDefinition GetMainActiveController(@Nonnull Map<String, String> modes)
	{
		for (VehicleComponentPath seatName : GetSeatOrdering())
		{
			List<ControlSchemeDefinition> activeForSeat = GetActiveControllersForSeat(seatName, modes);
			if(activeForSeat.size() > 1)
				FlansMod.LOGGER.warn("Seat " + seatName + " has more than 1 control scheme active?");
			if(activeForSeat.size() > 0)
				return activeForSeat.get(0);
		}
		return null;
	}
	@Nonnull
	public List<ControlSchemeDefinition> GetAllValidControllers()
	{
		List<ControlSchemeDefinition> list = new ArrayList<>();
		GetHierarchy().ForEachSeat((partPath, seatDef) -> {
			for(ControlSchemeDefinition controlScheme : seatDef.Controllers.get().values())
				if(!list.contains(controlScheme))
					list.add(controlScheme);
		});
		return list;
	}

	// ---------------------------------------------------------------------------------------------------------
	// GUNS MODULE
	public void SetGunSaveData(@Nonnull PerPartMap<GunSyncState> map) { entityData.set(GUNS_ACCESSOR, map); }
	@Nonnull
	public PerPartMap<GunSyncState> GetGunSaveData() { return entityData.get(GUNS_ACCESSOR); }
	protected void InitGuns()
	{
		GetHierarchy().ForEachGun((partPath, gunDef) -> {
			GunOrdering.add(partPath);
		});
	}
	public void SetGunState(@Nonnull VehicleComponentPath partName, @Nonnull GunSyncState gunState)
	{
		PerPartMap<GunSyncState> map = GetGunSaveData();
		map.Put(partName, gunState);
		SetGunSaveData(map);
	}
	public void SetGunState(int index, @Nonnull GunSyncState gunState)
	{
		SetGunState(GunOrdering.get(index), gunState);
	}
	@Nonnull
	public GunSyncState GetGunStateAtIndex(int index) {
		return GetGunSaveData().GetOrDefault(GunOrdering.get(index), GunSyncState.INVALID);
	}
	@Nonnull
	public VehicleComponentPath GetVehiclePartNameOfGunAtIndex(int index) { return GunOrdering.get(index); }
	@Nonnull
	public UUID GetGunIDAtIndex(int index) { return GetGunStateAtIndex(index).GetGunID(); }
	public int GetIndexOfGunID(@Nonnull UUID gunID)
	{
		for(int i = 0; i < GunOrdering.size(); i++)
			if(GetGunStateAtIndex(i).GetGunID().equals(gunID))
				return i;
		return -1;
	}
	@Nonnull
	public List<UUID> GetAllGunIDs()
	{
		List<UUID> uuids = new ArrayList<>();
		for(GunSyncState state : GetGunSaveData().Values())
		{
			UUID id = state.GetGunID();
			if(!id.equals(GunItem.InvalidGunUUID))
				uuids.add(id);
		}
		return uuids;
	}
	protected void LoadGunState(@Nonnull CompoundTag tags)
	{
		PerPartMap<GunSyncState> map = GetGunSaveData();
		for(String key : tags.getAllKeys())
		{
			VehicleComponentPath path = VehicleComponentPath.of(key);
			GunSyncState gunState = new GunSyncState();
			gunState.Load(this, tags.getCompound(key));
			map.Put(path, gunState);
		}
		SetGunSaveData(map);
	}

	@Nonnull
	protected CompoundTag SaveGunState()
	{
		PerPartMap<GunSyncState> map = GetGunSaveData();
		CompoundTag tags = new CompoundTag();
		GetHierarchy().ForEachGun((partPath, gunDef) ->
		{
			map.TryGet(partPath).ifPresent((gunState) ->
			{
				tags.put(partPath.toString(), gunState.Save(this));
			});
		});
		return tags;
	}


	// Inventory
	@Nonnull
	private VehicleInventory CreateInventory()
	{
		VehicleDefinition def = Def();
		// TODO
		return new VehicleInventory(1, 1, 1);
	}
	public double GetSpeedXZ() { return Maths.LengthXZ(getDeltaMovement()); }
	public double GetSpeed() { return Maths.LengthXYZ(getDeltaMovement()); }


	@Nonnull
	public ControlSchemeDefinition GetActiveControllerDef()
	{
		ControlSchemeDefinition active = GetMainActiveController(ModalStates);
		return active != null ? active : ControlSchemeDefinition.INVALID;
	}


	/// --------------------------------------------------------------------------
	// ITransformEntity
	@Override
	public void SyncTransformToEntity()
	{
		Transform worldRoot = GetWorldToEntity().GetCurrent();
		Vector3f euler = worldRoot.Euler();
		setPos(worldRoot.PositionVec3());
		yRotO = euler.y;
		xRotO = euler.x;
	}
	@Override
	public void SyncEntityToTransform()
	{
		Transform coreTransformNew = Transform.FromPosAndEuler(getPosition(0f), getXRot(), getYRot(), 0f);

		if(PhysicsParts.isEmpty())
		{
			// Init Physics should take care of all this for us.
			CheckInitPhysics();
		}
		else
		{
			// So we want to teleport the whole vehicle
			VehiclePartPhysics corePart = GetCorePhysics();
			Transform coreTransformOld = corePart.LocationCurrent;

			// We should move all the other parts, relative to the root
			Map<ColliderHandle, Transform> newPartPositions = new HashMap<>();
			for(var kvp : PhysicsParts.entrySet())
			{
				if(kvp.getKey().Part().IsRoot())
					continue;

				Transform relativeTransformOld = coreTransformOld.GlobalToLocalTransform(kvp.getValue().LocationCurrent);
				Transform partTransformNew = coreTransformNew.LocalToGlobalTransform(relativeTransformOld);
				newPartPositions.put(kvp.getValue().PhysicsHandle, partTransformNew);
				kvp.getValue().LocationCurrent = partTransformNew;
				kvp.getValue().LocationPrev = partTransformNew;
			}

			// And if those parts have physics handles, we should update them in the physics system too.
			OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());
			ColliderHandle rootHandle = GetCorePhsyicsHandle();
			corePart.LocationPrev = coreTransformNew;
			corePart.LocationCurrent = coreTransformNew;
			physics.Teleport(rootHandle, coreTransformNew);
			for(var kvp : newPartPositions.entrySet())
			{
				physics.Teleport(kvp.getKey(), kvp.getValue());
			}
		}
	}
	@Override public void SetWorldToEntity(@Nonnull Transform currentTransform) { TeleportTo(currentTransform); }
	@Override @Nonnull public ITransformPair GetWorldToEntity() { return GetWorldToRoot(); }
	@Override @Nonnull public ITransformPair GetEntityToAP(@Nonnull VehiclePartPath apPath) { return GetRootToPart(apPath); }
	@Override @Nonnull
	public Vec3 GetVelocity() { return getDeltaMovement().scale(20f); }
	@Override
	public void SetVelocity(@Nonnull Vec3 velocityMetersPerSecond) { setDeltaMovement(velocityMetersPerSecond.scale(1f/20f)); }
	@Override
	public void ApplyVelocity()
	{
		// Stash pre-values
		Vec3 prePos = position();
		Vec3 expectedMovement = getDeltaMovement();

		// Apply the movement
		move(MoverType.SELF, getDeltaMovement());
		CheckCollisions();

		// Check where we ended up
		Vec3 postPos = position();
		Vec3 actualMovement = postPos.subtract(prePos);

		// If we collided, add a normal reaction "force".
		// This should not actually be applied in the force model (we are after application now),
		// but it is useful to render it
		if(verticalCollision || horizontalCollision)
		{
			//ForcesLastFrame.AddGlobalForce(VehiclePartPath.Core,
			//	actualMovement.subtract(expectedMovement).scale(20f * 20f * Def().physics.mass),
			//	() -> "Normal reaction force");
		}
	}
	/// --------------------------------------------------------------------------------


	private void CheckCollisions()
	{
		Level level = level();
		List<Entity> list = level.getEntities(this, getBoundingBox().inflate((double)0.2F, (double)-0.01F, (double)0.2F), EntitySelector.pushableBy(this));
		if (!list.isEmpty()) {
			//boolean flag = !level.isClientSide && !(this.getControllingPassenger() instanceof Player);

			for(int j = 0; j < list.size(); ++j) {
				Entity entity = list.get(j);
				if (!entity.hasPassenger(this)) {
					push(entity);
					//if (flag && this.getPassengers().size() < this.getMaxPassengers() && !entity.isPassenger() && this.hasEnoughSpaceFor(entity) && entity instanceof LivingEntity && !(entity instanceof WaterAnimal) && !(entity instanceof Player)) {
					//	entity.startRiding(this);
					//} else {
					//	this.push(entity);
					//}
				}
			}
		}
	}
	/// ----------------------------------------


}
