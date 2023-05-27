package com.flansmod.common.types.teams;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ColourDefinition;
import com.flansmod.common.types.elements.ItemStackDefinition;
import net.minecraft.resources.ResourceLocation;

public class TeamDefinition extends JsonDefinition
{
	public static final TeamDefinition INVALID = new TeamDefinition(new ResourceLocation(FlansMod.MODID, "teams/null"));
	public static final String TYPE = "team";
	@Override
	public String GetTypeName() { return TYPE; }

	public TeamDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public String[] classes = new String[0];

	@JsonField
	public ColourDefinition flagColour = new ColourDefinition();
	@JsonField
	public char textColour = 'f';

	@JsonField
	public ItemStackDefinition hat = new ItemStackDefinition();
	@JsonField
	public ItemStackDefinition chest = new ItemStackDefinition();
	@JsonField
	public ItemStackDefinition legs = new ItemStackDefinition();
	@JsonField
	public ItemStackDefinition shoes = new ItemStackDefinition();
}
