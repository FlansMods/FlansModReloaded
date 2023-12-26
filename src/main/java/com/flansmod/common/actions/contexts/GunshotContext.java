package com.flansmod.common.actions.contexts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.*;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.ESpreadPattern;
import com.flansmod.util.Maths;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.registries.ForgeRegistries;


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

	public boolean IsValid() { return ActionGroup.IsValid() && Bullet.IsValid(); }

	public void Server_ProcessImpact(Level level, Gunshot shotData)
	{
		for (HitResult hit : shotData.hits)
		{
			Entity targetEntity = null;

			HitResult toProcess = hit;
			if(hit instanceof UnresolvedEntityHitResult unresolved)
			{
				Entity entity = level.getEntity(unresolved.EntityID());
				if(entity != null)
				{
					toProcess = new EntityHitResult(entity);
				}
			}

			// Apply damage etc
			switch (toProcess.getType())
			{
				case BLOCK -> {
					if (Bullet.shootStats.breaksBlockTags.length > 0)
					{
						BlockHitResult blockHit = (BlockHitResult) toProcess;
						BlockState stateHit = level.getBlockState(blockHit.getBlockPos());
						if (Bullet.shootStats.BreaksBlock(stateHit))
						{
							level.destroyBlock(blockHit.getBlockPos(), true, ActionGroup.Gun.GetShooter().Entity());
						}
					}
				}
				case ENTITY -> {
					EPlayerHitArea hitArea = EPlayerHitArea.BODY;
					if (toProcess instanceof UnresolvedEntityHitResult unresolvedHit)
					{
						targetEntity = level.getEntity(unresolvedHit.EntityID());
						hitArea = unresolvedHit.HitboxArea();
					} else if (toProcess instanceof PlayerHitResult playerHit)
					{
						targetEntity = playerHit.getEntity();
						hitArea = playerHit.GetHitbox().area;
					} else if (toProcess instanceof EntityHitResult entityHit)
					{
						targetEntity = entityHit.getEntity();
					}

					ApplyDamageToEntity(targetEntity, hitArea, shotData.trajectory, 1.0f, true);
				}
			}

			ActionGroup.Gun.GetActionStack().CheckAbilities_Hit(ActionGroup.Gun, toProcess);

			float splashRadius = SplashDamageRadius();
			if(splashRadius > 0.0f)
			{
				Vec3 center = toProcess.getLocation();
				Vec3 halfExtents = new Vec3(splashRadius, splashRadius, splashRadius);
				for(Entity splashEntity : level.getEntities(targetEntity, new AABB(center.subtract(halfExtents), center.add(halfExtents))))
				{
					double distance = Maths.Sqrt(splashEntity.distanceToSqr(center));
					if(distance <= splashRadius)
					{
						float splashMultiplier = (float) Maths.Lerp(1.0f, 1.0f - SplashDamageFalloff(), distance / splashRadius);
						ApplyDamageToEntity(splashEntity, EPlayerHitArea.BODY, shotData.trajectory, splashMultiplier, false);
					}
				}
			}

			float explosionRadius = ExplosionRadius();
			if(explosionRadius > 0.0f)
			{
				level.explode(null, toProcess.getLocation().x, toProcess.getLocation().y, toProcess.getLocation().z, explosionRadius, Level.ExplosionInteraction.TNT);
			}

			float fireSpreadRadius = FireSpreadRadius();
			float fireSpreadAmount = FireSpreadAmount();
			if(fireSpreadRadius > 0.0f && fireSpreadAmount > 0.0f)
			{
				for(int i = Maths.Floor(-fireSpreadRadius); i <= fireSpreadRadius; i++)
				{
					for(int j = Maths.Floor(-fireSpreadRadius); j <= fireSpreadRadius; j++)
					{
						for(int k = Maths.Floor(-fireSpreadRadius); k <= fireSpreadRadius; k++)
						{
							if(level.random.nextFloat() < fireSpreadAmount)
							{
								BlockPos pos = BlockPos.containing(toProcess.getLocation().add(i, j, k));
								if (pos.distToCenterSqr(toProcess.getLocation()) <= fireSpreadRadius * fireSpreadRadius)
								{
									if(level.getBlockState(pos).isAir())
									{
										if(!level.getBlockState(pos.below()).isAir()
											|| !level.getBlockState(pos.north()).isAir()
											|| !level.getBlockState(pos.east()).isAir()
											|| !level.getBlockState(pos.south()).isAir()
											|| !level.getBlockState(pos.west()).isAir())
										{
											level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void ApplyDamageToEntity(Entity entity, EPlayerHitArea hitArea, Vec3 impactDirection, float splashMultiplier, boolean isTarget)
	{
		if(entity == null)
			return;

		// Damage can be applied to anything living, with special multipliers if it was a player
		float damage = isTarget ? ImpactDamage() : SplashDamage();
		damage *= splashMultiplier;
		if (entity instanceof Player player)
		{
			damage *= MultiplierVsPlayers();
			damage *= hitArea.DamageMultiplier();

			// TODO: Shield item damage multipliers

			player.hurt(ActionGroup.Gun.CreateDamageSource(), damage);
			// We override the immortality cooldown when firing bullets, as it is too slow
			player.hurtTime = 0;
			player.hurtDuration = 0;
		} else if (entity instanceof LivingEntity living)
		{
			living.hurt(ActionGroup.Gun.CreateDamageSource(), damage);
			living.hurtTime = 0;
			living.hurtDuration = 0;
			living.invulnerableTime = 0;
		}

		// Also apply this code to all living entities
		if(entity instanceof LivingEntity living)
		{
			String potionEffect = PotionEffectOnTarget();
			if(potionEffect.length() > 0)
			{
				String[] parts = potionEffect.split(",");
				if(parts.length > 0)
				{
					int strength = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
					int duration = parts.length >= 2 ? Integer.parseInt(parts[1]) : 20;
					ResourceLocation resLoc = new ResourceLocation(parts[0]);
					MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(resLoc);
					if(effect != null)
					{
						MobEffectInstance instance = new MobEffectInstance(effect, duration, strength);
						living.addEffect(instance);
					}
				}
			}
		}

		// Fire and similar can be apllied to all entities
		float setFire = SetFireToTarget();
		if(setFire > 0.0f)
			entity.setSecondsOnFire(Maths.Floor(setFire * 20.0f));

		// Knockback
		float knockback = Knockback();
		if(knockback > 0.0f)
		{
			if(impactDirection.y < 0.0f)
				impactDirection = new Vec3(impactDirection.x, -impactDirection.y, impactDirection.z);
			impactDirection = impactDirection.scale(knockback);
			entity.addDeltaMovement(impactDirection);
		}
	}

	// --------------------------------------------------------------------------
	// STAT CACHE
	// --------------------------------------------------------------------------
	public void Apply(ModifierStack modStack)
	{
		ActionGroup.Apply(modStack);
	}
	public float ModifyFloat(String key, float baseValue)
	{
		ModifierStack stack = new ModifierStack(key, ActionGroup.GroupPath);
		Apply(stack);
		return stack.ApplyTo(baseValue);
	}
	public String ModifyString(String key, String defaultValue)
	{
		ModifierStack stack = new ModifierStack(key, ActionGroup.GroupPath);
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

	public float SplashDamage() 			{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_SPLASH_DAMAGE, Bullet.shootStats.impact.splashDamage); }
	public float SplashDamageRadius() 		{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_SPLASH_DAMAGE_RADIUS, Bullet.shootStats.impact.splashDamageRadius); }
	public float SplashDamageFalloff() 		{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_SPLASH_DAMAGE_FALLOFF, Bullet.shootStats.impact.splashDamageFalloff); }
	public String PotionEffectOnSplash()	{ return ModifyString(ModifierDefinition.STAT_IMPACT_POTION_EFFECT_ON_SPLASH, Bullet.shootStats.impact.potionEffectOnSplash); }

	public float FireSpreadRadius() 		{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_FIRE_SPREAD_RADIUS, Bullet.shootStats.impact.fireSpreadRadius); }
	public float FireSpreadAmount() 		{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_FIRE_SPREAD_AMOUNT, Bullet.shootStats.impact.fireSpreadAmount); }

	public float ExplosionRadius() 			{ return ModifyFloat(ModifierDefinition.STAT_IMPACT_EXPLOSION_RADIUS, Bullet.shootStats.impact.explosionRadius); }

	// UTIL
	public void Save(CompoundTag tags)
	{
		CompoundTag actionGroupTags = new CompoundTag();
		ActionGroup.Save(actionGroupTags);
		tags.put("action", actionGroupTags);
		tags.putInt("bullet", Bullet.hashCode());
	}

	public static GunshotContext Load(CompoundTag tags, boolean client)
	{
		BulletDefinition bullet = FlansMod.BULLETS.ByHash(tags.getInt("bullet"));
		ActionGroupContext actionGroup = ActionGroupContext.Load(tags.getCompound("action"), client);
		if(actionGroup.IsValid())
			return new GunshotContext(actionGroup, bullet);

		// If we don't have a action group context, there's no point making a bullet context
		return GunshotContext.INVALID;
	}
}

