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

public class DynamicObject implements IConstDynamicObject
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
		LocalBounds = getLocalBounds();
		Frames.add(new FrameData(Transform.copy(initialLocation), LinearVelocity.Zero, AngularVelocity.Zero));
		NextFrameLinearMotion = LinearVelocity.Zero;
		NextFrameAngularMotion = AngularVelocity.Zero;
		ReactionAcceleration = OffsetAcceleration.Zero;
		NextFrameTeleport = Optional.empty();
		DynamicCollisions = new ArrayList<>();
		StaticCollisions = new ArrayList<>();
	}

	@Override @Nonnull
	public Optional<Transform> getNextFrameTeleport() { return NextFrameTeleport; }
	@Override @Nonnull
	public LinearVelocity getNextFrameLinearVelocity() { return NextFrameLinearMotion; }
	@Override @Nonnull
	public AngularVelocity getNextFrameAngularVelocity() { return NextFrameAngularMotion; }

	@Nonnull
	public AABB getCurrentWorldBounds()
	{
		FrameData frame = getFrameNTicksAgo(0);
		return frame.Location.localToGlobalBounds(getLocalBounds());
	}
	@Nonnull
	public AABB getPendingWorldBounds()
	{
		FrameData frame = PendingFrame;
		return frame.Location.localToGlobalBounds(getLocalBounds());
	}
	@Nonnull
	public AABB getSweepTestAABB()
	{
		FrameData frame = getFrameNTicksAgo(0);
		AABB globalAABB = frame.Location.localToGlobalBounds(getLocalBounds());
		return globalAABB.expandTowards(NextFrameLinearMotion.applyOneTick()).inflate(LocalBounds.getSize());
	}

	@Nonnull
	public AABB getLocalBounds()
	{
		double xMin = Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double zMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double zMax = -Double.MAX_VALUE;
		for(AABB aabb : Colliders)
		{
			xMax = Maths.max(aabb.maxX, xMax);
			yMax = Maths.max(aabb.maxY, yMax);
			zMax = Maths.max(aabb.maxZ, zMax);
			xMin = Maths.min(aabb.minX, xMin);
			yMin = Maths.min(aabb.minY, yMin);
			zMin = Maths.min(aabb.minZ, zMin);
		}
		return new AABB(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	public void setLinearVelocity(@Nonnull LinearVelocity linearVelocity)
	{
		NextFrameLinearMotion = linearVelocity;
	}
	public void addLinearAcceleration(@Nonnull LinearAcceleration linearAcceleration)
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

	public void setAngularVelocity(@Nonnull AngularVelocity angularVelocity)
	{
		NextFrameAngularMotion = angularVelocity;
	}
	public void addAngularAcceleration(@Nonnull AngularAcceleration angularAcceleration)
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
	public void teleportTo(@Nonnull Transform location)
	{
		NextFrameTeleport = Optional.of(location);
	}

	@Nonnull
	private FrameData getFrameNTicksAgo(int n)
	{
		return Frames.get(Frames.size() - n - 1);
	}
	@Nonnull
	public TransformedBBCollection getCurrentColliders()
	{
		return new TransformedBBCollection(getFrameNTicksAgo(0).Location, Colliders);
	}
	@Nonnull
	public TransformedBB getCurrentBB()
	{
		return TransformedBB.Of(getFrameNTicksAgo(0).Location, LocalBounds);
	}
	@Nonnull
	public Transform getCurrentLocation()
	{
		return getFrameNTicksAgo(0).Location;
	}
	@Nonnull
	public TransformedBBCollection getPendingColliders()
	{
		if(PendingFrame != null)
			return new TransformedBBCollection(PendingFrame.Location, Colliders);
		return getCurrentColliders();
	}
	@Nonnull
	public TransformedBB getPendingBB()
	{
		if(PendingFrame != null)
			return TransformedBB.Of(PendingFrame.Location, LocalBounds);
		return getCurrentBB();
	}
	@Nonnull
	public Transform getPendingLocation()
	{
		if(PendingFrame != null)
			return PendingFrame.Location;
		return getCurrentLocation();
	}
	public void preTick()
	{
		StaticCollisions.clear();
		DynamicCollisions.clear();
		extrapolateNextFrame();
	}
	public boolean isInvalid()
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

	public void commitFrame()
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
	public void extrapolateNextFrame(boolean withReactionForce)
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
				var reaction = ReactionAcceleration.applyOneTick(getCurrentLocation());
				deltaPos = deltaPos.add(reaction.getFirst().applyOneTick());
				deltaRot.mul(reaction.getSecond().applyOneTick());
			}

			extrapolateNextFrame(deltaPos, deltaRot);
		}
	}
	public void extrapolateNextFrame(@Nonnull Vec3 deltaPos, @Nonnull Quaternionf deltaRot)
	{
		FrameData currentFrame = getFrameNTicksAgo(0);

		Transform newLoc = Transform.fromPosAndQuat(
			currentFrame.Location.positionVec3().add(deltaPos),
			currentFrame.Location.Orientation.mul(deltaRot, new Quaternionf()));

		//Transform newLoc = Transform.Compose(
		//	currentFrame.Location,
		//	Transform.FromPosAndQuat(deltaPos, deltaRot, () -> "ExtrapolatePhysicsFrame"));
		PendingFrame = new FrameData(newLoc, NextFrameLinearMotion, NextFrameAngularMotion);
	}
	public void extrapolateNextFrame() { extrapolateNextFrame(false); }
	public void extrapolateNextFrameWithReaction() { extrapolateNextFrame(true); }
}