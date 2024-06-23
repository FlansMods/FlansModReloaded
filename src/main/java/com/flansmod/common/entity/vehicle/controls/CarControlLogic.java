package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.hierarchy.WheelEntity;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.EVehicleAxis;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.ControlSchemeAxisDefinition;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;
import com.flansmod.common.types.vehicles.elements.WheelDefinition;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class CarControlLogic extends ControlLogic
{
	@Nonnull
	private final ControlSchemeAxisDefinition AcceleratorAxis;
	@Nonnull
	private final ControlSchemeAxisDefinition SteerLeftRightAxis;

	public CarControlLogic(@Nonnull ControlSchemeDefinition def)
	{
		super(def);

		AcceleratorAxis = FindAxis(EVehicleAxis.Accelerator);
		SteerLeftRightAxis = FindAxis(EVehicleAxis.Yaw);
	}

	@Override
	public boolean CanControl(@Nonnull VehicleDefinition vehicleDef)
	{
		List<WheelDefinition> allWheels = vehicleDef.AsHierarchy().AllWheels();
		int numWheels = allWheels.size();
		if(numWheels < 3)
			return false;

		boolean hasSteering = false;
		boolean hasFront = false;
		boolean hasRear = false;
		boolean hasDrive = false;
		for(WheelDefinition wheelDef : allWheels)
		{
			if(wheelDef.IsHintedAs(EControlLogicHint.Steering))
				hasSteering = true;
			if(wheelDef.IsHintedAs(EControlLogicHint.Front))
				hasFront = true;
			if(wheelDef.IsHintedAs(EControlLogicHint.Rear))
				hasRear = true;
			if(wheelDef.IsHintedAs(EControlLogicHint.Drive))
				hasDrive = true;
		}

		return hasSteering && hasFront && hasRear && hasDrive;
	}

	@Override
	public void TickAuthoritative(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs, @Nonnull ForceModel forces)
	{
		float accelerationInput = inputs.TickAxis(AcceleratorAxis);
		float steeringInput = inputs.TickAxis(SteerLeftRightAxis);

		Collection<WheelEntity> steeringAtFront = vehicle.Wheels.ByHints(EControlLogicHint.Front, EControlLogicHint.Steering);
		Collection<WheelEntity> steeringAtBack = vehicle.Wheels.ByHints(EControlLogicHint.Rear, EControlLogicHint.Steering);
		Collection<WheelEntity> driveWheels = vehicle.Wheels.ByHint(EControlLogicHint.Drive);

		for(WheelEntity wheel : steeringAtFront)
			wheel.SetYawParameter(steeringInput);
		for(WheelEntity wheel : steeringAtBack)
			wheel.SetYawParameter(-steeringInput);

		for(WheelEntity driveWheel : driveWheels)
			driveWheel.SetTorqueParameter(accelerationInput);

	}
	@Override
	public void TickRemote(@Nonnull VehicleEntity vehicle, @Nonnull VehicleInputState inputs, @Nonnull ForceModel forces)
	{

	}
}
