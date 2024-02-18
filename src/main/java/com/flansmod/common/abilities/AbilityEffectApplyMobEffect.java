package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.stats.StatCalculationContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;
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
		Effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(def.ModifyString(ModifierDefinition.KEY_MOB_EFFECT_ID, "")));
		PotionMultiplier = new StatHolder(ModifierDefinition.STAT_POTION_MULTIPLIER, def);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		if(Effect != null)
		{
			targets.ForEachEntity((entity) -> {
				if (entity instanceof LivingEntity living)
				{
					int decayTicks = stacks == null ? 20 : stacks.GetDecayTimeTicks(gun);
					living.addEffect(
						new MobEffectInstance(Effect, decayTicks, Maths.Ceil(PotionMultiplier.Get(gun, stacks)) - 1),
						gun.GetShooter().Owner());
				}
			});
		}
	}

	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}
}
