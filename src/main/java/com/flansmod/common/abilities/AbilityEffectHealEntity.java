package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.StatCalculationContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.gunshots.FloatModifier;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectHealEntity implements IAbilityEffect
{
	private final ModifierDefinition[] BaseHealAmounts;

	public AbilityEffectHealEntity(@Nonnull AbilityEffectDefinition def)
	{
		BaseHealAmounts = def.MatchModifiers(ModifierDefinition.STAT_HEAL_AMOUNT);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		targets.ForEachEntity((entity) ->
		{
			if(entity instanceof LivingEntity living)
			{
				living.heal(HealAmount(gun, stacks, tier));
			}
		});
	}
	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}

	public float HealAmount(@Nonnull GunContext gun, @Nullable AbilityStack stacks, int tier)
	{
		FloatModifier baseDamage = FloatModifier.of(StatCalculationContext.of(gun, stacks), BaseHealAmounts);
		FloatModifier gunModifier = gun.GetFloatModifier(ModifierDefinition.STAT_HEAL_AMOUNT);
		return FloatModifier.of(baseDamage, gunModifier).GetValue();
	}
}
