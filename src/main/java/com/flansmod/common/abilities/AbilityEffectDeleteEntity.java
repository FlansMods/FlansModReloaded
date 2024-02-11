package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectDeleteEntity implements IAbilityEffect
{
	@Nullable
	private final TagKey<EntityType<? extends Entity>> CheckTag;

	public AbilityEffectDeleteEntity(@Nonnull AbilityEffectDefinition def)
	{
		String tagPath = def.ModifyString(ModifierDefinition.KEY_ENTITY_TAG, "");
		CheckTag = !tagPath.isEmpty() ? TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation(tagPath)) : null;
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		targets.ForEachEntity((entity) -> {
			if(CheckTag == null || entity.getType().is(CheckTag))
				entity.kill();
		});
	}

	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}
}
