package com.flansmod.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

public class Transform
{
    public Vector3d position;
    public Quaternionf orientation;

    public static Vector3f IdentityPosf()
    {
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }

    public static Vector3d IdentityPos()
    {
        return new Vector3d(0.0d, 0.0d, 0.0d);
    }

    public static Quaternionf IdentityQuat()
    {
        return new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public static Transform Identity()
    {
        return new Transform(IdentityPos(), IdentityQuat());
    }

    public static Transform RotationFromEuler(Vector3f euler)
    {
        return new Transform(IdentityPos(), new Quaternionf().rotateXYZ(euler.x, euler.y, euler.z));
    }

    public Transform(Vec3 pos)
    {
        position = new Vector3d(pos.x, pos.y, pos.z);
        orientation = IdentityQuat();
    }

    public Transform(Quaternionf ori)
    {
        position = IdentityPos();
        orientation = new Quaternionf(ori);
    }

    public Transform(Vector3d pos, Quaternionf ori)
    {
        position = new Vector3d(pos);
        orientation = new Quaternionf(ori);
    }

    public Transform copy()
    {
        return new Transform(position, orientation);
    }

    public static final Vector3d X_AXIS = new Vector3d(1d, 0d, 0d), RIGHT = X_AXIS;
    public static final Vector3d Y_AXIS = new Vector3d(0d, 1d, 0d), UP = Y_AXIS;
    public static final Vector3d Z_AXIS = new Vector3d(0d, 0d, 1d), FORWARD = Z_AXIS;
    private static Vec3 ToVec3(Vector3d v) { return new Vec3(v.x, v.y, v.z); }
    private static Vector3d ToVec3d(Vec3 v) { return new Vector3d(v.x, v.y, v.z); }

    public Vec3 PositionVec3() { return new Vec3(position.x, position.y, position.z); }
    public Vec3 UpVec3() { return ToVec3(Up()); }
    public Vec3 RightVec3() { return ToVec3(Right()); }
    public Vec3 ForwardVec3() { return ToVec3(Forward()); }

    public Vector3d Position() { return position; }
    public Vector3d Up() { return orientation.transform(UP); }
    public Vector3d Right() { return orientation.transform(RIGHT); }
    public Vector3d Forward() { return orientation.transform(FORWARD); }

    // Yucky, but needed until I write my own Quaternion class
    public Vec3 InverseTransformDirection(Vec3 direction) { return ToVec3(InverseTransformDirection(ToVec3d(direction))); }
    public Vec3 InverseTransformPosition(Vec3 position) { return ToVec3(InverseTransformPosition(ToVec3d(position))); }
    public Vec3 TransformDirection(Vec3 direction) { return ToVec3(TransformDirection(ToVec3d(direction))); }
    public Vec3 TransformPosition(Vec3 position) { return ToVec3(TransformPosition(ToVec3d(position))); }

    public Vector3d InverseTransformDirection(Vector3d direction)
    {
        orientation.transformInverse(direction);
        return direction;
    }

    public Vector3d InverseTransformPosition(Vector3d localPos)
    {
        localPos.sub(position);
        orientation.transformInverse(localPos);
        return localPos;
    }

    public Vector3d TransformDirection(Vector3d direction)
    {
        orientation.transform(direction);
        return direction;
    }

    public Vector3d TransformPosition(Vector3d localPos)
    {
        orientation.transform(localPos);
        localPos.add(position);
        return localPos;
    }

    public Transform Translate(Vec3 deltaPos)
    {
        position.x += deltaPos.x;
        position.y += deltaPos.y;
        position.z += deltaPos.z;
        return this;
    }

    public Transform TranslateLocal(Vector3d localDeltaPos)
    {
        Vector3d globalDeltaPos = new Vector3d();
        orientation.transform(localDeltaPos, globalDeltaPos);
        position.add(globalDeltaPos);
        return this;
    }
    public Transform TranslateLocal(double x, double y, double z)
    {
        Vector3d deltaPos = new Vector3d(x, y, z);
        orientation.transform(deltaPos, deltaPos);
        position.add(deltaPos);
        return this;
    }
    public Transform Translate(double x, double y, double z) { position.x += x; position.y += y; position.z += z; return this; }
    public Transform RotateLocal(Quaternionf rotation) { orientation.mul(rotation); return this; }
    public Transform RotateGlobal(Quaternionf rotation)
    {
        rotation.transform(position);
        orientation.mul(rotation);
        return this;
    }
    public Transform RotateAround(Vector3d origin, Quaternionf rotation)
    {
        position = rotation.transform(position.sub(origin)).add(origin);
        orientation.mul(rotation);
        return this;
    }
    public Transform RotateLocalEuler(float yaw, float pitch, float roll)
    {
        orientation.rotateLocalZ(roll * Maths.DegToRadF).rotateLocalX(pitch * Maths.DegToRadF).rotateLocalY(-yaw * Maths.DegToRadF);
        return this;
    }
    public Transform RotateYaw(float yAngle) { orientation.rotateY(-yAngle * Maths.DegToRadF); return this; }
    public Transform RotatePitch(float xAngle) { orientation.rotateX(xAngle * Maths.DegToRadF); return this; }
    public Transform RotateRoll(float zAngle) { orientation.rotateZ(zAngle * Maths.DegToRadF); return this; }
    public Transform RotateLocalYaw(float yAngle) { orientation.rotateLocalY(-yAngle * Maths.DegToRadF); return this; }
    public Transform RotateLocalPitch(float xAngle) { orientation.rotateLocalX(xAngle * Maths.DegToRadF); return this; }
    public Transform RotateLocalRoll(float zAngle) { orientation.rotateLocalZ(zAngle * Maths.DegToRadF); return this; }
}
