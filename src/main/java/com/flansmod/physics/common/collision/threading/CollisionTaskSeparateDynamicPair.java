package com.flansmod.physics.common.collision.threading;

import com.flansmod.physics.common.collision.*;
import com.flansmod.physics.common.util.shapes.ISeparationAxis;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class CollisionTaskSeparateDynamicPair
	implements ICollisionTask<CollisionTaskSeparateDynamicPair.Input, CollisionTaskSeparateDynamicPair.Output>
{
	public record Input(@Nonnull IConstDynamicObject ObjectA,
						@Nonnull IConstDynamicObject ObjectB,
						@Nonnull ImmutableList<ISeparationAxis> ExistingSeparators)
	{

	}
	public record Output(@Nonnull ImmutableList<DynamicCollisionEvent> EventsA,
						 @Nonnull ImmutableList<DynamicCollisionEvent> EventsB,
						 @Nullable ImmutableList<ISeparationAxis> NewSeparatorList)
	{

	}

	@Nonnull
	public static CollisionTaskSeparateDynamicPair of(@Nonnull ColliderHandle handleA,
													  @Nonnull IConstDynamicObject objectA,
													  @Nonnull ColliderHandle handleB,
													  @Nonnull IConstDynamicObject objectB,
													  @Nonnull ImmutableList<ISeparationAxis> existingSeparators)
	{
		CollisionTaskSeparateDynamicPair task = new CollisionTaskSeparateDynamicPair(handleA, handleB);
		task.prepare(new Input(objectA, objectB, existingSeparators));
		return task;
	}

	@Nonnull
	public final ColliderHandle HandleA;
	@Nonnull
	public final ColliderHandle HandleB;
	@Nullable
	private Input Input;
	@Nullable
	private Output Output;
	private boolean Cancelled = false;

	public CollisionTaskSeparateDynamicPair(@Nonnull ColliderHandle handleA, @Nonnull ColliderHandle handleB)
	{
		HandleA = handleA;
		HandleB = handleB;
	}

	@Override
	public void prepare(@Nonnull Input input)
	{
		Input = input;
	}
	@Override
	public boolean canRun()
	{
		return Input != null;
	}
	@Override
	public void run()
	{
		if(Input != null)
		{
			// Bounds test
			TransformedBB boundsA = Input.ObjectA.getPendingBB();
			TransformedBB boundsB = Input.ObjectB.getPendingBB();

			for (ISeparationAxis separator : Input.ExistingSeparators)
			{
				if (separator.SeparatesWithMotion(
					boundsA,
					Input.ObjectA.getNextFrameLinearVelocity().applyOneTick(),
					boundsB,
					Input.ObjectB.getNextFrameLinearVelocity().applyOneTick()))
				{
					// We are separated easily, done.
					Output = new Output(ImmutableList.of(), ImmutableList.of(), null);
					return;
				}
			}

			// So we didn't separate with our existing separators. Maybe we need a new one. Maybe we intersected.
			SeparationResult test = CollisionTasks.separate(boundsA, boundsB);

			// If this test did not collide, then we can just report this new separator as worth keeping
			if(test.success())
			{
				Output = new Output(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(test.separator()));
				return;
			}

			// So we collided, right? Actually we might be non-colliding if we dig deeper than the bounds
			// TODO: Fine-grain

			Optional<Vec3> collidingPoint = test.separator().LinePlaneIntersect(boundsA.GetCenter(), boundsB.GetCenter());
			Vec3 pointInB = collidingPoint.orElse(boundsB.GetCenter());
			//Vec3 pointInB = test.GetCollidingPoint(boundsB);

			Output = new Output(
				ImmutableList.of(new DynamicCollisionEvent(HandleB, pointInB, test.separator().GetNormal().scale(-1d), test.depth())),
				ImmutableList.of(new DynamicCollisionEvent(HandleA, pointInB, test.separator().GetNormal(), test.depth())),
				ImmutableList.of(test.separator()));
		}
	}

	@Override
	public boolean canCancel()
	{
		return true;
	}
	@Override
	public void cancel()
	{
		Cancelled = true;
	}

	@Override
	public boolean isComplete()
	{
		return Cancelled || Output != null;
	}
	@Nullable
	@Override
	public Output getResult()
	{
		return Output;
	}


}
