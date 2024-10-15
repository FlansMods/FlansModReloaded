package com.flansmod.physics.client;

import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.physics.common.collision.*;
import com.flansmod.physics.common.entity.ForcesOnPart;
import com.flansmod.physics.common.entity.PhysicsEntity;
import com.flansmod.physics.common.units.LinearForce;
import com.flansmod.physics.common.units.Torque;
import com.flansmod.physics.common.units.IForce;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.shapes.IPolygon;
import com.flansmod.physics.common.util.shapes.ISeparationAxis;
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

public class PhysicsDebugRenderer
{
    private static class DebugPalette
    {
        public final Vector4f
                PositionCurrent, PositionPrevious,
                MotionCurrent, MotionPrevious,
                ForceInputs, ForceReactions,
                CollisionStatic, CollisionDynamic;

        public DebugPalette(float redMult, float greenMult)
        {
            PositionCurrent = new Vector4f(0f, greenMult, 1f, 1f);
            PositionPrevious = new Vector4f(0.25f * redMult, 0.75f * greenMult, 1f, 1f);

            MotionPrevious = new Vector4f(redMult * 0.25f, greenMult * 0.25f, 1.0f, 1.0f);
            MotionCurrent = new Vector4f(redMult * 0.5f, greenMult * 0.5f, 1.0f, 1.0f);

            ForceInputs = new Vector4f(redMult, greenMult * 0.75f, 0.75f, 1.0f);
            ForceReactions = new Vector4f(redMult, 0f, 0f, 1f);

            CollisionStatic = new Vector4f(redMult, 0f, 1f, 1f);
            CollisionDynamic = new Vector4f(redMult, greenMult * 0.25f, 0.75f, 1f);
        }
    }
    private static final DebugPalette CLIENT_PALETTE = new DebugPalette(0f, 1f);
    private static final DebugPalette SERVER_PALETTE = new DebugPalette(1f, 0f);


    public PhysicsDebugRenderer()
    {
        MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
    }

