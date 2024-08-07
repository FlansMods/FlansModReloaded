package com.flansmod.util.collision;

import com.flansmod.common.FlansMod;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.collision.threading.CollisionTaskSeparateDynamicFromStatic;
import com.flansmod.util.collision.threading.CollisionTaskSeparateDynamicPair;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Dynamic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class OBBCollisionSystem
{
	private static Map<Level, OBBCollisionSystem> Instances = new HashMap<>();
	@Nonnull
	public static OBBCollisionSystem ForLevel(@Nonnull Level level)
	{
		if(!Instances.containsKey(level))
		{
			Instances.put(level, new OBBCollisionSystem(level));
		}
		return Instances.get(level);
	}

	private Map<ColliderHandle, IStaticObject> Statics = new HashMap<>();

	private final CollisionGetter BlockAccess;
	private final Map<ColliderHandle, DynamicObject> Dynamics = new HashMap<>();
	private final List<ColliderHandle> AllHandles = new ArrayList<>();
	private final Map<Pair<ColliderHandle, ColliderHandle>, ImmutableList<ISeparator>> DynamicSeparations = new HashMap<>();
	private final Map<ColliderHandle, ImmutableList<ISeparator>> StaticSeparators = new HashMap<>();
	private long NextID = 1L;


	private final boolean SINGLE_THREAD_DEBUG = true;
	public static ColliderHandle DEBUG_HANDLE = new ColliderHandle(0L);
	public static ColliderHandle CycleDebugHandle(@Nonnull Level level)
	{
		OBBCollisionSystem system = ForLevel(level);
		int index = system.AllHandles.indexOf(DEBUG_HANDLE);
		if(index == system.AllHandles.size() - 1)
		{
			DEBUG_HANDLE = new ColliderHandle(0L);
			return DEBUG_HANDLE;
		}
		else
		{
			DEBUG_HANDLE = system.AllHandles.get(index + 1);
			return DEBUG_HANDLE;
		}
	}
	private final List<CollisionTaskSeparateDynamicPair> DynamicSeparationTasks = new ArrayList<>();
	private final List<CollisionTaskSeparateDynamicFromStatic> StaticSeparationTasks = new ArrayList<>();

	private boolean DoingPhysics = false;
	private final Queue<Runnable> DoAfterPhysics = new ArrayDeque<>();

	public OBBCollisionSystem(@Nonnull Level level)
	{
		BlockAccess = level;
	}

	@Nonnull
	public Collection<DynamicObject> GetDynamics()
	{
		return Dynamics.values();
	}


	private void DoAfterPhysics(@Nonnull Runnable func)
	{
		if(DoingPhysics)
			DoAfterPhysics.add(func);
		else
			func.run();
	}

	@Nonnull
	public ColliderHandle RegisterDynamic(@Nonnull List<AABB> localColliders, @Nonnull Transform initialTransform)
	{
		ColliderHandle handle = new ColliderHandle(NextID);
		DynamicObject dynObj = new DynamicObject(localColliders, initialTransform);
		NextID++;
		DoAfterPhysics(() -> {
			AllHandles.add(handle);
			Dynamics.put(handle, dynObj);
		});
		return handle;
	}
	public void UpdateColliders(@Nonnull ColliderHandle handle, @Nonnull List<AABB> localColliders)
	{
		// TODO:
		//DoAfterPhysics.add(() -> {
		//	Dynamics.put(handle, dynObj);
		//});
	}
	public void ApplyAcceleration(@Nonnull ColliderHandle handle, @Nonnull Vec3 linearMotion)
	{
		DoAfterPhysics(() -> {
			DynamicObject dyn = Dynamics.get(handle);
			if(dyn != null)
			{
				dyn.ApplyAcceleration(linearMotion);
			}
		});
	}
	public void Teleport(@Nonnull ColliderHandle handle, @Nonnull Transform to)
	{
		DoAfterPhysics(() -> {
			DynamicObject dyn = Dynamics.get(handle);
			if(dyn != null)
			{
				dyn.TeleportTo(to);
			}
		});
	}

	@Nonnull
	public Transform ProcessEvents(@Nonnull ColliderHandle handle,
							  @Nonnull Consumer<StaticCollisionEvent> staticFunc,
							  @Nonnull Consumer<DynamicCollisionEvent> dynamicFunc)
	{
		DynamicObject obj = Dynamics.get(handle);
		if(obj != null)
		{
			for(StaticCollisionEvent collision : obj.StaticCollisions)
				staticFunc.accept(collision);
			for(DynamicCollisionEvent collision : obj.DynamicCollisions)
				dynamicFunc.accept(collision);
			return obj.GetCurrentColliders().Location();
		}
		return Transform.IDENTITY;
	}


	//public long RegisterStatic(@Nonnull IStaticObject staticObj)
	//{
	//	NextID++;
	//	Statics.put(NextID, staticObj);
	//	return NextID;
	//}
	//@Nullable
	//public IStaticObject RequestChunk(@Nonnull ChunkPos chunkCoords)
	//{
//
	//}

	public void PreTick()
	{
		for(DynamicObject dyn : Dynamics.values())
		{
			dyn.PreTick();
		}
	}

	public void PhysicsTick()
	{
		PhysicsStep_ApplyMotions();
		PhysicsStep_CreateTasks();

		if(SINGLE_THREAD_DEBUG)
		{
			UnthreadedPhysics_ProcessTasks();
		}

		PhysicsStep_CollectTasks();
	}


	private void PhysicsStep_CacheStaticGeom()
	{

	}

	private void PhysicsStep_CreateTasks()
	{
		for (int i = 0; i < AllHandles.size(); i++)
		{
			ColliderHandle handleA = AllHandles.get(i);
			DynamicObject objectA = Dynamics.get(handleA);
			boolean movingA = objectA.NextFrameLinearMotion.lengthSqr() > Maths.EpsilonSq || objectA.NextFrameAngularMotion.angle() > Maths.EpsilonSq;



			// Check against the static objects
			AABB sweepTestAABB = objectA.GetSweepTestAABB();
			ImmutableList<VoxelShape> worldBBs = GetWorldColliders(sweepTestAABB);
			StaticSeparationTasks.add(CollisionTaskSeparateDynamicFromStatic.of(
				handleA,
				objectA,
				worldBBs,
				StaticSeparators.getOrDefault(handleA, ImmutableList.of())
			));


			// Check against all other dynamic objects
			for (int j = i + 1; j < AllHandles.size(); j++)
			{
				ColliderHandle handleB = AllHandles.get(j);
				DynamicObject objectB = Dynamics.get(handleB);
				boolean movingB = objectB.NextFrameLinearMotion.lengthSqr() > Maths.EpsilonSq || objectB.NextFrameAngularMotion.angle() > Maths.EpsilonSq;

				if(movingA || movingB)
				{
					Pair<ColliderHandle, ColliderHandle> key = ColliderHandle.uniquePairOf(handleA, handleB);

					DynamicSeparationTasks.add(CollisionTaskSeparateDynamicPair.of(
						handleA,
						objectA,
						handleB,
						objectB,
						DynamicSeparations.getOrDefault(key, ImmutableList.of())
					));
				}
			}
		}
	}

	private void UnthreadedPhysics_ProcessTasks()
	{
		for(int i = 0; i < StaticSeparationTasks.size(); i++)
		{
			CollisionTaskSeparateDynamicFromStatic task = StaticSeparationTasks.get(i);
			task.Run();
			if(!task.IsComplete())
				FlansMod.LOGGER.error("SINGLE_THREAD_PHYSICS: Failed to complete static task");
		}

		for(int i = 0; i < DynamicSeparationTasks.size(); i++)
		{
			CollisionTaskSeparateDynamicPair task = DynamicSeparationTasks.get(i);
			task.Run();
			if(!task.IsComplete())
				FlansMod.LOGGER.error("SINGLE_THREAD_PHYSICS: Failed to complete dynamic task");
		}
	}

	private void PhysicsStep_CollectTasks()
	{
		for(int i = 0; i < StaticSeparationTasks.size(); i++)
		{
			CollisionTaskSeparateDynamicFromStatic task = StaticSeparationTasks.get(i);
			if(task.IsComplete())
			{
				DynamicObject object = Dynamics.get(task.Handle);
				if(object != null)
				{
					var output = task.GetResult();
					if(output != null)
					{
						object.Frames.add(object.PendingFrame);
						object.StaticCollisions.addAll(output.EventsA());
						if(output.NewSeparatorList() != null && output.NewSeparatorList().size() > 0)
							StaticSeparators.put(task.Handle, output.NewSeparatorList());
					}
				}
			}
		}
		StaticSeparationTasks.clear();

		for(int i = 0; i < DynamicSeparationTasks.size(); i++)
		{
			CollisionTaskSeparateDynamicPair task = DynamicSeparationTasks.get(i);
			if(task.IsComplete())
			{
				var output = task.GetResult();
				if(output != null)
				{
					DynamicObject objectA = Dynamics.get(task.HandleA);
					if(objectA != null)
					{
						objectA.DynamicCollisions.addAll(output.EventsA());
					}
					DynamicObject objectB = Dynamics.get(task.HandleB);
					if(objectB != null)
					{
						objectB.DynamicCollisions.addAll(output.EventsB());
					}
					if(output.NewSeparatorList() != null && output.NewSeparatorList().size() > 0)
						DynamicSeparations.put(ColliderHandle.uniquePairOf(task.HandleA, task.HandleB), output.NewSeparatorList());
				}
			}
		}
		DynamicSeparationTasks.clear();
	}

	private void PhysicsStep_ApplyMotions()
	{
		//for(int i = AllHandles.size() - 1; i >= 0 ; i--)
		//{
		//	ColliderHandle handle = AllHandles.get(i);
		//	DynamicObject object = Dynamics.get(handle);
		//	if(object != null)
		//	{
		//		object.GenerateNextFrame();
		//	}
		//	else
		//	{
		//		FlansMod.LOGGER.error("Lost a physics handle in the system " + handle.Handle());
		//		AllHandles.remove(i);
		//	}
		//}
	}

	private void Threaded_SeparateSimpleDynamics(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
	{

	}

	@Nonnull
	private ImmutableList<VoxelShape> GetWorldColliders(@Nonnull AABB sweepAABB)
	{
		ImmutableList.Builder<VoxelShape> builder = ImmutableList.builder();

		// Add block shapes
		Iterable<VoxelShape> blockCollisions = BlockAccess.getBlockCollisions(null, sweepAABB);
		builder.addAll(blockCollisions);

		// Add worldborder shapes
		WorldBorder worldborder = BlockAccess.getWorldBorder();
		if(worldborder.getDistanceToBorder(sweepAABB.getCenter().x, sweepAABB.getCenter().z) < sweepAABB.getSize())
		{
			builder.add(worldborder.getCollisionShape());
		}

		return builder.build();
	}

	//private Vec3 SimpleAABBTest(@Nonnull AABB sweepAABB, @Nonnull List<VoxelShape> shapes)
	//{
//
	//}

	//@Nullable
	//private IContinuousSeparator TryCreateSeparator(@Nonnull DynamicObject a, @Nonnull DynamicObject b)
	//{
	//	TODO: With velocity
	//}




	//@Nonnull
	//public static ImmutableList<ISeparator> TryCreateMultiSeparator(@Nonnull DynamicObject a, @Nonnull DynamicObject b, @Nonnull ISeparator bestSingleSeparator)
	//{
	//	ImmutableList.Builder<ISeparator> builder = ImmutableList.builder();
	//	builder.add(bestSingleSeparator);
//
	//	TransformedBBCollection collidersA = a.GetCurrentColliders();
	//	TransformedBBCollection collidersB = b.GetCurrentColliders();
	//	int aCount = collidersA.GetCount();
	//	int bCount = collidersB.GetCount();
//
//
	//	byte[] matrix = new byte[aCount * bCount];
//
	//	// Project the A-boxes onto the best plane
	//	int[] aSide = new int[aCount];
	//	for(int i = 0; i < aCount; i++)
	//	{
	//		TransformedBB boxBB = collidersA.GetColliderBB(i);
	//		// This box is fully above
	//		Pair<Double, Double> projection = bestSingleSeparator.ProjectBoxMinMax(boxBB);
	//		double min = projection.getFirst();
	//		double max = projection.getSecond();
	//		if(max <= 0.0f)
	//			aSide[i] = -1;
	//		else if(min >= 0.0f)
	//			aSide[i] = 1;
	//		else
	//			aSide[i] = 0;
	//	}
//
	//	// Project the B-boxes onto the best plane
	//	int[] bSide = new int[bCount];
	//	for(int j = 0; j < bCount; j++)
	//	{
	//		TransformedBB boxBB = collidersB.GetColliderBB(j);
	//		// This box is fully above
	//		Pair<Double, Double> projection = bestSingleSeparator.ProjectBoxMinMax(boxBB);
	//		double min = projection.getFirst();
	//		double max = projection.getSecond();
	//		if(max <= 0.0f)
	//			bSide[j] = -1;
	//		else if(min >= 0.0f)
	//			bSide[j] = 1;
	//		else
	//			bSide[j] = 0;
	//	}
//
	//	for(int i = 0; i < aCount; i++)
	//	{
	//		for(int j = 0; j < bCount; j++)
	//		{
	//			// If our boxes are on different sides, we good
	//			if((aSide[i] == -1 && bSide[i] == 1) || (aSide[i] == 1 && bSide[i] == -1))
	//			{
	//				matrix[i + j * aCount] = 0;
	//			}
	//		}
	//	}
	//}
}
