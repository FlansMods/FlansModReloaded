package com.flansmod.physics.common.entity;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.collision.OBBCollisionSystem;
import com.flansmod.physics.common.units.AngularAcceleration;
import com.flansmod.physics.common.units.LinearAcceleration;
import com.flansmod.physics.common.units.LinearForce;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class PhysicsEntity extends Entity
{
    public static final double DEFAULT_PHYSICS_RANGE = 128d;
    public static final UUID CORE_PHYSICS = new UUID(0xc07ec07ec07ec07eL, 0xc07ec07ec07ec07eL);

    private boolean physicsActive = false;
    private Map<UUID, PhysicsComponent> physicsComponents = new HashMap<>();
    public double physicsRange = DEFAULT_PHYSICS_RANGE;


    public PhysicsEntity(@Nonnull EntityType<?> type, @Nonnull Level level)
    {
        super(type, level);
    }

    public void forEachPhysicsComponent(@Nonnull Consumer<PhysicsComponent> action)
    {
        for(final PhysicsComponent component : physicsComponents.values())
            action.accept(component);
    }
    @Nonnull
    public PhysicsComponent getRootComponent()
    {
        return physicsComponents.getOrDefault(CORE_PHYSICS, PhysicsComponent.invalid);
    }
    @Nonnull
    public Transform getCurrentRootTransform() { return getRootComponent().getCurrentTransform(); }
    @Nonnull
    public Transform getPreviousRootTransform() { return getRootComponent().getPreviousTransform(); }

    protected void syncTransformToEntity()
    {
        Transform worldRoot = getRootComponent().getCurrentTransform();
        Vector3f euler = worldRoot.Euler();
        setPos(worldRoot.PositionVec3());
        yRotO = euler.y;
        xRotO = euler.x;
    }
    @Nonnull
    protected Transform getEntityRootAsTransform()
    {
        return Transform.FromEntity(this);
    }

    protected abstract void tickPhysics();
    protected abstract void tickOutsidePhysicsRange();

    protected void initPhysics()
    {
        // Default implementation. Override to do your own colliders
        addPhysicsComponent(CORE_PHYSICS, getEntityRootAsTransform(), List.of(new AABB(-0.5d, -0.5d, -0.5d, 0.5d, 0.5d, 0.5d)));
    }
    protected void stopPhysics()
    {

    }
    protected void addPhysicsComponent(@Nonnull UUID id, @Nonnull PhysicsComponent component)
    {
        physicsComponents.put(id, component);
    }
    protected void addPhysicsComponent(@Nonnull UUID id, @Nonnull Transform worldPos, @Nonnull List<AABB> aabbs)
    {
        OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());
        physicsComponents.put(id, new PhysicsComponent(worldPos, physics.RegisterDynamic(aabbs, worldPos)));
    }

    @Override
    public void tick()
    {
        checkPhysicsActivate();
        if(physicsActive)
        {
            OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());

            // Respond to collision events from last frame, and update our location
            for(PhysicsComponent component : physicsComponents.values())
                copyResponsesFromCollision(physics, component);

            // For any vanilla calls that want to check in on this object, we keep the pos/yaw/pitch in sync
            syncTransformToEntity();

            // Let the entity handle these responses and build new forces
            tickPhysics();

            // Now send the results of the Force Model to the collision engine
            for(PhysicsComponent component : physicsComponents.values())
                sendForcesToCollision(physics, component);
        }
        else
        {
            tickOutsidePhysicsRange();
        }
    }
    private void copyResponsesFromCollision(@Nonnull OBBCollisionSystem physics, @Nonnull PhysicsComponent component)
    {
        component.LocationPrev = component.LocationCurrent;
        // Sum all the non-reactionary forces of the last frame
        LinearForce impactForce = component.Forces.SumLinearForces(component.LocationCurrent, false);
        component.Forces.EndFrame();

        if(component.PhysicsHandle.IsValid())
        {
            // Do we need to add these?
            // TODO: If debug, show them?
            AtomicReference<Boolean> collidedX = new AtomicReference<>(false),
                    collidedY = new AtomicReference<>(false),
                    collidedZ = new AtomicReference<>(false);

            Transform newPos =  physics.ProcessEvents(component.PhysicsHandle,
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
            component.Forces.AddReactionForce(new LinearForce(new Vec3(
                    collidedX.get() ? -impactForce.Force().x : 0.0d,
                    collidedY.get() ? -impactForce.Force().y : 0.0d,
                    collidedZ.get() ? -impactForce.Force().z : 0.0d)));

            component.LocationCurrent = newPos;
        }

    }
    private void sendForcesToCollision(@Nonnull OBBCollisionSystem physics, @Nonnull PhysicsComponent component)
    {
        LinearAcceleration linear = component.Forces.SumLinearAcceleration(component.LocationCurrent, component.mass, true);
        AngularAcceleration angular = component.Forces.SumAngularAcceleration(component.LocationCurrent, component.momentOfInertia, true);
        //float dampening = 1.0f;//part.Forces.GetDampeningRatio();
        //if(dampening < 1.0f)
        //{
        //	linear = linear.subtract(part.GetVelocityMS().scale(1.0f - dampening));
        //}
        physics.AddLinearAcceleration(component.PhysicsHandle, linear);
        physics.AddAngularAcceleration(component.PhysicsHandle, angular);
    }

    private void checkPhysicsActivate()
    {
        if(physicsActive)
        {
            boolean shouldDeactivate = false;
            if(!isAlive())
                shouldDeactivate = true;

            OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());
            for(PhysicsComponent component : physicsComponents.values())
            {
                if(physics.IsHandleInvalidated(component.PhysicsHandle))
                    shouldDeactivate = true;
            }

            // TODO: Check out of range of players

            if(shouldDeactivate)
            {
                stopPhysics();
                for(PhysicsComponent component : physicsComponents.values())
                {
                    physics.UnregisterDynamic(component.PhysicsHandle);
                }
                physicsComponents.clear();
                physicsActive = false;
            }
        }
        else
        {
            boolean shouldActivate = true;
            if(!isAlive())
                shouldActivate = false;

            // TODO: Check out of range of players

            if(shouldActivate)
            {
                physicsActive = true;
                initPhysics();
                if(!physicsComponents.containsKey(CORE_PHYSICS))
                {
                    FlansPhysicsMod.LOGGER.error("initPhysics on entity " + this + " did not add a core physics component");
                }
            }
        }
    }
}
