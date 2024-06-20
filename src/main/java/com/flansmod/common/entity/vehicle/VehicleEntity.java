package com.flansmod.common.entity.vehicle;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.ITransformPair;
import com.flansmod.common.entity.vehicle.controls.ControlLogic;
import com.flansmod.common.entity.vehicle.controls.VehicleInputState;
import com.flansmod.common.entity.vehicle.damage.VehicleDamageModule;
import com.flansmod.common.entity.vehicle.guns.VehicleGunModule;
import com.flansmod.common.entity.vehicle.hierarchy.ArticulationSyncState;
import com.flansmod.common.entity.vehicle.hierarchy.IVehicleTransformHelpers;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleHierarchyModule;
import com.flansmod.common.entity.vehicle.hierarchy.WheelEntity;
import com.flansmod.common.entity.vehicle.physics.*;
import com.flansmod.common.entity.vehicle.physics.VehicleEngineModule;
import com.flansmod.common.entity.vehicle.seats.VehicleSeatsModule;
import com.flansmod.common.network.FlansEntityDataSerializers;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.flansmod.util.collision.ColliderHandle;
import com.flansmod.util.collision.OBBCollisionSystem;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VehicleEntity extends Entity implements
	ITransformEntity,
	IVehicleEngineModule,
	IVehicleTransformHelpers

