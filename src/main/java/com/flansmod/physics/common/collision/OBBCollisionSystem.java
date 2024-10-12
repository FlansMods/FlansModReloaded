package com.flansmod.physics.common.collision;

import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.ProjectedRange;
import com.flansmod.physics.common.util.ProjectionUtil;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.collision.threading.CollisionTaskSeparateDynamicFromStatic;
import com.flansmod.physics.common.collision.threading.CollisionTaskSeparateDynamicPair;
import com.flansmod.physics.common.util.shapes.ISeparationAxis;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
	private final List<ColliderHandle> InvalidatedHandles = new ArrayList<>();
	private final Map<Pair<ColliderHandle, ColliderHandle>, ImmutableList<ISeparationAxis>> DynamicSeparations = new HashMap<>();
	private final Map<ColliderHandle, ImmutableList<ISeparationAxis>> StaticSeparators = new HashMap<>();
	private long NextID = 1L;

	public static final double MAX_LINEAR_BLOCKS_PER_TICK = 60d;
	public static final double MAX_ANGULAR_MOTION_PER_TICK = 60d;

	public static boolean PAUSE_PHYSICS = false;
	public static boolean DEBUG_SETTING_ONLY_LINEAR_REACTIONS = false;

	private final boolean SINGLE_THREAD_DEBUG = true;
	public static ColliderHandle DEBUG_HANDLE = new ColliderHandle(0L);
	public static ColliderHandle Debug_CycleInspectHandle(@Nonnull Level level, int delta)
	{
		OBBCollisionSystem system = ForLevel(level);
		int index = system.AllHandles.indexOf(DEBUG_HANDLE);
		int newIndex = index + delta;

		if(newIndex >= system.AllHandles.size() || newIndex < 0)
			DEBUG_HANDLE = new ColliderHandle(0L);
		else
			DEBUG_HANDLE = system.AllHandles.get(newIndex);

		return DEBUG_HANDLE;
	}
	public static ColliderHandle Debug_SetInspectHandleIndex(@Nonnull Level level, int index)
	{
		OBBCollisionSystem system = ForLevel(level);

		if(index >= system.AllHandles.size() || index < 0)
			DEBUG_HANDLE = new ColliderHandle(0L);
		else
			DEBUG_HANDLE = system.AllHandles.get(index);

		return DEBUG_HANDLE;
	}
	public static ColliderHandle Debug_SetNearestInspectHandle(@Nonnull Level level, @Nonnull Vec3 pos)
	{
		double closestDistSq = Double.MAX_VALUE;
		ColliderHandle closest = new ColliderHandle(0L);
		OBBCollisionSystem system = ForLevel(level);
		for(ColliderHandle handle : system.AllHandles)
		{
			if(handle.IsValid())
			{
				DynamicObject dynamic = system.Dynamics.get(handle);
				if(dynamic != null)
				{
					double distSq = dynamic.GetCurrentLocation().positionVec3().distanceToSqr(pos);
					if(distSq < closestDistSq) {
						closestDistSq = distSq;
						closest = handle;
					}
				}
			}
		}
		if(closest.IsValid())
			DEBUG_HANDLE = closest;
		else
			DEBUG_HANDLE = new ColliderHandle(0L);

		return DEBUG_HANDLE;
	}
	public static int Debug_GetNumHandles(@Nonnull Level level)
	{
		OBBCollisionSystem system = ForLevel(level);
		return system.AllHandles.size();
	}
	private final List<CollisionTaskSeparateDynamicPair> DynamicSeparationTasks = new ArrayList<>();
	private final List<CollisionTaskSeparateDynamicFromStatic> StaticSeparationTasks = new ArrayList<>();

	private boolean DoingPhysics = false;
	private final Queue<Runnable> DoAfterPhysics = new ArrayDeque<>();

	private static final int PHYSICS_LOCK_MS_TIMEOUT = 16;
	private final Lock DynamicsLock = new ReentrantLock();
	private final Lock TasksLock = new ReentrantLock();

	public OBBCollisionSystem(@Nonnull Level level)
	{
		BlockAccess = level;
	}

	private boolean LockDynamics(int msTimeout)
	{
		int msElapsed = 0;
		for(;;)
		{
			if(DynamicsLock.tryLock())
			{
				DoingPhysics = true;
				return true;
			}

			msElapsed++;
			if(msElapsed >= msTimeout)
				return false;

			try
			{
				Thread.sleep(1);
			} catch (InterruptedException ignored)
			{
			}
		}
	}
	private boolean TryLockDynamics()
	{
		return LockDynamics(0);
	}
	private void UnlockDynamics()
	{
		DoingPhysics = false;
		DynamicsLock.unlock();
	}

	public boolean WaitForEachDynamic(@Nonnull Consumer<DynamicObject> func, int msTimeout)
	{
		if(LockDynamics(msTimeout))
		{
			try
			{
				for(DynamicObject dyn : Dynamics.values())
					func.accept(dyn);
			}
			finally
			{
				UnlockDynamics();
			}
			return true;
		}
		return false;
	}
	public boolean TryForEachDynamic(@Nonnull Consumer<DynamicObject> func)
	{
		return WaitForEachDynamic(func, 0);
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
	public void UnregisterDynamic(@Nonnull ColliderHandle handle)
	{
		DoAfterPhysics(() -> {
			Dynamics.remove(handle);
			AllHandles.remove(handle);
			InvalidatedHandles.remove(handle);
		});
	}
	public void UpdateColliders(@Nonnull ColliderHandle handle, @Nonnull List<AABB> localColliders)
	{
		// TODO:
		//DoAfterPhysics.add(() -> {
		//	Dynamics.put(handle, dynObj);
		//});
	}
	public void AddLinearAcceleration(@Nonnull ColliderHandle handle, @Nonnull LinearAcceleration linearAcceleration)
	{
		DoAfterPhysics(() -> {
			DynamicObject dyn = Dynamics.get(handle);
			if(dyn != null)
				dyn.AddLinearAcceleration(linearAcceleration);
		});
	}
	@Nonnull
	public LinearVelocity GetLinearVelocity(@Nonnull ColliderHandle handle)
	{
		DynamicObject dyn = Dynamics.get(handle);
		if(dyn != null)
			return dyn.NextFrameLinearMotion;
		return LinearVelocity.Zero;
	}
	public void SetLinearVelocity(@Nonnull ColliderHandle handle, @Nonnull LinearVelocity linearVelocity)
	{
		DoAfterPhysics(() -> {
			DynamicObject dyn = Dynamics.get(handle);
			if(dyn != null)
				dyn.SetLinearVelocity(linearVelocity);
		});
	}
	public void AddAngularAcceleration(@Nonnull ColliderHandle handle, @Nonnull AngularAcceleration angularAcceleration)
	{
		DoAfterPhysics(() -> {
			DynamicObject dyn = Dynamics.get(handle);
			if(dyn != null)
				dyn.AddAngularAcceleration(angularAcceleration);
		});
	}
	public void SetAngularVelocity(@Nonnull ColliderHandle handle, @Nonnull AngularVelocity angularVelocity)
	{
		DoAfterPhysics(() -> {
			DynamicObject dyn = Dynamics.get(handle);
			if(dyn != null)
				dyn.SetAngularVelocity(angularVelocity);
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


	public boolean IsHandleInvalidated(@Nonnull ColliderHandle handle)
	{
		return InvalidatedHandles.contains(handle);
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
		if(LockDynamics(PHYSICS_LOCK_MS_TIMEOUT))
		{
			try
			{

			}
			finally
			{
				UnlockDynamics();
			}
		}
		else
		{
			FlansMod.LOGGER.warn("Physics could not lock after " + PHYSICS_LOCK_MS_TIMEOUT + "ms");
		}
	}

	public void PhysicsTick()
	{
		if(LockDynamics(PHYSICS_LOCK_MS_TIMEOUT))
		{
			try
			{
				List<ColliderHandle> expiredHandles = new ArrayList<>();
				for (var kvp : Dynamics.entrySet())
				{
					kvp.getValue().PreTick();
					if(kvp.getValue().Invalid())
					{
						expiredHandles.add(kvp.getKey());
					}
				}
				for(ColliderHandle handle : expiredHandles)
				{
					InvalidatedHandles.add(handle);
					AllHandles.remove(handle);
					Dynamics.remove(handle);
				}

				PhysicsStep_CreateTasks();

				if(SINGLE_THREAD_DEBUG)
				{
					UnthreadedPhysics_ProcessTasks();
				}

				PhysicsStep_CollectTasks();
				PhysicsStep_ResolveTransforms();

				while(!DoAfterPhysics.isEmpty())
				{
					DoAfterPhysics.remove().run();
				}
			}
			finally
			{
				UnlockDynamics();
			}
		}
		else
		{
			FlansMod.LOGGER.warn("Physics could not lock after " + PHYSICS_LOCK_MS_TIMEOUT + "ms");
		}
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
			if(objectA == null)
				continue;

			boolean movingA = !objectA.NextFrameLinearMotion.isApproxZero() || !objectA.NextFrameAngularMotion.isApproxZero();

			// Check against the static objects
			AABB sweepTestAABB = objectA.GetSweepTestAABB();
			ImmutableList<VoxelShape> worldBBs = GetWorldColliders(sweepTestAABB);
			if(!worldBBs.isEmpty())
			{
				StaticSeparationTasks.add(CollisionTaskSeparateDynamicFromStatic.of(
					handleA,
					objectA,
					worldBBs,
					StaticSeparators.getOrDefault(handleA, ImmutableList.of())
				));

				//if(DEBUG_HANDLE.Handle() == handleA.Handle())
				//{
				//	for(VoxelShape shape : worldBBs) {
				//		Transform pos = Transform.FromPos(shape.bounds().getCenter());
				//		Vector3f half = new Vector3f((float)shape.bounds().getXsize()*0.5f, (float)shape.bounds().getYsize()*0.5f, (float)shape.bounds().getZsize()*0.5f);
				//		DebugRenderer.RenderCube(pos, 3, new Vector4f(1.0f, 0.8f, 0.6f, 1.0f), half);
				//	}
				//}
			}



			// Check against all other dynamic objects
			for (int j = i + 1; j < AllHandles.size(); j++)
			{
				ColliderHandle handleB = AllHandles.get(j);
				DynamicObject objectB = Dynamics.get(handleB);
				if(objectB == null)
					continue;
				boolean movingB = !objectB.NextFrameLinearMotion.isApproxZero() || !objectB.NextFrameAngularMotion.isApproxZero();

				if (movingA || movingB)
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
						object.StaticCollisions.addAll(output.EventsA());
						if(output.NewSeparatorList() != null && !output.NewSeparatorList().isEmpty())
						{
							StaticSeparators.put(task.Handle, output.NewSeparatorList());

							if(DEBUG_HANDLE.Handle() == task.Handle.Handle())
							{
								List<VoxelShape> statics = new ArrayList<>(task.Debug_GetInput().StaticShapes());
								float blue = 1.0f;
								for(ISeparationAxis axis : output.NewSeparatorList())
								{
									Vec3 normal = axis.GetNormal();
									Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);

									TransformedBB obb = object.GetPendingBB();
									ProjectedRange range = axis.ProjectOBBMinMax(obb);
									double center = axis.Project(obb.GetCenter());

									Transform pos = Transform.fromPositionAndLookDirection(obb.GetCenter().add(normal.scale(range.max() - center)), normal, up);
									DebugRenderer.RenderArrow(pos, 3, new Vector4f(1.0f, 1.0f, blue, 1.0f), new Vec3(0d, 0d, -1d));

									for(int j = statics.size() - 1; j >= 0; j--)
									{
										AABB aabb = statics.get(j).bounds();
										ProjectedRange boxProj = axis.ProjectAABBMinMax(aabb);
										if(ProjectionUtil.SeparatedAThenB(range, boxProj))
										{
											Transform staticPos = Transform.fromPos(aabb.getCenter());
											DebugRenderer.RenderCube(staticPos, 3, new Vector4f(1.0f, 1.0f, blue, 1.0f), new Vector3f((float)aabb.getXsize()*0.5f, (float)aabb.getYsize()*0.5f, (float)aabb.getZsize()*0.5f));

											statics.remove(j);
										}
									}
									blue *= 0.66f;
								}
								for(int j = statics.size() - 1; j >= 0; j--)
								{
									AABB aabb = statics.get(j).bounds();
									Transform staticPos = Transform.fromPos(aabb.getCenter());
									DebugRenderer.RenderCube(staticPos, 3, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), new Vector3f((float)aabb.getXsize()*0.5f, (float)aabb.getYsize()*0.5f, (float)aabb.getZsize()*0.5f));
								}

								//for(VoxelShape shape : task.GetResult()) {
								//	Transform pos = Transform.FromPos(shape.bounds().getCenter());
								//	Vector3f half = new Vector3f((float)shape.bounds().getXsize()*0.5f, (float)shape.bounds().getYsize()*0.5f, (float)shape.bounds().getZsize()*0.5f);
								//	DebugRenderer.RenderCube(pos, 3, new Vector4f(1.0f, 0.8f, 0.6f, 1.0f), half);
								//}
							}
						}

					}
				}

				if(DEBUG_HANDLE.Handle() == task.Handle.Handle())
				{
					//for(VoxelShape shape : task.GetResult()) {
					//	Transform pos = Transform.FromPos(shape.bounds().getCenter());
					//	Vector3f half = new Vector3f((float)shape.bounds().getXsize()*0.5f, (float)shape.bounds().getYsize()*0.5f, (float)shape.bounds().getZsize()*0.5f);
					//	DebugRenderer.RenderCube(pos, 3, new Vector4f(1.0f, 0.8f, 0.6f, 1.0f), half);
					//}
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
	private void PhysicsStep_ResolveTransforms()
	{
		for(DynamicObject dyn : Dynamics.values())
		{
			if(dyn.NextFrameTeleport.isPresent())
			{
				dyn.CommitFrame();
			}
			else
			{
				if (dyn.StaticCollisions.isEmpty() && dyn.DynamicCollisions.isEmpty())
				{

				}
				else
				{
					// We have some collisions, we need to resolve them before committing the next frame data
					//TransformedBBCollection pending = dyn.GetPendingColliders();
					LinearVelocity linearV = dyn.NextFrameLinearMotion;
					AngularVelocity angularV = dyn.NextFrameAngularMotion;

					Vec3 v = linearV.applyOneTick();
					Quaternionf q = angularV.applyOneTick();
					//boolean collisionX = false, collisionY = false, collisionZ = false;

					ProjectedRange xMoveReq = null, yMoveReq = null, zMoveReq = null;
					List<StaticCollisionEvent> relevantX = new ArrayList<>(), relevantY = new ArrayList<>(), relevantZ = new ArrayList<>();
					for (StaticCollisionEvent collision : dyn.StaticCollisions)
					{
						Vec3 pushOutVec = collision.ContactNormal().scale(-collision.depth());
						pushOutVec = v.subtract(pushOutVec);
						if(!Maths.Approx(pushOutVec.x, 0d))
						{
							xMoveReq = ProjectedRange.add(xMoveReq, pushOutVec.x);
							relevantX.add(collision);
						}
						if(!Maths.Approx(pushOutVec.y, 0d))
						{
							yMoveReq = ProjectedRange.add(yMoveReq, pushOutVec.y);
							relevantY.add(collision);
						}
						if(!Maths.Approx(pushOutVec.z, 0d))
						{
							zMoveReq = ProjectedRange.add(zMoveReq, pushOutVec.z);
							relevantZ.add(collision);
						}
					}

					double xRange = ProjectedRange.width(xMoveReq);
					double yRange = ProjectedRange.width(yMoveReq);
					double zRange = ProjectedRange.width(zMoveReq);

					Vec3 forceOrigin = null;

					// Clamp on the smallest axis
					if(xRange < yRange && xRange < zRange)
					{
						v = new Vec3(ProjectedRange.clamp(xMoveReq, v.x), v.y, v.z);

						forceOrigin = Vec3.ZERO;
                        for (StaticCollisionEvent x : relevantX)
							forceOrigin.add(x.ContactSurface().GetAveragePos());
						forceOrigin = forceOrigin.scale(1d / relevantX.size());
					}
					else if(yRange < zRange)
					{
						v = new Vec3(v.x, ProjectedRange.clamp(yMoveReq, v.y), v.z);
						forceOrigin = Vec3.ZERO;
						for (StaticCollisionEvent y : relevantY)
							forceOrigin.add(y.ContactSurface().GetAveragePos());
						forceOrigin = forceOrigin.scale(1d / relevantY.size());
					}
					else
					{
						v = new Vec3(v.x, v.y, ProjectedRange.clamp(zMoveReq, v.z));
						forceOrigin = Vec3.ZERO;
						for (StaticCollisionEvent z : relevantZ)
							forceOrigin.add(z.ContactSurface().GetAveragePos());
						forceOrigin = forceOrigin.scale(1d / relevantZ.size());
					}

					// TODO: Check if this resolves all collisions
					// If not, clamp another axis

					// Now, we know what the maximum v is, we need to work out what the reaction force is that results in this v
					LinearVelocity maxV = LinearVelocity.blocksPerTick(v);
					LinearAcceleration reactionAcc = LinearAcceleration.reaction(linearV, maxV);

					if(DEBUG_SETTING_ONLY_LINEAR_REACTIONS)
						dyn.ReactionAcceleration = OffsetAcceleration.offset(reactionAcc, dyn.GetCurrentLocation().positionVec3());
					else
						dyn.ReactionAcceleration = OffsetAcceleration.offset(reactionAcc, forceOrigin);
					dyn.ExtrapolateNextFrameWithReaction();

					//dyn.SetLinearVelocity(LinearVelocity.blocksPerTick(v));
					//dyn.SetAngularVelocity(angularV.scale(maxT));

					// TODO: CHECK, we used to set the v/q direct, now we apply reaction
					// dyn.ExtrapolateNextFrame(v, q);
				}
				if(!PAUSE_PHYSICS)
				{
					dyn.CommitFrame();
				}
			}
		}
	}

	private void Threaded_SeparateSimpleDynamics(@Nonnull TransformedBB a, @Nonnull TransformedBB b)
	{

	}

	@Nonnull
	private ImmutableList<VoxelShape> GetWorldColliders(@Nonnull AABB sweepAABB)
	{
		ImmutableList.Builder<VoxelShape> builder = ImmutableList.builder();

		if(sweepAABB.getXsize() > 128d
		|| sweepAABB.getYsize() > 64d
		|| sweepAABB.getZsize() > 128d)
		{
			FlansMod.LOGGER.warn("Oversized SweepTest AABB at " + sweepAABB);
			return builder.build();
		}

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

	@Nonnull
	public ImmutableList<ISeparationAxis> Debug_GetSeparatorsFor(@Nonnull ColliderHandle handle)
	{
		return StaticSeparators.getOrDefault(handle, ImmutableList.of());
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
