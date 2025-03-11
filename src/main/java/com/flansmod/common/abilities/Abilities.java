package com.flansmod.common.abilities;

import com.flansmod.common.FlansMod;
import com.flansmod.common.FlansRegistries;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.ActionType;
import com.flansmod.common.actions.nodes.AnimationAction;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class Abilities
{
	public static final IAbilityEffect None = new AbilityEffectNone();

	public static final DeferredRegister<AbilityEffectType<?>> BASE_ABILITY_EFFECT_TYPES
		= DeferredRegister.create(FlansRegistries.ABILITY_EFFECT_TYPE, FlansMod.MODID);

	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_APPLY_DAMAGE
		= BASE_ABILITY_EFFECT_TYPES.register("apply_damage", () -> new AbilityEffectType<>(AbilityEffectApplyDamage::new));

	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_APPLY_MOB_EFFECT
		= BASE_ABILITY_EFFECT_TYPES.register("apply_mob_effect", () -> new AbilityEffectType<>(AbilityEffectApplyMobEffect::new));

	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_APPLY_ATTRIBUTE
		= BASE_ABILITY_EFFECT_TYPES.register("apply_attribute", () -> new AbilityEffectType<>(AbilityEffectApplyAttribute::new));

	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_APPLY_MODIFIER
		= BASE_ABILITY_EFFECT_TYPES.register("apply_modifier", () -> new AbilityEffectType<>(AbilityInstanceApplyModifier::new));

	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_APPLY_ENCHANTMENT
		= BASE_ABILITY_EFFECT_TYPES.register("apply_enchantment", () -> new AbilityEffectType<>(AbilityEffectProvideEnchantment::new));

	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_BREAK_BLOCK
		= BASE_ABILITY_EFFECT_TYPES.register("break_block", () -> new AbilityEffectType<>(AbilityEffectBreakBlock::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_PLACE_BLOCK
		= BASE_ABILITY_EFFECT_TYPES.register("place_block", () -> new AbilityEffectType<>(AbilityEffectPlaceBlock::new));

	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_SUMMON_NPC
		= BASE_ABILITY_EFFECT_TYPES.register("summon_npc", () -> new AbilityEffectType<>(AbilityEffectSummonNpc::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_SPAWN_ENTITY
		= BASE_ABILITY_EFFECT_TYPES.register("spawn_entity", () -> new AbilityEffectType<>(AbilityEffectSpawnEntity::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_DELETE_ENTITY
		= BASE_ABILITY_EFFECT_TYPES.register("delete_entity", () -> new AbilityEffectType<>(AbilityEffectDeleteEntity::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_HEAL_ENTITY
		= BASE_ABILITY_EFFECT_TYPES.register("heal_entity", () -> new AbilityEffectType<>(AbilityEffectHealEntity::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_FEED_ENTITY
		= BASE_ABILITY_EFFECT_TYPES.register("feed_entity", () -> new AbilityEffectType<>(AbilityEffectFeedEntity::new));

	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_START_ACTION_GROUP
		= BASE_ABILITY_EFFECT_TYPES.register("start_action_group", () -> new AbilityEffectType<>(AbilityEffectStartActionGroup::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_KNOCKBACK
		= BASE_ABILITY_EFFECT_TYPES.register("knockback", () -> new AbilityEffectType<>(AbilityEffectKnockback::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_SET_FIRE_TO_ENTITY
		= BASE_ABILITY_EFFECT_TYPES.register("set_fire", () -> new AbilityEffectType<>(AbilityEffectSetFireToEntity::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_EXPLODE
		= BASE_ABILITY_EFFECT_TYPES.register("explode", () -> new AbilityEffectType<>(AbilityEffectExplode::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_APPLY_DECAL
		= BASE_ABILITY_EFFECT_TYPES.register("apply_decal", () -> new AbilityEffectType<>(AbilityEffectApplyDecal::new));

	// Not working yet
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_DAMAGE_ARMOUR
		= BASE_ABILITY_EFFECT_TYPES.register("damage_armour", () -> new AbilityEffectType<>(AbilityEffectDamageArmour::new));
	// Passthrough, just check for it
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_TOTEM_OF_UNDYING
		= BASE_ABILITY_EFFECT_TYPES.register("totem_of_undying", () -> new AbilityEffectType<>(AbilityEffectNone::new));


	// TODO:
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_INTERACT
		= BASE_ABILITY_EFFECT_TYPES.register("interact", () -> new AbilityEffectType<>(AbilityEffectNone::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_REPAIR_VEHICLE
		= BASE_ABILITY_EFFECT_TYPES.register("repair_vehicle", () -> new AbilityEffectType<>(AbilityEffectNone::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_COLLECT_FLUID
		= BASE_ABILITY_EFFECT_TYPES.register("collect_fluid", () -> new AbilityEffectType<>(AbilityEffectNone::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_PLACE_FLUID
		= BASE_ABILITY_EFFECT_TYPES.register("place_fluid", () -> new AbilityEffectType<>(AbilityEffectNone::new));
	public static final RegistryObject<AbilityEffectType<?>> ABILITY_EFFECT_TYPE_END_ACITON_GROUP
		= BASE_ABILITY_EFFECT_TYPES.register("end_action_group", () -> new AbilityEffectType<>(AbilityEffectNone::new));



	@Nonnull
	public static IAbilityEffect CreateEffectProcessor(@Nonnull AbilityEffectDefinition def)
	{
		AbilityEffectType<? extends IAbilityEffect> abilityEffectType = FlansRegistries.ABILITY_EFFECT_TYPES.get().getValue(def.effectType);
		if(abilityEffectType != null)
		{
			return abilityEffectType.createFunc().apply(def);
		}
		return None;
	}
}
