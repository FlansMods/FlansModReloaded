package com.flansmod.util.collision;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.physics.AngularAcceleration;
import com.flansmod.util.physics.AngularVelocity;
import com.flansmod.util.physics.LinearAcceleration;
import com.flansmod.util.physics.LinearVelocity;
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
	public final Stack<FrameData> Frames = new Stack<>();
	public FrameData PendingFrame = null;

	// Next Frame Inputs

	// In M/Tick
	@Nonnull
	public LinearVelocity NextFrameLinearMotion;
	@Nonnull
	public AngularVelocity NextFrameAngularMotion;
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
		Frames.add(new FrameData(Transform.Copy(initialLocation), LinearVelocity.Zero, AngularVelocity.Zero));
		NextFrameLinearMotion = LinearVelocity.Zero;
		NextFrameAngularMotion = AngularVelocity.Zero;
		NextFrameTeleport = Optional.empty();
		DynamicCollisions = new ArrayList<>();
		StaticCollisions = new ArrayList<>();
	}

	@Nonnull
	public AABB GetCurrentWorldBounds()
	{
		FrameData frame = GetFrameNTicksAgo(0);
		return frame.Location.LocalToGlobalBounds(GetLocalBounds());
	}

	@Nonnull
	public AABB GetSweepTestAABB()
	{
		FrameData frame = GetFrameNTicksAgo(0);
		AABB globalAABB = frame.Location.LocalToGlobalBounds(GetLocalBounds());
		return globalAABB.expandTowards(NextFrameLinearMotion.ApplyOneTick());
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
		NextFrameLinearMotion = NextFrameLinearMotion.add(linearAcceleration.ApplyOneTick());
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
		NextFrameAngularMotion = NextFrameAngularMotion.compose(angularAcceleration.ApplyOneTick());
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
	public void PreTick()
	{
		StaticCollisions.clear();
		DynamicCollisions.clear();
		PendingFrame = ExtrapolateNextFrame();
	}
	public boolean Invalid()
	{
		if(PendingFrame != null)
		{
			if(PendingFrame.Location.HasNaN())
				return true;
			Vec3 pos = PendingFrame.Location.PositionVec3();
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
	@Nonnull
	public FrameData ExtrapolateNextFrame()
	{
		if(NextFrameTeleport.isPresent())
		{
			return new FrameData(NextFrameTeleport.get(), NextFrameLinearMotion, NextFrameAngularMotion);
		}
		else
		{
			FrameData currentFrame = GetFrameNTicksAgo(0);

			Vec3 deltaPos = NextFrameLinearMotion.ApplyOneTick();
			Quaternionf deltaRot = NextFrameAngularMotion.ApplyOneTick();

			Transform newLoc = Transform.FromPosAndQuat(
				currentFrame.Location.PositionVec3().add(deltaPos),
				currentFrame.Location.Orientation.mul(deltaRot, new Quaternionf()),
				() -> "");

			//Transform newLoc = Transform.Compose(
			//	currentFrame.Location,
			//	Transform.FromPosAndQuat(deltaPos, deltaRot, () -> "ExtrapolatePhysicsFrame"));
			return new FrameData(newLoc, NextFrameLinearMotion, NextFrameAngularMotion);
		}
	}
}