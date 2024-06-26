package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectSpawnEntity implements IAbilityEffect
{
	@Nullable
	private final EntityType<? extends Entity> EntityType;

	public AbilityEffectSpawnEntity(@Nonnull AbilityEffectDefinition def)
	{
		String entityID = def.ModifyString(Constants.KEY_ENTITY_ID, "");
		EntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityID));
	}

	@Override
	public void TriggerServer(@Nonnull ActionGroupContext actionGroup, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		if(EntityType != null)
		{
			Level level = actionGroup.Gun.GetLevel();
			if (level != null)
			{
				targets.ForOnePosition((pos) ->
				{
					Entity entity = EntityType.create(level);
					if (entity != null)
					{
						entity.setPos(pos);
						level.addFreshEntity(entity);
					}
				});
			}
		}
	}
}
