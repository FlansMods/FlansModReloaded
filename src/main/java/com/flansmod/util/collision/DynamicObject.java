package com.flansmod.util.collision;

import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class DynamicObject
{
	private static class FrameData
	{
		@Nonnull
		public Transform Location;
		@Nonnull
		public Vec3 LinearMotion;
		@Nonnull
		public Quaternionf AngularMotion;

		public FrameData(@Nonnull Transform loc, @Nonnull Vec3 motion, @Nonnull Quaternionf angular)
		{
			Location = loc;
			LinearMotion = motion;
			AngularMotion = angular;
		}
	}

	@Nonnull
	public final ImmutableList<AABB> Colliders;
	@Nonnull
	public final AABB LocalBounds;
	@Nonnull
	public final Stack<FrameData> Frames = new Stack<>();
	public FrameData PendingFrame = null;

	// Next Frame Inputs
	@Nonnull
	public Vec3 NextFrameLinearMotion;
	@Nonnull
	public Quaternionf NextFrameAngularMotion;
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
		Frames.add(new FrameData(Transform.Copy(initialLocation), Vec3.ZERO, new Quaternionf()));
		NextFrameLinearMotion = Vec3.ZERO;
		NextFrameAngularMotion = new Quaternionf();
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
		return globalAABB.expandTowards(NextFrameLinearMotion);
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

	public void SetLinearMotion(@Nonnull Vec3 linearMotion)
	{
		NextFrameLinearMotion = linearMotion;
	}
	public void ApplyAcceleration(@Nonnull Vec3 linearMotionDelta)
	{
		NextFrameLinearMotion = NextFrameLinearMotion.add(linearMotionDelta);
	}
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
		return new TransformedBBCollection(PendingFrame.Location, Colliders);
	}
	@Nonnull
	public TransformedBB GetPendingBB()
	{
		return TransformedBB.Of(PendingFrame.Location, LocalBounds);
	}
	public void PreTick()
	{
		StaticCollisions.clear();
		DynamicCollisions.clear();
		PendingFrame = ExtrapolateNextFrame();
		NextFrameTeleport = Optional.empty();
	}

	@Nonnull
	private FrameData ExtrapolateNextFrame()
	{
		if(NextFrameTeleport.isPresent())
		{
			return new FrameData(NextFrameTeleport.get(), NextFrameLinearMotion, NextFrameAngularMotion);
		}
		else
		{
			FrameData currentFrame = GetFrameNTicksAgo(0);
			Transform newLoc = Transform.Compose(currentFrame.Location, Transform.FromPosAndQuat(NextFrameLinearMotion, NextFrameAngularMotion, () -> ""));
			return new FrameData(newLoc, NextFrameLinearMotion, NextFrameAngularMotion);
		}
	}
}