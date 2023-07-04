package com.flansmod.common.gunshots;

import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.ShotDefinition;
import com.flansmod.common.types.guns.ESpreadPattern;
import com.flansmod.util.Maths;

import java.util.List;

public class CachedGunStats
{
	public final float VerticalRecoil;
	public final float HorizontalRecoil;
	public final float Spread;
	public final float Speed;
	public final int BulletCount;
	public final float PenetrationPower;
	public final float BaseDamage;
	public final float Knockback;
	public final float MultiplierVsPlayers;
	public final float MultiplierVsVehicles;
	public final float SplashDamageRadius;
	public final float SplashDamageFalloff;
	public final float SetFireToTarget;
	public final float FireSpreadRadius;
	public final float FireSpreadAmount;
	public final ESpreadPattern SpreadPattern;

	public CachedGunStats(ShotDefinition shotDef, List<ModifierDefinition> modifiers)
	{
		VerticalRecoil = ModifyFloat(shotDef.verticalRecoil, "verticalRecoil", modifiers);
		HorizontalRecoil = ModifyFloat(shotDef.horizontalRecoil, "horizontalRecoil", modifiers);
		Spread = Maths.Ceil(ModifyFloat(shotDef.spread, "spread", modifiers));
		Speed = ModifyFloat(shotDef.speed, "speed", modifiers);
		BulletCount = Maths.Ceil(ModifyFloat(shotDef.bulletCount, "bulletCount", modifiers));
		PenetrationPower = ModifyFloat(shotDef.penetrationPower, "penetrationPower", modifiers);
		SpreadPattern = ModifySpreadPattern(shotDef.spreadPattern, "spreadPattern", modifiers);


		BaseDamage = ModifyFloat(shotDef.impact.damageToTarget, "damageToTarget", modifiers);
		Knockback = ModifyFloat(shotDef.impact.knockback, "knockback", modifiers);
		MultiplierVsPlayers = ModifyFloat(shotDef.impact.multiplierVsPlayers, "multiplierVsPlayers", modifiers);
		MultiplierVsVehicles = ModifyFloat(shotDef.impact.multiplierVsVehicles, "multiplierVsVehicles", modifiers);
		SplashDamageRadius = ModifyFloat(shotDef.impact.splashDamageRadius, "splashDamageRadius", modifiers);
		SplashDamageFalloff = ModifyFloat(shotDef.impact.splashDamageFalloff, "splashDamageFalloff", modifiers);
		SetFireToTarget = ModifyFloat(shotDef.impact.setFireToTarget, "setFireToTarget", modifiers);
		FireSpreadRadius = ModifyFloat(shotDef.impact.fireSpreadRadius, "fireSpreadRadius", modifiers);
		FireSpreadAmount = ModifyFloat(shotDef.impact.fireSpreadAmount, "fireSpreadAmount", modifiers);
	}

	private ESpreadPattern ModifySpreadPattern(ESpreadPattern baseMode, String parameterName, List<ModifierDefinition> modifiers)
	{
		for(ModifierDefinition mod : modifiers)
		{
			if(mod.Stat.equals(parameterName))
			{
				if(!mod.SetValue.isEmpty())
				{
					try { return ESpreadPattern.valueOf(mod.SetValue); }
					catch(IllegalArgumentException ignored) { }
				}
			}
		}
		return baseMode;
	}

	private float ModifyFloat(float base, String parameterName, List<ModifierDefinition> modifiers)
	{
		float additives = 0.0f;
		float multiplicatives = 1.0f;

		for(ModifierDefinition mod : modifiers)
		{
			additives += mod.Add;
			multiplicatives *= mod.Multiply;
		}

		return (base + additives) * multiplicatives;
	}
}
