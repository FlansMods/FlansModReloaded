package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class ImpactDefinition
{
	@JsonField
	public String decal = "";

	@JsonField
	public float damageToTarget = 8.0f;
	@JsonField
	public float multiplierVsPlayers = 1.0f;
	@JsonField
	public float multiplierVsVehicles = 1.0f;
	@JsonField
	public float knockback = 0.0f;
	@JsonField
	public String potionEffectOnTarget = "";

	@JsonField(Docs = "The base amount of damage to apply to targets in the splash damage radius")
	public float splashDamage = 0.0f;
	@JsonField(Docs = "The radius within which to apply splash damage")
	public float splashDamageRadius = 0.0f;
	@JsonField(Docs = "The falloff rate of splash damage. 0=Full damage so long as they are in radius, 1=Scales to 0 at max radius")
	public float splashDamageFalloff = 1.0f;
	@JsonField
	public String potionEffectOnSplash = "";

	@JsonField
	public float setFireToTarget = 0.0f;
	@JsonField
	public float fireSpreadRadius = 0.0f;
	@JsonField
	public float fireSpreadAmount = 0.0f;

	@JsonField
	public float explosionRadius = 0.0f;

	@JsonField
	public SoundDefinition[] hitSounds = new SoundDefinition[0];

	// TODO: Impact Actions
	@JsonField(Docs = "WIP, will be able to apply actions at the point of impact")
	public ActionDefinition[] onImpactActions = new ActionDefinition[0];
}
