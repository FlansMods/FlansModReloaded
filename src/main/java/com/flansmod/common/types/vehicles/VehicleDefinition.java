package com.flansmod.common.types.vehicles;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.vehicles.elements.ArticulatedPartDefinition;
import com.flansmod.common.types.vehicles.elements.MountedGunDefinition;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;
import net.minecraft.resources.ResourceLocation;

public class VehicleDefinition extends JsonDefinition
{
	public static final VehicleDefinition INVALID = new VehicleDefinition(new ResourceLocation(FlansMod.MODID, "vehicles/null"));
	public static final String TYPE = "vehicle";
	@Override
	public String GetTypeName() { return TYPE; }

	public VehicleDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public SeatDefinition[] seats = new SeatDefinition[0];
	@JsonField
	public ArticulatedPartDefinition[] articulatedParts = new ArticulatedPartDefinition[0];
	@JsonField
	public MountedGunDefinition[] guns = new MountedGunDefinition[0];
}
