package com.flansmod.common.types.tools;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ItemDefinition;
import net.minecraft.resources.ResourceLocation;

public class ToolDefinition extends JsonDefinition
{
	public static final ToolDefinition INVALID = new ToolDefinition(new ResourceLocation(FlansMod.MODID, "tools/null"));
	public static final String TYPE = "tool";
	@Override
	public String GetTypeName() { return TYPE; }

	public ToolDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();

	@JsonField
	public ActionDefinition[] primaryActions = new ActionDefinition[0];
	@JsonField
	public ActionDefinition[] secondaryActions = new ActionDefinition[0];

	@JsonField
	public boolean hasDurability = false;
	@JsonField
	public int maxDurability = 0;
	@JsonField
	public boolean destroyWhenBroken = false;

	@JsonField
	public boolean usesPower = false;
	@JsonField
	public int internalFEStorage = 0;
	@JsonField
	public int primaryFEUsage = 0;
	@JsonField
	public int secondaryFEUsage = 0;
	@JsonField
	public float spendFEOnFailRatio = 0.0f;

	@JsonField
	public int foodValue = 0;


}
