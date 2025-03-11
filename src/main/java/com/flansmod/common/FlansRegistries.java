package com.flansmod.common;

import com.flansmod.common.abilities.Abilities;
import com.flansmod.common.abilities.AbilityEffectType;
import com.flansmod.common.actions.ActionType;
import com.flansmod.common.actions.Actions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class FlansRegistries
{
	public static final int REGISTRY_MAX = Integer.MAX_VALUE - 1;

	// Static init order matters. Make both keys
	public static final ResourceKey<Registry<ActionType<?>>> ACTION_TYPE = ResourceKey.createRegistryKey(new ResourceLocation(FlansMod.MODID, "action_type"));
	public static final ResourceKey<Registry<AbilityEffectType<?>>> ABILITY_EFFECT_TYPE = ResourceKey.createRegistryKey(new ResourceLocation(FlansMod.MODID, "ability_effect_type"));

	// Then load defaults
	public static final Supplier<IForgeRegistry<ActionType<?>>> ACTION_TYPES =
		Actions.BASE_ACTION_TYPES.makeRegistry(
			() -> new RegistryBuilder<ActionType<?>>()
				.setName(ACTION_TYPE.location())
				.setMaxID(REGISTRY_MAX)
				.disableSaving()
				.disableOverrides()
				.disableSync());
	public static final Supplier<IForgeRegistry<AbilityEffectType<?>>> ABILITY_EFFECT_TYPES =
		Abilities.BASE_ABILITY_EFFECT_TYPES.makeRegistry(
			() -> new RegistryBuilder<AbilityEffectType<?>>()
				.setName(ABILITY_EFFECT_TYPE.location())
				.setMaxID(REGISTRY_MAX)
				.disableSaving()
				.disableOverrides()
				.disableSync());

	public static void init()
	{

	}
}
