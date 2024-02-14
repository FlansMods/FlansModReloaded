package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.StatCalculationContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.gunshots.FloatModifier;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectFeedEntity implements IAbilityEffect
{
	private final ModifierDefinition[] BaseFeedAmounts;
	private final ModifierDefinition[] BaseFeedSaturations;

	public AbilityEffectFeedEntity(@Nonnull AbilityEffectDefinition def)
	{
		BaseFeedAmounts = def.MatchModifiers(ModifierDefinition.STAT_FEED_AMOUNT);
		BaseFeedSaturations = def.MatchModifiers(ModifierDefinition.STAT_FEED_SATURATION);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		targets.ForEachEntity((entity) ->
		{
			if (entity instanceof Player player)
			{
				player.getFoodData().eat(FeedAmount(gun, stacks), FeedSaturation(gun, stacks));
			}
		});
	}
	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}

	public int FeedAmount(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		FloatModifier baseDamage = FloatModifier.of(StatCalculationContext.of(gun, stacks), BaseFeedAmounts);
		FloatModifier gunModifier = gun.GetFloatModifier(ModifierDefinition.STAT_FEED_AMOUNT);
		return Maths.Ceil(FloatModifier.of(baseDamage, gunModifier).GetValue());
	}
	public float FeedSaturation(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		FloatModifier baseDamage = FloatModifier.of(StatCalculationContext.of(gun, stacks), BaseFeedSaturations);
		FloatModifier gunModifier = gun.GetFloatModifier(ModifierDefinition.STAT_FEED_SATURATION);
		return FloatModifier.of(baseDamage, gunModifier).GetValue();
	}
}
