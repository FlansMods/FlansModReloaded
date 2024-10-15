package com.flansmod.physics.common.entity;

import com.flansmod.physics.common.collision.OBBCollisionSystem;
import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.DeltaRingBuffer;
import com.flansmod.physics.common.util.ITransformPair;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.collision.ColliderHandle;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

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


	public double mass = DEFAULT_MASS;
	public Vec3 momentOfInertia = DEFAULT_MOMENT;
	private boolean scheduledUpdate = false;
	private ColliderHandle physicsHandle;

	private static class Frame implements Comparable<Frame>
	{
		public boolean fromAuthority;
		public long gameTick;
		public Transform location;
		public ForcesOnPart forces;
		public LinearVelocity linearVelocity;
		public AngularVelocity angularVelocity;

		public Frame(long tick, @Nonnull Transform loc)
		{
			fromAuthority = false;
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
	}
	private final DeltaRingBuffer<Frame> frameHistory;
	private Frame lastSyncFrame = null;
	private ForcesOnPart pendingForces;

	@Nonnull public ColliderHandle getPhysicsHandle() { return physicsHandle; }

	@Nonnull public ForcesOnPart getCurrentForces() { return getCurrentFrame().forces; }
	@Nonnull public Transform getCurrentTransform() { return getCurrentFrame().location; }
	@Nonnull public LinearVelocity getCurrentLinearVelocity() { return getCurrentFrame().linearVelocity; }
	@Nonnull public AngularVelocity getCurrentAngularVelocity() { return getCurrentFrame().angularVelocity; }

	@Nonnull public ForcesOnPart getPreviousForces() { return getPreviousFrameOrCurrent().forces; }
	@Nonnull public Transform getPreviousTransform() { return getPreviousFrameOrCurrent().location; }
	@Nonnull public LinearVelocity getPreviousLinearVelocity() { return getPreviousFrameOrCurrent().linearVelocity; }
	@Nonnull public AngularVelocity getPreviousAngularVelocity() { return getPreviousFrameOrCurrent().angularVelocity; }

	@Nonnull public ITransformPair pair() { return ITransformPair.of(this::getPreviousTransform, this::getCurrentTransform); }

	public PhysicsComponent()
	{
		physicsHandle = ColliderHandle.invalid;
		frameHistory = new DeltaRingBuffer<>(MAX_HISTORY_FRAMES, new Frame());
		pendingForces = new ForcesOnPart();
	}
	public PhysicsComponent(@Nonnull Transform initialTransform, @Nonnull ColliderHandle handle)
	{
		physicsHandle = handle;
		frameHistory = new DeltaRingBuffer<>(MAX_HISTORY_FRAMES, new Frame(0L, initialTransform));
		pendingForces = new ForcesOnPart();
	}

	public void unregister(@Nonnull OBBCollisionSystem system)
	{
		if(physicsHandle.IsValid())
		{
			system.UnregisterDynamic(getPhysicsHandle());
			physicsHandle = ColliderHandle.invalid;
		}
	}
	public void syncCollisionToComponent(@Nonnull OBBCollisionSystem system)
	{
		Frame pendingFrame = new Frame();

		pendingFrame.gameTick = 0L // TODO: Ticks

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

			Transform newPos = system.ProcessEvents(physicsHandle,
					(collision) -> {
						if(!Maths.Approx(collision.ContactNormal().x, 0d))
							collidedX.set(true);
						if(!Maths.Approx(collision.ContactNormal().y, 0d))
							collidedY.set(true);
						if(!Maths.Approx(collision.ContactNormal().z, 0d))
							collidedZ.set(true);

					},
					(collision) -> {
						if(!Maths.Approx(collision.ContactNormal().x, 0d))
							collidedX.set(true);
						if(!Maths.Approx(collision.ContactNormal().y, 0d))
							collidedY.set(true);
						if(!Maths.Approx(collision.ContactNormal().z, 0d))
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

			pendingFrame.location = newPos;
			boolean changed = frameHistory.addIfChanged(pendingFrame);
			if(changed)
				scheduledUpdate = checkIfUpdateNeeded(pendingFrame);
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
			physics.AddLinearAcceleration(physicsHandle, linear);
			physics.AddAngularAcceleration(physicsHandle, angular);
		}
	}
	@Nonnull
	public Transform getLocation(long gameTick, float partial)
	{
		for(int i = 0; i < MAX_HISTORY_FRAMES; i++)
		{

		}
	}
	@Nonnull
	private Frame getCurrentFrame() { return frameHistory.getMostRecent(); }
	@Nullable
	private Frame getPreviousFrame() { return frameHistory.getOldEntry(1); }
	@Nonnull
	private Frame getPreviousFrameOrCurrent()
	{
		Frame frame = frameHistory.getOldEntry(1);
		return frame == null ? getCurrentFrame() : frame;
	}

	@Nonnull
	private Frame createExtrapolatedFrame(@Nonnull Frame startingFrame, long ticks)
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
	private boolean checkIfUpdateNeeded(@Nonnull Frame newSyncFrame)
	{
		if(lastSyncFrame == null)
			return true;

		if(newSyncFrame.gameTick <= lastSyncFrame.gameTick)
			return false;

		long deltaT = newSyncFrame.gameTick - lastSyncFrame.gameTick;
		Frame clientExtrapolation = createExtrapolatedFrame(lastSyncFrame, deltaT);
		if(!clientExtrapolation.linearVelocity.isApprox(newSyncFrame.linearVelocity, LINEAR_VELOCITY_SYNC_THRESHOLD))
			return true;
		if(!clientExtrapolation.angularVelocity.isApprox(newSyncFrame.angularVelocity, ANGULAR_VELOCITY_SYNC_THRESHOLD))
			return true;
		if(!clientExtrapolation.location.isApprox(newSyncFrame.location, POSITION_SYNC_THRESHOLD, ORIENTATION_SYNC_THRESHOLD, SCALE_SYNC_THRESHOLD))
			return true;

		return false;
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
