package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class ImpactDefinition
{

	@JsonField
	public float DamageToTarget = 8.0f;
	@JsonField
	public float MultiplierVsPlayers = 1.0f;
	@JsonField
	public float MultiplierVsVehicles = 1.0f;
	@JsonField
	public float Knockback = 0.0f;

	@JsonField
	public float SplashDamageRadius = 0.0f;
	@JsonField
	public float SplashDamageFalloff = 1.0f;

	@JsonField
	public float SetFireToTarget = 0.0f;
	@JsonField
	public float FireSpreadRadius = 0.0f;
	@JsonField
	public float FireSpreadAmount = 0.0f;

	@JsonField
	public String HitSound = "";


}
