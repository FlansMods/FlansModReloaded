package com.flansmod.common.entity.vehicle;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.controls.ControlLogic;
import com.flansmod.common.entity.vehicle.controls.VehicleInputState;
import com.flansmod.common.entity.vehicle.damage.VehicleDamageModule;
import com.flansmod.common.entity.vehicle.guns.VehicleGunModule;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleHierarchyModule;
import com.flansmod.common.entity.vehicle.physics.VehicleEngineModule;
import com.flansmod.common.entity.vehicle.physics.VehiclePhysicsModule;
import com.flansmod.common.entity.vehicle.seats.VehicleSeatsModule;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

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
	}

	public boolean InitFromDefinition()
	{
		VehicleDefinition def = Def();
		if(!def.IsValid())
			return false;

		return true;
	}

	// -------------------------------------------------------------------------------------------
	// Transform and some vanilla overrides. We want to use Quaternions, pleassse Minecraft
	@Override public float getYRot() { return RootTransform0().Yaw(); }
	@Override public float getXRot() { return RootTransform0().Pitch(); }
	@Override public void setYRot(float yaw) { Hierarchy().SetYaw(yaw); }
	@Override public void setXRot(float pitch) { Hierarchy().SetPitch(pitch); }


	public void SetEulerAngles(float pitch, float yaw, float roll) { Hierarchy().SetEulerAngles(pitch, yaw, roll); }
	@Nonnull
	public Transform RootTransform0() { return Hierarchy().RootTransform; }
	@Nonnull
	public Transform RootTransform(float dt) { return Hierarchy().GetRootTransformDelta(dt); }
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
		int driverSeatIndex = Seats().GetControlSeatIndex();
		if(driverSeatIndex != VehicleSeatsModule.INVALID_SEAT_INDEX)
		{
			if (Seats().GetPassengerInSeat(driverSeatIndex) instanceof LivingEntity living)
				return living;
		}
		return null;
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

	@Nonnull
	@Override
	public Transform GetLocal0() { return Hierarchy().RootTransform; }
	@Nonnull
	@Override
	public Transform GetLocal(float dt) { return Hierarchy().GetRootTransformDelta(dt); }
	@Nullable
	@Override
	public ITransformEntity GetParent() { return null; }

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
}
