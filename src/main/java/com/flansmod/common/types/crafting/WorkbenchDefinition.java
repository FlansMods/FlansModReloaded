package com.flansmod.common.types.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.elements.*;
import com.flansmod.common.types.parts.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class WorkbenchDefinition extends JsonDefinition
{
	public static final WorkbenchDefinition INVALID = new WorkbenchDefinition(new ResourceLocation(FlansMod.MODID, "workbenches/null"));
	public static final String TYPE = "workbench";
	public static final String FOLDER = "workbenches";
	@Override
	public String GetTypeName() { return TYPE; }

	public WorkbenchDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}


	@JsonField
	public String titleString = "workbench";
	@JsonField
	public String bannerTextureLocation = "";

	@JsonField
	public GunCraftingDefinition gunCrafting = new GunCraftingDefinition();
	@JsonField
	public GunModifyingDefinition gunModifying = new GunModifyingDefinition();
	@JsonField
	public EnergyBlockDefinition energy = new EnergyBlockDefinition();
	@JsonField
	public ArmourCraftingDefinition armourCrafting = new ArmourCraftingDefinition();
	@JsonField
	public ItemHoldingDefinition itemHolding = new ItemHoldingDefinition();

	@JsonField
	public WorkbenchSideDefinition[] sides = new WorkbenchSideDefinition[0];
}
