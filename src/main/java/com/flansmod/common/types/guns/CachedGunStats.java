package com.flansmod.common.types.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.ShotDefinition;

import java.util.List;

public class CachedGunStats
{
	public float VerticalRecoil() 				{ return VerticalRecoil; }
	public float HorizontalRecoil() 			{ return HorizontalRecoil; }
	public float Spread() 						{ return Spread; }
	public float Speed() 						{ return Speed; }
	public int Count() 							{ return Count; }
	public float TimeToNextShot() 				{ return TimeToNextShot; }
	public float PenetrationPower() 			{ return PenetrationPower; }
	public float BaseDamage() 					{ return BaseDamage; }
	public float Knockback() 					{ return Knockback; }
	public float MultiplierVsPlayers() 			{ return MultiplierVsPlayers; }
	public float MultiplierVsVehicles() 		{ return MultiplierVsVehicles; }
	public ESpreadPattern SpreadPattern()		{ return SpreadPattern; }
	public float SplashDamageRadius()			{ return SplashDamageRadius; }
	public float SplashDamageFalloff()			{ return SplashDamageFalloff; }
	public float SetFireToTarget()				{ return SetFireToTarget; }
	public float FireSpreadRadius()				{ return FireSpreadRadius; }
	public float FireSpreadAmount()				{ return FireSpreadAmount; }


	private float VerticalRecoil;
	private float HorizontalRecoil;
	private float Spread;
	private float Speed;
	private int Count;
	private float TimeToNextShot;
	private float PenetrationPower;
	private float BaseDamage;
	private float Knockback;
	private float MultiplierVsPlayers;
	private float MultiplierVsVehicles;
	private float SplashDamageRadius;
	private float SplashDamageFalloff;
	private float SetFireToTarget;
	private float FireSpreadRadius;
	private float FireSpreadAmount;
	private ESpreadPattern SpreadPattern;

	public void InitializeFrom(ShotDefinition shotDef)
	{
		VerticalRecoil = shotDef.verticalReocil;
		HorizontalRecoil = shotDef.horizontalRecoil;
		Spread = shotDef.spread;
		Speed = shotDef.speed;
		Count = shotDef.bulletCount;
		TimeToNextShot = shotDef.timeToNextShot;
		PenetrationPower = shotDef.penetrationPower;
		BaseDamage = shotDef.impact.damageToTarget;
		Knockback = shotDef.impact.knockback;
		MultiplierVsPlayers = shotDef.impact.multiplierVsPlayers;
		MultiplierVsVehicles = shotDef.impact.multiplierVsVehicles;
		SpreadPattern = shotDef.spreadPattern;
		SplashDamageRadius = shotDef.impact.splashDamageRadius;
		SplashDamageFalloff = shotDef.impact.splashDamageFalloff;
		SetFireToTarget = shotDef.impact.setFireToTarget;
		FireSpreadRadius = shotDef.impact.fireSpreadRadius;
		FireSpreadAmount = shotDef.impact.fireSpreadAmount;

	}

	// The expectation when you call this is that you are passing all the applicable modifiers at once.
	// Calling again will apply again, with unexpected results
	public void ApplyModifiers(String shotStat, List<ModifierDefinition> modifiers)
	{
		float additives = 0.0f;
		float multiplicatives = 1.0f;
		String setValue = "";

		for(ModifierDefinition mod : modifiers)
		{
			additives += mod.Add;
			multiplicatives *= mod.Multiply;
			if(!mod.SetValue.isEmpty())
				setValue = mod.SetValue;
		}

		switch (shotStat)
		{
			case "VerticalRecoil" 			-> VerticalRecoil = (VerticalRecoil + additives) * multiplicatives;
			case "HorizontalRecoil" 		-> HorizontalRecoil = (HorizontalRecoil + additives) * multiplicatives;
			case "Spread" 					-> Spread = (Spread + additives) * multiplicatives;
			case "Speed" 					-> Speed = (Speed + additives) * multiplicatives;
			case "Count" 					-> Count = (int)((Count + additives) * multiplicatives);
			case "TimeToNextShot" 			-> TimeToNextShot = (TimeToNextShot + additives) * multiplicatives;
			case "PenetrationPower" 		-> PenetrationPower = (PenetrationPower + additives) * multiplicatives;
			case "BaseDamage" 				-> BaseDamage = (BaseDamage + additives) * multiplicatives;
			case "Knockback" 				-> Knockback = (Knockback + additives) * multiplicatives;
			case "MultiplierVsPlayers" 		-> MultiplierVsPlayers = (MultiplierVsPlayers + additives) * multiplicatives;
			case "MultiplierVsVehicles" 	-> MultiplierVsVehicles = (MultiplierVsVehicles + additives) * multiplicatives;
			case "SplashDamageRadius" 		-> SplashDamageRadius = (SplashDamageRadius + additives) * multiplicatives;
			case "SplashDamageFalloff"	 	-> SplashDamageFalloff = (SplashDamageFalloff + additives) * multiplicatives;
			case "SetFireToTarget" 			-> SetFireToTarget = (SetFireToTarget + additives) * multiplicatives;
			case "FireSpreadRadius" 		-> FireSpreadRadius = (FireSpreadRadius + additives) * multiplicatives;
			case "FireSpreadAmount" 		-> FireSpreadAmount = (FireSpreadAmount + additives) * multiplicatives;

			case "SpreadPattern" 			-> SpreadPattern = ESpreadPattern.valueOf(setValue);
			default							-> FlansMod.LOGGER.warn("Unknown stat: " + shotStat + " applied to gun");
		}
	}
}
