package com.flansmod.common.effects;

import com.flansmod.common.FlansMod;
import com.flansmod.common.FlansRegistries;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class Effects
{
	public static final IEffect None = new EffectNone();

	public static final DeferredRegister<EffectType<?>> BASE_EFFECT_TYPES
		= DeferredRegister.create(FlansRegistries.EFFECT_TYPE, FlansMod.MODID);

	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_APPLY_DAMAGE
		= BASE_EFFECT_TYPES.register("apply_damage", () -> new EffectType<>(EffectApplyDamage::new));

	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_APPLY_MOB_EFFECT
		= BASE_EFFECT_TYPES.register("apply_mob_effect", () -> new EffectType<>(EffectApplyMobEffect::new));

	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_APPLY_ATTRIBUTE
		= BASE_EFFECT_TYPES.register("apply_attribute", () -> new EffectType<>(EffectApplyAttribute::new));

	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_APPLY_MODIFIER
		= BASE_EFFECT_TYPES.register("apply_modifier", () -> new EffectType<>(EffectApplyModifier::new));

	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_APPLY_ENCHANTMENT
		= BASE_EFFECT_TYPES.register("apply_enchantment", () -> new EffectType<>(EffectProvideEnchantment::new));

	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_BREAK_BLOCK
		= BASE_EFFECT_TYPES.register("break_block", () -> new EffectType<>(EffectBreakBlock::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_PLACE_BLOCK
		= BASE_EFFECT_TYPES.register("place_block", () -> new EffectType<>(EffectPlaceBlock::new));

	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_SUMMON_NPC
		= BASE_EFFECT_TYPES.register("summon_npc", () -> new EffectType<>(EffectSummonNpc::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_SPAWN_ENTITY
		= BASE_EFFECT_TYPES.register("spawn_entity", () -> new EffectType<>(EffectSpawnEntity::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_DELETE_ENTITY
		= BASE_EFFECT_TYPES.register("delete_entity", () -> new EffectType<>(EffectDeleteEntity::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_HEAL_ENTITY
		= BASE_EFFECT_TYPES.register("heal_entity", () -> new EffectType<>(EffectHealEntity::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_FEED_ENTITY
		= BASE_EFFECT_TYPES.register("feed_entity", () -> new EffectType<>(EffectFeedEntity::new));

	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_START_ACTION_GROUP
		= BASE_EFFECT_TYPES.register("start_action_group", () -> new EffectType<>(EffectStartActionGroup::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_KNOCKBACK
		= BASE_EFFECT_TYPES.register("knockback", () -> new EffectType<>(EffectKnockback::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_SET_FIRE_TO_ENTITY
		= BASE_EFFECT_TYPES.register("set_fire", () -> new EffectType<>(EffectSetFireToEntity::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_EXPLODE
		= BASE_EFFECT_TYPES.register("explode", () -> new EffectType<>(EffectExplode::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_APPLY_DECAL
		= BASE_EFFECT_TYPES.register("apply_decal", () -> new EffectType<>(EffectApplyDecal::new));

	// Not working yet
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_DAMAGE_ARMOUR
		= BASE_EFFECT_TYPES.register("damage_armour", () -> new EffectType<>(EffectDamageArmour::new));
	// Passthrough, just check for it
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_TOTEM_OF_UNDYING
		= BASE_EFFECT_TYPES.register("totem_of_undying", () -> new EffectType<>(EffectNone::new));


	// TODO:
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_INTERACT
		= BASE_EFFECT_TYPES.register("interact", () -> new EffectType<>(EffectNone::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_REPAIR_VEHICLE
		= BASE_EFFECT_TYPES.register("repair_vehicle", () -> new EffectType<>(EffectNone::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_COLLECT_FLUID
		= BASE_EFFECT_TYPES.register("collect_fluid", () -> new EffectType<>(EffectNone::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_PLACE_FLUID
		= BASE_EFFECT_TYPES.register("place_fluid", () -> new EffectType<>(EffectNone::new));
	public static final RegistryObject<EffectType<?>> EFFECT_TYPE_END_ACITON_GROUP
		= BASE_EFFECT_TYPES.register("end_action_group", () -> new EffectType<>(EffectNone::new));



	@Nonnull
	public static IEffect CreateEffectProcessor(@Nonnull AbilityEffectDefinition def)
	{
		EffectType<? extends IEffect> effectType = FlansRegistries.EFFECT_TYPES.get().getValue(def.effectType);
		if(effectType != null)
		{
			return effectType.createFunc().apply(def);
		}
		return None;
	}
}
