package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.elements.EPlayerInput;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.EVehicleAxis;
import com.flansmod.common.types.vehicles.elements.ControlSchemeAxisDefinition;
import com.flansmod.common.types.vehicles.elements.EAxisBehaviourType;
import com.flansmod.physics.common.util.Maths;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class VehicleInputState
{
	public Map<EVehicleAxis, Float> AxisInputs = new HashMap<>();
	public Map<EPlayerInput, Boolean> AdditionalInputs = new HashMap<>();
	public Map<EVehicleAxis, Float> AxisValues = new HashMap<>();


	public void SetInput(@Nonnull EPlayerInput input, boolean pressed) { AdditionalInputs.put(input, pressed); }
	public void SetInput(@Nonnull EVehicleAxis control, float value)
	{
		if(control != EVehicleAxis.Unset)
			AxisInputs.put(control, value);
		else
			FlansMod.LOGGER.warn("Passing Unset control input to vehicle input state?");
	}
	public boolean GetInput(@Nonnull EPlayerInput input) { return AdditionalInputs.getOrDefault(input, false); }
	public float GetInput(@Nonnull EVehicleAxis axisType) { return AxisInputs.getOrDefault(axisType, 0.0f); }



	public void SetValue(@Nonnull EVehicleAxis control, float value)
	{
		AxisValues.put(control, value);
	}
	public float GetValue(@Nonnull EVehicleAxis control)
	{
		return AxisValues.getOrDefault(control, 0.0f);
	}


	public void Tick(@Nonnull ControlSchemeDefinition def)
	{
		for(ControlSchemeAxisDefinition axisDef : def.axes)
			TickAxis(axisDef);
	}
	public float TickAxis(@Nonnull ControlSchemeAxisDefinition axisDef)
	{
		float axisInput = GetInput(axisDef.axisType);
		float axisValue = GetValue(axisDef.axisType);

		// For resting sliders, we need to move back to rest if the input is not pressed
		if(Maths.Approx(axisInput, 0f) && axisDef.axisBehaviour == EAxisBehaviourType.SliderWithRestPosition)
		{
			axisInput = Maths.Sign(axisDef.restingPosition - axisValue);
		}

		// Apply the change
		float newValue = axisValue + axisInput;

		// If we are notched, we want to only go as far as the next notch
		if(axisDef.axisBehaviour == EAxisBehaviourType.Notched)
		{
			for(float notch : axisDef.notches)
			{
				if (newValue > notch && axisValue < notch)
					newValue = notch;
				if(newValue < notch && axisValue > notch)
					newValue = notch;
			}
		}

		newValue = Maths.Clamp(newValue, axisDef.minValue, axisDef.maxValue);
		SetValue(axisDef.axisType, newValue);
		return newValue;
	}
}
