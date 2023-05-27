package com.flansmod.common.types.teams;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.common.types.teams.elements.PaintjobUnlockDefinition;
import net.minecraft.resources.ResourceLocation;

public class RewardBoxDefinition extends JsonDefinition
{
	public static final RewardBoxDefinition INVALID = new RewardBoxDefinition(new ResourceLocation(FlansMod.MODID, "reward_boxes/null"));
	public static final String TYPE = "reward_box";

	@Override
	public String GetTypeName()
	{
		return TYPE;
	}

	public RewardBoxDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public PaintjobUnlockDefinition[] paintjobs = new PaintjobUnlockDefinition[0];
	@JsonField
	public float legendaryChance = 0.05f;
	@JsonField
	public float rareChance = 0.10f;
	@JsonField
	public float uncommonChance = 0.35f;
	@JsonField
	public float commonChance = 0.50f;
}
