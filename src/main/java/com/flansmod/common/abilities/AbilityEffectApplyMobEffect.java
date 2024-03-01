package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
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
	@Nullable
	public final MobEffect Effect;

	@Nonnull
	private final StatHolder PotionMultiplier;

	public AbilityEffectApplyMobEffect(@Nonnull AbilityEffectDefinition def)
	{
		Effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(def.ModifyString(Constants.KEY_MOB_EFFECT_ID, "")));
		PotionMultiplier = new StatHolder(Constants.STAT_POTION_MULTIPLIER, def);
	}

	@Override
	public void TriggerServer(@Nonnull ActionGroupContext actionGroup, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		if(Effect != null)
		{
			targets.ForEachEntity((entity) -> {
				if (entity instanceof LivingEntity living)
				{
					int decayTicks = stacks == null ? 20 : stacks.GetDecayTimeTicks(actionGroup.Gun);
					living.addEffect(
						new MobEffectInstance(Effect, decayTicks, Maths.Ceil(PotionMultiplier.Get(actionGroup, stacks)) - 1),
						actionGroup.Gun.GetShooter().Owner());
				}
			});
		}
	}
}
