package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.physics.common.util.ITransformPair;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.collision.ContinuousSeparationManifold;
import com.flansmod.physics.common.collision.TransformedBB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.List;

import static java.lang.Math.abs;

public class VehicleCollisionSystem
{
	private enum PlayerType
	{
		NOT_A_PLAYER,
		SERVER,
		CLIENT_LOCAL,
		CLIENT_REMOTE,
	}
	private static PlayerType GetAuth(@Nonnull Entity entity)
	{
		if(entity instanceof Player player)
		{
			if(!entity.level().isClientSide)
				return PlayerType.SERVER;
			return entity.isControlledByLocalInstance() ? PlayerType.CLIENT_LOCAL : PlayerType.CLIENT_REMOTE;
		}
		return PlayerType.NOT_A_PLAYER;
	}

	public static void CollideEntities(@Nonnull VehicleEntity vehicle)
	{
		Level world = vehicle.getCommandSenderWorld();

		Transform rootTransform = vehicle.RootTransformCurrent();
		AABB bounds = vehicle.getBoundingBox();

		// Entity AABB (1x1x1) expanded by 2 in all directions, then up to +32??
		List<Entity> entitiesWithinAABB = world.getEntitiesOfClass(Entity.class, bounds.inflate(2)
			.expandTowards(0, 32, 0), vehicle::canCollideWith);

		boolean processedLocalPlayer = false;
		for(Entity entity : entitiesWithinAABB)
		{
			if(!entity.isAlive())
				continue;

			PlayerType playerType = GetAuth(entity);
			switch(playerType)
			{
				case CLIENT_REMOTE -> {
					// TODO: Run on client : saveRemotePlayerFromClipping
				}
				case CLIENT_LOCAL -> {
					if(!processedLocalPlayer)
					{
						CollideWithAuthority(vehicle, entity);
						processedLocalPlayer = true;
					}
				}
				case SERVER -> {
					ResetGroundedTicks(entity);
				}
				case NOT_A_PLAYER -> {
					ResetGroundedTicks(entity);
					CollideWithAuthority(vehicle, entity);
				}
			}
		}
	}

	private static void ResetGroundedTicks(@Nonnull Entity entity)
	{
		// TODO :
		//entity.getSelfAndPassengers()
		//	.forEach(e -> {
		//		if(e instanceof ServerPlayer serverPlayer)
		//			serverPlayer.connection.aboveGroundTickCount = 0;
		//	});
	}

	private static void CollideWithAuthority(@Nonnull VehicleEntity vehicle, @Nonnull Entity entity)
	{
		//vehicle.ForEachCollider((partPath, partTransformPair, partColliders) -> {
		//	CollideWithAuthority(vehicle,
		//		partTransformPair,
		//		Vec3.ZERO, // TODO: Part motion is done on a per-point lookup
		//		partColliders,
		//		entity);
		//});
	}

