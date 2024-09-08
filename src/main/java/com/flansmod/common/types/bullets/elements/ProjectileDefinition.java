package com.flansmod.common.types.bullets.elements;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.projectiles.BulletGuidance;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.JsonField;
import com.mojang.datafixers.kinds.Const;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;

import java.util.ArrayList;
import java.util.List;

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
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_ACCELERATION, Docs = "Acceleration of the projectile in blocks/s per tick")
	public float acceleration = 0.0f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_MAX_SPEED, Docs = "The maximum linear speed of the projectile")
	public float maxSpeed = 3.0f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_GRAVITY_FACTOR, Docs = "How much does gravity affect this? 0=Not at all, 1=Regular")
	public float gravityFactor = 1.0f;

	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_RESPONSE_TO_BLOCK)
	public EProjectileResponseType responseToBlock = EProjectileResponseType.Bounce;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_RESPONSE_TO_ENTITY)
	public EProjectileResponseType responseToEntity = EProjectileResponseType.Detonate;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_RESPONSE_TO_VEHICLE)
	public EProjectileResponseType responseToVehicle = EProjectileResponseType.Detonate;

	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_FUSE_TIME, Docs = "If set to a non-zero amount, this projectile will have a fuse timer, in seconds")
	public float fuseTime = 0.0f;

	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_GUIDANCE, Docs = "The guidance mode of the projectile. none/beam_riding/lock_on/lock_on_proportional")
	public String guidanceType = "none";
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_LOCK_RANGE, Docs = "The size of the cone in which the projectile can lock onto a new target")
	public float lockRange = 128f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_LOCK_CONE, Docs = "The size of the cone in which the projectile can lock onto a new target")
	public float lockCone = 15f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_TRACK_CONE, Docs = "The size of the cone in which the projectile can track a locked-on target")
	public float trackCone = 70f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_LOCK_TIME, Docs = "The size of the cone in which the projectile can track a locked-on target")
	public float lockTime = 20f;
	@JsonField(ModifiedBy = Constants.STAT_PROJECTILE_TURN_RATE, Docs = "How quickly a projectile rotates to face the direction of travel")
	public float turnRate = 0.1f;
	@JsonField(Docs = "Altitude gained above target during top attack mode")
	public float topAttackHeight = 10;
	@JsonField(Docs = "Lateral distance where missile will directly track target during top attack mode")
	public float topAttackRange = 10;
	@JsonField(Docs = "What types of entity this can target")
	public String[] targetTypes = new String[0];

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

	public BulletGuidance.GuidanceType GetGuidanceMode()
	{
		return BulletGuidance.GuidanceType(guidanceType);
	}

	public boolean HasLockOn()
	{
		return BulletGuidance.HasLockOn(guidanceType);
	}

	public ArrayList<Class> Targets()
	{
		ArrayList<Class> result = new ArrayList<Class>();

		for(String s:targetTypes) //Surely there's a better way to do this but oh well
		{
			switch(s) {
				case "flyingMob" 	-> {
					result.add(FlyingMob.class);
					result.add(EnderDragon.class);
					result.add(WitherBoss.class);
					result.add(Blaze.class);
				}
				case "monster" 		-> {
					result.add(Monster.class);
					result.add(Raider.class);
				}
				case "vehicle" 		-> result.add(VehicleEntity.class);
				case "player" 		-> result.add(Player.class);
				case "living" 		-> result.add(LivingEntity.class);
			}
		}
		return result;
	}
}
