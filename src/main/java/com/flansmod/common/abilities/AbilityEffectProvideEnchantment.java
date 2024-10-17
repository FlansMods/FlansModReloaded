package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectProvideEnchantment implements IAbilityEffect
{
	public final Enchantment Enchant;
	@Nonnull
	private final StatHolder EnchantLevel;

	public AbilityEffectProvideEnchantment(@Nonnull AbilityEffectDefinition def)
	{
		Enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(def.ModifyString(Constants.STAT_ENCHANTMENT_ID, "")));

		EnchantLevel = new StatHolder(Constants.STAT_ENCHANTMENT_LEVEL, def);
	}

	public int GetLevel(@Nonnull ActionGroupContext context, @Nullable AbilityStack stacks)
	{
		return Maths.ceil(EnchantLevel.Get(context, stacks));
	}

	// No "active" effects from this one
}