    public void ClientTick(@Nonnull TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        if(Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes())
        {
            ClientLevel clientLevel = Minecraft.getInstance().level;
            if (clientLevel != null)
            {
                OBBCollisionSystem clientPhysics = OBBCollisionSystem.ForLevel(clientLevel);
                clientPhysics.TryForEachDynamic((dynamic) -> DebugRender(dynamic, CLIENT_PALETTE));
                DebugRenderSeparations(clientPhysics, CLIENT_PALETTE);
                DebugRender(clientLevel.entitiesForRendering(), CLIENT_PALETTE);

                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null)
                {
                    for (ServerLevel loadedLevel : server.getAllLevels())
                    {
                        if(loadedLevel.dimension() == clientLevel.dimension())
                            DebugRender(loadedLevel.getAllEntities(), SERVER_PALETTE);
                        OBBCollisionSystem serverPhysics = OBBCollisionSystem.ForLevel(loadedLevel);
                        serverPhysics.TryForEachDynamic((dynamic) -> DebugRender(dynamic, SERVER_PALETTE));
                        DebugRenderSeparations(serverPhysics, SERVER_PALETTE);
                    }
                }
            }
        }
    }

    private void DebugRenderSeparations(@Nonnull OBBCollisionSystem physics, @Nonnull DebugPalette palette)
    {
        /*
        if(OBBCollisionSystem.DEBUG_HANDLE.Handle() != 0L)
        {
            //List<Vec3> staticCollisionPoints = new ArrayList<>();
            //List<Vec3> dynamicCollisionPoints = new ArrayList<>();

            Transform position = physics.ProcessEvents(OBBCollisionSystem.DEBUG_HANDLE,
                    (stat) -> {
                        Vec3 normal = stat.ContactNormal();
                        Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);
                        IPolygon collisionSurface = stat.ContactSurface();
                        Vec3 avgPos = Vec3.ZERO;
                        int numVerts = collisionSurface.GetNumVertices();
                        for(int i = 0; i < numVerts; i++)
                        {
                            Vec3 vCurrent = collisionSurface.GetVertexLooped(i);
                            Vec3 vNext = collisionSurface.GetVertexLooped(i + 1);
                            DebugRenderer.RenderPoint(Transform.FromPos(vCurrent), 2, palette.CollisionStatic);
                            DebugRenderer.RenderLine(Transform.FromPos(vCurrent), 2, palette.CollisionStatic, vNext.subtract(vCurrent));
                            avgPos = avgPos.add(vCurrent);
                        }
                        if(numVerts > 0) {
                            Transform collisionFrame = Transform.FromPositionAndLookDirection(avgPos.scale(1d / numVerts), normal, up);
                            DebugRenderer.RenderArrow(collisionFrame, 2, palette.CollisionStatic, new Vec3(0d, 0d, -1d));
                            //staticCollisionPoints.add(collisionFrame.PositionVec3());
                        }
                    },
                    (dyn) -> {
                        Vec3 normal = dyn.ContactNormal();
                        Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);
                        Transform collisionFrame = Transform.FromPositionAndLookDirection(dyn.ContactPoint(), normal, up);
                        DebugRenderer.RenderPoint(collisionFrame, 1, palette.CollisionDynamic);
                        DebugRenderer.RenderArrow(collisionFrame, 1, palette.CollisionDynamic, new Vec3(0d,0d,-1d));
                        DebugRenderer.RenderCube(collisionFrame, 1, palette.CollisionDynamic, new Vector3f(1f, 1f, 0f));
                        //dynamicCollisionPoints.add(collisionFrame.PositionVec3());
                    });
            //DebugRenderer.RenderAxes(position, 1, palettte.WheelForces);
            //ImmutableList<ISeparationAxis> separators = physics.Debug_GetSeparatorsFor(OBBCollisionSystem.DEBUG_HANDLE);
            //for(ISeparationAxis separator : separators) {
            //    //Vec3 pos = position.PositionVec3();
            //    //DebugRender(position.PositionVec3(), separator, palette);
            //}


            //for(Vec3 point : staticCollisionPoints)
            //    DebugRenderer.RenderLine(position, 1, palette.CollisionStatic, point.subtract(position.PositionVec3()));
            //for(Vec3 point : dynamicCollisionPoints)
            //    DebugRenderer.RenderLine(position, 1, palette.CollisionDynamic, point.subtract(position.PositionVec3()));

        }

         */
    }

    private void DebugRender(@Nonnull Vec3 position, @Nonnull ISeparationAxis axis, @Nonnull DebugPalette palette)
    {
        Vec3 normal = axis.GetNormal();
        Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);
        Transform frame = Transform.fromPositionAndLookDirection(position, axis.GetNormal(), up);
        DebugRenderer.RenderArrow(frame, 2, palette.CollisionStatic, new Vec3(0d, 0d, -1d));
        DebugRenderer.RenderCube(frame, 2, palette.CollisionStatic, new Vector3f(0.25f, 0.25f, 0f));
    }

    private void DebugRender(@Nonnull DynamicObject dynamic, @Nonnull DebugPalette palette)
    {
        DebugRender(dynamic.getCurrentColliders(), palette, false);
        DebugRender(dynamic.getPendingColliders(), palette, true);
        for (StaticCollisionEvent collision : dynamic.StaticCollisions)
        {
            Vec3 normal = collision.ContactNormal();
            Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);
            IPolygon collisionSurface = collision.ContactSurface();
            Vec3 avgPos = Vec3.ZERO;
            int numVerts = collisionSurface.GetNumVertices();
            for(int i = 0; i < numVerts; i++)
            {
                Vec3 vCurrent = collisionSurface.GetVertexLooped(i);
                Vec3 vNext = collisionSurface.GetVertexLooped(i + 1);
                DebugRenderer.RenderPoint(Transform.fromPos(vCurrent), 2, palette.CollisionStatic);
                DebugRenderer.RenderLine(Transform.fromPos(vCurrent), 2, palette.CollisionStatic, vNext.subtract(vCurrent));
                avgPos = avgPos.add(vCurrent);
            }
            if(numVerts > 0) {
                Transform collisionFrame = Transform.fromPositionAndLookDirection(avgPos.scale(1d / numVerts), normal, up);
                DebugRenderer.RenderArrow(collisionFrame, 2, palette.CollisionStatic, new Vec3(0d, 0d, -1d));
                //staticCollisionPoints.add(collisionFrame.PositionVec3());
            }
            else {
                Transform collisionFrame = Transform.fromPositionAndLookDirection(dynamic.getCurrentWorldBounds().getCenter(), normal, up);
                DebugRenderer.RenderArrow(collisionFrame, 2, palette.CollisionStatic.mul(1.0f, 0.5f, 0.5f, 1.0f, new Vector4f()), new Vec3(0d, 0d, -1d));
                //staticCollisionPoints.add(collisionFrame.PositionVec3());
            }
            //Transform collisionFrame = Transform.FromPositionAndLookDirection(collisionSurface.GetVertexLooped(0), normal, up);
            //DebugRenderer.RenderArrow(collisionFrame, 2, palette.CollisionStatic, new Vec3(0d,0d,-1d));
        }
        for (DynamicCollisionEvent collision : dynamic.DynamicCollisions)
        {
            Vec3 normal = collision.ContactNormal();
            Vec3 up = new Vec3(normal.z, -normal.x, -normal.y);
            Transform collisionFrame = Transform.fromPositionAndLookDirection(collision.ContactPoint(), normal, up);
            DebugRenderer.RenderCube(collisionFrame, 2, palette.CollisionDynamic, new Vector3f(0.25f, 0.25f, 0f));
            DebugRenderer.RenderArrow(collisionFrame, 2, palette.CollisionDynamic, new Vec3(0d,0d,-1d));
        }
    }
    private void DebugRender(@Nonnull TransformedBBCollection bbs, @Nonnull DebugPalette palette, boolean predicted)
    {
        for(int i = 0; i < bbs.getCount(); i++)
        {
            TransformedBB bb = bbs.getColliderBB(i);
            DebugRenderer.RenderCube(bb.Loc(), 2, predicted ? palette.PositionCurrent : palette.PositionPrevious, bb.HalfExtents());
        }
    }
    private void DebugRender(@Nonnull Iterable<Entity> entityList, @Nonnull DebugPalette palette)
    {
        for(Entity entity : entityList)
        {
            // Render a regular entity debug view
            if(entity instanceof PhysicsEntity vehicle)
            {
                //Transform vehiclePos = vehicle.getCurrentRootTransform();
                vehicle.forEachPhysicsComponent((component) -> {
                    Transform componentPos = component.getCurrentTransform();
                    Vec3 coreMotionNextFrame = DebugRenderForces(
                            component.getCurrentForces(),
                            vehicle.getDeltaMovement(),
                            componentPos,
                            palette,
                            true,
                            (float) component.mass);
                    Transform componentPosNext = Transform.compose(componentPos, Transform.fromPos(coreMotionNextFrame.scale(1f/20f)));
                });
            }
        }
    }
    private Vec3 DebugRenderForces(@Nullable ForcesOnPart forces,
                                   @Nonnull Vec3 motion,
                                   @Nonnull Transform worldTransform,
                                   @Nonnull DebugPalette palette,
                                   boolean isReactionForce,
                                   float mass)
    {
        float inertia = 1.0f / mass;
        float forceArrowScale = 1.0f / 20f;
        float motionArrowScale = 1.0f;
        if(forces != null)
        {
            Vec3 origin = worldTransform.positionVec3();
            Vec3 forceTotal = new Vec3(0d, 0d, 0d);
            Vector4f forceColour = isReactionForce ? palette.ForceInputs : palette.ForceReactions;

            for(IForce force : forces.Debug_GetForces())
            {
                if(force.hasLinearComponent(worldTransform))
                {
                    LinearForce linear = force.getLinearComponent(worldTransform);
                    DebugRenderer.RenderArrow(worldTransform, 1, forceColour, linear.Force().scale(forceArrowScale));
                }
                if(force.hasAngularComponent(worldTransform))
                {
                    Torque torque = force.getTorqueComponent(worldTransform);
                    DebugRenderer.RenderRotation(worldTransform, 1, forceColour, torque.Axis(), torque.Magnitude());
                    //Quaternionf angular = force.GetAngularComponentRadiansPerSecondSq(worldTransform);
                    //forceTotal = forceTotal.add(global.Vector());
                }
            }

            Vec3 motionNext = motion.add(forceTotal.scale(inertia));
            DebugRenderer.RenderArrow(worldTransform, 1, palette.MotionCurrent, motion.scale(motionArrowScale));
            //DebugRenderer.RenderArrow(worldTransform, 1, palette.MotionCurrent, motionNext.scale(motionArrowScale));
            DebugRenderer.RenderArrow(worldTransform.translated(motion.scale(motionArrowScale)), 1, palette.ForceInputs, forceTotal.scale(motionArrowScale));
            return motionNext;
        }
        return Vec3.ZERO;
    }
}
