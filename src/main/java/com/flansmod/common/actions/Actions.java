package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.FlansRegistries;
import com.flansmod.common.actions.nodes.*;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class Actions
{
	public static final String DefaultPrimaryActionKey = "primary_fire";

	public static final DeferredRegister<ActionType<?>> BASE_ACTION_TYPES = DeferredRegister.create(FlansRegistries.ACTION_TYPE, FlansMod.MODID);

	public static final RegistryObject<ActionType<?>> ACTION_TYPE_ANIMATION 		= BASE_ACTION_TYPES.register("animation", () -> new ActionType<>(AnimationAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_SHOOT 			= BASE_ACTION_TYPES.register("shoot", () -> new ActionType<>(ShootAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_SCOPE 			= BASE_ACTION_TYPES.register("scope", () -> new ActionType<>(ScopeAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_AIM_DOWN_SIGHTS 	= BASE_ACTION_TYPES.register("aim_down_sights", () -> new ActionType<>(AimDownSightAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_PLAY_SOUND 		= BASE_ACTION_TYPES.register("play_sound", () -> new ActionType<>(PlaySoundAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_COOK_GRENADE 		= BASE_ACTION_TYPES.register("cook_grenade", () -> new ActionType<>(CookGrenadeAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_RAYCAST 			= BASE_ACTION_TYPES.register("raycast", () -> new ActionType<>(RaycastAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_DROP 				= BASE_ACTION_TYPES.register("drop", () -> new ActionType<>(DropAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_ATTACH_EFFECT		= BASE_ACTION_TYPES.register("attach_effect", () -> new ActionType<>(AttachEffectAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_SPAWN_PARTICLE	= BASE_ACTION_TYPES.register("spawn_particle", () -> new ActionType<>(SpawnParticleAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_SWITCH_MODE		= BASE_ACTION_TYPES.register("switch_mode", () -> new ActionType<>(SwitchModeAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_ABILITY_ON_SELF	= BASE_ACTION_TYPES.register("ability_on_self", () -> new ActionType<>(AbilityOnSelfAction::new));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_EJECT_CASING		= BASE_ACTION_TYPES.register("eject_casing", () -> new ActionType<>(EjectCasingAction::new));

	public static final RegistryObject<ActionType<?>> ACTION_TYPE_MELEE 			= BASE_ACTION_TYPES.register("melee", () -> new ActionType<>((x, y) -> null));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_SHIELD			= BASE_ACTION_TYPES.register("shield", () -> new ActionType<>((x, y) -> null));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_PICKAXE 			= BASE_ACTION_TYPES.register("pickaxe", () -> new ActionType<>((x, y) -> null));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_SHOVEL			= BASE_ACTION_TYPES.register("shovel", () -> new ActionType<>((x, y) -> null));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_AXE 				= BASE_ACTION_TYPES.register("axe", () -> new ActionType<>((x, y) -> null));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_HOE 				= BASE_ACTION_TYPES.register("hoe", () -> new ActionType<>((x, y) -> null));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_TILL 				= BASE_ACTION_TYPES.register("till", () -> new ActionType<>((x, y) -> null));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_STRIP 			= BASE_ACTION_TYPES.register("strip", () -> new ActionType<>((x, y) -> null));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_SHEAR 			= BASE_ACTION_TYPES.register("shear", () -> new ActionType<>((x, y) -> null));
	public static final RegistryObject<ActionType<?>> ACTION_TYPE_FLATTEN 			= BASE_ACTION_TYPES.register("flatten", () -> new ActionType<>((x, y) -> null));

	@Nullable
	public static ActionInstance InstanceAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		ActionType<? extends ActionInstance> actionType = FlansRegistries.ACTION_TYPES.get().getValue(def.actionType);
		if(actionType != null)
		{
			return actionType.createFunc().apply(group, def);
		}
		return null;
	}

	public static ActionInstance.NetData CreateEmptyNetData(int type)
	{
		switch(type)
		{
			case ActionInstance.NetData.INVALID_ID -> { return ActionInstance.NetData.Invalid; }
			case ShootAction.ShootNetData.ID -> { return new ShootAction.ShootNetData(); }
			case EjectCasingAction.CasingNetData.ID -> { return new EjectCasingAction.CasingNetData(); }
		}
		return ActionInstance.NetData.Invalid;
	}
}
