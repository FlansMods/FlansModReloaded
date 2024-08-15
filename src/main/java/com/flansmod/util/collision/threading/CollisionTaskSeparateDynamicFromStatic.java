package com.flansmod.util.collision.threading;

import com.flansmod.util.collision.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CollisionTaskSeparateDynamicFromStatic
	implements ICollisionTask<CollisionTaskSeparateDynamicFromStatic.Input, CollisionTaskSeparateDynamicFromStatic.Output>
{
	public record Input(
						@Nonnull DynamicObject ObjectA,
						@Nonnull ImmutableList<VoxelShape> StaticShapes,
						@Nonnull ImmutableList<ISeparator> ExistingSeparators)
	{

	}
	public record Output(@Nonnull ImmutableList<StaticCollisionEvent> EventsA,
						 @Nullable ImmutableList<ISeparator> NewSeparatorList)
	{

	}


	@Nonnull
	public static CollisionTaskSeparateDynamicFromStatic of(@Nonnull ColliderHandle handleA,
															@Nonnull DynamicObject objectA,
															@Nonnull ImmutableList<VoxelShape> staticShapes,
															@Nonnull ImmutableList<ISeparator> existingSeparators)
	{
		CollisionTaskSeparateDynamicFromStatic task = new CollisionTaskSeparateDynamicFromStatic(handleA);
		task.Prepare(new CollisionTaskSeparateDynamicFromStatic.Input(objectA, staticShapes, existingSeparators));
		return task;
	}

	@Nonnull
	public final ColliderHandle Handle;
	@Nullable
	private Input Input;
	@Nullable
	private List<ISeparator> NewSeparators = null;
	@Nullable
	private Output Output;
	private boolean Cancelled = false;


	public CollisionTaskSeparateDynamicFromStatic(@Nonnull ColliderHandle handle)
	{
		Handle = handle;
	}
	@Override
	public void Prepare(@Nonnull Input input)
	{
		Input = input;
	}
	@Override
	public boolean CanRun()
	{
		return Input != null && Input.StaticShapes.size() > 0;
	}
	@Override
	public void Run()
	{
		if(Input != null)
		{
			// No colliders?! No collision
			if(Input.StaticShapes.size() == 0)
			{
				Output = new Output(ImmutableList.of(), ImmutableList.of());
				return;
			}

			// Bounds test
			TransformedBB boundsA = Input.ObjectA.GetPendingBB();

			SeparationTestResult objectToStaticSeparation = FindClosestSeparation(boundsA);
			if(objectToStaticSeparation == null)
			{
				// Weird, we should find some sort of separation, even if its bad.
				Output = new Output(ImmutableList.of(), ImmutableList.of());
				return;
			}

			ImmutableList<ISeparator> newCollidersImmutable = NewSeparators != null ? ImmutableList.copyOf(NewSeparators) : ImmutableList.of();

			// Non-colliding is nice, off we go
			if(objectToStaticSeparation.IsNonColliding())
			{
				Output = new Output(ImmutableList.of(), newCollidersImmutable);
				return;
			}

			// So we collided, right? Actually we might be non-colliding if we dig deeper than the bounds
			// TODO: Fine-grain

			;

			// Collision!
			Output = new Output(ImmutableList.of(
				new StaticCollisionEvent(
					objectToStaticSeparation.GetCollidingPoint(boundsA),
					objectToStaticSeparation.Separator().GetNormal(),
					objectToStaticSeparation.GetCollisionDepth())),
				newCollidersImmutable);
		}
	}
	@Nullable
	private SeparationTestResult FindClosestSeparation(TransformedBB boundsA)
	{
		SeparationTestResult shortestIntersection = null;
		double shortestIntersectionDist = Double.MAX_VALUE;

		for(int i = 0; i < Input.StaticShapes.size(); i++)
		{
			VoxelShape shape = Input.StaticShapes.get(i);
			TransformedBB aabb = TransformedBB.Of(shape.bounds());
			boolean isSeparated = false;

			for(ISeparator separator : Input.ExistingSeparators)
			{
				double sepDistance = separator.GetSeparationDistance(aabb, boundsA);
				if(sepDistance < shortestIntersectionDist)
				{
					shortestIntersection = new SeparationTestResult(separator, sepDistance);
					shortestIntersectionDist = sepDistance;
				}
				if (sepDistance >= 0.0f)
				{
					isSeparated = true;
					break;
				}
			}
			if(NewSeparators != null)
			{
				for (ISeparator separator : NewSeparators)
				{
					double sepDistance = separator.GetSeparationDistance(aabb, boundsA);
					if (sepDistance < shortestIntersectionDist)
					{
						shortestIntersection = new SeparationTestResult(separator, sepDistance);
						shortestIntersectionDist = sepDistance;
					}
					if (sepDistance >= 0.0f)
					{
						isSeparated = true;
						break;
					}
				}
			}

			if(!isSeparated)
			{
				SeparationTestResult test = CollisionTasks.TestSeparation(aabb, boundsA);
				if(test.Distance() < shortestIntersectionDist)
				{
					shortestIntersection = test;
					shortestIntersectionDist = test.Distance();
				}
				if(NewSeparators == null)
					NewSeparators = new ArrayList<>();
				NewSeparators.add(test.Separator());
			}
		}
		return shortestIntersection;
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