	private static void CollideWithAuthority(@Nonnull VehicleEntity vehicle,
											 @Nonnull ITransformPair partTransformPair,
											 @Nonnull Vec3 partMotion,
											 @Nonnull List<AABB> partColliders,
											 @Nonnull Entity entity)
	{


		//Matrix3d rotationMatrix = new Matrix3d().fromTransform();

		Transform partTransform = partTransformPair.current();

		// Get the entity pos and motion in vehicle space
		Vec3 entityPosGlobal = entity.position();
		AABB entityBounds = entity.getBoundingBox();
		Vec3 motionGlobal = entity.getDeltaMovement();

		// Transform into part-local space
		// CollisionUtility should support OBB to OBB, but we have an array of BBs at this orientation
		TransformedBB entityTransformLocal = TransformedBB.EntityInSpace(entityBounds, entityPosGlobal, partTransform);
		// Motion is part-motion-relative
		Vec3 motionLocal = partTransform.globalToLocalVelocity(motionGlobal).subtract(partMotion);


		// Apply separation maths
		CollisionResolver resolver = new CollisionResolver(entity,
													       entityTransformLocal,
													       motionLocal);

		// Not sure about this "if(!hasVerticalRotation)" thing in Create. Let's do both passes every time.
		resolver.DoPassAgainst(partColliders, true);
		resolver.DoPassAgainst(partColliders, false);

		Vec3 entityMotion = entity.getDeltaMovement();
		Vec3 entityMotionNoTemporal = entityMotion;
		Vec3 resultNormal = resolver.CollisionNormal;
		Vec3 resultPosition = resolver.CollisionPosition;
		Vec3 totalResponse = resolver.CollisionResponse;
		boolean hardCollision = !totalResponse.equals(Vec3.ZERO);
		boolean temporalCollision = !Maths.Approx(resolver.TemporalResponse, 1f);
		Vec3 motionResponse = temporalCollision ? motionLocal.scale(resolver.TemporalResponse) : motionLocal;

		Vec3 globalMotionResponse = partTransform.localToGlobalVelocity(motionResponse).add(partMotion);
		Vec3 globalTotalResponse = partTransform.localToGlobalVelocity(totalResponse);
		// Not sure we need to do this wacky stuff VecHelper.rotate(yawOffset, Axis.Y)
		Vec3 globalNormal = partTransform.localToGlobalDirection(resultNormal).normalize();
		Vec3 globalPosition = partTransform.localToGlobalPosition(resultPosition);

		//double bounce = 0d;
		double slide = 0d;

		// This is sus. What if it happens at ZERO. ZERO is not a good null
		if(!Maths.Approx(globalPosition, Vec3.ZERO))
		{
			// This is where we get into Collider specifics
			// Create (ContraptionCollider.java) now tests against the block grid
			// They actually seem to go back into local space after converting to global space?

			// Is this ONLY to get bounciness??

			globalPosition = globalPosition.add(entity.position().add(entity.getBoundingBox().getCenter()).scale(0.5f));

			if(temporalCollision)
				globalPosition = globalPosition.add(0, globalMotionResponse.y, 0);


			//BlockPos pos = new BlockPos(vehicle.)
		}

		boolean hasNormal = !globalNormal.equals(Vec3.ZERO);
		boolean anyCollision = hardCollision || temporalCollision;

		// I don't think we need to bounce off a vehicle _yet_

		//if (bounce > 0 && hasNormal && anyCollision
		//	&& bounceEntity(entity, collisionNormal, contraptionEntity, bounce)) {
		//	entity.level.playSound(playerType == PlayerType.CLIENT ? (Player) entity : null, entity.getX(),
		//		entity.getY(), entity.getZ(), SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, .5f, 1);
		//	continue;
		//}

		if(temporalCollision)
		{
			double idealVerticalMotion = globalMotionResponse.y;
			if(Maths.Approx(idealVerticalMotion, entityMotion.y))
			{
				entity.setDeltaMovement(entityMotion.multiply(1d, 0d, 1d).add(0d, idealVerticalMotion, 0d));
				entityMotion = entity.getDeltaMovement();
			}
		}

		if(hardCollision)
		{
			double motionX = entityMotion.x();
			double motionY = entityMotion.y();
			double motionZ = entityMotion.z();
			double intersectX = totalResponse.x();
			double intersectY = totalResponse.y();
			double intersectZ = totalResponse.z();

			double horizonalEpsilon = 1 / 128f;
			if (motionX != 0 && Math.abs(intersectX) > horizonalEpsilon && motionX > 0 == intersectX < 0)
				entityMotion = entityMotion.multiply(0, 1, 1);
			if (motionY != 0 && intersectY != 0 && motionY > 0 == intersectY < 0)
				entityMotion = entityMotion.multiply(1, 0, 1).add(0, partMotion.y, 0);
			if (motionZ != 0 && Math.abs(intersectZ) > horizonalEpsilon && motionZ > 0 == intersectZ < 0)
				entityMotion = entityMotion.multiply(1, 1, 0);
		}

		// if(bounce == 0 &&
		if(slide > 0d && hasNormal && anyCollision)
		{
			double slideFactor = globalNormal.multiply(1, 0, 1)
				.length() * 1.25f;
			Vec3 motionIn = entityMotionNoTemporal.multiply(0, .9, 0)
				.add(0, -.01f, 0);
			Vec3 slideNormal = globalNormal.cross(motionIn.cross(globalNormal))
				.normalize();
			Vec3 newMotion = entityMotion.multiply(.85, 0, .85)
				.add(slideNormal.scale((.2f + slide) * motionIn.length() * slideFactor)
					.add(0, -.1f - globalNormal.y * .125f, 0));
			entity.setDeltaMovement(newMotion);
			entityMotion = entity.getDeltaMovement();
		}

		if (hardCollision || resolver.IsSurfaceCollision)
		{
			Vec3 allowedMovement = ApplyDefaultEntityCollision(globalTotalResponse, entity);
			entity.setPos(
				entityPosGlobal.x + allowedMovement.x,
				entityPosGlobal.y + allowedMovement.y,
				entityPosGlobal.z + allowedMovement.z);
			entityPosGlobal = entity.position();
			//TODO: entityMotion = handleDamageFromTrain

			entity.hurtMarked = true;
			Vec3 contactPointMotion = Vec3.ZERO;

			if(resolver.IsSurfaceCollision)
			{
				// TODO: vehicle.RegisterColliding(entity);
				entity.fallDistance = 0.0f;
				for(Entity rider : entity.getIndirectPassengers())
				{
					//if(GetAuth(rider) == PlayerType.CLIENT_LOCAL)
					// TODO: 	AllPackets.getChannel()
					//			.sendToServer(new ClientMotionPacket(rider.getDeltaMovement(), true, 0));
				}

				boolean canWalk = slide == 0d;
				if(canWalk)
				{
					entity.setOnGround(true);
					if(entity instanceof ItemEntity)
						entityMotion = entityMotion.multiply(0.5f, 1f, 0.5f);
				}

				contactPointMotion = GetMovementOfPointOnPart(globalPosition, partTransformPair);
				allowedMovement = ApplyDefaultEntityCollision(contactPointMotion, entity);
				entity.setPos(
					entityPosGlobal.x + allowedMovement.x,
					entityPosGlobal.y,
					entityPosGlobal.z + allowedMovement.z);

			}

			entity.setDeltaMovement(entityMotion);
			if (GetAuth(entity) == PlayerType.CLIENT_LOCAL)
			{
				double d0 = entity.getX() - entity.xo - contactPointMotion.x;
				double d1 = entity.getZ() - entity.zo - contactPointMotion.z;
				//float limbSwing = Mth.sqrt((float) (d0 * d0 + d1 * d1)) * 4.0F;
				//if (limbSwing > 1.0F)
				//	limbSwing = 1.0F;
				//AllPackets.getChannel()
				//	.sendToServer(new ClientMotionPacket(entityMotion, true, limbSwing));
//
				//if (entity.isOnGround() && contraption instanceof TranslatingContraption) {
				//	safetyLock.setLeft(new WeakReference<>(contraptionEntity));
				//	safetyLock.setRight(entity.getY() - contraptionEntity.getY());
				//}

			}


		}


	}

