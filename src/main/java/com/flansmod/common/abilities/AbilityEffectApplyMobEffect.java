package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectApplyMobEffect implements IAbilityEffect
{
	public final float BaseMultiplier;
	public final float BaseDurationSeconds;
	@Nullable
	public final MobEffect Effect;

	public AbilityEffectApplyMobEffect(@Nonnull AbilityEffectDefinition def)
	{
		Effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(def.ModifyString(ModifierDefinition.KEY_MOB_EFFECT_ID, "")));
		BaseMultiplier = def.ModifyFloat(ModifierDefinition.STAT_POTION_MULTIPLIER, 1.0f);
		BaseDurationSeconds = def.ModifyFloat(ModifierDefinition.STAT_POTION_DURATION, 1.0f);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		if(Effect != null)
		{
			targets.ForEachEntity((entity) -> {
				if (entity instanceof LivingEntity living)
				{
					living.addEffect(
						new MobEffectInstance(Effect, DurationTicks(gun, stacks), PotionMultiplier(gun, stacks) - 1),
						gun.GetShooter().Owner());
				}
			});
		}
	}

	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}

	private int PotionMultiplier(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		float intensity = gun.ModifyFloat(ModifierDefinition.STAT_POTION_DURATION, BaseMultiplier);
		if(stacks != null)
		{
			intensity *= stacks.GetIntensity();
		}
		return Maths.Ceil(intensity);
	}
	private int DurationTicks(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		float duration = gun.ModifyFloat(ModifierDefinition.STAT_POTION_DURATION, BaseDurationSeconds);
		if(stacks != null)
		{
			duration *= stacks.GetDurationSeconds();
		}
		return Maths.Ceil(duration);
	}

}
