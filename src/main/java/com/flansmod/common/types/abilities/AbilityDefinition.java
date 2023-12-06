package com.flansmod.common.types.abilities;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.EAbilityEffect;
import com.flansmod.common.types.abilities.elements.EAbilityTarget;
import com.flansmod.common.types.abilities.elements.EAbilityTrigger;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import net.minecraft.resources.ResourceLocation;

public class AbilityDefinition extends JsonDefinition
{
	public static final AbilityDefinition INVALID = new AbilityDefinition(new ResourceLocation(FlansMod.MODID, "abilities/null"));
	public static final String TYPE = "ability";
	public static final String FOLDER = "abilities";
	@Override
	public String GetTypeName() { return TYPE; }

	public AbilityDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public int maxLevel = 5;

	// --
	@JsonField
	public EAbilityTrigger startTrigger = EAbilityTrigger.Instant;
	@JsonField
	public EAbilityTrigger endTrigger = EAbilityTrigger.Instant;
	@JsonField
	public String[] triggerConditions = new String[0];

	// -- Targeting --
	@JsonField
	public EAbilityTarget targetType = EAbilityTarget.Shooter;

	// -- Effect --
	@JsonField
	public EAbilityEffect effectType = EAbilityEffect.Nothing;
	@JsonField
	public String[] effectParameters = new String[0];
	@JsonField(Docs = "The modifiers to add when the effect is active")
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];

	public float GetAmount(int level)
	{
		level = Maths.Clamp(level, 1, maxLevel);
		return baseAmount + (level - 1) * extraAmountPerLevel;
	}
	@JsonField
	public float baseAmount = 1.0f;
	@JsonField
	public float extraAmountPerLevel = 1.0f;

	public float GetDuration(int level)
	{
		level = Maths.Clamp(level, 1, maxLevel);
		return baseDuration + (level - 1) * extraDurationPerLevel;
	}
	@JsonField
	public float baseDuration = 1.0f;
	@JsonField
	public float extraDurationPerLevel = 1.0f;

	@JsonField
	public boolean stackAmount = false;
	@JsonField
	public float maxAmount = 1.0f;
	@JsonField
	public boolean stackDuration = false;
	@JsonField
	public float maxDuration = 1.0f;


}
