package com.flansmod.physics.common.entity;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.collision.ColliderHandle;
import com.flansmod.physics.common.collision.OBBCollisionSystem;
import com.flansmod.physics.common.util.ITransformEntity;
import com.flansmod.physics.common.util.ITransformPair;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.network.PhysicsPacketHandler;
import com.flansmod.physics.network.PhysicsSyncMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class PhysicsEntity extends Entity implements ITransformEntity
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
    @Nullable
    public PhysicsComponent getComponent(@Nonnull UUID componentID)
    {
        return physicsComponents.get(componentID);
    }
    @Nonnull
    public PhysicsComponent getComponentOrDefault(@Nonnull UUID componentID)
    {
        return physicsComponents.getOrDefault(componentID, PhysicsComponent.invalid);
    }
    @Nonnull
    public Transform getCurrentRootTransform() { return getRootComponent().getCurrentTransform(); }
    @Nonnull
    public Transform getPreviousRootTransform() { return getRootComponent().getPreviousTransform(); }

    @Override
    public void syncTransformToEntity()
    {
        Transform worldRoot = getRootComponent().getCurrentTransform();
        Vector3f euler = worldRoot.euler();
        setPos(worldRoot.positionVec3());
        yRotO = euler.y;
        xRotO = euler.x;
        setXRot(euler.x);
        setYRot(euler.y);
    }
    @Override
    public void syncEntityToTransform()
    {
        Transform coreTransformNew = Transform.fromPosAndEuler(getPosition(0f), getXRot(), getYRot(), 0f);
        teleportTo(coreTransformNew);
    }
    @Override @Nonnull
    public ITransformPair getRootTransform() { return ITransformPair.of(this::getPreviousRootTransform, this::getCurrentRootTransform); }
    @Override
    public void teleportTo(@Nonnull Transform coreTransformNew)
    {
        // So we want to teleport the whole vehicle
        PhysicsComponent corePart = getRootComponent();
        Transform coreTransformOld = corePart.getCurrentTransform();

        // We should move all the other parts, relative to the root
        // And if those parts have physics handles, we should update them in the physics system too.
        OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());
        Map<ColliderHandle, Transform> newPartPositions = new HashMap<>();
        for (var kvp : physicsComponents.entrySet())
        {
            if (kvp.getKey().equals(CORE_PHYSICS))
                continue;

            Transform relativeTransformOld = coreTransformOld.globalToLocalTransform(kvp.getValue().getCurrentTransform());
            Transform partTransformNew = coreTransformNew.localToGlobalTransform(relativeTransformOld);
            kvp.getValue().teleportTo(physics, partTransformNew);
        }
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
        physicsComponents.put(id, new PhysicsComponent(worldPos, level().getGameTime(), physics.registerDynamic(aabbs, worldPos)));
    }

    @Override
    public void tick()
    {
        super.tick();
        checkPhysicsActivate();
        if(physicsActive)
        {
            OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(level());

            if(isControlledByLocalInstance())
            {
                // Respond to collision events from last frame, and update our location
                for(PhysicsComponent component : physicsComponents.values())
                    component.syncCollisionToComponent(physics);

                // For any vanilla calls that want to check in on this object, we keep the pos/yaw/pitch in sync
                syncTransformToEntity();
                sendPhysicsSync();

                // Let the entity handle these responses and build new forces
                tickPhysics();

                // Now send the results of the Force Model to the collision engine
                for(PhysicsComponent component : physicsComponents.values())
                    component.syncComponentToCollision(physics);
            }
            else
            {
                for(PhysicsComponent component : physicsComponents.values())
                    component.syncAndLerpToComponent(physics);
                syncTransformToEntity();
                tickPhysics();
                for(PhysicsComponent component : physicsComponents.values())
                    component.syncComponentToCollision(physics);
            }
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
                if(physics.isHandleInvalidated(component.getPhysicsHandle()))
                    shouldDeactivate = true;
            }

            // TODO: Check out of range of players

            if(shouldDeactivate)
            {
                stopPhysics();
                for(PhysicsComponent component : physicsComponents.values())
                {
                    physics.unregisterDynamic(component.getPhysicsHandle());
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
            PhysicsPacketHandler.sendToServer(createSyncMessage(new PhysicsSyncMessage.ToServer()));
        }
        else
        {
            PhysicsPacketHandler.sendToAllAroundPoint(createSyncMessage(new PhysicsSyncMessage.ToClient()), level().dimension(), position(), 100d, null);
        }
    }
    @Nonnull
    private PhysicsSyncMessage createSyncMessage(@Nonnull PhysicsSyncMessage message)
    {
        message.EntityID = getId();
        message.GameTick = level().getGameTime();
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
    public void handleSyncMessage(@Nonnull PhysicsSyncMessage syncMessage)
    {
        if(isControlledByLocalInstance())
            return;

        for(var stateChange : syncMessage.StateChanges)
        {
            PhysicsComponent component = getComponent(stateChange.PhysicsComponentID);
            if(component != null)
            {
                component.receiveUpdate(syncMessage.GameTick, stateChange);
            }
        }
    }
}
