package com.flansmod.common.types.armour;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.CraftingTraitProviderDefinition;
import com.flansmod.common.types.elements.ItemDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.guns.elements.AbilityDefinition;
import com.flansmod.common.types.guns.elements.HandlerDefinition;
import com.flansmod.common.types.guns.elements.ModeDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ArmourDefinition extends JsonDefinition
{
	public static final String TYPE = "armour";
	public static final String FOLDER = "armours";
	public static final ArmourDefinition INVALID = new ArmourDefinition(new ResourceLocation(FlansMod.MODID, "armour/null"));
	@Override
	public String GetTypeName() { return TYPE; }

	public ArmourDefinition(@Nonnull ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public boolean hasDurability = false;
	@JsonField
	public int maxDurability = 0;

	@JsonField
	public EArmourType armourType = EArmourType.Chest;
	@JsonField
	public int armourToughness = 0;
	@JsonField
	public int damageReduction = 0;

	@JsonField
	public boolean enchantable = false;
	@JsonField
	public int enchantability = 0;

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();
	@JsonField
	public PaintableDefinition paints = new PaintableDefinition();
	@JsonField(Docs = "If you want this armour to have mode toggles or other inputs, put them here")
	public HandlerDefinition[] inputHandlers = new HandlerDefinition[0];
	@JsonField(Docs = "These are triggered actions that fire when certain conditions are met")
	public AbilityDefinition[] staticAbilities = new AbilityDefinition[0];
	@JsonField
	public CraftingTraitProviderDefinition[] traitProviders = new CraftingTraitProviderDefinition[0];
	@JsonField
	public ModeDefinition[] modes = new ModeDefinition[0];
	@JsonField
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];
	@JsonField
	public String[] immunities = new String[0];
	@JsonField
	public String animationSet = "";
	@JsonField
	public String armourTextureName = "";

	@JsonField
	public boolean nightVision = false;
	@JsonField
	public String screenOverlay = "";
}
