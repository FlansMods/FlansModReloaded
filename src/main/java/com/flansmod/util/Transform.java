package com.flansmod.util;

import com.flansmod.common.FlansMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.util.List;

public class Transform
{
    public final Vector3d position;
    public final Quaternionf orientation;

    public static Transform Identity()
    {
        return new Transform(Maths.IdentityPosD(), Maths.IdentityQuat());
    }

    public static Transform RotationFromEuler(Vector3f euler)
    {
        return new Transform(Maths.IdentityPosD(), new Quaternionf().rotateXYZ(euler.x, euler.y, euler.z));
    }

    public Transform(Vec3 pos)
    {
        position = new Vector3d(pos.x, pos.y, pos.z);
        orientation = Maths.IdentityQuat();
    }

    public Transform(Quaternionf ori)
    {
        position = Maths.IdentityPosD();
        orientation = new Quaternionf(ori);
    }

    public Transform(Vector3f pos, Quaternionf ori)
    {
        position = new Vector3d(pos.x, pos.y, pos.z);
        orientation = new Quaternionf(ori);
    }

    public Transform(Matrix4f pose)
    {
        position = new Vector3d(pose.transformPosition(new Vector3f()));
        orientation = pose.getNormalizedRotation(new Quaternionf());
    }

    public Transform(Vector3d pos, Quaternionf ori)
    {
        position = new Vector3d(pos);
        orientation = new Quaternionf(ori);
    }

    public static Transform Interpolate(Transform a, Transform b, double t)
    {
        return new Transform(
            a.position.lerp(b.position, t, new Vector3d()),
            a.orientation.slerp(b.orientation, (float)t, new Quaternionf())
        );
    }

    public static Transform Interpolate(List<Transform> transforms)
    {
        if(transforms.size() <= 0)
            return Transform.Identity();
        if(transforms.size() == 1)
            return transforms.get(0);

        Vector3d position = new Vector3d();
        Quaternionf[] orientations = new Quaternionf[transforms.size()];
        float[] weights = new float[transforms.size()];
        for(int i = 0; i < transforms.size(); i++)
        {
            position.add(transforms.get(i).position);
            orientations[i] = transforms.get(i).orientation;
            weights[i] = 1f / transforms.size();
        }

        return new Transform(
            position.mul(1d / transforms.size()),
            (Quaternionf) Quaternionf.slerp(orientations, weights, new Quaternionf()));
    }

    public Transform copy()
    {
        return new Transform(position, orientation);
    }

    public static Vector3d UnitX() { return new Vector3d(1d, 0d, 0d); }
    public static Vector3d UnitY() { return new Vector3d(0d, 1d, 0d); }
    public static Vector3d UnitZ() { return new Vector3d(0d, 0d, 1d); }
    private static Vec3 ToVec3(Vector3d v) { return new Vec3(v.x, v.y, v.z); }
    private static Vector3d ToVec3d(Vec3 v) { return new Vector3d(v.x, v.y, v.z); }

    public BlockPos BlockPos() { return new BlockPos(Maths.Floor(position.x), Maths.Floor(position.y), Maths.Floor(position.z)); }
    public Vec3 PositionVec3() { return new Vec3(position.x, position.y, position.z); }
    public Vec3 UpVec3() { return ToVec3(Up()); }
    public Vec3 RightVec3() { return ToVec3(Right()); }
    public Vec3 ForwardVec3() { return ToVec3(Forward()); }

    public Vector3d Position() { return position; }
    public Vector3d Up() { return orientation.transform(UnitY()); }
    public Vector3d Right() { return orientation.transform(UnitX()); }
    public Vector3d Forward() { return orientation.transform(UnitZ()); }

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
        return new Transform(
            new Vector3d(position.x + deltaPos.x, position.y + deltaPos.y, position.z + deltaPos.z),
            orientation);
    }

    public Transform TranslateLocal(Vector3d localDeltaPos)
    {
        Vector3d globalDeltaPos = new Vector3d();
        orientation.transform(localDeltaPos, globalDeltaPos);

        return new Transform(
            new Vector3d(position.x + globalDeltaPos.x, position.y + globalDeltaPos.y, position.z + globalDeltaPos.z),
            orientation);
    }
    public Transform TranslateLocal(double x, double y, double z)
    {
        Vector3d deltaPos = new Vector3d(x, y, z);
        orientation.transform(deltaPos, deltaPos);
        return new Transform(
            new Vector3d(position.x + deltaPos.x, position.y + deltaPos.y, position.z + deltaPos.z),
            orientation);
    }
    public Transform Translate(double x, double y, double z)
    {
        return new Transform(
            new Vector3d(position.x + x, position.y + y, position.z + z),
            orientation);
    }
    public Transform RotateLocal(Quaternionf rotation)
    {
        return new Transform(position,
            orientation.mul(rotation, new Quaternionf()));
    }
    public Transform RotateGlobal(Quaternionf rotation)
    {
        return new Transform(rotation.transform(position, new Vector3d()),
            orientation.mul(rotation, new Quaternionf()));
    }
    public Transform RotateAround(Vector3d origin, Quaternionf rotation)
    {
        Vector3d pos = new Vector3d(position);
        pos.sub(origin);
        rotation.transform(pos);
        pos.add(origin);
        return new Transform(pos, orientation.mul(rotation, new Quaternionf()));
    }
    public Transform RotateLocalEuler(float roll, float yaw, float pitch)
    {
        Quaternionf ori = new Quaternionf(orientation);
        ori.rotateLocalX(roll * Maths.DegToRadF);
        ori.rotateLocalZ(pitch * Maths.DegToRadF);
        ori.rotateLocalY(-yaw * Maths.DegToRadF);
        return new Transform(position, ori);
    }
    public Transform RotateYaw(float yAngle) { return new Transform(position, orientation.rotateY(-yAngle * Maths.DegToRadF, new Quaternionf())); }
    public Transform RotatePitch(float xAngle) { return new Transform(position, orientation.rotateX(xAngle * Maths.DegToRadF, new Quaternionf())); }
    public Transform RotateRoll(float zAngle) {  return new Transform(position, orientation.rotateZ(zAngle * Maths.DegToRadF, new Quaternionf()));  }
    public Transform RotateLocalYaw(float yAngle) {  return new Transform(position, orientation.rotateLocalY(-yAngle * Maths.DegToRadF, new Quaternionf()));  }
    public Transform RotateLocalPitch(float xAngle) {  return new Transform(position, orientation.rotateLocalX(xAngle * Maths.DegToRadF, new Quaternionf()));  }
    public Transform RotateLocalRoll(float zAngle) {  return new Transform(position, orientation.rotateLocalZ(zAngle * Maths.DegToRadF, new Quaternionf()));  }
}
