package com.flansmod.physics.common.collision.threading;

import com.flansmod.physics.common.collision.*;
import com.flansmod.physics.common.units.AngularVelocity;
import com.flansmod.physics.common.units.LinearAcceleration;
import com.flansmod.physics.common.units.LinearVelocity;
import com.flansmod.physics.common.units.OffsetAcceleration;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.ProjectedRange;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CollisionTaskResolveDynamic
        implements ICollisionTask<CollisionTaskResolveDynamic.Input, CollisionTaskResolveDynamic.Output>
{
    public record Input(@Nonnull IConstDynamicObject Dynamic,
                        @Nonnull List<DynamicCollisionEvent> DynamicCollisions,
                        @Nonnull List<StaticCollisionEvent> StaticCollisions)
    {

    }
    public record Output(@Nonnull OffsetAcceleration ReactionAcceleration)
    {

    }
    @Nonnull
    public static CollisionTaskResolveDynamic of(@Nonnull ColliderHandle handle,
                                                 @Nonnull IConstDynamicObject dynamic,
                                                 @Nonnull List<DynamicCollisionEvent> dynamicCollisions,
                                                 @Nonnull List<StaticCollisionEvent> staticCollisions)
    {
        var task = new CollisionTaskResolveDynamic(handle);
        task.prepare(new Input(dynamic, dynamicCollisions, staticCollisions));
        return task;
    }

    @Nonnull
    public final ColliderHandle Handle;
    @Nullable
    private Input Input;
    @Nullable
    private Output Output;

    private CollisionTaskResolveDynamic(@Nonnull ColliderHandle handle)
    {
        Handle = handle;
    }

    @Override
    public void prepare(@Nonnull CollisionTaskResolveDynamic.Input input)
    {

    }

    @Override
    public boolean canRun() { return true; }

    @Override
    public void run()
    {
        LinearVelocity linearV = Input.Dynamic.getNextFrameLinearVelocity();
        AngularVelocity angularV = Input.Dynamic.getNextFrameAngularVelocity();

        Vec3 v = linearV.applyOneTick();
        Quaternionf q = angularV.applyOneTick();

        ProjectedRange xMoveReq = null, yMoveReq = null, zMoveReq = null;
        List<StaticCollisionEvent> relevantX = new ArrayList<>(), relevantY = new ArrayList<>(), relevantZ = new ArrayList<>();
        for (StaticCollisionEvent collision : Input.StaticCollisions)
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

        if(OBBCollisionSystem.DEBUG_SETTING_ONLY_LINEAR_REACTIONS)
            Output = new Output(OffsetAcceleration.offset(reactionAcc, Input.Dynamic.getCurrentLocation().positionVec3()));
        else
            Output = new Output(OffsetAcceleration.offset(reactionAcc, forceOrigin));
        //Input.Dynamic.ExtrapolateNextFrameWithReaction();

        //dyn.SetLinearVelocity(LinearVelocity.blocksPerTick(v));
        //dyn.SetAngularVelocity(angularV.scale(maxT));

        // TODO: CHECK, we used to set the v/q direct, now we apply reaction
        // dyn.ExtrapolateNextFrame(v, q);
    }

    @Override
    public boolean isComplete() { return Output != null; }
    @Override
    public boolean canCancel() { return false; }
    @Override
    public void cancel() { }
    @Override @Nullable
    public Output getResult()
    {
        return Output;
    }
}
