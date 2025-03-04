package com.flansmod.common;

import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;

public class FlansModConfig
{
	public static final ForgeConfigSpec GeneralConfig;

	public static ForgeConfigSpec.BooleanValue AllowBulletsBreakBlocks;
	public static ForgeConfigSpec.BooleanValue AllowBulletsCreateExplosions;
	public static ForgeConfigSpec.BooleanValue AllowBulletsCreateFire;
	public static ForgeConfigSpec.BooleanValue AllowBulletCasingDrops;

	public static ForgeConfigSpec.BooleanValue AllowSummonNpc;
	public static ForgeConfigSpec.DoubleValue SummonNpcMinDistance;
	public static ForgeConfigSpec.DoubleValue SummonNpcExtraCooldown;

	public static ForgeConfigSpec.BooleanValue AllowPainting;
	public static ForgeConfigSpec.IntValue AdditionalPaintCanCost;
	public static ForgeConfigSpec.BooleanValue AllowMagazineModifying;
	public static ForgeConfigSpec.IntValue AdditionalMagazineModifyCost;
	public static ForgeConfigSpec.BooleanValue AllowGunCrafting;
	public static ForgeConfigSpec.BooleanValue AllowPartCrafting;


	public static ForgeConfigSpec.BooleanValue AllowShootActions;
	public static ForgeConfigSpec.BooleanValue AllowRaycastActions;
	public static ForgeConfigSpec.BooleanValue AllowLaserActions;

	public static ForgeConfigSpec.DoubleValue GlobalDamageMultiplier;
	public static ForgeConfigSpec.DoubleValue GlobalHealMultiplier;
	public static ForgeConfigSpec.DoubleValue GlobalFireDurationMultiplier;
	public static ForgeConfigSpec.DoubleValue GlobalRepairMultiplier;
	public static ForgeConfigSpec.DoubleValue GlobalHeadshotMultiplier;


	static
	{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		Init(builder);
		GeneralConfig = builder.build();
	}

	private static void Init(@Nonnull ForgeConfigSpec.Builder builder)
	{
		builder.push("World Settings");
		AllowBulletsBreakBlocks = builder.define("allow_bullet_break_blocks", true);
		AllowBulletsCreateExplosions = builder.define("allow_bullet_create_explosions", true);
		AllowBulletsCreateFire = builder.define("allow_bullet_create_fire", true);
		AllowBulletCasingDrops = builder.define("allow_bullet_casing_drops", true);
		AllowSummonNpc = builder.define("allow_summon_npc", true);
		SummonNpcMinDistance = builder.defineInRange("summon_npc_min_distance", 400d, 0d, 1000d);
		SummonNpcExtraCooldown = builder.defineInRange("summon_npc_extra_cooldown", 0d, 0d, 10000d);
		builder.pop();

		builder.push("Crafting Settings");
		AllowPainting = builder.define("allow_painting", true);
		AdditionalPaintCanCost = builder.defineInRange("additional_paint_can_cost", 0, 0, 100);
		AllowMagazineModifying = builder.define("allow_magazine_modifying", true);
		AdditionalMagazineModifyCost = builder.defineInRange("additional_magazine_modify_cost", 0, 0, 100);
		AllowGunCrafting = builder.define("allow_gun_crafting", true);
		AllowPartCrafting = builder.define("allow_part_crafting", true);
		builder.pop();

		builder.push("Actions");
		AllowShootActions = builder.define("allow_shoot_actions", true);
		AllowRaycastActions = builder.define("allow_raycast_actions", true);
		AllowLaserActions = builder.define("allow_laser_actions", true);
		GlobalDamageMultiplier = builder.defineInRange("global_damage_multiplier", 1d, 0d, 100d);
		GlobalHealMultiplier = builder.defineInRange("global_heal_multiplier", 1d, 0d, 100d);
		GlobalFireDurationMultiplier = builder.defineInRange("global_fire_duration_multiplier", 1d, 0d, 100d);
		GlobalRepairMultiplier = builder.defineInRange("global_repair_multiplier", 1d, 0d, 100d);
		GlobalHeadshotMultiplier = builder.defineInRange("global_headshot_multiplier", 1.4d, 0d, 100d);
		builder.pop();
	}
}
