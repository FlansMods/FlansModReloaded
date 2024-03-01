package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.util.Maths;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectFeedEntity implements IAbilityEffect
{
	@Nonnull
	private final StatHolder FeedAmount;
	@Nonnull
	private final StatHolder FeedSaturation;

	public AbilityEffectFeedEntity(@Nonnull AbilityEffectDefinition def)
	{
		FeedAmount = new StatHolder(Constants.STAT_FEED_AMOUNT, def);
		FeedSaturation = new StatHolder(Constants.STAT_FEED_SATURATION, def);
	}

	@Override
	public void TriggerServer(@Nonnull GunContext gun, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		targets.ForEachEntity((entity) ->
		{
			if (entity instanceof Player player)
			{
				player.getFoodData().eat(Maths.Ceil(FeedAmount.Get(gun, stacks)), FeedSaturation.Get(gun, stacks));
			}
		});
	}
}
