package com.flansmod.common.types.abilities;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.EAbilityEffect;
import com.flansmod.common.types.abilities.elements.EAbilityTarget;
import com.flansmod.common.types.abilities.elements.EAbilityTrigger;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.guns.elements.ActionGroupDefinition;
import com.flansmod.util.Maths;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

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

	public float CalculateIntensity(int level, @Nonnull GunContext gunContext)
	{
		float intensity = CalculateBaseIntensity(level);
		intensity = ApplyAttachmentMulti(intensity, gunContext);
		intensity = ApplyFullnessMulti(intensity, gunContext);
		return intensity;
	}

	public float CalculateBaseIntensity(int level)
	{
		level = Maths.Clamp(level, 1, maxLevel);
		return baseIntensity + (level - 1) * addIntensityPerLevel;
	}

	@JsonField
	public float baseIntensity = 1.0f;
	@JsonField
	public float addIntensityPerLevel = 1.0f;

	// Helper function, won't look up mags in NBT if its not relevant to this ability
	public float ApplyAttachmentMulti(float base, @Nonnull GunContext gunContext)
	{
		if(mulIntensityPerAttachment > 0.0f)
		{
			return base * (mulIntensityPerAttachment * gunContext.GetNumAttachments());
		}
		return base;
	}

	@JsonField(Docs = "This is applied separately, multiplying intensity by (mulIntensityPerAttachment * numAttachments). So 0.33 would give 2x (1+0.33*3) intensity with 3 attachments")
	public float mulIntensityPerAttachment = 0.0f;

	// Helper function, won't look up mags in NBT if its not relevant to this ability
	public float ApplyFullnessMulti(float base, @Nonnull GunContext gunContext)
	{
		if(mulIntensityForFullMag > 0.0f)
		{
			// TODO: Look up different paths
			ActionGroupContext groupContext = gunContext.GetActionGroupContext(Actions.DefaultPrimaryActionKey);
			if(groupContext.IsValid())
			{
				int magSize = groupContext.GetMagazineSize(0);
				int bulletCount = groupContext.GetNumBulletsInMag(0);

				float ratio = bulletCount / (float)magSize;
				if(invertMagFullness)
					ratio = 1.0f - ratio;

				return base * (1.0f + (mulIntensityForFullMag * ratio));
			}
		}
		return base;
	}

	@JsonField(Docs = "This is applied separately, multiplying intensity by 1 + (mulIntensityForFullMag * #num-bullets/#mag-size). So 0.5 would give 1.5x intensity with a full mag, or 1.25x with half full")
	public float mulIntensityForFullMag = 0.0f;
	@JsonField(Docs = "If true, you are instead passing in 'mag emptiness'")
	public boolean invertMagFullness = false;

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
