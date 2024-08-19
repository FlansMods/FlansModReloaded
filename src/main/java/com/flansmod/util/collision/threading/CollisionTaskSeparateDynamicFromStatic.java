package com.flansmod.util.collision.threading;

import com.flansmod.util.collision.*;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Vector3f;

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

			Pair<Integer, SeparationTestResult> bestSeparator = FindClosestSeparation(boundsA);
			int voxelIndex = bestSeparator.getFirst();
			SeparationTestResult bestSeparationResult = bestSeparator.getSecond();

			if(voxelIndex == -1 || bestSeparationResult == null)
			{
				// Weird, we should find some sort of separation, even if its bad.
				Output = new Output(ImmutableList.of(), ImmutableList.of());
				return;
			}

			ImmutableList<ISeparator> newCollidersImmutable = NewSeparators != null ? ImmutableList.copyOf(NewSeparators) : ImmutableList.of();

			// Non-colliding is nice, off we go
			if(bestSeparationResult.IsNonColliding())
			{
				Output = new Output(ImmutableList.of(), newCollidersImmutable);
				return;
			}

			// So we collided, right? Actually we might be non-colliding if we dig deeper than the bounds
			// TODO: Fine-grain

			VoxelShape shape = Input.StaticShapes.get(voxelIndex);
			TransformedBB voxelBB = TransformedBB.Of(shape.bounds());
			//Vec3 pointOnVoxel = bestSeparationResult.GetCollidingPoint(voxelBB);

			Vec3 sepNormal = bestSeparationResult.Separator().GetNormal();
			Vec3 voxelCenter = voxelBB.GetCenter();
			double d = bestSeparationResult.Separator().GetDistance();
			double cDotV = voxelCenter.dot(sepNormal) - d;
			Vec3 intersectionPoint = voxelCenter.subtract(sepNormal.scale(cDotV));


			//bestSeparationResult.
			//Vec3 pointOnVoxel = voxelBB.GetCenter().add(bestSeparationResult.Separator().GetNormal().scale(bestSeparationResult.Distance()));

			// Collision!
			Output = new Output(ImmutableList.of(
				new StaticCollisionEvent(
					intersectionPoint,
					bestSeparationResult.Separator().GetNormal().scale(-1d),
					bestSeparationResult.GetCollisionDepth())),
				newCollidersImmutable);
		}
	}
	@Nonnull
	private Pair<Integer, SeparationTestResult> FindClosestSeparation(TransformedBB boundsA)
	{
		int shortestIntersectionVoxelIndex = -1;
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
					shortestIntersectionVoxelIndex = i;
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
						shortestIntersectionVoxelIndex = i;
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
					shortestIntersectionVoxelIndex = i;
				}
				if(NewSeparators == null)
					NewSeparators = new ArrayList<>();
				NewSeparators.add(test.Separator());
			}
		}
		return Pair.of(shortestIntersectionVoxelIndex, shortestIntersection);
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
