package com.flansmod.physics.common.entity;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.collision.OBBCollisionSystem;
import com.flansmod.physics.common.units.AngularAcceleration;
import com.flansmod.physics.common.units.LinearAcceleration;
import com.flansmod.physics.common.units.LinearForce;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.network.PhysicsPacketHandler;
import com.flansmod.physics.network.PhysicsSyncMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class PhysicsEntity extends Entity
{
    public static final double DEFAULT_PHYSICS_RANGE = 128d;
    public static final UUID CORE_PHYSICS = new UUID(0xc07ec07ec07ec07eL, 0xc07ec07ec07ec07eL);

    private boolean physicsActive = false;
    private Map<UUID, PhysicsComponent> physicsComponents = new HashMap<>();
    private boolean scheduledFullPhysicsUpdate = false;

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
    public void forEachPhysicsComponent(@Nonnull BiConsumer<UUID, PhysicsComponent> action)
    {
        for(var kvp : physicsComponents.entrySet())
            action.accept(kvp.getKey(), kvp.getValue());
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
        Vector3f euler = worldRoot.euler();
        setPos(worldRoot.positionVec3());
        yRotO = euler.y;
        xRotO = euler.x;
        setXRot(euler.x);
        setYRot(euler.y);
    }
    @Nonnull
    protected Transform getEntityRootAsTransform()
    {
        return Transform.fromEntity(this);
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
        super.tick();
        checkPhysicsActivate();
        if(physicsActive)
        {
            OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());

            // Respond to collision events from last frame, and update our location
            for(PhysicsComponent component : physicsComponents.values())
                component.syncCollisionToComponent(physics);

            // For any vanilla calls that want to check in on this object, we keep the pos/yaw/pitch in sync
            syncTransformToEntity();
            if(isControlledByLocalInstance())
                sendPhysicsSync();

            // Let the entity handle these responses and build new forces
            tickPhysics();

            // Now send the results of the Force Model to the collision engine
            for(PhysicsComponent component : physicsComponents.values())
                component.syncComponentToCollision(physics);
        }
        else
        {
            tickOutsidePhysicsRange();
        }
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
                if(physics.IsHandleInvalidated(component.getPhysicsHandle()))
                    shouldDeactivate = true;
            }

            // TODO: Check out of range of players

            if(shouldDeactivate)
            {
                stopPhysics();
                for(PhysicsComponent component : physicsComponents.values())
                {
                    physics.UnregisterDynamic(component.getPhysicsHandle());
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
    private void sendPhysicsSync()
    {
        if(level().isClientSide)
        {
            PhysicsPacketHandler.sendToServer(createSyncMessage());
        }
        else
        {
            PhysicsPacketHandler.sendToAllAroundPoint(createSyncMessage(), level().dimension(), position(), 100d, null);
        }
    }
    @Nonnull
    private PhysicsSyncMessage createSyncMessage()
    {
        PhysicsSyncMessage message = new PhysicsSyncMessage();

        forEachPhysicsComponent((id, physicsComponent) -> {
            if(scheduledFullPhysicsUpdate || physicsComponent.needsUpdate())
            {
                message.addStateChange(
                        id,
                        physicsComponent.getCurrentTransform(),
                        physicsComponent.getCurrentLinearVelocity(),
                        physicsComponent.getCurrentAngularVelocity());
            }
        });

        return message;
    }
}
