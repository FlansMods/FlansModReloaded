package com.flansmod.common.types.elements;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.JsonField;
import com.flansmod.util.Maths;
import com.mojang.datafixers.kinds.IdF;
import net.minecraft.network.chat.Component;

public class ModifierDefinition
{
	public static final String STAT_SHOT_SPREAD = "spread";
	public static final String STAT_SHOT_VERTICAL_RECOIL = "vertical_recoil";
	public static final String STAT_SHOT_HORIZONTAL_RECOIL = "horizontal_recoil";
	public static final String STAT_SHOT_SPEED = "speed";
	public static final String STAT_SHOT_BULLET_COUNT = "bullet_count";
	public static final String STAT_SHOT_PENETRATION_POWER = "penetration_power";
	public static final String STAT_SHOT_SPREAD_PATTERN = "spread_pattern";

	public static final String STAT_IMPACT_DAMAGE = "impact_damage";
	public static final String STAT_IMPACT_POTION_EFFECT_ON_TARGET = "potion_effect_on_target";
	public static final String STAT_IMPACT_KNOCKBACK = "knockback";
	public static final String STAT_IMPACT_MULTIPLIER_VS_PLAYERS = "multiplier_vs_players";
	public static final String STAT_IMPACT_MULTIPLIER_VS_VEHICLES = "multiplier_vs_vehicles";
	public static final String STAT_IMPACT_SPLASH_DAMAGE = "splash_damage";
	public static final String STAT_IMPACT_SPLASH_DAMAGE_RADIUS = "splash_damage_radius";
	public static final String STAT_IMPACT_SPLASH_DAMAGE_FALLOFF = "splash_damage_falloff";
	public static final String STAT_IMPACT_POTION_EFFECT_ON_SPLASH = "potion_effect_on_splash";
	public static final String STAT_IMPACT_SET_FIRE_TO_TARGET = "set_fire_to_target";
	public static final String STAT_IMPACT_FIRE_SPREAD_RADIUS = "fire_spread_radius";
	public static final String STAT_IMPACT_FIRE_SPREAD_AMOUNT = "fire_spread_amount";
	public static final String STAT_IMPACT_EXPLOSION_RADIUS = "explosion_radius";

	public static final String STAT_MELEE_DAMAGE = "melee_damage";
	public static final String STAT_TOOL_REACH = "reach";
	public static final String STAT_TOOL_HARVEST_LEVEL = "tool_level";
	public static final String STAT_TOOL_HARVEST_SPEED = "harvest_speed";
	public static final String STAT_ZOOM_FOV_FACTOR = "fov_factor";
	public static final String STAT_ZOOM_SCOPE_OVERLAY = "scope_overlay";
	public static final String STAT_ANIM = "anim";
	public static final String STAT_BLOCK_ID = "block_id";
	public static final String STAT_DURATION = "duration";
	public static final String STAT_HEAL_AMOUNT = "heal_amount";
	public static final String STAT_FEED_AMOUNT = "feed_amount";
	public static final String STAT_FEED_SATURATION = "feed_saturation";
	public static final String STAT_ENTITY_TAG = "entity_tag";
	public static final String STAT_ENTITY_ID = "entity_id";

	@JsonField
	public String Stat = "";
	@JsonField
	public String Filter = "";
	@JsonField(Docs = "Additive modifiers are applied first")
	public float Add = 0.0f;
	@JsonField(Docs = "All multiplys are applied after all adds, so notably a 0x multiplier will always 0 the stat")
	public float Multiply = 1.0f;
	@JsonField(Docs = "For non-numeric values, such as enums, this is a simple override")
	public String SetValue = "";
	@JsonField
	public boolean ApplyToPrimary = true;
	@JsonField
	public boolean ApplyToSecondary = false;


	public boolean AppliesTo(EActionInput actionSet)
	{
		return actionSet == EActionInput.PRIMARY ? ApplyToPrimary : ApplyToSecondary;
	}
	public boolean AppliesTo(String stat, EActionInput actionSet)
	{
		return Stat.equals(stat)
			&& (actionSet == EActionInput.PRIMARY ? ApplyToPrimary : ApplyToSecondary);
	}
	public Component GetModifierString()
	{
		Component statName = Component.translatable("modifier.stat_name." + Stat);
		if(Add != 0.0f && Multiply != 1.0f)
		{
			return Component.translatable("modifier.unknown");
		}
		else if(Add != 0.0f)
		{
			if(Add > 0.0f)
				return Component.translatable("modifier.increase_stat", PrintIntOrFloat(Add), statName);
			else
				return Component.translatable("modifier.decrease_stat", PrintIntOrFloat(-Add), statName);
		}
		else if(Multiply != 1.0f)
		{
			if(Multiply > 1.0f)
			{
				float percentIncrease = (Multiply - 1.0f) * 100.0f;
				return Component.translatable("modifier.increase_stat_percent", PrintIntOrFloat(percentIncrease), statName);
			}
			else
			{
				float percentDecrease = (1.0f - Multiply) * 100.0f;
				return Component.translatable("modifier.decrease_stat_percent", PrintIntOrFloat(percentDecrease), statName);
			}
		}
		else if(!SetValue.isEmpty())
		{
			return Component.translatable("modifier.override_value", statName, SetValue);
		}
		else
		{
			return Component.translatable("modifier.unknown");
		}
	}
	private String PrintIntOrFloat(float value)
	{
		if(Maths.Approx(value, Maths.Round(value)))
		{
			return Integer.toString(Maths.Round(value));
		}
		return Float.toString(value);
	}
}
