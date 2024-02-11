package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.abilities.AbilityStack;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.JsonField;
import com.flansmod.util.Maths;

import javax.annotation.Nonnull;

public class AbilityStackingDefinition
{
	public boolean IsStackable()
	{
		return !stackingKey.isEmpty() && maxStacks > 1 && (stackIntensity || stackDuration);
	}

	@JsonField
	public String stackingKey = "";
	@JsonField
	public int maxStacks = 4;
	@JsonField
	public float decayTime = 1.0f;
	@JsonField
	public boolean decayAllAtOnce = false;

	@JsonField
	public boolean stackIntensity = false;
	@JsonField
	public float baseIntensity = 1.0f;
	@JsonField
	public float addIntensityPerStack = 1.0f;

	@JsonField
	public boolean stackDuration = false;
	@JsonField
	public float baseDuration = 1.0f;
	@JsonField
	public float addDurationPerStack = 1.0f;

	public float GetDecayTimeSeconds()
	{
		return decayTime;
	}
	public float GetDecayTimeTicks()
	{
		return Maths.Ceil(decayTime * 20.0f);
	}
	public float GetIntensity(int numStacks)
	{
		if(stackIntensity)
			return baseIntensity + (addIntensityPerStack * numStacks);
		return baseIntensity;
	}
	public int GetDurationTicks(int numStacks)
	{
		return Maths.Ceil(GetDurationSeconds(numStacks) * 20f);
	}
	public float GetDurationSeconds(int numStacks)
	{
		if(stackDuration)
			return baseDuration + (addDurationPerStack * numStacks);
		return baseDuration;
	}



	// TODO: Move - Levelling only applies to crafted traits
	@JsonField
	public float addIntensityPerLevel = 1.0f;
	@JsonField
	public float extraDurationPerLevel = 1.0f;


	// TODO: These go elsewhere - not based on the stack
	@JsonField(Docs = "This is applied separately, multiplying intensity by (mulIntensityPerAttachment * numAttachments). So 0.33 would give 2x (1+0.33*3) intensity with 3 attachments")
	public float mulIntensityPerAttachment = 0.0f;
	@JsonField(Docs = "This is applied separately, multiplying intensity by 1 + (mulIntensityForFullMag * #num-bullets/#mag-size). So 0.5 would give 1.5x intensity with a full mag, or 1.25x with half full")
	public float mulIntensityForFullMag = 0.0f;
	@JsonField(Docs = "If true, you are instead passing in 'mag emptiness'")
	public boolean invertMagFullness = false;



	public float CalculateIntensity(int level, @Nonnull GunContext gunContext, @Nonnull AbilityStack stacks)
	{
		float intensity = CalculateBaseIntensity(level);
		intensity = ApplyAttachmentMulti(intensity, gunContext);
		intensity = ApplyFullnessMulti(intensity, gunContext);
		return intensity;
	}
	public float CalculateDuration(int level, @Nonnull GunContext gunContext)
	{
		return baseDuration + (level - 1) * extraDurationPerLevel;
	}





	public float CalculateBaseIntensity(int level)
	{
		return baseIntensity + (level - 1) * addIntensityPerLevel;
	}
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
	// Helper function, won't look up mags in NBT if its not relevant to this ability
	public float ApplyAttachmentMulti(float base, @Nonnull GunContext gunContext)
	{
		if(mulIntensityPerAttachment > 0.0f)
		{
			return base * (mulIntensityPerAttachment * gunContext.GetNumAttachments());
		}
		return base;
	}

}
