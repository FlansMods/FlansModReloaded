package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.vehicles.EDrivingControl;
import com.flansmod.common.types.vehicles.elements.ControlSchemeAxisDefinition;
import com.flansmod.common.types.vehicles.elements.EAxisBehaviourType;
import com.flansmod.util.Maths;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class VehicleInputState
{
	public Map<EDrivingControl, Float> AxisInputs = new HashMap<>();
	public Map<EDrivingControl, Float> AxisValues = new HashMap<>();

	public void SetValue(@Nonnull EDrivingControl control, float value)
	{
		AxisValues.put(control, value);
	}
	public float GetValue(@Nonnull EDrivingControl control)
	{
		return AxisValues.getOrDefault(control, 0.0f);
	}

	public void SetInput(@Nonnull EDrivingControl control, float value)
	{
		if(control != EDrivingControl.Unset)
			AxisInputs.put(control, value);
		else
			FlansMod.LOGGER.warn("Passing Unset control input to vehicle input state?");
	}
	public float GetInput(@Nonnull EDrivingControl control)
	{
		if(AxisInputs.containsKey(control))
			return AxisInputs.get(control);
		return 0.0f;
	}
	public float GetAxisValue(@Nonnull ControlSchemeAxisDefinition axisDef)
	{
		return GetInputPair(axisDef.positive, axisDef.negative);
	}
	public float GetInputPair(@Nonnull EDrivingControl positive, @Nonnull EDrivingControl negative)
	{
		float mag = 0.0f;
		if(AxisInputs.containsKey(positive))
			mag += AxisInputs.get(positive);
		if(AxisInputs.containsKey(negative))
			mag -= AxisInputs.get(negative);
		return mag;
	}

	public float TickAxis(@Nonnull ControlSchemeAxisDefinition axisDef)
	{
		float axisInput = GetInputPair(axisDef.positive, axisDef.negative);
		float axisValue = GetValue(axisDef.positive);

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
		SetValue(axisDef.positive, newValue);
		return newValue;
	}
}
