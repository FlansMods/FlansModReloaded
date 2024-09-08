package com.flansmod.common.projectiles;

import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.common.types.bullets.elements.ProjectileDefinition;
import com.flansmod.util.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.ArrayList;


public class BulletGuidance {
    public enum GuidanceType {NONE, BEAM_RIDING, BEAM_RIDING_TOP, BEAM_AND_LOCK, BEAM_AND_LOCK_TOP,LOCKON_SIMPLE, LOCKON_LEADING, LOCKON_TOP};

    public GuidanceType mode = GuidanceType.NONE;

    public static Vec3 BeamRide(BulletEntity bullet,Vec3 motionIn) //Projectile attempts to ride a beam from launch platform, in the direction that platform is facing
    {
        Entity e = bullet.GetContext().ActionGroup.Gun.GetShooter().Entity();
        Vec3 target = e.getEyePosition();

        double distance = target.distanceTo(bullet.position());

        distance += 2d;

        target = e.getEyePosition().add(e.getLookAngle().scale(distance));

        return RotateTo(bullet,motionIn,target);
    }

    public static Vec3 BeamRideTop(BulletEntity bullet, Vec3 motionIn){
        Vec3 target = GetLookingAt(bullet.GetContext().ActionGroup.Gun.GetShooter().Entity(),128);
        Vec3 initTarget = target;
        double lateralDist = lateralDistance(bullet.position(),initTarget);

        //Vec3 dir2 = target.subtract(bullet.GetContext().ActionGroup.Gun.GetShooter().Entity().position());
        //DebugRenderer.RenderLine(Transform.FromPos(bullet.GetContext().ActionGroup.Gun.GetShooter().Entity().getEyePosition()),1,new Vector4f(1,0,0,1),dir2);
        //DebugRenderer.RenderPoint(Transform.FromPos(initTarget),1,new Vector4f(1,0,0,1));

        if(lateralDist > bullet.GetContext().GetProjectileDef().topAttackRange)
        {
            Vec3 currentFlat = new Vec3(bullet.position().x,0,bullet.position().z);
            Vec3 targetFlat= new Vec3(initTarget.x,0,initTarget.z);
            Vec3 dir = targetFlat.subtract(currentFlat);

            dir = dir.normalize();
            target = bullet.position().add(dir.scale(bullet.GetContext().GetProjectileDef().topAttackHeight));

            //dir2 = target.subtract(bullet.position());
            //DebugRenderer.RenderLine(Transform.FromPos(bullet.position()),1,new Vector4f(1,0,1,1),dir2);

            target = new Vec3(target.x,Math.max(bullet.GetContext().ActionGroup.Gun.GetShooter().Entity().position().y+bullet.GetContext().GetProjectileDef().topAttackHeight,
                    initTarget.y+bullet.GetContext().GetProjectileDef().topAttackHeight
            ),target.z);
            //dir2 = target.subtract(bullet.position());
            //DebugRenderer.RenderLine(Transform.FromPos(bullet.position()),1,new Vector4f(1,1,0,1),dir2);
        }

        return RotateTo(bullet, motionIn,target);
    }


    public static Vec3 TopAttackLocked(BulletEntity bullet, Vec3 motionIn){
        if(bullet.LockedOnTo == null){
            return motionIn;
        }

        Vec3 target = bullet.LockedOnTo.position();

        double lateralDist = lateralDistance(bullet.position(),bullet.LockedOnTo.position());

        if(lateralDist > bullet.GetContext().GetProjectileDef().topAttackRange){
            Vec3 currentFlat = new Vec3(bullet.position().x,0,bullet.position().z);
            Vec3 targetFlat= new Vec3(bullet.LockedOnTo.position().x,0,bullet.LockedOnTo.position().z);
            Vec3 dir = targetFlat.subtract(currentFlat);
            dir = dir.normalize();
            target = bullet.position().add(dir.scale(3));
            target = new Vec3(target.x,Math.max(bullet.GetContext().ActionGroup.Gun.GetShooter().Entity().position().y+bullet.GetContext().GetProjectileDef().topAttackHeight,
                    bullet.LockedOnTo.position().y+bullet.GetContext().GetProjectileDef().topAttackHeight
            ),target.z);
        }

        return RotateTo(bullet, motionIn,target);
    }