	@Nonnull
	private static Vec3 GetMovementOfPointOnPart(@Nonnull Vec3 globalPointPrevious, @Nonnull ITransformPair partTransformPair)
	{
		// Not sure why but AbstractContraptionEntity.java takes the globalPoint as a point from last frame
		Vec3 partLocalPos = partTransformPair.previous().globalToLocalPosition(globalPointPrevious);
		Vec3 globalPosCurrent = partTransformPair.current().localToGlobalPosition(partLocalPos);
		return globalPosCurrent.subtract(partLocalPos);
	}

	/** From Entity#collide **/
	private static Vec3 ApplyDefaultEntityCollision(Vec3 p_20273_, Entity e)
	{
		AABB aabb = e.getBoundingBox();
		List<VoxelShape> list = e.level().getEntityCollisions(e, aabb.expandTowards(p_20273_));
		Vec3 vec3 = p_20273_.lengthSqr() == 0.0D ? p_20273_ : Entity.collideBoundingBox(e, p_20273_, aabb, e.level(), list);
		boolean flag = p_20273_.x != vec3.x;
		boolean flag1 = p_20273_.y != vec3.y;
		boolean flag2 = p_20273_.z != vec3.z;
		boolean flag3 = flag1 && p_20273_.y < 0.0D;
		if (e.getStepHeight() > 0.0F && flag3 && (flag || flag2)) {
			Vec3 vec31 = Entity.collideBoundingBox(e, new Vec3(p_20273_.x, (double) e.getStepHeight(), p_20273_.z), aabb, e.level(), list);
			Vec3 vec32 = Entity.collideBoundingBox(e, new Vec3(0.0D, (double) e.getStepHeight(), 0.0D), aabb.expandTowards(p_20273_.x, 0.0D, p_20273_.z), e.level(), list);
			if (vec32.y < (double) e.getStepHeight()) {
				Vec3 vec33 =
					Entity.collideBoundingBox(e, new Vec3(p_20273_.x, 0.0D, p_20273_.z), aabb.move(vec32), e.level(), list)
						.add(vec32);
				if (vec33.horizontalDistanceSqr() > vec31.horizontalDistanceSqr()) {
					vec31 = vec33;
				}
			}

			if (vec31.horizontalDistanceSqr() > vec3.horizontalDistanceSqr()) {
				return vec31.add(Entity.collideBoundingBox(e, new Vec3(0.0D, -vec31.y + p_20273_.y, 0.0D), aabb.move(vec31),
					e.level(), list));
			}
		}

		return vec3;
	}

