package com.flansmod.common.types.abilities;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.*;
import com.flansmod.common.types.guns.elements.AbilityDefinition;
import com.flansmod.util.Maths;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class CraftingTraitDefinition extends JsonDefinition
{
	public static final CraftingTraitDefinition INVALID = new CraftingTraitDefinition(new ResourceLocation(FlansMod.MODID, "traits/null"));
	public static final String TYPE = "trait";
	public static final String FOLDER = "traits";
	@Override
	public String GetTypeName() { return TYPE; }

	public CraftingTraitDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public int maxLevel = 5;

	// --
	@JsonField
	public AbilityDefinition[] abilities = new AbilityDefinition[0];
}
