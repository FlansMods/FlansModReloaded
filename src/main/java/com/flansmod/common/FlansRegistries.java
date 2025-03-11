package com.flansmod.common;

import com.flansmod.common.effects.Effects;
import com.flansmod.common.effects.EffectType;
import com.flansmod.common.actions.ActionType;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.entity.vehicle.controls.ControlLogicType;
import com.flansmod.common.entity.vehicle.controls.ControlLogics;
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
	public static final ResourceKey<Registry<EffectType<?>>> EFFECT_TYPE = ResourceKey.createRegistryKey(new ResourceLocation(FlansMod.MODID, "ability_effect_type"));
	public static final ResourceKey<Registry<ControlLogicType<?>>> CONTROL_LOGIC_TYPE = ResourceKey.createRegistryKey(new ResourceLocation(FlansMod.MODID, "control_logic_type"));

	// Then load defaults
	public static final Supplier<IForgeRegistry<ActionType<?>>> ACTION_TYPES =
		Actions.BASE_ACTION_TYPES.makeRegistry(
			() -> new RegistryBuilder<ActionType<?>>()
				.setName(ACTION_TYPE.location())
				.setMaxID(REGISTRY_MAX)
				.disableSaving()
				.disableOverrides()
				.disableSync());
	public static final Supplier<IForgeRegistry<EffectType<?>>> EFFECT_TYPES =
		Effects.BASE_EFFECT_TYPES.makeRegistry(
			() -> new RegistryBuilder<EffectType<?>>()
				.setName(EFFECT_TYPE.location())
				.setMaxID(REGISTRY_MAX)
				.disableSaving()
				.disableOverrides()
				.disableSync());
	public static final Supplier<IForgeRegistry<ControlLogicType<?>>> CONTROL_LOGIC_TYPES =
		ControlLogics.BASE_CONTROL_LOGIC_TYPES.makeRegistry(
			() -> new RegistryBuilder<ControlLogicType<?>>()
				.setName(CONTROL_LOGIC_TYPE.location())
				.setMaxID(REGISTRY_MAX)
				.disableSaving()
				.disableOverrides()
				.disableSync());

	public static void init()
	{

	}
}
