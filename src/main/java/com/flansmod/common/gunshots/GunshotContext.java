package com.flansmod.common.gunshots;

import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.guns.ERepeatMode;
import com.flansmod.common.types.guns.ESpreadPattern;
import com.flansmod.util.Maths;

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
	public float VerticalRecoil() { return ModifyFloat("vertical_recoil", Bullet.shootStats.verticalRecoil); }
	public float HorizontalRecoil() { return ModifyFloat("horizontal_recoil", Bullet.shootStats.horizontalRecoil); }
	public float Spread() { return ModifyFloat("spread", Bullet.shootStats.spread); }
	public float Speed() { return ModifyFloat("speed", Bullet.shootStats.speed); }
	public int BulletCount() { return Maths.Ceil(ModifyFloat("bullet_count", Bullet.shootStats.bulletCount)); }
	public float PenetrationPower() { return ModifyFloat("penetration_power", Bullet.shootStats.penetrationPower); }
	public ESpreadPattern SpreadPattern() { return (ESpreadPattern)ModifyEnum("spread_pattern", Bullet.shootStats.spreadPattern, ESpreadPattern.class); }

	// Impact settings
	public float ImpactDamage() { return ModifyFloat("impact_damage", Bullet.shootStats.impact.damageToTarget); }
	public float Knockback() { return ModifyFloat("knockback", Bullet.shootStats.impact.knockback); }
	public float MultiplierVsPlayers() { return ModifyFloat("multiplier_vs_players", Bullet.shootStats.impact.multiplierVsPlayers); }
	public float MultiplierVsVehicles() { return ModifyFloat("multiplier_vs_vehicles", Bullet.shootStats.impact.multiplierVsVehicles); }
	public float SplashDamageRadius() { return ModifyFloat("splash_damage_radius", Bullet.shootStats.impact.splashDamageRadius); }
	public float SplashDamageFalloff() { return ModifyFloat("splash_damage_falloff", Bullet.shootStats.impact.splashDamageFalloff); }
	public float SetFireToTarget() { return ModifyFloat("set_fire_to_target", Bullet.shootStats.impact.setFireToTarget); }
	public float FireSpreadRadius() { return ModifyFloat("fire_spread_radius", Bullet.shootStats.impact.fireSpreadRadius); }
	public float FireSpreadAmount() { return ModifyFloat("fire_spread_amount", Bullet.shootStats.impact.fireSpreadAmount); }
}

