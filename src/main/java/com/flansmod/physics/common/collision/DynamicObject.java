package com.flansmod.physics.common.collision;

import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class DynamicObject
{
	public static final int MAX_HISTORY = 20;
	public static final int KILL_VOLUME_NEGATIVE_Y = -256;
	public static final int KILL_VOLUME_POSITIVE_Y = Short.MAX_VALUE;

	private record FrameData(@Nonnull Transform Location,
							 @Nonnull LinearVelocity linearVelocity,
							 @Nonnull AngularVelocity angularVelocity)
	{

	}

	@Nonnull
	public final ImmutableList<AABB> Colliders;
	@Nonnull
	public final AABB LocalBounds;
	@Nonnull
	private final Stack<FrameData> Frames = new Stack<>();
	private FrameData PendingFrame = null;

	// Next Frame Inputs

	// In M/Tick
	@Nonnull
	public LinearVelocity NextFrameLinearMotion;
	@Nonnull
	public AngularVelocity NextFrameAngularMotion;
	@Nonnull
	public OffsetAcceleration ReactionAcceleration;
	@Nonnull
	public Optional<Transform> NextFrameTeleport;
	@Nonnull
	public final List<DynamicCollisionEvent> DynamicCollisions;
	@Nonnull
	public final List<StaticCollisionEvent> StaticCollisions;



	public DynamicObject(@Nonnull List<AABB> localColliders, @Nonnull Transform initialLocation)
	{
		ImmutableList.Builder<AABB> builder = ImmutableList.builder();
		Colliders = builder.addAll(localColliders).build();
		LocalBounds = GetLocalBounds();
		Frames.add(new FrameData(Transform.copy(initialLocation), LinearVelocity.Zero, AngularVelocity.Zero));
		NextFrameLinearMotion = LinearVelocity.Zero;
		NextFrameAngularMotion = AngularVelocity.Zero;
		ReactionAcceleration = OffsetAcceleration.Zero;
		NextFrameTeleport = Optional.empty();
		DynamicCollisions = new ArrayList<>();
		StaticCollisions = new ArrayList<>();
	}

	@Nonnull
	public AABB GetCurrentWorldBounds()
	{
		FrameData frame = GetFrameNTicksAgo(0);
		return frame.Location.localToGlobalBounds(GetLocalBounds());
	}

	@Nonnull
	public AABB GetSweepTestAABB()
	{
		FrameData frame = GetFrameNTicksAgo(0);
		AABB globalAABB = frame.Location.localToGlobalBounds(GetLocalBounds());
		return globalAABB.expandTowards(NextFrameLinearMotion.applyOneTick()).inflate(LocalBounds.getSize());
	}

	@Nonnull
	public AABB GetLocalBounds()
	{
		double xMin = Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double zMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double zMax = -Double.MAX_VALUE;
		for(AABB aabb : Colliders)
		{
			xMax = Maths.Max(aabb.maxX, xMax);
			yMax = Maths.Max(aabb.maxY, yMax);
			zMax = Maths.Max(aabb.maxZ, zMax);
			xMin = Maths.Min(aabb.minX, xMin);
			yMin = Maths.Min(aabb.minY, yMin);
			zMin = Maths.Min(aabb.minZ, zMin);
		}
		return new AABB(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	public void SetLinearVelocity(@Nonnull LinearVelocity linearVelocity)
	{
		NextFrameLinearMotion = linearVelocity;
	}
	public void AddLinearAcceleration(@Nonnull LinearAcceleration linearAcceleration)
	{
		NextFrameLinearMotion = NextFrameLinearMotion.add(linearAcceleration.applyOneTick());
	}
	//public void AddOneTickOfLinearAcceleration(@Nonnull LinearAcceleration linearMotionDelta)
	//{
	//	NextFrameLinearMotion = Maths.Clamp(
	//		NextFrameLinearMotion.add(linearMotionDelta),
	//		-OBBCollisionSystem.MAX_LINEAR_BLOCKS_PER_TICK,
	//		OBBCollisionSystem.MAX_LINEAR_BLOCKS_PER_TICK);
	//}

	public void SetAngularVelocity(@Nonnull AngularVelocity angularVelocity)
	{
		NextFrameAngularMotion = angularVelocity;
	}
	public void AddAngularAcceleration(@Nonnull AngularAcceleration angularAcceleration)
	{
		NextFrameAngularMotion = NextFrameAngularMotion.compose(angularAcceleration.applyOneTick());
	}
	//public void AddAngularAccelerationPerTick(@Nonnull AxisAngle4f angularMotionDelta)
	//{
	//	Quaternionf nextFrameQ = new Quaternionf().set(NextFrameAngularMotion);
	//	Quaternionf deltaQ = new Quaternionf().set(angularMotionDelta);
	//	nextFrameQ.mul(deltaQ);
	//	// TODO: If AngularMotion.angle > Pi, does quaternion composition not have the chance of going backwards?
	//	nextFrameQ.get(NextFrameAngularMotion);
	//}
	public void TeleportTo(@Nonnull Transform location)
	{
		NextFrameTeleport = Optional.of(location);
	}

	@Nonnull
	private FrameData GetFrameNTicksAgo(int n)
	{
		return Frames.get(Frames.size() - n - 1);
	}
	@Nonnull
	public TransformedBBCollection GetCurrentColliders()
	{
		return new TransformedBBCollection(GetFrameNTicksAgo(0).Location, Colliders);
	}
	@Nonnull
	public TransformedBB GetCurrentBB()
	{
		return TransformedBB.Of(GetFrameNTicksAgo(0).Location, LocalBounds);
	}
	@Nonnull
	public Transform GetCurrentLocation()
	{
		return GetFrameNTicksAgo(0).Location;
	}
	@Nonnull
	public TransformedBBCollection GetPendingColliders()
	{
		if(PendingFrame != null)
			return new TransformedBBCollection(PendingFrame.Location, Colliders);
		return GetCurrentColliders();
	}
	@Nonnull
	public TransformedBB GetPendingBB()
	{
		if(PendingFrame != null)
			return TransformedBB.Of(PendingFrame.Location, LocalBounds);
		return GetCurrentBB();
	}
	@Nonnull
	public Transform GetPendingLocation()
	{
		if(PendingFrame != null)
			return PendingFrame.Location;
		return GetCurrentLocation();
	}
	public void PreTick()
	{
		StaticCollisions.clear();
		DynamicCollisions.clear();
		ExtrapolateNextFrame();
	}
	public boolean Invalid()
	{
		if(PendingFrame != null)
		{
			if(PendingFrame.Location.hasNaN())
				return true;
			Vec3 pos = PendingFrame.Location.positionVec3();
			if(pos.y < KILL_VOLUME_NEGATIVE_Y || pos.y > KILL_VOLUME_POSITIVE_Y)
			{
				return true;
			}
		}
		return false;
	}

	public void CommitFrame()
	{
		if (Frames.size() >= MAX_HISTORY)
			Frames.remove(0);

		// If we are teleporting, double push so the "last" frame is the same
		if(NextFrameTeleport.isPresent())
		{
			Frames.push(PendingFrame);
			Frames.push(PendingFrame);
		}
		else //if(!VehicleEntity.PAUSE_PHYSICS)
		{
			Frames.push(PendingFrame);
		}

		NextFrameTeleport = Optional.empty();
	}
	public void ExtrapolateNextFrame(boolean withReactionForce)
	{
		if(NextFrameTeleport.isPresent())
		{
			PendingFrame = new FrameData(NextFrameTeleport.get(), NextFrameLinearMotion, NextFrameAngularMotion);
		}
		else
		{
			Vec3 deltaPos = NextFrameLinearMotion.applyOneTick();
			Quaternionf deltaRot = NextFrameAngularMotion.applyOneTick();

			if(withReactionForce)
			{
				var reaction = ReactionAcceleration.applyOneTick(GetCurrentLocation());
				deltaPos = deltaPos.add(reaction.getFirst().applyOneTick());
				deltaRot.mul(reaction.getSecond().applyOneTick());
			}

			ExtrapolateNextFrame(deltaPos, deltaRot);
		}
	}
	public void ExtrapolateNextFrame(@Nonnull Vec3 deltaPos, @Nonnull Quaternionf deltaRot)
	{
		FrameData currentFrame = GetFrameNTicksAgo(0);

		Transform newLoc = Transform.fromPosAndQuat(
			currentFrame.Location.positionVec3().add(deltaPos),
			currentFrame.Location.Orientation.mul(deltaRot, new Quaternionf()),
			() -> "");

		//Transform newLoc = Transform.Compose(
		//	currentFrame.Location,
		//	Transform.FromPosAndQuat(deltaPos, deltaRot, () -> "ExtrapolatePhysicsFrame"));
		PendingFrame = new FrameData(newLoc, NextFrameLinearMotion, NextFrameAngularMotion);
	}
	public void ExtrapolateNextFrame() { ExtrapolateNextFrame(false); }
	public void ExtrapolateNextFrameWithReaction() { ExtrapolateNextFrame(true); }
}