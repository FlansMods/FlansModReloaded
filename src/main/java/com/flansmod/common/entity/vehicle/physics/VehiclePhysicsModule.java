package com.flansmod.common.entity.vehicle.physics;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.ControlLogic;
import com.flansmod.common.entity.vehicle.controls.ControlLogics;
import com.flansmod.common.entity.vehicle.controls.VehicleInputState;
import com.flansmod.common.entity.vehicle.hierarchy.WheelEntity;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.common.types.vehicles.elements.VehiclePhysicsDefinition;
import com.flansmod.common.types.vehicles.elements.WheelDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.codehaus.plexus.util.CachedMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class VehiclePhysicsModule implements IVehicleModule
{
	@Nonnull
	public final VehiclePhysicsDefinition Def;

	// Part lookups
	private final MultiLookup<EControlLogicHint, WheelEntity> Wheels = new MultiLookup<>();
	private final MultiLookup<EControlLogicHint, VehiclePropellerSaveState> Propellers = new MultiLookup<>();



	private final Map<ResourceLocation, ControlLogic> Controllers = new HashMap<>();
	@Nonnull
	private ResourceLocation SelectedControllerLocation = new ResourceLocation(FlansMod.MODID, "control_schemes/null");




	public VehiclePhysicsModule(@Nonnull VehiclePhysicsDefinition def)
	{
		Def = def;
	}

	public void CreateSubEntities(@Nonnull VehicleEntity vehicle)
	{
		for(WheelDefinition wheelDef : Def.wheels)
		{
			WheelEntity wheel = new WheelEntity(vehicle, wheelDef);
			Wheels.Add(wheel, wheelDef.attachedTo, wheelDef.controlHints);
		}
	}
	private void RegisterWheel(@Nonnull WheelEntity wheel)
	{
		Wheels.Add(wheel, wheel.Def.attachedTo, wheel.Def.controlHints);
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

		ControlLogic controller = CurrentController(vehicle);
		if(controller != null)
		{
			VehicleInputState inputs = vehicle.GetInputStateFor(controller);
			if(vehicle.IsAuthority())
			{
				controller.TickAuthoritative(vehicle, inputs);
			}
			else
			{
				controller.TickRemote(vehicle, inputs);
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

		Wheels.ForEach(wheel -> wheel.ParentTick(vehicle));
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
