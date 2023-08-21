package com.flansmod.common.types.teams;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.teams.elements.EArmourSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public class ArmourDefinition extends JsonDefinition
{
	public static final ArmourDefinition INVALID = new ArmourDefinition(new ResourceLocation(FlansMod.MODID, "armours/null"));
	public static final String TYPE = "armour";
	@Override
	public String GetTypeName() { return TYPE; }

	public ArmourDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();

	@JsonField
	public EArmourSlot slot = EArmourSlot.HEAD;
	@JsonField
	public int maxDurability = 128;
	@JsonField
	public int toughness = 1;
	@JsonField
	public int enchantability = 0;
	@JsonField
	public int damageReduction = 0;
	@JsonField
	public String armourTextureName = "";
	@JsonField
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];
	@JsonField
	public boolean nightVision = false;
	@JsonField
	public String screenOverlay = "";
	@JsonField
	public String[] immunities = new String[0];
}
