package com.flansmod.util;

import com.flansmod.common.FlansMod;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Debug;
import org.joml.*;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;

public class Transform
{
    private static final Vector3d ZERO_POS = new Vector3d();
    private static final Quaternionf IDENTITY_QUAT = new Quaternionf();
    public static final Transform IDENTITY = new Transform("Identity");
    public static Transform Error(String errorMessage){
        return new Transform() {
            @Override
            public String toString()
            {
                return "ERROR:" + errorMessage;
            }
        };
    }
    public static Transform FromPoseStack(String sourceName, PoseStack poseStack) {
        return new Transform("PoseStack[" + sourceName +"]", poseStack.last().pose());
    }
    public PoseStack ToNewPoseStack()
    {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(Position.x, Position.y, Position.z);
        poseStack.mulPose(Orientation);
        poseStack.scale(Scale, Scale, Scale);
        return poseStack;
    }

    private final String DebugInfo;
    public final Vector3d Position;
    public final Quaternionf Orientation;
    public final float Scale;

    public static Quaternionf FromEuler(Vector3f euler)
    {
        return new Quaternionf()
            .rotateLocalZ(euler.z * Maths.DegToRadF)
            .rotateLocalX(euler.x * Maths.DegToRadF)
            .rotateLocalY(euler.y * Maths.DegToRadF);
    }
    public static Quaternionf FromEuler(float pitch, float yaw, float roll)
    {
        return new Quaternionf()
            .rotateLocalZ(roll * Maths.DegToRadF)
            .rotateLocalX(pitch * Maths.DegToRadF)
            .rotateLocalY(yaw * Maths.DegToRadF);
    }
    public static Vector3f ToEuler(Quaternionf quat)
    {
        return quat.getEulerAnglesXYZ(new Vector3f()).mul(Maths.RadToDegF);
    }


    public Transform(String debugInfo, double x, double y, double z, float yaw, float pitch, float roll, float scale)
    {
        DebugInfo = debugInfo;
        Position = new Vector3d(x, y, z);
        Orientation = new Quaternionf(); // TODO;
        Scale = scale;
    }
    public Transform(String debugInfo, double x, double y, double z, Quaternionf rotation, float scale)
    {
        DebugInfo = debugInfo;
        Position = new Vector3d(x, y, z);
        Orientation = new Quaternionf(rotation);
        Scale = scale;
    }
    public Transform(String debugInfo, double x, double y, double z, float scale)
    {
        DebugInfo = debugInfo;
        Position = new Vector3d(x, y, z);
        Orientation = IDENTITY_QUAT;
        Scale = scale;
    }
    public Transform(String debugInfo, float scale)
    {
        DebugInfo = debugInfo;
        Position = ZERO_POS;
        Orientation = IDENTITY_QUAT;
        Scale = scale;
    }

    // From complete transform
    public Transform(Transform other) { this(other.DebugInfo, other.Position, other.Orientation, other.Scale); }
    public Transform(String debugInfo, Matrix4f pose)
    {
        this(debugInfo,
             pose.transformPosition(new Vector3f()),
             pose.getUnnormalizedRotation(new Quaternionf()),
             pose.getScale(new Vector3f()).x);
    }
    public Transform(String debugInfo, ItemTransform itemTransform) { this(debugInfo, itemTransform.translation, FromEuler(itemTransform.rotation), itemTransform.scale.x); }

