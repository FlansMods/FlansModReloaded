package com.flansmod.common.gunshots;

import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.ESpreadPattern;
import com.flansmod.util.Maths;

import java.lang.reflect.Modifier;


public class GunshotContext
{
	public static final GunshotContext INVALID = new GunshotContext(ActionGroupContext.INVALID, BulletDefinition.INVALID)
	{
		@Override
		public boolean IsValid() { return false; }
	};

	public final ActionGroupContext ActionGroup;
	public final BulletDefinition Bullet;

	public static GunshotContext CreateFrom(ActionGroupContext actionGroupContext)
	{
		return new GunshotContext(actionGroupContext, BulletDefinition.INVALID);
	}

	public static GunshotContext CreateFrom(ActionGroupContext actionGroupContext, BulletDefinition bulletFired)
	{
		return new GunshotContext(actionGroupContext, bulletFired);
	}

	public GunshotContext(ActionGroupContext actionGroupContext, BulletDefinition bullet)
	{
		ActionGroup = actionGroupContext;
		Bullet = bullet;
	}

	public boolean IsValid() { return true; }

	// --------------------------------------------------------------------------
	// STAT CACHE
	// --------------------------------------------------------------------------
	public void Apply(ModifierStack modStack)
	{
		ActionGroup.Apply(modStack);
	}
	public float ModifyFloat(String key, float baseValue)
	{
		ModifierStack stack = new ModifierStack(key, ActionGroup.InputType);
		Apply(stack);
		return stack.ApplyTo(baseValue);
	}
	public String ModifyString(String key, String defaultValue)
	{
		ModifierStack stack = new ModifierStack(key, ActionGroup.InputType);
		Apply(stack);
		return stack.ApplyTo(defaultValue);
	}
	public <T extends Enum<T>> Enum<T> ModifyEnum(String key, T defaultValue, Class<T> clazz)
	{
		String modified = ModifyString(key, defaultValue.toString());
		return Enum.valueOf(clazz, modified);
	}

	// Shot settings
	public float VerticalRecoil() 			{ return ModifyFloat(ModifierDefinition.STAT_SHOT_VERTICAL_RECOIL, Bullet.shootStats.verticalRecoil); }
	public float HorizontalRecoil() 		{ return ModifyFloat(ModifierDefinition.STAT_SHOT_HORIZONTAL_RECOIL, Bullet.shootStats.horizontalRecoil); }
	public float Spread() 					{ return ModifyFloat(ModifierDefinition.STAT_SHOT_SPREAD, Bullet.shootStats.spread); }
	public float Speed() 					{ return ModifyFloat(ModifierDefinition.STAT_SHOT_SPEED, Bullet.shootStats.speed); }
	public int BulletCount() 				{ return Maths.Ceil(ModifyFloat(ModifierDefinition.STAT_SHOT_BULLET_COUNT, Bullet.shootStats.bulletCount)); }
	public float PenetrationPower() 		{ return ModifyFloat(ModifierDefinition.STAT_SHOT_PENETRATION_POWER, Bullet.shootStats.penetrationPower); }
	public ESpreadPattern SpreadPattern() 	{ return (ESpreadPattern)ModifyEnum(ModifierDefinition.STAT_SHOT_SPREAD_PATTERN, Bullet.shootStats.spreadPattern, ESpreadPattern.class); }

	// Impact settings
	public float ImpactDamage() 			{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_DAMAGE, Bullet.shootStats.impact.damageToTarget); }
	public String PotionEffectOnTarget()	{ return ModifyString(ModifierDefinition.STAT_IMPACT_POTION_EFFECT_ON_TARGET, Bullet.shootStats.impact.potionEffectOnTarget); }
	public float SetFireToTarget() 			{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_SET_FIRE_TO_TARGET, Bullet.shootStats.impact.setFireToTarget); }
	public float Knockback() 				{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_KNOCKBACK, Bullet.shootStats.impact.knockback); }
	public float MultiplierVsPlayers() 		{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_MULTIPLIER_VS_PLAYERS, Bullet.shootStats.impact.multiplierVsPlayers); }
	public float MultiplierVsVehicles() 	{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_MULTIPLIER_VS_VEHICLES, Bullet.shootStats.impact.multiplierVsVehicles); }

	public float SplashDamageRadius() 		{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_SPLASH_DAMAGE_RADIUS, Bullet.shootStats.impact.splashDamageRadius); }
	public float SplashDamageFalloff() 		{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_SPLASH_DAMAGE_FALLOFF, Bullet.shootStats.impact.splashDamageFalloff); }
	public String PotionEffectOnSplash()	{ return ModifyString(ModifierDefinition.STAT_IMPACT_POTION_EFFECT_ON_SPLASH, Bullet.shootStats.impact.potionEffectOnSplash); }

	public float FireSpreadRadius() 		{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_FIRE_SPREAD_RADIUS, Bullet.shootStats.impact.fireSpreadRadius); }
	public float FireSpreadAmount() 		{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_FIRE_SPREAD_AMOUNT, Bullet.shootStats.impact.fireSpreadAmount); }
}

