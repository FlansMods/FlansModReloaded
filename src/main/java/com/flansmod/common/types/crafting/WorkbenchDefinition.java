package com.flansmod.common.types.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.elements.*;
import com.flansmod.common.types.elements.ItemDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

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
	@JsonField(AssetPathHint = "textures/gui/")
	public ResourceLocation bannerTextureLocation = InvalidLocation;

	@JsonField
	public GunCraftingDefinition gunCrafting = new GunCraftingDefinition();
	@JsonField
	public PartCraftingDefinition partCrafting = new PartCraftingDefinition();
	@JsonField
	public GunModifyingDefinition gunModifying = new GunModifyingDefinition();
	@JsonField
	public EnergyBlockDefinition energy = new EnergyBlockDefinition();
	@JsonField
	public ArmourCraftingDefinition armourCrafting = new ArmourCraftingDefinition();
	@JsonField
	public ItemHoldingDefinition itemHolding = new ItemHoldingDefinition();

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();

	@JsonField
	public WorkbenchSideDefinition[] sides = new WorkbenchSideDefinition[0];

	@Nullable
	public WorkbenchSideDefinition GetSideDef(Direction direction)
	{
		for(WorkbenchSideDefinition side : sides)
			if(side.side == direction)
				return side;
		return null;
	}
}
