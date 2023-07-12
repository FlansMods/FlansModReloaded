package com.flansmod.common.types.parts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemDefinition;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import net.minecraft.resources.ResourceLocation;

public class PartDefinition extends JsonDefinition
{
	public static final PartDefinition INVALID = new PartDefinition(new ResourceLocation(FlansMod.MODID, "parts/null"));
	public static final String TYPE = "part";
	public static final String FOLDER = "parts";
	@Override
	public String GetTypeName() { return TYPE; }

	public PartDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public boolean canPlaceInMachiningTable = false;
	@JsonField
	public boolean canPlaceInModificationTable = false;

	@JsonField
	public String[] compatiblityTags = new String[] { "mecha", "groundVehicle", "plane" };

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();

	// Engine properties
	@JsonField
	public EngineDefinition engine = new EngineDefinition();
}
