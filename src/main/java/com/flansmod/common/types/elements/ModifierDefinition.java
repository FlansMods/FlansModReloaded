package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.util.Maths;
import net.minecraft.network.chat.Component;

public class ModifierDefinition
{
	public static final String STAT_GROUP_REPEAT_MODE = "repeat_mode";
	public static final String STAT_GROUP_REPEAT_DELAY = "repeat_delay";
	public static final String STAT_GROUP_REPEAT_COUNT = "repeat_count";
	public static final String STAT_GROUP_SPIN_UP_DURATION = "spin_up_duration";
	public static final String STAT_GROUP_LOUDNESS = "loudness";


	public static final String STAT_SHOT_SPREAD = "spread";
	public static final String STAT_SHOT_VERTICAL_RECOIL = "vertical_recoil";
	public static final String STAT_SHOT_HORIZONTAL_RECOIL = "horizontal_recoil";
	public static final String STAT_SHOT_SPEED = "speed";
	public static final String STAT_SHOT_BULLET_COUNT = "bullet_count";
	public static final String STAT_SHOT_PENETRATION_POWER = "penetration_power";
	public static final String STAT_SHOT_SPREAD_PATTERN = "spread_pattern";

	public static final String STAT_IMPACT_DAMAGE = "impact_damage";
	public static final String STAT_INSTANT_DAMAGE = "instant_damage";
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
	public static final String STAT_SOUND_PITCH = "pitch";
	public static final String STAT_LIGHT_STRENGTH = "flashlight_strength";
	public static final String STAT_LIGHT_RANGE = "flashlight_range";
	public static final String STAT_EYE_LINE_ROLL = "eye_line_roll";

	public static final String KEY_ENTITY_TAG = "entity_tag";
	public static final String KEY_ENTITY_ID = "entity_id";
	public static final String KEY_ACTION_KEY = "action_key";
	public static final String KEY_MODEL_ID = "model_id";
	public static final String KEY_MODE = "mode";
	public static final String KEY_SET_VALUE = "set_value";
	public static final String KEY_EYE_LINE_NAME = "eye_line_name";
	public static final String KEY_MOB_EFFECT_ID = "mob_effect_id";
	public static final String STAT_POTION_MULTIPLIER = "potion_multiplier";
	public static final String STAT_POTION_DURATION = "potion_duration";
	public static final String STAT_ATTRIBUTE_MULTIPLIER = "attribute_multiplier";


	public static final String STAT_LASER_ORIGIN = "laser_origin";
	public static final String STAT_LASER_RED = "laser_red";
	public static final String STAT_LASER_GREEN = "laser_green";
	public static final String STAT_LASER_BLUE = "laser_blue";
	public static final String MODAL_FIXED_LASER_DIRECTION = "fixed_laser_direction";

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
	public String[] ApplyFilters = new String[0];


	public boolean AppliesTo(String groupPath)
	{
		if(ApplyFilters.length == 0)
			return true;
		for(String filter : ApplyFilters)
			if(groupPath.contains(filter))
				return true;
		return false;
	}
	public boolean AppliesTo(String stat, String groupPath)
	{
		return Stat.equals(stat)
			&& AppliesTo(groupPath);
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
