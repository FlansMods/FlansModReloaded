package com.flansmod.common.types.vehicles;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.vehicles.elements.ControlSchemeAxisDefinition;
import com.flansmod.common.types.vehicles.elements.EControlLogicType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ControlSchemeDefinition extends JsonDefinition
{
	public static final ControlSchemeDefinition INVALID = new ControlSchemeDefinition(new ResourceLocation(FlansMod.MODID, "control_schemes/null"));
	public static final String TYPE = "control_scheme";
	public static final String FOLDER = "control_schemes";
	@Override
	@Nonnull
	public String GetTypeName() { return TYPE; }

	public ControlSchemeDefinition(@Nonnull ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public EControlLogicType logicType = EControlLogicType.Car;

	@JsonField
	public ControlSchemeAxisDefinition[] axes = new ControlSchemeAxisDefinition[0];


	@Nullable
	public ControlSchemeAxisDefinition FindMatchingAxis(@Nonnull EVehicleAxis axisType)
	{
		for(ControlSchemeAxisDefinition axis : axes)
			if(axis.axisType == axisType)
				return axis;
		return null;
	}

}
