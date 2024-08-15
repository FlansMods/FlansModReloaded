package com.flansmod.client.render.vehicles;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.longdistance.LongDistanceEntity;
import com.flansmod.common.entity.longdistance.LongDistanceVehicle;
import com.flansmod.common.entity.vehicle.physics.*;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleComponentPath;
import com.flansmod.util.Transform;
import com.flansmod.util.collision.*;
import com.flansmod.util.physics.IForce;
import com.flansmod.util.physics.LinearForce;
import com.flansmod.util.physics.Torque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class VehicleDebugRenderer
{
	private static final float ArrowLengthSeconds = 1f/20f;
	private record DebugPalette(@Nonnull Vector4f Default,
								@Nonnull Vector4f MotionCurrent,
								@Nonnull Vector4f MotionNext,
								@Nonnull Vector4f TotalForce,
								@Nonnull Vector4f WheelForces,
								@Nonnull Vector4f WheelCurrent,
								@Nonnull Vector4f WheelNext,
								@Nonnull Vector4f CoreForces,
								@Nonnull Vector4f CoreCurrent,
								@Nonnull Vector4f CoreNext,
								@Nonnull Vector4f StaticCollision,
								@Nonnull Vector4f DynamicCollision)
	{}

	// Client palette is red(forces) -> blue(motions)
	private static final DebugPalette Client = new DebugPalette(
		new Vector4f(1f, 1f, 1f, 1f),			// Default
		new Vector4f(0.25f, 0.25f, 1.0f, 1f),	// MotionCurrent
		new Vector4f(0f, 0f, 1f, 1f),			// MotionNext
		new Vector4f(1f, 0.75f, 0.75f, 1f),		// TotalForce
		new Vector4f(1f, 0f, 0f, 1f),			// WheelForces
		new Vector4f(0.125f, 0.125f, 1f, 0.5f),	// WheelCurrent
		new Vector4f(0.25f, 0.25f, 1f, 0.25f),	// WheelNext
		new Vector4f(1f, 0f, 0f, 1f),			// CoreForces
		new Vector4f(0.125f, 0.125f, 1f, 0.5f),	// CoreCurrent
		new Vector4f(0.25f, 0.25f, 1f, 0.25f),  // CoreNext
		new Vector4f(1f, 0f, 1f, 1f),			// StaticCollision
		new Vector4f(1f, 0.25f, 0.75f, 1f)		// DynamicCollision
	);

	// Server palette is green -> yellow
	private static final DebugPalette Server = new DebugPalette(
		new Vector4f(1f, 1f, 1f, 1f),			// Default
		new Vector4f(1f, 1f, 0.25f, 1f),		// MotionCurrent
		new Vector4f(1f, 1f, 0f, 1f),			// MotionNext
		new Vector4f(0.75f, 1f, 0.75f, 1f),		// TotalForce
		new Vector4f(0f, 1f, 0f, 1f),			// WheelForces
		new Vector4f(1f, 1f, 0.125f, 0.5f),		// WheelCurrent
		new Vector4f(1f, 1f, 0.25f, 0.25f),		// WheelNext
		new Vector4f(0f, 1f, 0f, 1f),			// CoreForces
		new Vector4f(1f, 1f, 0.125f, 0.5f),		// CoreCurrent
		new Vector4f(1f, 1f, 0.25f, 0.25f),		// CoreNext
		new Vector4f(1f, 1f, 0f, 1f),			// StaticCollision
		new Vector4f(1f, 0.75f, 0.25f, 1f)		// DynamicCollision
	);



	public VehicleDebugRenderer()
	{
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
	}

	public void ClientTick(@Nonnull TickEvent.ClientTickEvent event)
	{
		if(event.phase != TickEvent.Phase.END)
			return;

		if (FlansMod.DEBUG || Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes())
		{
			ClientLevel level = Minecraft.getInstance().level;
			if (level != null)
			{

				for (LongDistanceEntity longEntity : FlansModClient.CLIENT_LONG_DISTANCE.GetAllLongEntities(level))
				{
					if (longEntity instanceof LongDistanceVehicle longVehicle)
					{
						// Render a long distance debug view
					}
				}

				OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level);
				physics.TryForEachDynamic((dynamic) -> DebugRender(dynamic, Client));
				DebugRenderSeparations(physics, Client);

				DebugRender(level.entitiesForRendering(), Client);
			}

			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null)
			{
				for (ServerLevel loadedLevel : server.getAllLevels())
				{
					DebugRender(loadedLevel.getAllEntities(), Server);
					OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(loadedLevel);
					physics.TryForEachDynamic((dynamic) -> DebugRender(dynamic, Server));
					DebugRenderSeparations(physics, Server);
				}
			}
		}
	}

	private void DebugRenderSeparations(@Nonnull OBBCollisionSystem physics, @Nonnull DebugPalette palettte)
	{
		if(OBBCollisionSystem.DEBUG_HANDLE.Handle() != 0L)
		{
			List<Vec3> staticCollisionPoints = new ArrayList<>();
			List<Vec3> dynamicCollisionPoints = new ArrayList<>();
			Transform position = physics.ProcessEvents(OBBCollisionSystem.DEBUG_HANDLE,
				(stat) -> {
					Vec3 normal = stat.ContactNormal();
					Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);
					Transform collisionFrame = Transform.FromPositionAndLookDirection(stat.ContactPoint(), normal, up);
					DebugRenderer.RenderPoint(collisionFrame, 1, palettte.WheelCurrent);
					DebugRenderer.RenderArrow(collisionFrame, 1, palettte.WheelCurrent, new Vec3(0d,0d,-1d));
					DebugRenderer.RenderCube(collisionFrame, 1, palettte.WheelCurrent, new Vector3f(1f, 1f, 0f));
					staticCollisionPoints.add(collisionFrame.PositionVec3());
				},
				(dyn) -> {
					Vec3 normal = dyn.ContactNormal();
					Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);
					Transform collisionFrame = Transform.FromPositionAndLookDirection(dyn.ContactPoint(), normal, up);
					DebugRenderer.RenderPoint(collisionFrame, 1, palettte.WheelNext);
					DebugRenderer.RenderArrow(collisionFrame, 1, palettte.WheelNext, new Vec3(0d,0d,-1d));
					DebugRenderer.RenderCube(collisionFrame, 1, palettte.WheelNext, new Vector3f(1f, 1f, 0f));
					dynamicCollisionPoints.add(collisionFrame.PositionVec3());
				});
			DebugRenderer.RenderAxes(position, 1, palettte.WheelForces);

			for(Vec3 point : staticCollisionPoints)
				DebugRenderer.RenderLine(position, 1, palettte.Default, point.subtract(position.PositionVec3()));
			for(Vec3 point : dynamicCollisionPoints)
				DebugRenderer.RenderLine(position, 1, palettte.CoreCurrent, point.subtract(position.PositionVec3()));

		}
	}

	private void DebugRender(@Nonnull DynamicObject dynamic, @Nonnull DebugPalette palette)
	{
		DebugRender(dynamic.GetPendingColliders(), palette);
		for (StaticCollisionEvent collision : dynamic.StaticCollisions)
		{
			Vec3 normal = collision.ContactNormal();
			Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);
			Transform collisionFrame = Transform.FromPositionAndLookDirection(collision.ContactPoint(), normal, up);
			DebugRenderer.RenderCube(collisionFrame, 1, palette.StaticCollision, new Vector3f(0.25f, 0.25f, 0f));
			DebugRenderer.RenderArrow(collisionFrame, 1, palette.StaticCollision, new Vec3(0d,0d,-1d));
		}
		for (DynamicCollisionEvent collision : dynamic.DynamicCollisions)
		{
			Vec3 normal = collision.ContactNormal();
			Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);
			Transform collisionFrame = Transform.FromPositionAndLookDirection(collision.ContactPoint(), normal, up);
			DebugRenderer.RenderCube(collisionFrame, 1, palette.DynamicCollision, new Vector3f(0.25f, 0.25f, 0f));
			DebugRenderer.RenderArrow(collisionFrame, 1, palette.DynamicCollision, new Vec3(0d,0d,-1d));
		}
	}

	private void DebugRender(@Nonnull TransformedBBCollection bbs, @Nonnull DebugPalette palette)
	{
		for(int i = 0; i < bbs.GetCount(); i++)
		{
			TransformedBB bb = bbs.GetColliderBB(i);
			DebugRenderer.RenderCube(bb.Loc(), 1, palette.CoreCurrent, bb.HalfExtents());
		}
	}

	private void DebugRender(@Nonnull Iterable<Entity> entityList, @Nonnull DebugPalette palette)
	{
		for(Entity entity : entityList)
		{
			// Render a regular entity debug view
			if(entity instanceof VehicleEntity vehicle)
			{
				Transform vehiclePos = vehicle.GetWorldToEntity().GetCurrent();

				//DebugRenderer.RenderCube(Transform.FromPos(vehicle.position()), 1, palette.CoreCurrent, new Vector3f(0.15f, 0.1f, 0.15f));
				//DebugRenderer.RenderAxes(vehiclePos, 1, palette.Default);
				//DebugRenderer.RenderCube(vehiclePos, 1, palette.CoreCurrent, new Vector3f(0.6f, 0.25f, 0.6f));
				Vec3 coreMotionNextFrame = DebugRenderForces(vehicle.GetCoreForces(), vehicle.getDeltaMovement(), vehicle.GetWorldToEntity().GetCurrent(), palette, true, vehicle.Def().physics.mass, vehicle::GetWorldToPartCurrent);
				Transform vehiclePosNext = Transform.Compose(vehiclePos, Transform.FromPos(coreMotionNextFrame.scale(1f/20f)));
				//DebugRenderer.RenderCube(vehiclePosNext, 1, palette.CoreNext,  new Vector3f(0.6f, 0.25f, 0.6f));

				vehicle.GetHierarchy().ForEachNode((node) -> {
					Transform pos = vehicle.GetWorldToPartCurrent(node.GetPath());
					DebugRenderer.RenderPoint(pos, 1, palette.WheelCurrent);
					if(node.ParentNode != null)
					{
						Transform parent = vehicle.GetWorldToPartCurrent(node.ParentNode.GetPath());
						DebugRenderer.RenderLine(parent, 1, palette.WheelCurrent, parent.GlobalToLocalPosition(pos.PositionVec3()));
					}

				});

				vehicle.GetHierarchy().ForEachWheel((path, def) -> {

				//});
				//
				//for(int wheelIndex = 0; wheelIndex < vehicle.Wheels.All().size(); wheelIndex++)
				//{
				//	WheelEntity wheel = vehicle.Wheels.ByIndex(wheelIndex);
				//	if(wheel != null)
				//	{
					VehiclePartPhysics physics = vehicle.GetPartPhysics(path);

					Transform wheelPos =physics.LocationCurrent;
					Vector3f debugWheelBoxSize = new Vector3f(0.5f * def.radius, def.radius, def.radius);
					//DebugRenderer.RenderAxes(wheel.GetWorldTransformCurrent(), 1, palette.Default);
					//DebugRenderer.RenderCube(wheelPos, 1, palette.WheelCurrent, debugWheelBoxSize);

					Vec3 wheelMotionNextFrame = DebugRenderForces(physics.Forces,
						physics.GetDeltaFramePos(),
						wheelPos,
						palette,
						false,
						def.mass,
						vehicle::GetWorldToPartCurrent);
					Transform wheelPosNext = Transform.Compose(wheelPos, Transform.FromPos(wheelMotionNextFrame.scale(1f/20f)));
					//DebugRenderer.RenderCube(wheelPosNext, 1, palette.WheelNext, debugWheelBoxSize);
					//}
				});
			}
		}
	}



	private Vec3 DebugRenderForces(@Nullable ForcesOnPart forces,
								   @Nonnull Vec3 motion,
								   @Nonnull Transform worldTransform,
								   @Nonnull DebugPalette palette,
								   boolean isCore,
								   float mass,
								   @Nonnull Function<VehicleComponentPath, Transform> lookup)
	{
		float inertia = 1.0f / mass;
		float forceArrowScale = 1.0f / 20f;
		float motionArrowScale = 1.0f;
		if(forces != null)
		{
			Vec3 origin = worldTransform.PositionVec3();
			Vec3 forceTotal = new Vec3(0d, 0d, 0d);
			Vector4f forceColour = isCore ? palette.CoreForces : palette.WheelForces;

			for(IForce force : forces.Debug_GetForces())
			{
				if(force.HasLinearComponent(worldTransform))
				{
					LinearForce linear = force.GetLinearComponent(worldTransform);
					DebugRenderer.RenderArrow(worldTransform, 1, forceColour, linear.Force().scale(forceArrowScale));
				}
				if(force.HasAngularComponent(worldTransform))
				{
					Torque torque = force.GetTorqueComponent(worldTransform);
					DebugRenderer.RenderRotation(worldTransform, 1, forceColour, torque.Axis(), torque.Magnitude());
					//Quaternionf angular = force.GetAngularComponentRadiansPerSecondSq(worldTransform);
					//forceTotal = forceTotal.add(global.Vector());
				}
			}



			//for(LinearForce global : forces.GlobalForces)
			//{
			//	Vec3 force = global.Vector();
			//	DebugRenderer.RenderArrow(origin, 1, forceColour, force.scale(arrowScale));
			//	forceTotal = forceTotal.add(global.Vector());
			//}
			//for(OffsetForce global : forces.OffsetGlobalForces)
			//{
			//	Vec3 offsetOrigin = origin.add(global.Offset());
			//	Vec3 force = global.Vector();
			//	DebugRenderer.RenderArrow(offsetOrigin, 1, forceColour, force.scale(arrowScale));
			//	forceTotal = forceTotal.add(force);
			//}
			//for(LinearForce local : forces.LocalForces)
			//{
			//	Vec3 force = worldTransform.LocalToGlobalDirection(local.Vector());
			//	forceTotal = forceTotal.add(force);
			//	DebugRenderer.RenderArrow(origin, 1, forceColour, force.scale(arrowScale));
			//}
			//for(OffsetForce local : forces.OffsetLocalForces)
			//{
			//	Vec3 offsetOrigin = worldTransform.LocalToGlobalPosition(local.Offset());
			//	Vec3 force = worldTransform.LocalToGlobalDirection(local.Vector());
			//	forceTotal = forceTotal.add(force);
			//	DebugRenderer.RenderArrow(offsetOrigin, 1, forceColour, force.scale(arrowScale));
			//}
			//for(ForceModel.SpringJoint spring : forces.Springs)
			//{
			//	Transform pullTowards = lookup.apply(spring.PullTowardsAP());
			//	if(pullTowards != null)
			//	{
			//		Vec3 delta = pullTowards.PositionVec3().subtract(origin);
			//		Vec3 springForce = delta.scale(spring.SpringStrength());
			//		Vec3 acceleration = springForce.scale(inertia);
//
			//		DebugRenderer.RenderArrow(origin, 1, SpringColour, acceleration.scale(arrowScale));
			//		DebugRenderer.RenderLine(origin, 1, SpringConnectorColour, delta);
			//		accelerationTotal = accelerationTotal.add(acceleration);
			//	}
			//}

			Vec3 motionNext = motion.add(forceTotal.scale(inertia));
			DebugRenderer.RenderArrow(worldTransform, 1, palette.MotionCurrent, motion.scale(motionArrowScale));
			DebugRenderer.RenderArrow(worldTransform, 1, palette.MotionNext, motionNext.scale(motionArrowScale));
			DebugRenderer.RenderArrow(worldTransform.Translated(motion.scale(motionArrowScale)), 1, palette.TotalForce, forceTotal.scale(motionArrowScale));
			return motionNext;
		}
		return Vec3.ZERO;
	}
}
