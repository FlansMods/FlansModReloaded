package com.flansmod.common.gunshots;

import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.util.Transform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PlayerHitbox
{
    // The position and rotation of the centerpoint of this box.
    public Transform transform;
    // The half-extents of this box, centered on the transform
    public Vector3f halfExtents;

    //public RotatedAxes axes;
    //public Vec3 rP;
    //public Vec3 o;
    //public Vec3 d;
    public EPlayerHitArea area;

    public PlayerHitbox(EPlayerHitArea hitArea, Transform trans, Vector3f half)
    {
        transform = trans;
        halfExtents = half;
        area = hitArea;
    }

    public PlayerHitResult Raycast(Player player, Vec3 origin, Vec3 motion)
    {
        // Localise
        transform.InverseTransformPosition(origin);
        transform.InverseTransformDirection(motion);

        // We now have an AABB starting at -halfExtents and with dimensions 2*halfExtents and our ray in the same coordinate system
        // We are looking for a point at which the ray enters the box, so we need only consider faces that the ray can see. Partition the space into 3 areas in each axis

        // X - axis and faces x = -half.x, and x = half.x
        if(motion.x != 0F)
        {
            if(origin.x < -halfExtents.x) //Check face -half.x
            {
                double intersectTime = (-halfExtents.x - origin.x) / motion.x;
                double intersectY = origin.y + motion.y * intersectTime;
                double intersectZ = origin.z + motion.z * intersectTime;
                if(-halfExtents.y <= intersectY && intersectY <= halfExtents.y
                && -halfExtents.z <= intersectZ && intersectZ <= halfExtents.z)
                    return new PlayerHitResult(player, this);
            }
            else if(origin.x > halfExtents.x) //Check face +half.x
            {
                double intersectTime = (halfExtents.x - origin.x) / motion.x;
                double intersectY = origin.y + motion.y * intersectTime;
                double intersectZ = origin.z + motion.z * intersectTime;
                if(-halfExtents.y <= intersectY && intersectY <= halfExtents.y
                && -halfExtents.z <= intersectZ && intersectZ <= halfExtents.z)
                    return new PlayerHitResult(player, this);
            }
        }

        // Z - axis and faces z = -half.z and z = half.z
        if(motion.z != 0F)
        {
            if(origin.z < -halfExtents.z) // Check face z = -half.z
            {
                double intersectTime = (-halfExtents.z - origin.z) / motion.z;
                double intersectX = origin.x + motion.x * intersectTime;
                double intersectY = origin.y + motion.y * intersectTime;
                if(-halfExtents.x <= intersectX && intersectX <= halfExtents.x
                && -halfExtents.y <= intersectY && intersectY <= halfExtents.y)
                    return new PlayerHitResult(player, this);
            }
            else if(origin.z > halfExtents.z) //Check face z = +half.z
            {
                double intersectTime = (halfExtents.z - origin.z) / motion.z;
                double intersectX = origin.x + motion.x * intersectTime;
                double intersectY = origin.y + motion.y * intersectTime;
                if(-halfExtents.x <= intersectX && intersectX <= halfExtents.x
                && -halfExtents.y <= intersectY && intersectY <= halfExtents.y)
                    return new PlayerHitResult(player, this);
            }
        }

        // Y - axis and faces y = -half.y and y = +half.y
        if(motion.y != 0F)
        {
            if(origin.y < -halfExtents.y) // Check face y = -half.y
            {
                double intersectTime = (-halfExtents.y - origin.y) / motion.y;
                double intersectX = origin.x + motion.x * intersectTime;
                double intersectZ = origin.z + motion.z * intersectTime;
                if(-halfExtents.x <= intersectX && intersectX <= halfExtents.x
                && -halfExtents.z <= intersectZ && intersectZ <= halfExtents.z)
                    return new PlayerHitResult(player, this);
            }
            else if(origin.y > halfExtents.y) // Check face y = +half.y
            {
                double intersectTime = (halfExtents.y - origin.y) / motion.y;
                double intersectX = origin.x + motion.x * intersectTime;
                double intersectZ = origin.z + motion.z * intersectTime;
                if(-halfExtents.x <= intersectX && intersectX <= halfExtents.x
                && -halfExtents.z <= intersectZ && intersectZ <= halfExtents.z)
                    return new PlayerHitResult(player, this);
            }
        }

        return null;

    }

    // ----------------------------------------------------------
    // Debug Rendering
    @OnlyIn(Dist.CLIENT)
    public void debugRender(Vector4f colour)
    {
        Transform renderPos = transform.copy(); //.Translate(0.0d, 0.0d, 2.0d);
        
        DebugRenderer.RenderCube(renderPos, 40, colour, halfExtents);
        renderPos.TranslateLocal(new Vector3d(0.0d, 0.0d, 1.0d));
        Vector3f quarterExtents = new Vector3f(halfExtents.x * 0.5f, halfExtents.y * 0.5f, halfExtents.z * 0.5f);
        DebugRenderer.RenderCube(renderPos, 40, colour, quarterExtents);

/*
        //Vector3f boxOrigin = new Vector3f(pos.x + rP.x, pos.y + rP.y, pos.z + rP.z);
        //world.spawnEntity(new EntityDebugAABB(world, boxOrigin, d, 2, 1F, 1F, 0F, axes.getYaw(), axes.getPitch(), axes.getRoll(), o));
        if(type != EnumHitboxType.RIGHTARM)
            return;
        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 3; j++)
                for(int k = 0; k < 3; k++)
                {
                    Vector3f point = new Vector3f(o.x + d.x * i / 2, o.y + d.y * j / 2, o.z + d.z * k / 2);
                    point = axes.findLocalVectorGlobally(point);
                    if(FlansMod.DEBUG && world.isRemote)
                        world.spawnEntity(new EntityDebugDot(world, new Vector3f(pos.x + rP.x + point.x, pos.y + rP.y + point.y, pos.z + rP.z + point.z), 1, 0F, 1F, 0F));
                }
*/
    }

}
