package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
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
	@Nullable
	private final TagKey<EntityType<? extends Entity>> CheckTag;
	private final float BaseHealAmount;

	public AbilityEffectHealEntity(@Nonnull AbilityEffectDefinition def)
	{
		String tagPath = def.ModifyString(ModifierDefinition.KEY_ENTITY_TAG, "");
		CheckTag = !tagPath.isEmpty() ? TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation(tagPath)) : null;
		BaseHealAmount = def.ModifyFloat(ModifierDefinition.STAT_HEAL_AMOUNT, 1.0f);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		targets.ForEachEntity((entity) ->
		{
			if(entity instanceof LivingEntity living)
			{
				if(CheckTag == null || entity.getType().is(CheckTag))
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
		float healAmount = gun.ModifyFloat(ModifierDefinition.STAT_HEAL_AMOUNT, BaseHealAmount);
		if(stacks != null)
			healAmount *= stacks.GetIntensity();
		return healAmount;
	}
}
