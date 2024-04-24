package com.flansmod.common.types.vehicles;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.vehicles.elements.EControlLogicType;
import com.flansmod.common.types.vehicles.elements.WheelDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

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




}
