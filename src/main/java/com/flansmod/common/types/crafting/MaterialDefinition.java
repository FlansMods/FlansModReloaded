package com.flansmod.common.types.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;

public class MaterialDefinition extends JsonDefinition
{
	public static final MaterialDefinition INVALID = new MaterialDefinition(new ResourceLocation(FlansMod.MODID, "materials/null"));
	public static final String TYPE = "material";
	public static final String FOLDER = "materials";
	@Override
	public String GetTypeName() { return TYPE; }

	public MaterialDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public String[] matchItems = new String[0];
	@JsonField
	public String[] matchTags = new String[0];
	@JsonField
	public int craftingTier = 1;
	@JsonField
	public EMaterialType materialType = EMaterialType.Misc;
}
