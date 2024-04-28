package com.flansmod.common.entity.vehicle.physics;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.IVehicleModule;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.controls.ControlLogic;
import com.flansmod.common.entity.vehicle.controls.ControlLogics;
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
	private final Map<String, List<WheelEntity>> WheelsByPartName = new HashMap<>();
	private final List<WheelEntity> WheelsByIndex = new ArrayList<>();
	private final Map<EControlLogicHint, List<WheelEntity>> WheelsByHint = new HashMap<>();

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


			// Add lookup by part name
			if(!WheelsByPartName.containsKey(wheelDef.attachedTo))
				WheelsByPartName.put(wheelDef.attachedTo, new ArrayList<>());
			WheelsByPartName.get(wheelDef.attachedTo).add(wheel);

			// Add to raw indexed wheel list
			WheelsByIndex.add(wheel);

			// Add lookup by hint (can be multiple hints)
			for(EControlLogicHint hint : wheelDef.controlHints)
			{
				if (!WheelsByHint.containsKey(hint))
					WheelsByHint.put(hint, new ArrayList<>());
				WheelsByHint.get(hint).add(wheel);
			}
		}
	}
	private void RegisterWheel(@Nonnull WheelEntity wheel)
	{
		// Add lookup by part name
		if(!WheelsByPartName.containsKey(wheel.Def.attachedTo))
			WheelsByPartName.put(wheel.Def.attachedTo, new ArrayList<>());
		WheelsByPartName.get(wheel.Def.attachedTo).add(wheel);

		// Add to raw indexed wheel list
		WheelsByIndex.add(wheel);

		// Add lookup by hint (can be multiple hints)
		for(EControlLogicHint hint : wheel.Def.controlHints)
		{
			if (!WheelsByHint.containsKey(hint))
				WheelsByHint.put(hint, new ArrayList<>());
			WheelsByHint.get(hint).add(wheel);
		}
	}
	private void UnregisterWheelAt(int wheelIndex)
	{
		WheelEntity wheel = WheelsByIndex.get(wheelIndex);
		WheelsByIndex.remove(wheelIndex);
		for(List<WheelEntity> byPart : WheelsByPartName.values())
			byPart.remove(wheel);
		for(List<WheelEntity> byHint : WheelsByHint.values())
			byHint.remove(wheel);
	}


	@Nullable
	public ControlLogic CurrentController() { return Controllers.get(SelectedControllerLocation); }
	@Nullable
	public WheelEntity WheelByIndex(int index) { return WheelsByIndex.get(index); }
	@Nonnull
	public Collection<WheelEntity> WheelByVehiclePart(@Nonnull String partName) { return WheelsByPartName.getOrDefault(partName, List.of()); }
	@Nonnull
	public Collection<WheelEntity> WheelsThatMatch(@Nonnull EControlLogicHint hint) { return WheelsByHint.getOrDefault(hint, List.of()); }
	@Nonnull
	public Collection<WheelEntity> WheelsThatMatch(@Nonnull EControlLogicHint ... hints)
	{
		if(hints.length == 0)
			return List.of();
		Collection<WheelEntity> check0 = WheelsThatMatch(hints[0]);
		if(hints.length == 1)
			return check0;

		List<WheelEntity> validWheels = new ArrayList<>(check0.size());
		for(WheelEntity wheel : check0)
		{
			boolean valid = true;
			for (int i = 1; i < hints.length; i++)
			{
				if (!wheel.Def.IsHintedAs(hints[i]))
				{
					valid = false;
					break;
				}
			}
			if(valid)
				validWheels.add(wheel);
		}
		return validWheels;
	}


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
		for(int i = WheelsByIndex.size() - 1; i >= 0; i--)
		{
			WheelEntity wheel = WheelsByIndex.get(i);
			if(wheel == null || !wheel.isAlive())
			{
				FlansMod.LOGGER.warn(vehicle + ": Did not expect our wheel to die");
				UnregisterWheelAt(i);
			}
		}

		ControlLogic controller = CurrentController();
		if(controller != null)
		{

		}



		for(WheelEntity wheel : WheelsByIndex)
		{
			wheel.ParentTick(vehicle);
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
		return null;
	}
}
