package com.flansmod.common.gunshots;

import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    public PlayerHitbox(@Nonnull EPlayerHitArea hitArea, @Nonnull Transform trans, @Nonnull Vector3f half)
    {
        transform = trans;
        halfExtents = half;
        area = hitArea;
    }

    public boolean Raycast(@Nonnull Vec3 startPos, @Nonnull Vec3 endPos, @Nullable Vector3d outPos)
    {
        return Maths.RayBoxIntersect(
            startPos,
            endPos,
            transform,
            halfExtents,
            outPos);
    }

    // ----------------------------------------------------------
    // Debug Rendering
    @OnlyIn(Dist.CLIENT)
    public void debugRender(@Nonnull Vec3 withOffset, @Nonnull Vector4f colour)
    {
        Transform renderPos = Transform.Compose(Transform.FromPos(withOffset), transform); //.Translate(0.0d, 0.0d, 2.0d);

        DebugRenderer.RenderCube(renderPos, 1, colour, halfExtents);
        DebugRenderer.RenderAxes(renderPos, 1, new Vector4f(1f, 1f, 1f, 1f));
        //Vector3f quarterExtents = new Vector3f(halfExtents.x * 0.5f, halfExtents.y * 0.5f, halfExtents.z * 0.5f);
        //DebugRenderer.RenderCube(renderPos, 40, colour, quarterExtents);

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