    public static Vec3 RotateTowards(BulletEntity bullet,Vec3 motionIn) //Rotate projectile to face target
    {
        if(bullet.LockedOnTo == null){
            return motionIn;
        }
        if(!checkTrack(bullet,bullet.LockedOnTo)){
            return motionIn;
        }
        return RotateTo(bullet, motionIn,bullet.LockedOnTo.position());
    }

    public static Vec3 RotateTowardsProportional(BulletEntity bullet,Vec3 motionIn) //Projectile will attempt to lead target instead of simple facing it
    {
        if(bullet.LockedOnTo == null){
            return motionIn;
        }
        if(!checkTrack(bullet,bullet.LockedOnTo)){
            return motionIn;
        }
        float distance = bullet.distanceTo(bullet.LockedOnTo);

        float speed = (float)motionIn.length();

        float timeToReach = distance/speed;

        Vec3 predicted = bullet.LockedOnTo.position().add(bullet.LockedOnTo.getDeltaMovement().scale(timeToReach)); //Could use average delta movement to smooth out??

        //Vec3 dir2 = predicted.subtract(bullet.position());
        //DebugRenderer.RenderLine(Transform.FromPos(bullet.position()),1,new Vector4f(1,0,0,1),dir2);
        //DebugRenderer.RenderPoint(Transform.FromPos(initTarget),1,new Vector4f(1,0,0,1));

        return RotateTo(bullet, motionIn,predicted);
    }

    public static Vec3 RotateTo(BulletEntity bullet, Vec3 current, Vec3 target){

        double speed = current.length();

        current = current.normalize();

        Vec3 targetDir = (target.subtract(bullet.position())).normalize();

        float rotAngle = (float)Math.acos(current.dot(targetDir));

        Vec3 rotAxis = current.cross(targetDir).normalize();

        return rotateVector(current.scale(speed),rotAxis,Math.min(rotAngle,bullet.GetContext().TurnRate()));
    }

    public static Vec3 rotateVector(Vec3 vec, Vec3 axis, double theta){
        double x, y, z;
        double u, v, w;
        x=vec.x();y=vec.y();z=vec.z();
        u=axis.x();v=axis.y();w=axis.z();
        double xPrime = u*(u*x + v*y + w*z)*(1d - Math.cos(theta))
                + x*Math.cos(theta)
                + (-w*y + v*z)*Math.sin(theta);
        double yPrime = v*(u*x + v*y + w*z)*(1d - Math.cos(theta))
                + y*Math.cos(theta)
                + (w*x - u*z)*Math.sin(theta);
        double zPrime = w*(u*x + v*y + w*z)*(1d - Math.cos(theta))
                + z*Math.cos(theta)
                + (-v*x + u*y)*Math.sin(theta);
        return new Vec3(xPrime, yPrime, zPrime);
    }

    public static double lateralDistance(Vec3 p1, Vec3 p2) {
        double d0 = p1.x - p2.x;
        //double d1 = p1.y - p2.y;
        double d2 = p1.z - p2.z;
        return Math.sqrt(d0 * d0 + d2 * d2);
    }

    public static boolean checkLock(Entity origin, Entity target, ProjectileDefinition bulletDef)
    {
        Vec3 v1 = origin.getLookAngle();

        Vec3 v2 = target.position().subtract(origin.position());
        v2 = v2.normalize();

        return checkWithinCone(v1,v2,bulletDef.lockCone);
    }

    public static boolean checkLock(Entity origin, Entity target, float lockCone)
    {
        Vec3 v1 = origin.getLookAngle();

        Vec3 v2 = target.position().subtract(origin.position());
        v2 = v2.normalize();

        return checkWithinCone(v1,v2,lockCone);
    }

    public static boolean checkTrack(Entity origin, Entity target, ProjectileDefinition bulletDef)
    {
        Vec3 v1 = origin.getLookAngle();

        Vec3 v2 = target.position().subtract(origin.position());
        v2 = v2.normalize();

        return checkWithinCone(v1,v2,bulletDef.trackCone);
    }

    public static boolean checkTrack(BulletEntity bullet,Entity target)
    {
        Vec3 v1 = bullet.getDeltaMovement();

        Vec3 v2 = target.position().subtract(bullet.position());
        v2 = v2.normalize();

        return checkWithinCone(v1,v2,bullet.GetContext().TrackCone());
    }

    public static boolean checkLock(BulletEntity bullet,Entity target)
    {
        Vec3 v1 = bullet.getDeltaMovement();

        Vec3 v2 = target.position().subtract(bullet.position());
        v2 = v2.normalize();

        return checkWithinCone(v1,v2,bullet.GetContext().LockCone()) && CanSeeEntity(bullet,target);
    }

