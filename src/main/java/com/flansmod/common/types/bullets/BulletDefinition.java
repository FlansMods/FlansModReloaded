package com.flansmod.common.types.bullets;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ItemDefinition;
import com.flansmod.common.types.guns.elements.ShotDefinition;
import net.minecraft.resources.ResourceLocation;

public class BulletDefinition extends JsonDefinition
{
	public static final BulletDefinition INVALID = new BulletDefinition(new ResourceLocation(FlansMod.MODID, "bullets/null"));
	public static final String TYPE = "bullet";
	public static final String FOLDER = "bullets";

	@Override
	public String GetTypeName() { return TYPE; }

	public boolean HasTag(String tag)
	{
		for (String s : itemSettings.tags)
			if (s.equals(tag))
				return true;
		return false;
	}

	public int GetItemDurability() { return roundsPerItem > 1 ? roundsPerItem : 0; }
	public int GetMaxStackSize() { return itemSettings.maxStackSize; }

	public BulletDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}


	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();
	@JsonField
	public int roundsPerItem = 1;
	@JsonField
	public ShotDefinition shootStats = new ShotDefinition();

	@JsonField
	public ActionDefinition[] onShootActions = new ActionDefinition[0];
	@JsonField
	public ActionDefinition[] onClipEmptyActions = new ActionDefinition[0];
	@JsonField
	public ActionDefinition[] onReloadActions = new ActionDefinition[0];

}
