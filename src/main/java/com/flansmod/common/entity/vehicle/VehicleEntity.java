package com.flansmod.common.entity.vehicle;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.ITransformPair;
import com.flansmod.common.entity.vehicle.controls.ControlLogic;
import com.flansmod.common.entity.vehicle.controls.VehicleInputState;
import com.flansmod.common.entity.vehicle.damage.VehicleDamageModule;
import com.flansmod.common.entity.vehicle.guns.VehicleGunModule;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleHierarchyModule;
import com.flansmod.common.entity.vehicle.hierarchy.WheelEntity;
import com.flansmod.common.entity.vehicle.physics.VehicleEngineModule;
import com.flansmod.common.entity.vehicle.physics.VehiclePhysicsModule;
import com.flansmod.common.entity.vehicle.seats.VehicleSeatsModule;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.entity.PartEntity;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleEntity extends Entity implements ITransformEntity
{
	private final LazyDefinition<VehicleDefinition> DefRef;
	private final Lazy<VehicleDamageModule> LazyDamage = Lazy.of(() -> new VehicleDamageModule(Def().AsHierarchy.get(), this));
	private final Lazy<VehicleHierarchyModule> LazyHierarchy = Lazy.of(() -> new VehicleHierarchyModule(Def().AsHierarchy.get(), this));
	private final Lazy<VehicleGunModule> LazyGuns = Lazy.of(() -> new VehicleGunModule(Def().AsHierarchy.get(), this));
	private final Lazy<VehicleSeatsModule> LazySeats = Lazy.of(() -> new VehicleSeatsModule(Def().AsHierarchy.get(), this));
	private final Lazy<VehiclePhysicsModule> LazyPhysics = Lazy.of(() -> new VehiclePhysicsModule(Def().physics));
	private final Lazy<VehicleEngineModule> LazyEngine = Lazy.of(() -> new VehicleEngineModule(this));
	private final Lazy<VehicleInventory> LazyInventory = Lazy.of(this::CreateInventory);

	// Definition / ID Access
	@Nonnull public VehicleDefinition Def() { return DefRef.DefGetter().get(); }
	@Nonnull public ResourceLocation Loc() { return DefRef.Loc(); }

	// Module getters
	@Nonnull public VehicleDamageModule Damage() { return LazyDamage.get(); }
	@Nonnull public VehicleHierarchyModule Hierarchy() { return LazyHierarchy.get(); }
	@Nonnull public VehicleGunModule Guns() { return LazyGuns.get(); }
	@Nonnull public VehicleSeatsModule Seats() { return LazySeats.get(); }
	@Nonnull public VehiclePhysicsModule Physics() { return LazyPhysics.get(); }
	@Nonnull public VehicleEngineModule Engine() { return LazyEngine.get(); }


	// Inventory (sort of module-ey)
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
	@Override public float getYRot() { return RootTransform0().Yaw(); }
	@Override public float getXRot() { return RootTransform0().Pitch(); }
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
			wheel.SyncTransformToEntity();
	}

	public void SetEulerAngles(float pitch, float yaw, float roll) { Hierarchy().SetEulerAngles(pitch, yaw, roll); }
	@Nonnull
	public Transform RootTransform0() { return Hierarchy().RootTransform; }
	@Nonnull
	public Transform RootTransform(float dt) { return Hierarchy().GetWorldToRoot().GetDelta(dt); }
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


	// ---------------------------------------------------------------------------------------------------------
	// MODULES
	// ---------------------------------------------------------------------------------------------------------
	private void TickModules()
	{
		Engine().Tick(this);
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
		entityData.define(VehicleEngineModule.ENGINES_ACCESSOR, new PerPartMap<>());
		entityData.define(VehicleHierarchyModule.ARTICULATIONS_ACCESSOR, new PerPartMap<>());
		// TOO early for these to exist
		//Engine().DefineSyncedData(entityData);
		//Damage().DefineSyncedData(entityData);
		//Hierarchy().DefineSyncedData(entityData);
		//Guns().DefineSyncedData(entityData);
		//Seats().DefineSyncedData(entityData);
		//Physics().DefineSyncedData(entityData);
	}
	private void SaveModules(@Nonnull CompoundTag tags)
	{
		tags.put("engine", Engine().Save(this));
		tags.put("damage", Damage().Save(this));
		tags.put("articulation", Hierarchy().Save(this));
		tags.put("guns", Guns().Save(this));
		tags.put("seats", Seats().Save(this));
		tags.put("physics", Physics().Save(this));
	}
	private void LoadModules(@Nonnull CompoundTag tags)
	{
		if(tags.contains("engine"))
			Engine().Load(this, tags.getCompound("engine"));
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
		move(MoverType.SELF, getDeltaMovement());
		CheckCollisions();
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