	private static class CollisionResolver
	{
		@Nonnull public final Entity TestEntity;
		@Nonnull public final Vec3 RelativeMotion;


		@Nonnull public TransformedBB RelativeEntity;
		@Nonnull public Vec3 CollisionResponse;
		@Nonnull public Vec3 CollisionPosition;
		@Nonnull public Vec3 CollisionNormal;
		public boolean IsSurfaceCollision;
		public float TemporalResponse;

		public CollisionResolver(@Nonnull Entity testEntity,
								 @Nonnull TransformedBB relativeEntity,
								 @Nonnull Vec3 relativeMotion)
		{
			TestEntity = testEntity;
			RelativeEntity = relativeEntity;
			RelativeMotion = relativeMotion;
			CollisionPosition = Vec3.ZERO;
			CollisionNormal = Vec3.ZERO;
			CollisionResponse = Vec3.ZERO;
			IsSurfaceCollision = false;
			TemporalResponse = 1.0f;
		}

		public void DoPassAgainst(@Nonnull List<AABB> partAABBs, boolean horizontal)
		{
			for (AABB bb : partAABBs)
			{
				Vec3 currentResponse = CollisionResponse;
				// Offset by the current response
				Vec3 currentCenter = RelativeEntity.GetCenter().add(currentResponse);

				// Not actually sure what these conditions are checking for
				double dX = abs(currentCenter.x - bb.getCenter().x) - RelativeEntity.XSize() - 1;
				if (dX < bb.getXsize() / 2d)
					continue;
				double dY = abs(currentCenter.y + RelativeMotion.y - bb.getCenter().x) - RelativeEntity.YSize() - 1;
				if (dY > bb.getYsize() / 2d)
					continue;
				double dZ = abs(currentCenter.z - bb.getCenter().z) - RelativeEntity.ZSize() - 1;
				if (dZ > bb.getZsize() / 2d)
					continue;

				RelativeEntity = RelativeEntity.Move(t -> t.withPosition(currentCenter));
				ContinuousSeparationManifold intersection = RelativeEntity.Intersect(bb, RelativeMotion);
				if(intersection != null)
				{
					if(!horizontal && !IsSurfaceCollision && intersection.IsSurfaceCollision())
					{
						IsSurfaceCollision = true;
					}

					double tImpact = intersection.GetTimeOfImpact();
					boolean isTemporal = 0 < tImpact && tImpact < 1;

					if(!isTemporal)
					{
						Vec3 separation = intersection.AsSeparationVec(TestEntity.getStepHeight());
						if(separation != null && !separation.equals(Vec3.ZERO))
						{
							CollisionResponse = currentResponse.add(separation);
							tImpact = 0.0f;
						}
					}

					boolean isNearest = 0 <= tImpact && tImpact < TemporalResponse;
					if(isNearest)
					{
						Vec3 collidingNormal = intersection.GetCollisionNormal();
						if(collidingNormal != null)
							CollisionNormal = collidingNormal;

						Vec3 collidingPosition = intersection.GetCollisionPosition();
						if(collidingPosition != null)
							CollisionPosition = collidingPosition;
					}

					if(isTemporal)
					{
						if(tImpact < TemporalResponse)
							TemporalResponse = (float)tImpact;
					}
				}
			}

			if(horizontal)
			{
				boolean noVerticalMotionResponse = TemporalResponse == 1f;
				boolean noVerticalCollision = CollisionResponse.y == 0;
				if(!noVerticalCollision || !noVerticalMotionResponse)
				{
					// Re-run collisions with horizontal offset
					// WHAT?!
					CollisionResponse = CollisionResponse.multiply(129d / 128d, 0, 129d / 128d);
				}
			}
		}
	}
}
