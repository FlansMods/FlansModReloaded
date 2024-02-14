package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.StatCalculationContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.gunshots.FloatModifier;
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
	private final ModifierDefinition[] BaseMultipliers;
	@Nullable
	public final MobEffect Effect;

	public AbilityEffectApplyMobEffect(@Nonnull AbilityEffectDefinition def)
	{
		Effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(def.ModifyString(ModifierDefinition.KEY_MOB_EFFECT_ID, "")));
		BaseMultipliers = def.MatchModifiers(ModifierDefinition.STAT_POTION_MULTIPLIER);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		if(Effect != null)
		{
			targets.ForEachEntity((entity) -> {
				if (entity instanceof LivingEntity living)
				{
					int decayTicks = stacks == null ? 20 : stacks.GetDecayTimeTicks(gun);
					living.addEffect(
						new MobEffectInstance(Effect, decayTicks, PotionMultiplier(gun, stacks) - 1),
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
		FloatModifier baseMultiplier = FloatModifier.of(StatCalculationContext.of(gun, stacks), BaseMultipliers);
		FloatModifier gunMultiplier = gun.GetFloatModifier(ModifierDefinition.STAT_POTION_MULTIPLIER);
		return Maths.Ceil(FloatModifier.of(baseMultiplier, gunMultiplier).GetValue());
	}
}
