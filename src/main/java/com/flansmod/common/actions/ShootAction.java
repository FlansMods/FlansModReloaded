package com.flansmod.common.actions;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.gunshots.*;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ESpreadPattern;
import com.flansmod.common.types.elements.ShotDefinition;
import com.flansmod.common.types.guns.CachedGunStats;
import com.flansmod.common.types.guns.GunContext;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

public class ShootAction extends Action
{
	private GunshotCollection results;

	public ShotDefinition ShotDef() { return actionDef.ShootStats; }

	public ShootAction(ActionDefinition def)
	{
		super(def);
		results = null;
	}

	public boolean CanStart(GunContext context)
	{
		if(context.shootFrom == null)
			return false;
		if(ShotDef() == null)
			return false;

		if(!actionDef.canActUnderwater)
		{
			if(context.shootFrom.level.isWaterAt(new BlockPos(context.shootOrigin.PositionVec3())))
				return false;
		}
		if(!actionDef.canActUnderOtherLiquid)
		{
			if(context.shootFrom.level.isFluidAtPosition(new BlockPos(context.shootOrigin.PositionVec3()), (fluidState) -> { return !fluidState.isSourceOfType(Fluids.WATER); }))
				return false;
		}

		return super.CanStart(context);
	}

	public void SetResults(GunshotCollection shots)
	{
		results = shots;
	}

	public boolean ValidateAndSetResults(GunContext context, GunshotCollection shots)
	{
		results = shots;

		// TODO: Verify that these shots could definitely come from this context

		return true;
	}

	public GunshotCollection GetResults()
	{
		return results;
	}

	private static final double RAYCAST_LENGTH = 500.0d;

	public void Calculate(GunContext context, EActionSet actionSet)
	{
		results = new GunshotCollection()
			.WithOwner(context.owner)
			.WithGun(context.GunDef());

		CachedGunStats stats = context.GetStatBlock(actionSet);

		// Multiplier from https://github.com/FlansMods/FlansMod/blob/71ba7ed065d906d48f34ca471bbd0172b5192f6b/src/main/java/com/flansmod/common/guns/ShotHandler.java#L93
		float bulletSpread = 0.0025f * stats.Spread();
		for(int i = 0; i < stats.Count(); i++)
		{
			Transform randomizedDirection = RandomizeVectorDirection(context.shootFrom.level.random, context.shootOrigin, bulletSpread, stats.SpreadPattern());

			float penetrationPower = stats.PenetrationPower();

			List<HitResult> hits = new ArrayList<HitResult>(8);
			Raytracer.ForLevel(context.shootFrom.level).CastBullet(
				context.shootFrom,
				randomizedDirection.PositionVec3(),
				randomizedDirection.ForwardVec3().scale(RAYCAST_LENGTH),
				penetrationPower,
				penetrationPower,
				hits
			);

			HitResult[] hitArray = new HitResult[hits.size()];
			hits.toArray(hitArray);
			results.AddShot(new Gunshot()
				.WithOrigin(randomizedDirection.PositionVec3())
				.WithTrajectory(randomizedDirection.ForwardVec3().scale(RAYCAST_LENGTH))
				.WithHits(hitArray));
		}
	}

	private Transform RandomizeVectorDirection(RandomSource rand, Transform aim, float spread, ESpreadPattern spreadPattern)
	{
		Transform result = aim.copy();
		Vector3d yAxis = aim.Up();
		Vector3d xAxis = aim.Right();

		switch (spreadPattern)
		{
			case Circle, FilledCircle ->
			{
				float theta = rand.nextFloat() * Maths.TauF;
				float radius = (spreadPattern == ESpreadPattern.Circle ? 1.0f : rand.nextFloat()) * spread;
				float xComponent = radius * Maths.SinF(theta);
				float yComponent = radius * Maths.CosF(theta);

				result.TranslateLocal(xComponent, yComponent, 0.0f);
			}
			case Horizontal ->
			{
				float xComponent = spread * (rand.nextFloat() * 2f - 1f);
				result.TranslateLocal(xComponent, 0f, 0f);
			}
			case Vertical ->
			{
				float yComponent = spread * (rand.nextFloat() * 2f - 1f);
				result.TranslateLocal(0f, yComponent, 0f);
			}
			case Triangle ->
			{
				// Random square, then fold the corners
				float xComponent = rand.nextFloat() * 2f - 1f;
				float yComponent = rand.nextFloat() * 2f - 1f;

				if (xComponent > 0f)
				{
					if (yComponent > 1.0f - xComponent * 2f)
					{
						yComponent = -yComponent;
						xComponent = 1f - xComponent;
					}
				} else
				{
					if (yComponent > xComponent * 2f + 1f)
					{
						yComponent = -yComponent;
						xComponent = -1f - xComponent;
					}
				}

				result.TranslateLocal(spread * xComponent, spread * yComponent, 0d);
			}
			default -> {}
		}
		return result;
	}

