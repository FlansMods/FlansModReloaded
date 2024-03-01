package com.flansmod.common.types.bullets.elements;

import com.flansmod.common.types.Constants;
import com.flansmod.common.types.JsonField;

public class ProjectileDefinition
{
	// Projectile / Hitscan shared settings
	@JsonField(ModifiedBy = Constants.STAT_SHOOT_SHOT_COUNT, Docs = "Number of bullet entities to create", Min = 0, Max = 128)
	public int shotCount = 1;
	@JsonField(ModifiedBy = Constants.STAT_SHOOT_SPLASH_RADIUS, Docs = "The radius within which to apply splash effects. If 0, any Impacts on splash won't trigger")
	public float splashRadius = 0.0f;
	@JsonField(Docs = "Impact settings. You probably want at least a ShotPosition, or ShotEntity and ShotBlock")
	public ImpactDefinition[] impacts = new ImpactDefinition[0];


	// Projectile Settings
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_LAUNCH_SPEED, Docs = "The speed (blocks/s) with which this projectile leaves the gun")
	public float launchSpeed = 3.0f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_GRAVITY_FACTOR, Docs = "How much does gravity affect this? 0=Not at all, 1=Regular")
	public float gravityFactor = 1.0f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_STICKY)
	public boolean sticky = false;

	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_FUSE_TIME, Docs = "If set to a non-zero amount, this projectile will have a fuse timer, in seconds")
	public float fuseTime = 0.0f;

	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_TURN_RATE, Docs = "How quickly a projectile rotates to face the direction of travel")
	public float turnRate = 0.5f;

	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_DRAG_IN_AIR, Docs = "Percent speed loss per tick (1/20s)")
	public float dragInAir = 0.01f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_PARTICLES_IN_AIR)
	public String airParticles = "";
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_DRAG_IN_WATER, Docs = "Percent speed loss per tick (1/20s)")
	public float dragInWater = 0.2f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_PARTICLES_IN_WATER)
	public String waterParticles = "";
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_TIME_BETWEEN_PARTICLES)
	public float timeBetweenTrailParticles = 0.25f;


	public boolean HasSplash() { return splashRadius > 0f; }
}
