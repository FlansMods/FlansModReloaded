package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public record Ray(@Nonnull Vec3 Origin, @Nonnull Vec3 Vector)
{
    @Nonnull
    public AxisAngle4f getAngularComponent(@Nonnull Transform actingOn)
    {
        Vec3 relativeOffset = Origin.subtract(actingOn.positionVec3());
        Vec3 axis = relativeOffset.cross(Vector).normalize();
        double magnitude = Vector.length() * relativeOffset.length();
        return new AxisAngle4f((float)magnitude, (float)axis.x, (float)axis.y, (float)axis.z);
    }
}
