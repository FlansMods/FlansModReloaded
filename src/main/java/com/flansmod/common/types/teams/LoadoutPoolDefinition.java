package com.flansmod.common.types.teams;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.common.types.teams.elements.LevelUpDefinition;
import com.flansmod.common.types.teams.elements.LoadoutDefinition;
import com.flansmod.common.types.teams.elements.LoadoutEntryDefinition;
import com.flansmod.common.types.teams.elements.PaintjobUnlockDefinition;
import net.minecraft.resources.ResourceLocation;

public class LoadoutPoolDefinition extends JsonDefinition
{
	public static final LoadoutPoolDefinition INVALID = new LoadoutPoolDefinition(new ResourceLocation(FlansMod.MODID, "loadout_pools/null"));
	public static final String TYPE = "loadout_pool";
	@Override
	public String GetTypeName() { return TYPE; }

	public LoadoutPoolDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public int maxLevel = 20;
	@JsonField
	public int xpForKill = 10;
	@JsonField
	public int xpForDeath = 5;
	@JsonField
	public int xpForKillstreakBonus = 10;
	@JsonField
	public int xpForAssist = 5;
	@JsonField
	public int xpForMultikill = 10;

	@JsonField
	public LoadoutDefinition[] defaultLoadouts = new LoadoutDefinition[0];
	@JsonField
	public String[] availableRewardBoxes = new String[0];
	@JsonField(Docs = "Level 0 will be unlocked automatically. Put starter gear in there.")
	public LevelUpDefinition[] levelUps = new LevelUpDefinition[0];


}

