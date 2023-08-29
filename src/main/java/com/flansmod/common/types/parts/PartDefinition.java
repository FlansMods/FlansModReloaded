package com.flansmod.common.types.parts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.crafting.EMaterialType;
import com.flansmod.common.types.crafting.elements.IngredientDefinition;
import com.flansmod.common.types.crafting.elements.RecipePartDefinition;
import com.flansmod.common.types.crafting.elements.TieredIngredientDefinition;
import com.flansmod.common.types.elements.ItemDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
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
	public IngredientDefinition[] recipeIngredients = new IngredientDefinition[0];

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();
	@JsonField
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];

	// Engine properties
	@JsonField
	public EngineDefinition engine = new EngineDefinition();

	@JsonField
	public int materialTier = 0;
	@JsonField
	public EMaterialType materialType = EMaterialType.Misc;

}