    public static boolean checkWithinCone(Vec3 v1, Vec3 v2, float coneSize)
    {
        double dot = v1.dot(v2);
        double v1Length = v1.length();
        double v2Length = v2.length();

        double angle = Math.acos(dot/(v1Length*v2Length));
        angle = Math.toDegrees(angle);
        return angle < coneSize;
    }

    public static Entity getLockOnTarget(ArrayList<Class> targetTypes, BulletEntity bullet){
        return getLockOnTarget(targetTypes,bullet,bullet.GetContext().LockRange(),bullet.GetContext().LockCone());
    }

    public static Entity getLockOnTarget(ArrayList<Class> targetTypes, Entity shooter, ProjectileDefinition def){
        return getLockOnTarget(targetTypes,shooter,def.lockRange,def.lockCone);
    }

    public static Entity getLockOnTarget(ArrayList<Class> targetTypes, Entity shooter, float lockRange, float lockCone)
    {
        float closestRange = 90000;
        Entity closest = null;
        //Iterate through target types, check within min range then check if in lock cone
        //TODO: CHECK TARGET IS VISIBLE
        for(Class c : targetTypes){
            for(Object nearest : shooter.level().getEntitiesOfClass(
                    c,
                    new AABB(shooter.getX() - lockRange, shooter.getY() - lockRange, shooter.getZ() - lockRange,
                            shooter.getX() + lockRange, shooter.getY() + lockRange, shooter.getZ()  + lockRange)))
            {
                if(shooter.distanceTo((Entity)nearest) < closestRange)
                {
                    if(checkLock(shooter,(Entity)nearest,lockCone))
                    {
                        if(CanSeeEntity(shooter,(Entity)nearest)) {
                            closestRange = shooter.distanceTo((Entity) nearest);
                            closest = (Entity) nearest;
                        }
                    }
                }
            }
        }

        return closest;
    }

    public static BulletGuidance.GuidanceType GuidanceType(ProjectileDefinition projectileDef){ //Bit stinky
        if(projectileDef == null) return BulletGuidance.GuidanceType.NONE;

        return GuidanceType(projectileDef.guidanceType);
    }

    public static GuidanceType GuidanceType(String type){ //Bit stinky

        return switch (type) {
            case "beam_riding"          -> GuidanceType.BEAM_RIDING;
            case "beam_riding_top"      -> GuidanceType.BEAM_RIDING_TOP;
            case "beam_and_lock"        -> GuidanceType.BEAM_AND_LOCK;
            case "beam_and_lock_top"    -> GuidanceType.BEAM_AND_LOCK_TOP;
            case "lock_on"              -> GuidanceType.LOCKON_SIMPLE;
            case "lock_on_predictive"   -> GuidanceType.LOCKON_LEADING;
            case "lock_on_top"          -> GuidanceType.LOCKON_TOP;
            default                     -> GuidanceType.NONE;
        };
    }

    public static boolean HasLockOn(String type){
        return switch (type) {
            case "beam_and_lock"        -> true;
            case "beam_and_lock_top"    -> true;
            case "lock_on"              -> true;
            case "lock_on_predictive"   -> true;
            case "lock_on_top"          -> true;
            default                     -> false;
        };
    }

    public static boolean CanSeeEntity(Entity e1, Entity e2){
        return CanSee(e1.getEyePosition(),e2.position(),e1.level());
    }

    public static boolean CanSee(Vec3 v1, Vec3 v2, Level level){
       double distance = v1.distanceTo(v2);

       Vec3 dir = v2.subtract(v1);
       dir = dir.normalize();

        ClipContext clipContext = new ClipContext(
                v1,
                v2,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
        );
        BlockHitResult blockHit = level.clip(clipContext);
        if(blockHit.getType() != HitResult.Type.MISS)
        {
            return false;
        }

       return true;
    }

    public static Vec3 GetLookingAt(Entity e1, float maxRange){

        Vec3 pos1 = e1.getEyePosition();
        Vec3 pos2 = pos1.add(e1.getLookAngle().scale(maxRange));

        ClipContext clipContext = new ClipContext(
                pos1,
                pos2,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
        );
        BlockHitResult blockHit = e1.level().clip(clipContext);
        if(blockHit.getType() != HitResult.Type.MISS)
        {
            return blockHit.getLocation();
        }

        return pos2;
    }
}