{
	// Data Synchronizer Accessors
	public static final EntityDataAccessor<PerPartMap<EngineSyncState>> ENGINES_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, FlansEntityDataSerializers.ENGINE_MAP);
	public static final EntityDataAccessor<PerPartMap<ArticulationSyncState>> ARTICULATIONS_ACCESSOR =
		SynchedEntityData.defineId(VehicleEntity.class, FlansEntityDataSerializers.ARTICULATION_MAP);



	// Location and sync
	@Nonnull public Transform RootTransform = Transform.Identity();
	@Nonnull public Transform RootTransformPrev = Transform.Identity();

	// Definition / ID Access
	@Nonnull private final LazyDefinition<VehicleDefinition> DefRef;
	@Nonnull public VehicleDefinition Def() { return DefRef.DefGetter().get(); }
	@Nonnull public ResourceLocation Loc() { return DefRef.Loc(); }


	// Inventory (sort of module-ey)
	private final Lazy<VehicleInventory> LazyInventory = Lazy.of(this::CreateInventory);
	@Nonnull public VehicleInventory Inventory() { return LazyInventory.get(); }

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
		if(!world.isClientSide)
			Physics().CreateSubEntities(this);
	}

	public boolean InitFromDefinition()
	{
		VehicleDefinition def = Def();
		if(!def.IsValid())
			return false;

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
			int seatIndex = Seats().GetSeatIndexOf(rider);

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
	@Override public void setYRot(float yaw) { Hierarchy().SetYaw(yaw); }
	@Override public void setXRot(float pitch) { Hierarchy().SetPitch(pitch); }
	//@Override public void setPos(double x, double y, double z) { SetPosition(x, y, z); }
	@Override
	public void moveTo(double x, double y, double z, float xRot, float yRot) {
		super.moveTo(x, y, z, xRot, yRot);
		SetAllPositionsFromEntity();
	}
	// This is the default entity movement packet
	@Override
	public void lerpTo(double x, double y, double z, float yaw, float pitch, int i, boolean flag)
	{
		super.lerpTo(x, y, z, yaw, pitch, i, flag);
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
		for(WheelEntity wheel : Physics().AllWheels())
			wheel.setPos(GetWorldToAP(wheel.GetWheelPath()).GetCurrent().PositionVec3());
	}

	public void SetEulerAngles(float pitch, float yaw, float roll) { Hierarchy().SetEulerAngles(pitch, yaw, roll); }
	@Nonnull public Transform RootTransformCurrent() { return Hierarchy().GetWorldToRoot().GetCurrent(); }
	@Nonnull public Transform RootTransformPrevious() { return Hierarchy().GetWorldToRoot().GetPrevious(); }
	@Nonnull public Transform RootTransform(float dt) { return Hierarchy().GetWorldToRoot().GetDelta(dt); }
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
		DefineModuleSyncing(entityData);
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
	@Override
	protected boolean canAddPassenger(@Nonnull Entity entity)
	{
		// TODO: Locking module (Do you have the car keys?)

		int seatIndex = Seats().GetSeatIndexForNewPassenger(entity);

		return seatIndex != VehicleSeatsModule.INVALID_SEAT_INDEX;
	}
	@Nullable
	@Override
	public LivingEntity getControllingPassenger()
	{
		if (Seats().GetControllingPassenger(this) instanceof LivingEntity living)
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

	// Useful Lookups
	// Part lookups
	private final MultiLookup<EControlLogicHint, WheelEntity> Wheels = new MultiLookup<>();
	private final MultiLookup<EControlLogicHint, VehiclePropellerSaveState> Propellers = new MultiLookup<>();
	private final Map<ResourceLocation, ControlLogic> Controllers = new HashMap<>();


	// ---------------------------------------------------------------------------------------------------------
	// ARTICULATION MODULE
	@Nonnull private PerPartMap<ArticulationSyncState> GetArticulationMap() { return VehicleDataSynchronizer.get(ARTICULATIONS_ACCESSOR); }
	private void SetArticulationMap(@Nonnull PerPartMap<ArticulationSyncState> map) { VehicleDataSynchronizer.set(ARTICULATIONS_ACCESSOR, map); }

	@Override
	public void ApplyWorldToRootPrevious(@Nonnull TransformStack stack)
	{
		stack.add(RootTransformPrev);
	}
	@Override
	public void ApplyWorldToRootCurrent(@Nonnull TransformStack stack)
	{
		stack.add(RootTransform);
	}
	@Override
	public void ApplyPartToPartPrevious(@Nonnull VehicleDefinitionHierarchy.Node childPart, @Nonnull TransformStack stack)
	{

	}
	@Override
	public void ApplyPartToPartCurrent(@Nonnull VehicleDefinitionHierarchy.Node childPart, @Nonnull TransformStack stack)
	{

	}
	@Override
	@Nonnull
	public VehicleDefinitionHierarchy.Node GetHeirarchyRoot()
	{
		return Def().AsHierarchy.get().RootNode;
	}



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
		Def().AsHierarchy.get().Traverse(vehiclePart, (node) -> { stack.add(GetPartLocalPrevious(node)); });
	}
	public void TransformRootToPartCurrent(@Nonnull String vehiclePart, @Nonnull TransformStack stack) {
		Def().AsHierarchy.get().Traverse(vehiclePart, (node) -> { stack.add(GetPartLocalCurrent(node)); });
	}

	// Part-to-Part Transforms
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
	// PHYSICS MODULE
	@Nullable
	public ColliderHandle CorePhsyicsHandle = null;
	@Nullable
	public ColliderHandle[] WheelPhysicsHandles = new ColliderHandle[0];

	private void InitPhysics()
	{
		OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());
		physics.RegisterDynamic()


		Def().AsHierarchy.get().ForEachWheel((wheelPath, wheelDef) -> {
			WheelEntity wheel = new WheelEntity(FlansMod.ENT_TYPE_WHEEL.get(), level());
			int wheelIndex = Wheels.Add(wheel, wheelPath, wheelDef.controlHints);
			wheel.SetLinkToVehicle(this, wheelIndex);
		});
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
	public void ModifyEngineSaveData(@Nonnull String enginePath, @Nonnull Consumer<EngineSyncState> func)
	{
		PerPartMap<EngineSyncState> map = GetEngineSaveData();
		map.CreateAndApply(enginePath,
			EngineSyncState::new,
			func);
		SetEngineSaveData(map);
	}
	// ---------------------------------------------------------------------------------------------------------


	// ---------------------------------------------------------------------------------------------------------
	// MODULES
	// ---------------------------------------------------------------------------------------------------------
	private void TickModules()
	{

		Damage().Tick(this);
		Hierarchy().Tick(this);
		Guns().Tick(this);
		Seats().Tick(this);
		Physics().Tick(this);
	}
	private void DefineModuleSyncing(@Nonnull SynchedEntityData entityData)
	{
		entityData.define(VehicleSeatsModule.SEATS_ACCESSOR, new PerPartMap<>());
		entityData.define(VehicleGunModule.GUNS_ACCESSOR, new PerPartMap<>());
		entityData.define(VehicleDamageModule.DAMAGE_ACCESSOR, new PerPartMap<>());
		entityData.define(ENGINES_ACCESSOR, new PerPartMap<>());
		entityData.define(VehicleHierarchyModule.ARTICULATIONS_ACCESSOR, new PerPartMap<>());
	}
	private void SaveModules(@Nonnull CompoundTag tags)
	{
		tags.put("engine", SaveEngineData(this));
		tags.put("damage", Damage().Save(this));
		tags.put("articulation", Hierarchy().Save(this));
		tags.put("guns", Guns().Save(this));
		tags.put("seats", Seats().Save(this));
		tags.put("physics", Physics().Save(this));
	}
	private void LoadModules(@Nonnull CompoundTag tags)
	{
		if(tags.contains("engine"))
			LoadEngineData(this, tags.getCompound("engine"));
		if(tags.contains("damage"))
			Damage().Load(this, tags.getCompound("damage"));
		if(tags.contains("articulation"))
			Hierarchy().Load(this, tags.getCompound("articulation"));
		if(tags.contains("guns"))
			Guns().Load(this, tags.getCompound("guns"));
		if(tags.contains("seats"))
			Seats().Load(this, tags.getCompound("seats"));
		if(tags.contains("physics"))
			Seats().Load(this, tags.getCompound("physics"));
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
	public double GetSpeedXZ() { return Maths.LengthXZ(getDeltaMovement()); }
	public double GetSpeed() { return Maths.LengthXYZ(getDeltaMovement()); }

	public void Raycast(@Nonnull Vec3 start, @Nonnull Vec3 end, @Nonnull List<HitResult> results)
	{
		Hierarchy().Raycast(this, start, end, results, 0f);
	}
	public void Raycast(@Nonnull Vec3 start, @Nonnull Vec3 end, @Nonnull List<HitResult> results, float dt)
	{
		Hierarchy().Raycast(this, start, end, results, dt);
	}
	@Nonnull
	public ControlSchemeDefinition GetActiveControllerDef()
	{
		ControlSchemeDefinition active = Seats().GetMainActiveController(ModalStates);
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
		Hierarchy().RootTransform = Transform.FromPosAndEuler(getPosition(1f), getXRot(), getYRot(), 0f);
	}
	@Override public void SetWorldToEntity(@Nonnull Transform currentTransform) { Hierarchy().SetCurrentRootTransform(currentTransform); }
	@Override @Nonnull public ITransformPair GetWorldToEntity() { return Hierarchy().GetWorldToRoot(); }
	@Override @Nonnull public ITransformPair GetEntityToAP(@Nonnull String apPath) { return Hierarchy().GetRootToPart(apPath); }
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
			Physics().ForcesLastFrame.AddGlobalForce(VehicleDefinition.CoreName,
				actualMovement.subtract(expectedMovement).scale(20f * 20f * Physics().Def.mass),
				() -> "Normal reaction force");
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