	@Override
	protected void OnStartServer(GunContext context)
	{
		super.OnStartServer(context);

		Level level = context.shootFrom.level;
		CachedGunStats stats = context.GetStatBlock(results.actionUsed);

		if(results != null)
		{
			for(Gunshot shot : results.shots)
			{
				for(HitResult hit : shot.hits)
				{
					// Apply damage etc
					switch(hit.getType())
					{
						case BLOCK ->
						{
							if(actionDef.ShootStats.BreaksMaterials.length > 0)
							{
								BlockHitResult blockHit = (BlockHitResult) hit;
								BlockState stateHit = level.getBlockState(blockHit.getBlockPos());
								if(actionDef.ShootStats.BreaksMaterial(stateHit.getMaterial()))
								{
									level.destroyBlock(blockHit.getBlockPos(), true, context.shootFrom);
								}
							}
						}
						case ENTITY ->
						{
							Entity entity = null;
							EPlayerHitArea hitArea = EPlayerHitArea.BODY;
							if(hit instanceof UnresolvedEntityHitResult unresolvedHit)
							{
								entity = level.getEntity(unresolvedHit.EntityID());
								hitArea = unresolvedHit.HitboxArea();
							}
							else if(hit instanceof PlayerHitResult playerHit)
							{
								entity = playerHit.getEntity();
								hitArea = playerHit.GetHitbox().area;
							}
							else if(hit instanceof EntityHitResult entityHit)
							{
								entity = entityHit.getEntity();
							}

							// Damage can be applied to anything living, with special multipliers if it was a player
							float damage = context.BaseDamage(results.actionUsed);
							if(entity instanceof Player player)
							{
								damage *= context.MultiplierVsPlayers(results.actionUsed);
								damage *= hitArea.DamageMultiplier();

								// TODO: Shield item damage multipliers

								player.hurt(context.CreateDamageSource(), damage);
								// We override the immortality cooldown when firing bullets, as it is too slow
								player.hurtTime = 0;
								player.hurtDuration = 0;
							}
							else if(entity instanceof LivingEntity living)
							{
								living.hurt(context.CreateDamageSource(), damage);
								living.hurtTime = 0;
								living.hurtDuration = 0;
							}

							// Fire and similar can be apllied to all entities
							if(entity != null)
							{
								entity.setSecondsOnFire(Maths.Floor(stats.SetFireToTarget() * 20.0f));
							}
						}
					}

					// Apply other impact effects to the surrounding area

				}
			}
		}
	}

	@Override
	protected void OnStartClient(GunContext context)
	{
		super.OnStartClient(context);
		if(results != null)
		{
			for(Gunshot shot : results.shots)
			{
				// Create a bullet trail render
				duration = FlansModClient.SHOT_RENDERER.AddTrail(shot.origin, shot.Endpoint());


			}
		}
	}

	@Override
	protected void OnTickClient(GunContext context)
	{
		float tBefore = progress;
		super.OnTickClient(context);
		float tAfter = progress;

		boolean playedASoundThisTick = false;

		ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;

		for(Gunshot shot : results.shots)
		{
			for (HitResult hit : shot.hits)
			{
				// Check if this hit should be processed on this frame
				double t = Maths.CalculateParameter(shot.origin, shot.Endpoint(), hit.getLocation()) * duration;
				if(tBefore <= t && t < tAfter)
				{
					// Create hit particles
					switch (hit.getType())
					{
						case BLOCK ->
						{
							BlockHitResult blockHit = (BlockHitResult)hit;
							particleEngine.addBlockHitEffects(
								blockHit.getBlockPos(),
								blockHit
							);
						}
						case ENTITY ->
						{
							Vec3 shotMotion = shot.trajectory.normalize().scale(duration);
							particleEngine.createParticle(
								ParticleTypes.DAMAGE_INDICATOR,
								hit.getLocation().x,
								hit.getLocation().y,
								hit.getLocation().z,
								shotMotion.x,
								shotMotion.y,
								shotMotion.z);
						}
					}

					// Play a sound, only once per tick to avoid audio overload
					if(!playedASoundThisTick && actionDef.ShootStats.Impact.HitSound != null)
					{
						playedASoundThisTick = true;
						//Minecraft.getInstance().getSoundManager().play(actionDef.ShootStats.Impact.HitSound);
					}
				}



			}
		}
	}

	public Vec3 GetPlayerMuzzlePosition(GunContext context, int nTicksAgo)
	{
		if(context.shootFrom instanceof Player player)
		{
			PlayerSnapshot snapshot = Raytracer.ForLevel(player.level).GetSnapshot(player, nTicksAgo);
			snapshot.GetMuzzlePosition();
		}
		else if(context.shootFrom instanceof LivingEntity living)
		{
			return living.getEyePosition();
		}
		return context.shootFrom.getEyePosition();

		/*
		ItemStack itemstack = hand == EnumHand.OFF_HAND ? player.getHeldItemOffhand() : player.getHeldItemMainhand();

		if(itemstack.getItem() instanceof ItemGun)
		{
			GunType gunType = ((ItemGun)itemstack.getItem()).GetType();
			AttachmentType barrelType = gunType.getBarrel(itemstack);

			return Vector3f.add(new Vector3f(player.posX, player.posY, player.posZ), snapshot.GetMuzzleLocation(gunType, barrelType, hand), null);
		}
		 */
	}
}
