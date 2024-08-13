package com.flansmod.util.collision.threading;

import com.flansmod.util.collision.*;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CollisionTaskSeparateDynamicPair
	implements ICollisionTask<CollisionTaskSeparateDynamicPair.Input, CollisionTaskSeparateDynamicPair.Output>
{
	public record Input(@Nonnull DynamicObject ObjectA,
						@Nonnull DynamicObject ObjectB,
						@Nonnull ImmutableList<ISeparator> ExistingSeparators)
	{

	}
	public record Output(@Nonnull ImmutableList<DynamicCollisionEvent> EventsA,
						 @Nonnull ImmutableList<DynamicCollisionEvent> EventsB,
						 @Nullable ImmutableList<ISeparator> NewSeparatorList)
	{

	}

	@Nonnull
	public static CollisionTaskSeparateDynamicPair of(@Nonnull ColliderHandle handleA,
													  @Nonnull DynamicObject objectA,
													  @Nonnull ColliderHandle handleB,
													  @Nonnull DynamicObject objectB,
													  @Nonnull ImmutableList<ISeparator> existingSeparators)
	{
		CollisionTaskSeparateDynamicPair task = new CollisionTaskSeparateDynamicPair(handleA, handleB);
		task.Prepare(new Input(objectA, objectB, existingSeparators));
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
	public void Prepare(@Nonnull Input input)
	{
		Input = input;
	}
	@Override
	public boolean CanRun()
	{
		return Input != null;
	}
	@Override
	public void Run()
	{
		if(Input != null)
		{
			// Bounds test
			TransformedBB boundsA = Input.ObjectA.GetPendingBB();
			TransformedBB boundsB = Input.ObjectB.GetPendingBB();

			for (ISeparator separator : Input.ExistingSeparators)
			{
				if (separator.SeparatesWithMotion(
					boundsA,
					Input.ObjectA.NextFrameLinearMotion.ApplyOneTick(),
					boundsB,
					Input.ObjectB.NextFrameLinearMotion.ApplyOneTick()))
				{
					// We are separated easily, done.
					Output = new Output(ImmutableList.of(), ImmutableList.of(), null);
					return;
				}
			}

			// So we didn't separate with our existing separators. Maybe we need a new one. Maybe we intersected.
			SeparationTestResult test = CollisionTasks.TestSeparation(boundsA, boundsB);

			// If this test did not collide, then we can just report this new separator as worth keeping
			if(test.IsNonColliding())
			{
				Output = new Output(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(test.Separator()));
				return;
			}

			// So we collided, right? Actually we might be non-colliding if we dig deeper than the bounds
			// TODO: Fine-grain

			Vec3 pointInA = test.GetCollidingPoint(boundsA);
			Vec3 pointInB = test.GetCollidingPoint(boundsB);

			Output = new Output(
				ImmutableList.of(new DynamicCollisionEvent(HandleB, pointInA, test.Separator().GetNormal())),
				ImmutableList.of(new DynamicCollisionEvent(HandleA, pointInB, test.Separator().GetNormal())),
				ImmutableList.of(test.Separator()));
		}
	}

	@Override
	public boolean CanCancel()
	{
		return true;
	}
	@Override
	public void Cancel()
	{
		Cancelled = true;
	}

	@Override
	public boolean IsComplete()
	{
		return Cancelled || Output != null;
	}
	@Nullable
	@Override
	public Output GetResult()
	{
		return Output;
	}


}
