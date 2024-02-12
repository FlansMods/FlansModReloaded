package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
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
	private final float BaseFeedAmount;
	private final float BaseFeedSaturation;

	public AbilityEffectFeedEntity(@Nonnull AbilityEffectDefinition def)
	{
		BaseFeedAmount = def.ModifyFloat(ModifierDefinition.STAT_FEED_AMOUNT, 1.0f);
		BaseFeedSaturation = def.ModifyFloat(ModifierDefinition.STAT_FEED_SATURATION, 1.0f);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		targets.ForEachEntity((entity) ->
		{
			if (entity instanceof Player player)
			{
				player.getFoodData().eat(FeedAmount(gun, stacks, tier), FeedSaturation(gun, stacks, tier));
			}
		});
	}
	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}

	public int FeedAmount(@Nonnull GunContext gun, @Nullable AbilityStack stacks, int tier)
	{
		float amount = gun.ModifyFloat(ModifierDefinition.STAT_FEED_AMOUNT, BaseFeedAmount);
		if(stacks != null)
			amount *= stacks.GetIntensity();
		return Maths.Ceil(amount);
	}
	public float FeedSaturation(@Nonnull GunContext gun, @Nullable AbilityStack stacks, int tier)
	{
		float amount = gun.ModifyFloat(ModifierDefinition.STAT_FEED_SATURATION, BaseFeedSaturation);
		if(stacks != null)
			amount *= stacks.GetIntensity();
		return amount;
	}
}
