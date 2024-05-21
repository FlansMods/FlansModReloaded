package com.flansmod.common.entity.vehicle.physics;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.ControlLogic;
import com.flansmod.common.entity.vehicle.controls.ControlLogics;
import com.flansmod.common.entity.vehicle.controls.ForceModel;
import com.flansmod.common.entity.vehicle.controls.VehicleInputState;
import com.flansmod.common.entity.vehicle.hierarchy.WheelEntity;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.common.types.vehicles.elements.VehiclePhysicsDefinition;
import com.flansmod.common.types.vehicles.elements.WheelDefinition;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.codehaus.plexus.util.CachedMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class VehiclePhysicsModule implements IVehicleModule
{
	public static boolean PAUSE = false;

	@Nonnull
	public final VehiclePhysicsDefinition Def;
	@Nonnull
	public ForceModel ForcesLastFrame;

	// Part lookups
	private final MultiLookup<EControlLogicHint, WheelEntity> Wheels = new MultiLookup<>();
	private final MultiLookup<EControlLogicHint, VehiclePropellerSaveState> Propellers = new MultiLookup<>();



	private final Map<ResourceLocation, ControlLogic> Controllers = new HashMap<>();
	@Nonnull
	private ResourceLocation SelectedControllerLocation = new ResourceLocation(FlansMod.MODID, "control_schemes/null");




	public VehiclePhysicsModule(@Nonnull VehiclePhysicsDefinition def)
	{
		Def = def;
		ForcesLastFrame = new ForceModel();
	}

	public void CreateSubEntities(@Nonnull VehicleEntity vehicle)
	{
		vehicle.Def().AsHierarchy.get().ForEachWheel((wheelPath, wheelDef) -> {
			WheelEntity wheel = new WheelEntity(vehicle, wheelPath, wheelDef);
			Wheels.Add(wheel, wheelPath, wheelDef.controlHints);
		});

		for(int i = 0; i < Wheels.All().size(); i++)
		{
			WheelEntity wheel = WheelByIndex(i);
			if(wheel != null)
				vehicle.Hierarchy().RegisterWheel(i, wheel);
		}
	}
	private void UnregisterWheelAt(int wheelIndex)
	{
		Wheels.RemoveAt(wheelIndex);
	}

	@Nullable
	public ControlLogic CurrentController(@Nonnull VehicleEntity vehicle)
	{
		return Controllers.get(vehicle.GetActiveControllerDef().GetLocation());
	}

	// Wheel getters
	@Nonnull
	public Collection<WheelEntity> AllWheels() { return Wheels.All(); }
	@Nullable
	public WheelEntity WheelByIndex(int index) { return Wheels.ByIndex(index); }
	@Nonnull
	public Collection<WheelEntity> WheelsByVehiclePart(@Nonnull String partName) { return Wheels.ByPart(partName); }
	@Nonnull
	public Collection<WheelEntity> WheelsThatMatch(@Nonnull EControlLogicHint hint) { return Wheels.ByHint(hint); }
	@Nonnull
	public Collection<WheelEntity> WheelsThatMatch(@Nonnull EControlLogicHint ... hints) { return Wheels.ByHints(hints); }

	// Propeller getters
	@Nonnull
	public Collection<VehiclePropellerSaveState> AllProps() { return Propellers.All(); }
	@Nullable
	public VehiclePropellerSaveState PropByIndex(int index) { return Propellers.ByIndex(index); }
	@Nonnull
	public Collection<VehiclePropellerSaveState> PropsByVehiclePart(@Nonnull String partName) { return Propellers.ByPart(partName); }
	@Nonnull
	public Collection<VehiclePropellerSaveState> PropsThatMatch(@Nonnull EControlLogicHint hint) { return Propellers.ByHint(hint); }
	@Nonnull
	public Collection<VehiclePropellerSaveState> PropsThatMatch(@Nonnull EControlLogicHint ... hints) { return Propellers.ByHints(hints); }



	public void SelectController(@Nonnull VehicleEntity vehicle, @Nonnull ControlSchemeDefinition controllerDef)
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
				FlansMod.LOGGER.warn(vehicle + ": Could not create control logic for '" + controllerDef.GetLocation() + "'");
			}
		}
	}



	@Override
	public void Tick(@Nonnull VehicleEntity vehicle)
	{
		// Check for missing wheels
		Wheels.ForEachWithRemoval((wheel -> {
			if(wheel == null || !wheel.isAlive())
			{
				FlansMod.LOGGER.warn(vehicle + ": Did not expect our wheel to die");
				return true;
			}
			return false;
		}));

		Wheels.ForEach(wheel -> wheel.ParentTick(vehicle));

		// Create a force model and load it up with forces for debug rendering
		ForceModel forces = new ForceModel();

		ControlLogic controller = CurrentController(vehicle);
		if(controller != null)
		{
			VehicleInputState inputs = vehicle.GetInputStateFor(controller);
			if(vehicle.IsAuthority())
			{
				controller.TickAuthoritative(vehicle, inputs, forces);
			}
			else
			{
				controller.TickRemote(vehicle, inputs, forces);
			}

			// We should make sure the controller didn't go wrong!

			if(Double.isNaN(vehicle.position().x)
			|| Double.isNaN(vehicle.position().y)
			|| Double.isNaN(vehicle.position().z))
			{
				FlansMod.LOGGER.error("Vehicle went to NaNsville. Reverting one frame");
				vehicle.setPos(vehicle.xOld, vehicle.yOld, vehicle.zOld);
			}
		}
		else
		{
			forces.AddGlobalForceToCore(new Vec3(0f, -9.81f * Def.mass, 0f), () -> "Gravity");
			for(int i = 0; i < AllWheels().size(); i++)
			{
				WheelEntity wheel = WheelByIndex(i);
				if(wheel != null)
				{
					forces.AddGlobalForceToWheel(i, new Vec3(0f, -9.81f * wheel.Def.mass, 0f), () -> "Gravity");
					forces.AddDefaultWheelSpring(vehicle, wheel);
				}
			}

			//Vec3 motion = vehicle.getDeltaMovement();
			//motion = motion.add(0f, -9.81f / 20f, 0f);
			//vehicle.setDeltaMovement(motion);
			//vehicle.move(MoverType.SELF, motion);
		}


		// Stash our latest Force Model for debug rendering
		ForcesLastFrame = forces;
		if(!PAUSE)
		{
			// Now process the results of the Force Model
			Vec3 motion = vehicle.GetVelocity();
			motion = forces.ApplyLinearForcesToCore(motion, vehicle.GetWorldToEntity().GetCurrent(), Def.mass);
			motion = forces.ApplySpringForcesToCore(motion, vehicle.GetWorldToEntity().GetCurrent(), Def.mass, vehicle.Hierarchy()::GetWorldToPartCurrent);
			motion = forces.ApplyDampeningToCore(motion);
			vehicle.SetVelocity(motion);
			vehicle.ApplyVelocity();
		}

		vehicle.SyncEntityToTransform();


		// Wheels need to move too
		for(int i = 0; i < Wheels.All().size(); i++)
		{
			WheelEntity wheel = Wheels.ByIndex(i);
			if(wheel != null)
				wheel.PhysicsTick(vehicle, i, forces);
		}
	}

	@Override
	public void Load(@Nonnull VehicleEntity vehicle, @Nonnull CompoundTag tags)
	{

	}

	@Nonnull
	@Override
	public CompoundTag Save(@Nonnull VehicleEntity vehicle)
	{
		return new CompoundTag();
	}
}
