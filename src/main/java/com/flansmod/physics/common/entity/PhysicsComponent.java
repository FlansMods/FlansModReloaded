package com.flansmod.physics.common.entity;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.collision.OBBCollisionSystem;
import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.DeltaRingBuffer;
import com.flansmod.physics.common.util.ITransformPair;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.collision.ColliderHandle;
import com.flansmod.physics.network.PhysicsSyncMessage;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriFunction;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class PhysicsComponent
{
	public static final PhysicsComponent invalid = new PhysicsComponent();
	public static final double DEFAULT_MASS = 1.0d;
	public static final Vec3 DEFAULT_MOMENT = new Vec3(1d, 1d, 1d);
	public static final int MAX_HISTORY_FRAMES = 20;

	public static final double LINEAR_VELOCITY_SYNC_THRESHOLD = 0.02d;
	public static final double ANGULAR_VELOCITY_SYNC_THRESHOLD = 1d; // degrees
	public static final double POSITION_SYNC_THRESHOLD = 0.1d;
	public static final float ORIENTATION_SYNC_THRESHOLD = 0.1f;
	public static final float SCALE_SYNC_THRESHOLD = 0.01f;

	private static class Frame implements Comparable<Frame>
	{
		public long gameTick;
		public Transform location;
		public ForcesOnPart forces;
		public LinearVelocity linearVelocity;
		public AngularVelocity angularVelocity;

		public Frame(long tick, @Nonnull Transform loc)
		{
			gameTick = tick;
			location = loc;
			forces = new ForcesOnPart();
			linearVelocity = LinearVelocity.Zero;
			angularVelocity = AngularVelocity.Zero;
		}
		public Frame()
		{
			this(0L, Transform.IDENTITY);
		}

		@Nonnull
		public static Frame interpolate(@Nonnull Frame a, @Nonnull Frame b, float t)
		{
			Frame interp = new Frame();
			interp.linearVelocity = LinearVelocity.interpolate(a.linearVelocity, b.linearVelocity, t);
			interp.angularVelocity = AngularVelocity.interpolate(a.angularVelocity, b.angularVelocity, t);
			interp.location = Transform.interpolate(a.location, b.location, t);
			interp.gameTick = Maths.roundLerp(a.gameTick, b.gameTick, t);
			return interp;
		}
		@Nonnull
		public static Frame extrapolate(@Nonnull Frame startingFrame, long ticks, double mass, @Nonnull Vec3 momentOfInertia)
		{
			Frame extrapolatedFrame = new Frame();
			extrapolatedFrame.gameTick = startingFrame.gameTick + ticks;
			extrapolatedFrame.forces = startingFrame.forces;

			// assume accelerations are constant
			final LinearAcceleration linearA = startingFrame.forces.sumLinearAcceleration(startingFrame.location, mass, false);
			final AngularAcceleration angularA = startingFrame.forces.sumAngularAcceleration(startingFrame.location, momentOfInertia, false);

			// calculate v = u+at
			extrapolatedFrame.linearVelocity = startingFrame.linearVelocity.add(linearA.applyOverTicks(ticks));
			extrapolatedFrame.angularVelocity = startingFrame.angularVelocity.compose(angularA.applyOverTicks(ticks));

			// calculate s = s_0 + ut + 1/2at^2
			Vec3 linearVTerm = startingFrame.linearVelocity.applyOverTicks(ticks);
			Vec3 linearATerm = linearA.applyOverTicks(ticks).scale(0.5d).applyOverTicks(ticks);
			Vec3 pos = startingFrame.location.positionVec3().add(linearVTerm).add(linearATerm);
			Quaternionf angularVTerm = startingFrame.angularVelocity.applyOverTicks(ticks);
			Quaternionf angularATerm = angularA.applyOverTicks(ticks).scale(0.5d).applyOverTicks(ticks);
			Quaternionf ori = new Quaternionf(startingFrame.location.Orientation);
			ori.mul(angularVTerm);
			ori.mul(angularATerm);
			extrapolatedFrame.location = Transform.fromPosAndQuat(pos, ori);
			return extrapolatedFrame;
		}

		@Override
		public int compareTo(@Nonnull PhysicsComponent.Frame other)
		{
			if(isSameLocation(this, other)
			&& isSameForces(this, other)
			&& isSameLinearVelocity(this, other)
			&& isSameAngularVelocity(this, other))
				return 0;

			return Long.compare(gameTick, other.gameTick);
		}

		public static boolean isSameForces(@Nonnull Frame a, @Nonnull Frame b)
		{
			return a.forces.equals(b.forces);
		}
		public static boolean isSameLinearVelocity(@Nonnull Frame a, @Nonnull Frame b)
		{
			return a.linearVelocity.equals(b.linearVelocity);
		}
		public static boolean isSameAngularVelocity(@Nonnull Frame a, @Nonnull Frame b)
		{
			return a.angularVelocity.equals(b.angularVelocity);
		}
		public static boolean isSameLocation(@Nonnull Frame a, @Nonnull Frame b)
		{
			return a.location.equals(b.location);
		}

		@Override @Nonnull
		public String toString()
		{
			return "["+gameTick+"]@"+location+" with ["+linearVelocity+"] and ["+angularVelocity+"]";
		}
	}
	private static class FrameBuilder
	{
		public Frame FrameA;
		public Frame FrameB;
		public final long TargetTick;

		public FrameBuilder(long gameTick)
		{
			TargetTick = gameTick;
		}
		@Nonnull
		public FrameBuilder unknown()
		{
			return this;
		}
		@Nonnull
		public FrameBuilder interpolated(@Nonnull Frame srcA, @Nonnull Frame srcB)
		{
			FrameA = srcA;
			FrameB = srcB;
			return this;
		}
		@Nonnull
		public FrameBuilder extrapolated(@Nonnull Frame src)
		{
			FrameA = src;
			return this;
		}
		@Nonnull
		public FrameBuilder exact(@Nonnull Frame src)
		{
			FrameA = src;
			return this;
		}
		@Nonnull
		public FrameBuilder test(@Nonnull DeltaRingBuffer<Frame> frameHistory)
		{
			Frame before = null, after = null;
			for(Frame frame : frameHistory)
			{
				long delta = TargetTick - frame.gameTick;
				if(delta == 0)
					return exact(frame);

				else if(delta < 0)
				{
					if(before == null || frame.gameTick > before.gameTick)
						before = frame;
				}
				else // delta > 0
				{
					if(after == null || frame.gameTick < after.gameTick)
						after = frame;
				}
			}

			if(before != null && after != null)
				return interpolated(before, after);
			else if(before != null)
				return extrapolated(before);
			else if(after != null)
				return extrapolated(after);
			else
				return unknown();
		}

		public boolean isExact() { return FrameA != null && FrameA.gameTick == TargetTick; }
		public boolean isExtrapolation() { return FrameA != null && FrameB == null && FrameA.gameTick != TargetTick; }
		public boolean isInterpolation() { return FrameA != null && FrameB != null; }

		@Nonnull
		public Optional<Frame> build(boolean allowInterpolation, boolean allowExtrapolation, double mass, @Nonnull Vec3 momentOfInertia)
		{
			if(FrameA == null)
				return Optional.empty();
			if(FrameB == null)
			{
				// Only one source frame, either exact or an extrapolation
				if(FrameA.gameTick == TargetTick)
					return Optional.of(FrameA);
				else if(allowExtrapolation)
					return Optional.of(Frame.extrapolate(FrameA, TargetTick - FrameA.gameTick, mass, momentOfInertia));
				else
					return Optional.empty();
			}
			else
			{
				// Two source frames, this must be interpolation
				if(allowInterpolation)
					return Optional.of(Frame.interpolate(FrameA, FrameB, (TargetTick - FrameA.gameTick) / (float)(FrameB.gameTick - FrameA.gameTick)));
				else
					return Optional.empty();
			}
		}
	}


	public double mass = DEFAULT_MASS;
	public Vec3 momentOfInertia = DEFAULT_MOMENT;
	private boolean scheduledUpdate = false;
	private ColliderHandle physicsHandle;
	private final DeltaRingBuffer<Frame> frameHistory;
	private Frame lastSyncFrame = null;
	private Frame localFrameAtLastReceive = null;
	private ForcesOnPart pendingForces;


	@Nonnull public ColliderHandle getPhysicsHandle() { return physicsHandle; }

	@Nonnull public ForcesOnPart getPendingForces() { return pendingForces; }

	@Nonnull public ForcesOnPart getCurrentForces() { return getMostRecentFrame().forces; }
	@Nonnull public Transform getCurrentTransform() { return getMostRecentFrame().location; }
	@Nonnull public LinearVelocity getCurrentLinearVelocity() { return getMostRecentFrame().linearVelocity; }
	@Nonnull public AngularVelocity getCurrentAngularVelocity() { return getMostRecentFrame().angularVelocity; }

	@Nonnull public ForcesOnPart getPreviousForces() { return getPreviousRecentFrame().forces; }
	@Nonnull public Transform getPreviousTransform() { return getPreviousRecentFrame().location; }
	@Nonnull public LinearVelocity getPreviousLinearVelocity() { return getPreviousRecentFrame().linearVelocity; }
	@Nonnull public AngularVelocity getPreviousAngularVelocity() { return getPreviousRecentFrame().angularVelocity; }

	@Nonnull public ITransformPair pair() { return ITransformPair.of(this::getPreviousTransform, this::getCurrentTransform); }

	public PhysicsComponent()
	{
		physicsHandle = ColliderHandle.invalid;
		frameHistory = new DeltaRingBuffer<>(MAX_HISTORY_FRAMES, new Frame());
		pendingForces = new ForcesOnPart();
	}
	public PhysicsComponent(@Nonnull Transform initialTransform, long gameTick, @Nonnull ColliderHandle handle)
	{
		physicsHandle = handle;
		frameHistory = new DeltaRingBuffer<>(MAX_HISTORY_FRAMES, new Frame(gameTick, initialTransform));
		pendingForces = new ForcesOnPart();
	}

	public void unregister(@Nonnull OBBCollisionSystem system)
	{
		if(physicsHandle.IsValid())
		{
			system.unregisterDynamic(getPhysicsHandle());
			physicsHandle = ColliderHandle.invalid;
		}
	}
	public void syncCollisionToComponent(@Nonnull OBBCollisionSystem system)
	{
		Frame pendingFrame = new Frame();
		pendingFrame.gameTick = system.getGameTick();
		generatePendingFrameFromCollision(system, pendingFrame);
		boolean changed = frameHistory.addIfChanged(pendingFrame);
		if(changed)
			scheduledUpdate = checkIfUpdateNeeded(pendingFrame);
	}
	private void generatePendingFrameFromCollision(@Nonnull OBBCollisionSystem system, @Nonnull Frame target)
	{
		// Sum all the non-reactionary forces of the last frame
		LinearForce impactForce = pendingForces.sumLinearForces(getCurrentTransform(), false);
		pendingForces.endFrame();
		if(physicsHandle.IsValid())
		{
			// Do we need to add these?
			// TODO: If debug, show them?
			AtomicReference<Boolean> collidedX = new AtomicReference<>(false),
					collidedY = new AtomicReference<>(false),
					collidedZ = new AtomicReference<>(false);

			Transform newPos = system.processEvents(physicsHandle,
					(collision) -> {
						if(!Maths.approx(collision.ContactNormal().x, 0d))
							collidedX.set(true);
						if(!Maths.approx(collision.ContactNormal().y, 0d))
							collidedY.set(true);
						if(!Maths.approx(collision.ContactNormal().z, 0d))
							collidedZ.set(true);

					},
					(collision) -> {
						if(!Maths.approx(collision.ContactNormal().x, 0d))
							collidedX.set(true);
						if(!Maths.approx(collision.ContactNormal().y, 0d))
							collidedY.set(true);
						if(!Maths.approx(collision.ContactNormal().z, 0d))
							collidedZ.set(true);
					});
			//LinearVelocity v1 = physics.GetLinearVelocity(part.PhysicsHandle);
			//LinearVelocity v2 = new LinearVelocity(new Vec3(
			//	collidedX.get() ? 0.0d : v1.Velocity().x,
			//	collidedY.get() ? 0.0d : v1.Velocity().y,
			//	collidedZ.get() ? 0.0d : v1.Velocity().z));
			//physics.SetLinearVelocity(part.PhysicsHandle, v2);
			pendingForces.addReactionForce(new LinearForce(new Vec3(
					collidedX.get() ? -impactForce.Force().x : 0.0d,
					collidedY.get() ? -impactForce.Force().y : 0.0d,
					collidedZ.get() ? -impactForce.Force().z : 0.0d)));

			target.location = newPos;
		}
	}
	public void syncComponentToCollision(@Nonnull OBBCollisionSystem physics)
	{
		if(physicsHandle.IsValid())
		{
			LinearAcceleration linear = pendingForces.sumLinearAcceleration(getCurrentTransform(), mass, true);
			AngularAcceleration angular = pendingForces.sumAngularAcceleration(getCurrentTransform(), momentOfInertia, true);
			//float dampening = 1.0f;//part.Forces.GetDampeningRatio();
			//if(dampening < 1.0f)
			//{
			//	linear = linear.subtract(part.GetVelocityMS().scale(1.0f - dampening));
			//}
			physics.addLinearAcceleration(physicsHandle, linear);
			physics.addAngularAcceleration(physicsHandle, angular);
		}
	}

	@Nonnull
	private Optional<Frame> getExactFrame(long gameTick) { return getFrame(gameTick, false, false); }
	@Nonnull
	private Optional<Frame> getInterpolatedFrame(long gameTick) { return getFrame(gameTick, true, false); }
	@Nonnull
	private Optional<Frame> getExtrapolatedFrame(long gameTick) { return getFrame(gameTick, false, true); }
	@Nonnull
	private Optional<Frame> getFrame(long gameTick, boolean allowInterpolation, boolean allowExtrapolation)
	{
		return new FrameBuilder(gameTick).test(frameHistory).build(allowInterpolation, allowExtrapolation, mass, momentOfInertia);
	}
	@Nonnull
	private Frame getMostRecentFrame() { return frameHistory.getMostRecent(); }
	@Nonnull
	private Frame getPreviousRecentFrame()
	{
		// TODO: Have I gone too complex for a simple task? Probably
		return getInterpolatedFrame(frameHistory.getMostRecent().gameTick - 1).orElse(getMostRecentFrame());
	}

	@Nonnull
	public Transform getLocation(long gameTick)
	{
		return interpolate(gameTick, frame -> frame.location, Transform::interpolate, Transform.IDENTITY);
	}
	@Nonnull
	public LinearVelocity getLinearVelocity(long gameTick)
	{
		return interpolate(gameTick, frame -> frame.linearVelocity, LinearVelocity::interpolate, LinearVelocity.Zero);
	}
	@Nonnull
	public AngularVelocity getAngularVelocity(long gameTick)
	{
		return interpolate(gameTick, frame -> frame.angularVelocity, AngularVelocity::interpolate, AngularVelocity.Zero);
	}
	private <T> T interpolate(long gameTick,
							  @Nonnull Function<Frame, T> getFunc,
							  @Nonnull TriFunction<T, T, Float, T> interpFunc,
							  @Nullable T defaultValue)
	{
		Frame before = null, after = null;
		for(Frame frame : frameHistory)
		{
			long delta = gameTick - frame.gameTick;
			if(delta == 0)
				return getFunc.apply(frame);

			else if(delta < 0)
			{
				if(before == null || frame.gameTick > before.gameTick)
					before = frame;
			}
			else // delta > 0
			{
				if(after == null || frame.gameTick < after.gameTick)
					after = frame;
			}
		}

		if(before != null && after != null)
		{
			float blend = (float)(gameTick - before.gameTick) / (float)(after.gameTick - before.gameTick);
			return interpFunc.apply(getFunc.apply(before), getFunc.apply(after), blend);
		}
		else if(before != null)
			return getFunc.apply(before);
		else if(after != null)
			return getFunc.apply(after);
		else
		{
			FlansPhysicsMod.LOGGER.warn("Could not get location of physics component");
			return defaultValue;
		}
	}
	public void teleportTo(@Nonnull OBBCollisionSystem system, @Nonnull Transform newLocation)
	{
		if(physicsHandle.IsValid())
		{
			system.teleport(physicsHandle, newLocation);
		}
		else
		{
			Frame pendingFrame = new Frame();
			pendingFrame.gameTick = system.getGameTick();
			pendingFrame.location = newLocation;
			pendingFrame.linearVelocity = getCurrentLinearVelocity();
			pendingFrame.angularVelocity = getCurrentAngularVelocity();
			pendingFrame.forces = getCurrentForces();

			// Add twice to remove teleport artifacts
			frameHistory.add(pendingFrame);
			frameHistory.add(pendingFrame);
		}
	}

	@Nonnull
	private Frame createExtrapolatedFrame(@Nonnull Frame startingFrame, long ticks)
	{
		return Frame.extrapolate(startingFrame, ticks, mass, momentOfInertia);
	}
	private boolean checkIfUpdateNeeded(@Nonnull Frame newSyncFrame)
	{
		if(lastSyncFrame == null)
			return true;

		if(newSyncFrame.gameTick <= lastSyncFrame.gameTick)
			return false;

		long deltaT = newSyncFrame.gameTick - lastSyncFrame.gameTick;
		Frame remotePredicition = createExtrapolatedFrame(lastSyncFrame, deltaT);
		if(!remotePredicition.linearVelocity.isApprox(newSyncFrame.linearVelocity, LINEAR_VELOCITY_SYNC_THRESHOLD))
			return true;
		if(!remotePredicition.angularVelocity.isApprox(newSyncFrame.angularVelocity, ANGULAR_VELOCITY_SYNC_THRESHOLD))
			return true;
		if(!remotePredicition.location.isApprox(newSyncFrame.location, POSITION_SYNC_THRESHOLD, ORIENTATION_SYNC_THRESHOLD, SCALE_SYNC_THRESHOLD))
			return true;

		return false;
	}
	public void receiveUpdate(long gameTick, @Nonnull PhysicsSyncMessage.PhysicsStateChange syncMessage)
	{
		lastSyncFrame = new Frame();
		lastSyncFrame.location = syncMessage.Location != null ? syncMessage.Location : getLocation(gameTick);
		lastSyncFrame.linearVelocity = syncMessage.LinearVelocityUpdate != null ? syncMessage.LinearVelocityUpdate : getLinearVelocity(gameTick);
		lastSyncFrame.angularVelocity = syncMessage.AngularVelocityUpdate != null ? syncMessage.AngularVelocityUpdate : getAngularVelocity(gameTick);
		lastSyncFrame.gameTick = gameTick;
		localFrameAtLastReceive = getMostRecentFrame();
	}
	public static long REMOTE_LERP_CONVERGE_TICKS = 3;
	public void syncAndLerpToComponent(@Nonnull OBBCollisionSystem system)
	{
		Frame pendingFrame = new Frame();
		pendingFrame.gameTick = system.getGameTick();

		if(lastSyncFrame != null && localFrameAtLastReceive != null)
		{
			long remoteT = lastSyncFrame.gameTick;
			long receiveT = localFrameAtLastReceive.gameTick;
			//long maxT = Maths.max(receiveT, remoteT);
			long convergeT = receiveT + REMOTE_LERP_CONVERGE_TICKS;

			if(pendingFrame.gameTick <= convergeT)
			{
				float blend = 1.0f - Maths.clamp((float)(convergeT - pendingFrame.gameTick) / (float)REMOTE_LERP_CONVERGE_TICKS, 0f, 1f);
				blendExtrapolated(localFrameAtLastReceive, lastSyncFrame, blend, pendingFrame.gameTick, pendingFrame);
				frameHistory.addIfChanged(pendingFrame);
				return;
			}
		}

		// So either we don't have net sync data OR we are past the point where our old data is relevant
		generatePendingFrameFromCollision(system, pendingFrame);
		frameHistory.addIfChanged(pendingFrame);
	}
	private void blendExtrapolated(@Nonnull Frame a, @Nonnull Frame b, float t, long targetTime, @Nonnull Frame target)
	{
		long ticksSinceA = targetTime - a.gameTick;
		long ticksSinceB = targetTime - b.gameTick;
		blend(createExtrapolatedFrame(a, ticksSinceA), createExtrapolatedFrame(b, ticksSinceB), t, target);
	}
	private void blend(@Nonnull Frame a, @Nonnull Frame b, float t, @Nonnull Frame target)
	{
		target.location = Transform.interpolate(a.location, b.location, t);
		target.linearVelocity = LinearVelocity.interpolate(a.linearVelocity, b.linearVelocity, t);
		target.angularVelocity = AngularVelocity.interpolate(a.angularVelocity, b.angularVelocity, t);
	}
	public void forceUpdate()
	{
		scheduledUpdate = true;
	}
	public boolean needsUpdate()
	{
		return scheduledUpdate;
	}
}