    public Transform(String debugInfo, Vec3 pos, Quaternionf ori, float scale) { this(debugInfo, pos.x, pos.y, pos.z, ori, scale); }
    public Transform(String debugInfo, Vector3d pos, Quaternionf ori, float scale) { this(debugInfo, pos.x, pos.y, pos.z, ori, scale); }
    public Transform(String debugInfo, Vector3f pos, Quaternionf ori, float scale) { this(debugInfo, pos.x, pos.y, pos.z, ori, scale); }
    public Transform(String debugInfo, Vec3 pos, Vector3f euler, float scale) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), scale); }
    public Transform(String debugInfo, Vector3d pos, Vector3f euler, float scale) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), scale); }
    public Transform(String debugInfo, Vector3f pos, Vector3f euler, float scale) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), scale); }

    // From pos and ori
    public Transform(String debugInfo, Vec3 pos, Quaternionf ori) { this(debugInfo, pos.x, pos.y, pos.z, ori, 1f); }
    public Transform(String debugInfo, Vector3d pos, Quaternionf ori) { this(debugInfo, pos.x, pos.y, pos.z, ori, 1f); }
    public Transform(String debugInfo, Vector3f pos, Quaternionf ori) { this(debugInfo, pos.x, pos.y, pos.z, ori, 1f); }
    public Transform(String debugInfo, Vec3 pos, Vector3f euler) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), 1f); }
    public Transform(String debugInfo, Vector3d pos, Vector3f euler) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), 1f); }
    public Transform(String debugInfo, Vector3f pos, Vector3f euler) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), 1f); }

    // From just ori
    public Transform(String debugInfo, Quaternionf ori) { this(debugInfo, 0d, 0d, 0d, ori, 1f); }

    // From pos and scale
    public Transform(String debugInfo, Vec3 pos, float scale) { this(debugInfo, pos.x, pos.y, pos.z, scale); }
    public Transform(String debugInfo, Vector3i pos, float scale) { this(debugInfo, pos.x, pos.y, pos.z, scale); }
    public Transform(String debugInfo, Vector3d pos, float scale) { this(debugInfo, pos.x, pos.y, pos.z, scale); }
    public Transform(String debugInfo, Vector3f pos, float scale) { this(debugInfo, pos.x, pos.y, pos.z, scale); }

    // From just pos
    public Transform(String debugInfo, Vec3 pos) { this(debugInfo, pos.x, pos.y, pos.z, 1f); }
    public Transform(String debugInfo, Vector3i pos) { this(debugInfo, pos.x, pos.y, pos.z, 1f); }
    public Transform(String debugInfo, Vector3d pos) { this(debugInfo, pos.x, pos.y, pos.z, 1f); }
    public Transform(String debugInfo, Vector3f pos) { this(debugInfo, pos.x, pos.y, pos.z, 1f); }

    public Transform(String debugInfo) { this(debugInfo, 1f); }
    public Transform() { this("", 1f); }

    // -- Transform Position --
    //  Applied as follows: Rotate, then translate, then scale.
    public Vec3 LocalToGlobalPosition(Vec3 localPos)
    {
        Vector3d scratch = new Vector3d(localPos.x, localPos.y, localPos.z);
        scratch.mul(Scale);
        Orientation.transform(scratch);
        scratch.add(Position);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    public Vec3 GlobalToLocalPosition(Vec3 globalPos)
    {
        Vector3d scratch = new Vector3d(globalPos.x, globalPos.y, globalPos.z);
        scratch.sub(Position);
        Orientation.transformInverse(scratch);
        scratch.mul(1.0f / Scale);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }

    // -- Transform Velocity --
    //  In this case, we no longer care about the position offset
    public Vec3 LocalToGlobalVelocity(Vec3 localVelocity)
    {
        Vector3d scratch = new Vector3d(localVelocity.x, localVelocity.y, localVelocity.z);
        Orientation.transform(scratch);
        scratch.mul(Scale);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    public Vec3 GlobalToLocalVelocity(Vec3 globalVelocity)
    {
        Vector3d scratch = new Vector3d(globalVelocity.x, globalVelocity.y, globalVelocity.z);
        scratch.mul(1.0f / Scale);
        Orientation.transformInverse(scratch);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }

    // -- Transform Direction --
    //  In this case, we also don't want to scale. If it's normalized in, it should be normalized out
    public Vec3 LocalToGlobalDirection(Vec3 localDirection)
    {
        Vector3d scratch = new Vector3d(localDirection.x, localDirection.y, localDirection.z);
        Orientation.transform(scratch);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    public Vec3 GlobalToLocalDirection(Vec3 globalDirection)
    {
        Vector3d scratch = new Vector3d(globalDirection.x, globalDirection.y, globalDirection.z);
        Orientation.transformInverse(scratch);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }

    // -- Transform Orientation --
    public Quaternionf LocalToGlobalOrientation(Quaternionf localOri)
    {
        return Orientation.mul(localOri, new Quaternionf());
    }
    public Quaternionf GlobalToLocalOrientation(Quaternionf globalOri)
    {
        return Orientation.invert(new Quaternionf()).mul(globalOri, new Quaternionf());
    }

    // -- Transform Scale --
    // Technically including these for completion
    public float LocalToGlobalScale(float localScale)
    {
        return localScale * Scale;
    }
    public float GlobalToLocalScale(float globalScale)
    {
        return globalScale / Scale;
    }

    // And the complete transformers
    public Transform LocalToGlobalTransform(Transform localTransform)
    {
        return new Transform(
            "ToGlobal["+toString()+"]",
            LocalToGlobalPosition(localTransform.PositionVec3()),
            LocalToGlobalOrientation(localTransform.Orientation),
            LocalToGlobalScale(localTransform.Scale));
    }
    public Transform GlobalToLocalTransform(Transform globalTransform)
    {
        return new Transform(
            "ToLocal["+toString()+"]",
            GlobalToLocalPosition(globalTransform.PositionVec3()),
            GlobalToLocalOrientation(globalTransform.Orientation),
            GlobalToLocalScale(globalTransform.Scale));
    }

    public static Transform Interpolate(Transform a, Transform b, float t)
    {
        return new Transform(
            "Interpolate["+a.toString()+b.toString()+"]",
            a.Position.lerp(b.Position, t, new Vector3d()),
            a.Orientation.slerp(b.Orientation, t, new Quaternionf()),
            Maths.LerpF(a.Scale, b.Scale, t));
    }

    public static Transform Interpolate(List<Transform> transforms)
    {
        if(transforms.size() <= 0)
            return Transform.IDENTITY;
        if(transforms.size() == 1)
            return transforms.get(0);

        Vector3d position = new Vector3d();
        Quaternionf[] orientations = new Quaternionf[transforms.size()];
        float[] weights = new float[transforms.size()];
        for(int i = 0; i < transforms.size(); i++)
        {
            position.add(transforms.get(i).Position);
            orientations[i] = transforms.get(i).Orientation;
            weights[i] = 1f / transforms.size();
        }

        return new Transform(
            "Interpolate["+transforms.size()+"]",
            position.mul(1d / transforms.size()),
            (Quaternionf) Quaternionf.slerp(orientations, weights, new Quaternionf()));
    }

    public static Transform Interpolate(List<Transform> transforms, float[] weights)
    {
        if(transforms.size() <= 0)
            return Transform.IDENTITY;
        if(transforms.size() == 1)
            return transforms.get(0);

        Vector3d position = new Vector3d();
        Quaternionf[] orientations = new Quaternionf[transforms.size()];
        float totalWeight = 0.0f;
        for(int i = 0; i < transforms.size(); i++)
        {
            position.add(transforms.get(i).Position.mul(weights[i], new Vector3d()));
            orientations[i] = transforms.get(i).Orientation;
            totalWeight += weights[i];
        }

        return new Transform(
            "Interpolate["+transforms.size()+"]",
            position.mul(1d / totalWeight),
            (Quaternionf) Quaternionf.slerp(orientations, weights, new Quaternionf()));
    }

    public BlockPos BlockPos() { return new BlockPos(Maths.Floor(Position.x), Maths.Floor(Position.y), Maths.Floor(Position.z)); }
    public Vec3 PositionVec3() { return new Vec3(Position.x, Position.y, Position.z); }
    public Vec3 ForwardVec3() { return LocalToGlobalDirection(new Vec3(0d, 0d, -1d)); }
    public Vec3 UpVec3() { return LocalToGlobalDirection(new Vec3(0d, 1d, 0d)); }
    public Vec3 RightVec3() { return LocalToGlobalDirection(new Vec3(1d, 0d, 0d)); }

    private static final NumberFormat FLOAT_FORMAT = new DecimalFormat("#.##");

    @Override
    public String toString()
    {
        boolean isZeroPos = Maths.Approx(Position.lengthSquared(), 0d);
        boolean isIdentityRot = Maths.Approx(Orientation.x, 0f) && Maths.Approx(Orientation.y, 0f) && Maths.Approx(Orientation.z, 0f) && Maths.Approx(Orientation.w, 1f);
        boolean isOneScale = Maths.Approx(Scale, 1f);
        if(isZeroPos && isIdentityRot && isOneScale)
            return "[Identity]{"+DebugInfo+"}";
        else
        {
            StringBuilder output = new StringBuilder("[");
            if(!isZeroPos)
            {
                output.append("Translate:").append(Position.toString(FLOAT_FORMAT));
            }
            if(!isIdentityRot)
            {
                if(!isZeroPos)
                    output.append('|');
                output.append("Rotate:").append(ToEuler(Orientation).toString(FLOAT_FORMAT));
            }
            if(!isOneScale)
            {
                if(!isZeroPos || !isIdentityRot)
                    output.append('|');
                output.append("Scale:").append(Scale);
            }
            output.append("] -- {");
            output.append(DebugInfo);
            output.append('}');
            return output.toString();
        }
    }


    /*
    public Transform copy()
    {
        return new Transform(position, orientation);
    }

    public Transform ScalePosition(float scalar) { return new Transform(position.mul(scalar, new Vector3d()), orientation); }
    public Transform ScalePosition(float dx, float dy, float dz) { return new Transform(new Vector3d(position.x * dx, position.y * dy, position.z * dz), orientation); }
    public static Vector3d UnitX() { return new Vector3d(1d, 0d, 0d); }
    public static Vector3d UnitY() { return new Vector3d(0d, 1d, 0d); }
    public static Vector3d UnitZ() { return new Vector3d(0d, 0d, 1d); }
    private static Vec3 ToVec3(Vector3d v) { return new Vec3(v.x, v.y, v.z); }
    private static Vector3d ToVec3d(Vec3 v) { return new Vector3d(v.x, v.y, v.z); }

    public BlockPos BlockPos() { return new BlockPos(Maths.Floor(Position.x), Maths.Floor(Position.y), Maths.Floor(Position.z)); }
    public Vec3 PositionVec3() { return new Vec3(Position.x, Position.y, Position.z); }
    public Vec3 UpVec3() { return ToVec3(Up()); }
    public Vec3 RightVec3() { return ToVec3(Right()); }
    public Vec3 ForwardVec3() { return ToVec3(Forward()); }

    public Vector3d Position() { return Position; }
    public Vector3d Up() { return orientation.transform(UnitY()); }
    public Vector3d Right() { return orientation.transform(UnitX()); }
    public Vector3d Forward() { return orientation.transform(UnitZ()); }




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

    // LOCAL IS GLOBAL???!!!
    public Transform RotateGlobalYaw(float yAngle) { return new Transform(position.rotateY(-yAngle * Maths.DegToRadF, new Vector3d()), orientation.rotateLocalY(-yAngle * Maths.DegToRadF, new Quaternionf())); }
    public Transform RotateGlobalPitch(float xAngle) { return new Transform(position.rotateX(xAngle * Maths.DegToRadF, new Vector3d()), orientation.rotateLocalX(xAngle * Maths.DegToRadF, new Quaternionf())); }
    public Transform RotateGlobalRoll(float zAngle) {  return new Transform(position.rotateZ(zAngle * Maths.DegToRadF, new Vector3d()), orientation.rotateLocalZ(zAngle * Maths.DegToRadF, new Quaternionf()));  }
   ublic Transform RotateLocalYaw(float yAngle) {  return new Transform(position, orientation.rotateLocalY(-yAngle * Maths.DegToRadF, new Quaternionf()));  }
    public Transform RotateLocalPitch(float xAngle) {  return new Transform(position, orientation.rotateLocalX(xAngle * Maths.DegToRadF, new Quaternionf()));  }
    public Transform RotateLocalRoll(float zAngle) {  return new Transform(position, orientation.rotateLocalZ(zAngle * Maths.DegToRadF, new Quaternionf()));  }

     */
}
