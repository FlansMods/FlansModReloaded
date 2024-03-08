package com.flansmod.util;

import com.flansmod.common.FlansMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.joml.Runtime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Transform
{
    private static final Vector3d       IDENTITY_POS    = new Vector3d();
    private static final Quaternionf    IDENTITY_QUAT   = new Quaternionf();
    private static final Vector3f       IDENTITY_SCALE  = new Vector3f(1f, 1f, 1f);
    public static final Transform IDENTITY = new Transform(() -> "\"Identity\"");

    // -- Fields --
    @Nullable
    public final Supplier<String> DebugInfo;
    @Nonnull
    public final Vector3d Position;
    @Nonnull
    public final Quaternionf Orientation;
    @Nonnull
    public final Vector3f Scale;

    private Transform(double x, double y, double z, float pitch, float yaw, float roll, float scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = debugFunc;
        Position = new Vector3d(x, y, z);
        Orientation = QuatFromEuler(pitch, yaw, roll);
        Scale = new Vector3f(scale, scale, scale);
    }
    private Transform(@Nonnull Vec3 pos,  @Nonnull Quaternionf rotation, @Nonnull Vector3f scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = debugFunc;
        Position = new Vector3d(pos.x, pos.y, pos.z);
        Orientation = new Quaternionf(rotation);
        Scale = new Vector3f(scale);
    }
    private Transform(@Nonnull Vector3d pos,  @Nonnull Quaternionf rotation, @Nonnull Vector3f scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = debugFunc;
        Position = new Vector3d(pos.x, pos.y, pos.z);
        Orientation = new Quaternionf(rotation);
        Scale = new Vector3f(scale);
    }
    private Transform(double x, double y, double z, @Nonnull Quaternionf rotation, float scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = debugFunc;
        Position = new Vector3d(x, y, z);
        Orientation = new Quaternionf(rotation);
        Scale = new Vector3f(scale, scale, scale);
    }
    private Transform(double x, double y, double z, float scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = debugFunc;
        Position = new Vector3d(x, y, z);
        Orientation = IDENTITY_QUAT;
        Scale = new Vector3f(scale, scale, scale);
    }
    private Transform(@Nonnull Vector3f pos, @Nonnull Quaternionf rotation, @Nonnull Vector3f scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = debugFunc;
        Position = new Vector3d(pos.x, pos.y, pos.z);
        Orientation = new Quaternionf(rotation);
        Scale = new Vector3f(scale);
    }
    private Transform(float scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = debugFunc;
        Position = IDENTITY_POS;
        Orientation = IDENTITY_QUAT;
        Scale = new Vector3f(scale, scale, scale);
    }
    private Transform(@Nullable Supplier<String> debugFunc)
    {
        DebugInfo = debugFunc;
        Position = IDENTITY_POS;
        Orientation = IDENTITY_QUAT;
        Scale = IDENTITY_SCALE;
    }
    private Transform()
    {
        DebugInfo = null;
        Position = IDENTITY_POS;
        Orientation = IDENTITY_QUAT;
        Scale = IDENTITY_SCALE;
    }

    // From complete transform
    private Transform(Transform other) { this(other.Position, other.Orientation, other.Scale, other.DebugInfo); }


    public static Transform Compose(Transform ... transforms)
    {
        TransformStack stack = new TransformStack();
        stack.addAll(Arrays.asList(transforms));
        return stack.Top();
    }
    @Nonnull public static Transform Identity()                                                                               { return new Transform((Supplier<String>) null); }
    @Nonnull public static Transform FromScale(float scale)                                                                   { return new Transform(scale, null);  }
    @Nonnull public static Transform FromPos(@Nonnull Vec3 pos)                                                               { return new Transform(pos.x, pos.y, pos.z, 1f, null); }
    @Nonnull public static Transform FromPos(double x, double y, double z)                                                    { return new Transform(x, y, z, 1f, null); }
    @Nonnull public static Transform FromPosAndEuler(@Nonnull Vec3 pos, @Nonnull Vector3f euler)                              { return new Transform(pos.x, pos.y, pos.z, euler.x, euler.y, euler.z, 1f, null); }
    @Nonnull public static Transform FromPosAndEuler(@Nonnull Vector3f pos, @Nonnull Vector3f euler)                          { return new Transform(pos.x, pos.y, pos.z, euler.x, euler.y, euler.z, 1f, null);}
    @Nonnull public static Transform FromPosAndEuler(@Nonnull Vec3 pos, float pitch, float yaw, float roll)                   { return new Transform(pos.x, pos.y, pos.z, pitch, yaw, roll, 1f, null);}
    @Nonnull public static Transform FromEuler(float pitch, float yaw, float roll)                                            { return new Transform(0d, 0d, 0d, QuatFromEuler(pitch, yaw, roll), 1f, null);}
    @Nonnull public static Transform FromEulerRadians(float pitch, float yaw, float roll)                                     { return new Transform(0d, 0d, 0d, QuatFromEulerRadians(pitch, yaw, roll), 1f, null); }
    @Nonnull public static Transform FromEuler(@Nonnull Vector3f euler)                                                       { return new Transform(0d, 0d, 0d, QuatFromEuler(euler), 1f, null); }
    @Nonnull public static Transform FromLookDirection(@Nonnull Vec3 forward, @Nonnull Vec3 up)                               { return new Transform(0d, 0d, 0d, LookAlong(forward, up), 1f, null); }
    @Nonnull public static Transform FromPositionAndLookDirection(@Nonnull Vec3 pos, @Nonnull Vec3 forward, @Nonnull Vec3 up) { return new Transform(pos.x, pos.y, pos.z, LookAlong(forward, up), 1f, null); }
    @Nonnull public static Transform FromBlockPos(@Nonnull BlockPos blockPos)                                                 { return new Transform(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1f, null); }
    @Nonnull public static Transform FromPose(@Nonnull Matrix4f pose)                                                         { return new Transform(pose.transformPosition(new Vector3f()), pose.getUnnormalizedRotation(new Quaternionf()), GetScale(pose), null); }
    @Nonnull public static Transform FromPose(@Nonnull PoseStack poseStack)                                                   { return FromPose(poseStack.last().pose(), null); }


    @Nonnull public static Transform Identity(@Nullable Supplier<String> debugFunc)                                                                                 { return new Transform(debugFunc); }
    @Nonnull public static Transform FromScale(float scale, @Nullable Supplier<String> debugFunc)                                                                   { return new Transform(scale, debugFunc);  }
    @Nonnull public static Transform FromScale(@Nonnull Vector3f scale, @Nullable Supplier<String> debugFunc)                                                       { return new Transform(IDENTITY_POS, IDENTITY_QUAT, scale, debugFunc);  }
    @Nonnull public static Transform FromPos(@Nonnull Vec3 pos, @Nullable Supplier<String> debugFunc)                                                               { return new Transform(pos.x, pos.y, pos.z, 1f, debugFunc); }
    @Nonnull public static Transform FromPos(double x, double y, double z, @Nullable Supplier<String> debugFunc)                                                    { return new Transform(x, y, z, 1f, debugFunc); }
    @Nonnull public static Transform FromPosAndEuler(@Nonnull Vec3 pos, @Nonnull Vector3f euler, @Nullable Supplier<String> debugFunc)                              { return new Transform(pos.x, pos.y, pos.z, euler.x, euler.y, euler.z, 1f, debugFunc); }
    @Nonnull public static Transform FromPosAndQuat(@Nonnull Vector3d pos, @Nonnull Quaternionf ori, @Nullable Supplier<String> debugFunc)                          { return new Transform(pos.x, pos.y, pos.z, ori, 1f, debugFunc); }
    @Nonnull public static Transform FromPosAndQuat(@Nonnull Vec3 pos, @Nonnull Quaternionf ori, @Nullable Supplier<String> debugFunc)                              { return new Transform(pos.x, pos.y, pos.z, ori, 1f, debugFunc); }
    @Nonnull public static Transform FromPosAndEuler(@Nonnull Vector3f pos, @Nonnull Vector3f euler, @Nullable Supplier<String> debugFunc)                          { return new Transform(pos.x, pos.y, pos.z, euler.x, euler.y, euler.z, 1f, debugFunc);}
    @Nonnull public static Transform FromPosAndEuler(@Nonnull Vec3 pos, float pitch, float yaw, float roll, @Nullable Supplier<String> debugFunc)                   { return new Transform(pos.x, pos.y, pos.z, pitch, yaw, roll, 1f, debugFunc);}
    @Nonnull public static Transform FromEuler(float pitch, float yaw, float roll, @Nullable Supplier<String> debugFunc)                                            { return new Transform(0d, 0d, 0d, QuatFromEuler(pitch, yaw, roll), 1f, debugFunc);}
    @Nonnull public static Transform FromEulerRadians(float pitch, float yaw, float roll, @Nullable Supplier<String> debugFunc)                                     { return new Transform(0d, 0d, 0d, QuatFromEulerRadians(pitch, yaw, roll), 1f, debugFunc); }
    @Nonnull public static Transform FromEuler(@Nonnull Vector3f euler, @Nullable Supplier<String> debugFunc)                                                       { return new Transform(0d, 0d, 0d, QuatFromEuler(euler), 1f, debugFunc); }
    @Nonnull public static Transform FromLookDirection(@Nonnull Vec3 forward, @Nonnull Vec3 up, @Nullable Supplier<String> debugFunc)                               { return new Transform(0d, 0d, 0d, LookAlong(forward, up), 1f, debugFunc); }
    @Nonnull public static Transform FromPositionAndLookDirection(@Nonnull Vec3 pos, @Nonnull Vec3 forward, @Nonnull Vec3 up, @Nullable Supplier<String> debugFunc) { return new Transform(pos.x, pos.y, pos.z, LookAlong(forward, up), 1f, debugFunc); }
    @Nonnull public static Transform FromItem(@Nonnull ItemTransform itemTransform, @Nullable Supplier<String> debugFunc)                                           { return new Transform(itemTransform.translation.x, itemTransform.translation.y, itemTransform.translation.z, QuatFromEuler(itemTransform.rotation), itemTransform.scale.x, debugFunc); }
    @Nonnull public static Transform FromPose(@Nonnull Matrix4f pose, @Nullable Supplier<String> debugFunc)                                                         { return new Transform(pose.transformPosition(new Vector3f()), pose.getUnnormalizedRotation(new Quaternionf()), GetScale(pose), debugFunc); }
    @Nonnull public static Transform FromPose(@Nonnull PoseStack poseStack, @Nullable Supplier<String> debugFunc)                                                   { return FromPose(poseStack.last().pose(), debugFunc); }
    @Nonnull public static Transform FromBlockPos(@Nonnull BlockPos blockPos, @Nullable Supplier<String> debugFunc)                                                 { return new Transform(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1f, debugFunc); }
    @Nonnull public static Transform Debug(@Nullable Supplier<String> debugFunc)                                                                                    { return new Transform(debugFunc); }
    @Nonnull public static Transform Error(@Nonnull String errorMessage)                                                                                            { return new Transform(() -> errorMessage); }
    @Nonnull
    public static Transform ExtractOrientation(@Nonnull Transform from, boolean invert, @Nullable Supplier<String> debugFunc)
    {
        Quaternionf ori = invert ? from.Orientation.invert(new Quaternionf()) : from.Orientation;
        return new Transform(0d, 0d, 0d, ori, 1f, debugFunc == null ? () -> "{\"OriFrom\":"+from.DebugInfo+"}" : debugFunc);
    }
    @Nonnull
    public static Transform ExtractPosition(@Nonnull Transform from, double scale, @Nullable Supplier<String> debugFunc)
    {
        Vector3d pos = from.Position.mul(scale, new Vector3d());
        return new Transform(pos.x, pos.y, pos.z, IDENTITY_QUAT, 1f, debugFunc == null ? () -> "{\"PosFrom\":"+from.DebugInfo+"}" : debugFunc);
    }

    // ----------------------------------------------------------------------------------------
    // --- Minecraft RenderSystem / JOML interface ---
    // ----------------------------------------------------------------------------------------
    public void ApplyToPoseStack(@Nonnull PoseStack poseStack)
    {
        poseStack.translate(Position.x, Position.y, Position.z);
        poseStack.mulPose(Orientation);
        poseStack.scale(Scale.x, Scale.y, Scale.z);
    }
    @Nonnull
    public PoseStack ToNewPoseStack()
    {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(Position.x, Position.y, Position.z);
        poseStack.mulPose(Orientation);
        poseStack.scale(Scale.x, Scale.y, Scale.z);
        return poseStack;
    }
    // ----------------------------------------------------------------------------------------

    // ----------------------------------------------------------------------------------------
    // --------- Angular Operations, inc. conversion to/from MC eulers and composing ----------
    // ----------------------------------------------------------------------------------------
    @Nonnull
    private static Quaternionf QuatFromEuler(@Nonnull Vector3f euler)
    {
        return new Quaternionf()
            .rotateY(-euler.y * Maths.DegToRadF)
            .rotateX(-euler.x * Maths.DegToRadF)
            .rotateZ(-euler.z * Maths.DegToRadF);
    }
    @Nonnull
    private static Quaternionf QuatFromEulerRadians(float pitch, float yaw, float roll)
    {
        return new Quaternionf()
            .rotateY(-yaw)
            .rotateX(-pitch)
            .rotateZ(-roll);
    }
    @Nonnull
    private static Quaternionf QuatFromEuler(float pitch, float yaw, float roll)
    {
       //float cr = Maths.CosF(roll * 0.5f * Maths.DegToRadF);
       //float sr = Maths.SinF(roll * 0.5f * Maths.DegToRadF);
       //float cp = Maths.CosF(pitch * 0.5f * Maths.DegToRadF);
       //float sp = Maths.SinF(pitch * 0.5f * Maths.DegToRadF);
       //float cy = Maths.CosF(yaw * 0.5f * Maths.DegToRadF);
       //float sy = Maths.SinF(yaw * 0.5f * Maths.DegToRadF);


        return new Quaternionf()
            .rotateY(-yaw * Maths.DegToRadF)
            .rotateX(-pitch * Maths.DegToRadF)
            .rotateZ(-roll * Maths.DegToRadF);
    }
    @Nonnull
    public Transform Reflect(boolean inX, boolean inY, boolean inZ)
    {
        Vec3 reflectedPos = new Vec3(inX ? -Position.x : Position.x, inY ? -Position.y : Position.y, inZ ? -Position.z : Position.z);
        Vec3 fwd = ForwardVec3();
        Vec3 reflectedFwd = new Vec3(inX ? -fwd.x : fwd.x, inY ? -fwd.y : fwd.y, inZ ? -fwd.z : fwd.z);
        Vec3 up = UpVec3();
        Vec3 reflectedUp = new Vec3(inX ? -up.x : up.x, inY ? -up.y : up.y, inZ ? -up.z : up.z);
        return Transform.FromPositionAndLookDirection(reflectedPos, reflectedFwd, reflectedUp);
    }
    @Nonnull
    public static Vector3f GetScale(@Nonnull Matrix4f mat)
    {
        Vector3f result = new Vector3f();
        mat.getScale(result);
        if(mat.determinant() < 0.0f)
            result.mul(-1f);
        return result;
    }

    @Nonnull
    private static Vector3f ToEuler(@Nonnull Quaternionf quat)
    {
        Vector3f euler = quat.getEulerAnglesYXZ(new Vector3f());
        return euler.mul(-Maths.RadToDegF, -Maths.RadToDegF, -Maths.RadToDegF);
    }
    @Nonnull
    private static Quaternionf LookAlong(@Nonnull Vec3 forward, @Nonnull Vec3 up)
    {
        return new Quaternionf().lookAlong(
            (float)forward.x, (float)forward.y, (float)forward.z,
            (float)up.x, (float)up.y, (float)up.z)
            .invert();
    }
    @Nonnull
    public static Quaternionf Compose(@Nonnull Quaternionf firstA, @Nonnull Quaternionf thenB)
    {
        return thenB.mul(firstA, new Quaternionf());
    }
    @Nonnull
    public static Quaternionf Compose(@Nonnull Quaternionf firstA, @Nonnull Quaternionf thenB, @Nonnull Quaternionf thenC)
    {
        return thenC.mul(thenB, new Quaternionf()).mul(firstA, new Quaternionf());
    }
    @Nonnull
    public static Vector3f Rotate(@Nonnull Vector3f vec, @Nonnull Quaternionf around)
    {
        return around.transform(vec, new Vector3f());
    }
    // ----------------------------------------------------------------------------------------


    // ----------------------------------------------------------------------------------------
    // -------- Positional Operations, inc. conversion to/from MC coords and composing --------
    // ----------------------------------------------------------------------------------------
    @Nonnull
    public BlockPos BlockPos() { return new BlockPos(Maths.Floor(Position.x), Maths.Floor(Position.y), Maths.Floor(Position.z)); }
    @Nonnull
    public Vec3 PositionVec3() { return new Vec3(Position.x, Position.y, Position.z); }
    @Nonnull
    public Vec3 ForwardVec3() { return LocalToGlobalDirection(new Vec3(0d, 0d, -1d)); }
    @Nonnull
    public Vec3 UpVec3() { return LocalToGlobalDirection(new Vec3(0d, 1d, 0d)); }
    @Nonnull
    public Vec3 RightVec3() { return LocalToGlobalDirection(new Vec3(1d, 0d, 0d)); }


    // ----------------------------------------------------------------------------------------
    // -------- Transformations i.e. Convert between this space and the parent space ----------
    // ----------------------------------------------------------------------------------------
    //  Applied as follows: Rotate, then translate, then scale.
    @Nonnull
    public Vec3 LocalToGlobalPosition(@Nonnull Vec3 localPos)
    {
        Vector3d scratch = new Vector3d(localPos.x, localPos.y, localPos.z);
        Orientation.transform(scratch);
        scratch.mul(Scale);
        scratch.add(Position);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    @Nonnull
    public Vec3 GlobalToLocalPosition(@Nonnull Vec3 globalPos)
    {
        Vector3d scratch = new Vector3d(globalPos.x, globalPos.y, globalPos.z);
        scratch.sub(Position);
        Orientation.transformInverse(scratch);
        scratch.mul(1.0f / Scale.x, 1.0f / Scale.y, 1.0f / Scale.z);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    //  In this case, we no longer care about the position offset
    @Nonnull
    public Vec3 LocalToGlobalVelocity(@Nonnull Vec3 localVelocity)
    {
        Vector3d scratch = new Vector3d(localVelocity.x, localVelocity.y, localVelocity.z);
        Orientation.transform(scratch);
        scratch.mul(Scale);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    @Nonnull
    public Vec3 GlobalToLocalVelocity(@Nonnull Vec3 globalVelocity)
    {
        Vector3d scratch = new Vector3d(globalVelocity.x, globalVelocity.y, globalVelocity.z);
        scratch.mul(1.0f / Scale.x, 1.0f / Scale.y, 1.0f / Scale.z);
        Orientation.transformInverse(scratch);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    //  In this case, we also don't want to scale. If it's normalized in, it should be normalized out
    @Nonnull
    public Vec3 LocalToGlobalDirection(@Nonnull Vec3 localDirection)
    {
        Vector3d scratch = new Vector3d(localDirection.x, localDirection.y, localDirection.z);
        Orientation.transform(scratch);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    @Nonnull
    public Vec3 GlobalToLocalDirection(@Nonnull Vec3 globalDirection)
    {
        Vector3d scratch = new Vector3d(globalDirection.x, globalDirection.y, globalDirection.z);
        Orientation.transformInverse(scratch);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    @Nonnull
    public Quaternionf LocalToGlobalOrientation(@Nonnull Quaternionf localOri)
    {
        // Scale CAN affect rotations, iff it is negative in one or more axes
        boolean flipX = Scale.x < 0.0f;
        boolean flipY = Scale.y < 0.0f;
        boolean flipZ = Scale.z < 0.0f;
        if(flipX || flipY || flipZ)
            return Reflect(flipX, flipY, flipZ).Orientation.mul(localOri, new Quaternionf());

        return Orientation.mul(localOri, new Quaternionf());
    }
    @Nonnull
    public Quaternionf GlobalToLocalOrientation(@Nonnull Quaternionf globalOri)
    {
        // Scale CAN affect rotations, iff it is negative in one or more axes
        boolean flipX = Scale.x < 0.0f;
        boolean flipY = Scale.y < 0.0f;
        boolean flipZ = Scale.z < 0.0f;
        if(flipX || flipY || flipZ)
            return Reflect(flipX, flipY, flipZ).Orientation.invert(new Quaternionf()).mul(globalOri, new Quaternionf());

        return Orientation.invert(new Quaternionf()).mul(globalOri, new Quaternionf());
    }
    @Nonnull
    public Vector3f LocalToGlobalScale(@Nonnull Vector3f localScale)
    {
        return localScale.mul(Scale, new Vector3f());
    }
    @Nonnull
    public Vector3f GlobalToLocalScale(@Nonnull Vector3f  globalScale)
    {
        return globalScale.div(Scale, new Vector3f());
    }
    @Nonnull
    public Transform LocalToGlobalTransform(@Nonnull Transform localTransform)
    {
        return new Transform(
            LocalToGlobalPosition(localTransform.PositionVec3()),
            LocalToGlobalOrientation(localTransform.Orientation),
            LocalToGlobalScale(localTransform.Scale),
            () -> "{\"Local\":"+localTransform+",\"Apply\":"+this+"}");
    }
    @Nonnull
    public Transform GlobalToLocalTransform(@Nonnull Transform globalTransform)
    {
        return new Transform(
            GlobalToLocalPosition(globalTransform.PositionVec3()),
            GlobalToLocalOrientation(globalTransform.Orientation),
            GlobalToLocalScale(globalTransform.Scale),
            () -> "{\"Globl\":"+globalTransform+",\"Apply\":"+this+"}");
    }
    // ----------------------------------------------------------------------------------------


    // ----------------------------------------------------------------------------------------
    // ------------------------------- Misc Transform functions -------------------------------
    // ----------------------------------------------------------------------------------------
    @Nonnull
    public static Transform Interpolate(@Nonnull Transform a, @Nonnull Transform b, float t)
    {
        return new Transform(
            a.Position.lerp(b.Position, t, new Vector3d()),
            a.Orientation.slerp(b.Orientation, t, new Quaternionf()),
            a.Scale.lerp(b.Scale, t, new Vector3f()),
            () -> "{\"Interpolate\":["+a.DebugInfo+","+b.DebugInfo+"]}");
    }
    @Nonnull
    public static Transform Interpolate(@Nonnull List<Transform> transforms)
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

        // This will lambda capture a lot of info, so it being a supplier is still heavy
        Supplier<String> debugInfoBuilder = () -> {

            StringBuilder debugInfo = new StringBuilder("{\"Interpolate\":[");
            for(int i = 0; i < transforms.size(); i++)
            {
                debugInfo.append(transforms.get(i).DebugInfo);
                if (i != transforms.size() - 1)
                    debugInfo.append(',');
            }
            return debugInfo.append("]}").toString();
        };

        return Transform.FromPosAndQuat(
            position.mul(1d / transforms.size()),
            (Quaternionf) Quaternionf.slerp(orientations, weights, new Quaternionf()),
            debugInfoBuilder);
    }
    @Nonnull
    public static Transform Interpolate(@Nonnull List<Transform> transforms, @Nonnull float[] weights)
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

        // This will lambda capture a lot of info, so it being a supplier is still heavy
        Supplier<String> debugInfoBuilder = () -> {
            StringBuilder debugInfo = new StringBuilder("{\"Interpolate\":[");
            for(int i = 0; i < transforms.size(); i++)
            {
                debugInfo.append('{').append("\"Weight\":").append(weights[i]).append(", \"Trans\":").append(transforms.get(i).DebugInfo).append('}');
                if(i != transforms.size() - 1)
                    debugInfo.append(',');
            }
            return debugInfo.append("]}").toString();
        };

        return Transform.FromPosAndQuat(
            position.mul(1d / totalWeight),
            (Quaternionf) Quaternionf.slerp(orientations, weights, new Quaternionf()),
            debugInfoBuilder);
    }



    private static final NumberFormat FLOAT_FORMAT = new DecimalFormat("#.##");
    private static final NumberFormat ANGLE_FORMAT = new DecimalFormat("#");

    @Nonnull
    public String GetDebugInfo()
    {
        if(DebugInfo == null)
            return "N/A";
        return DebugInfo.get();
    }

    @Override
    public String toString()
    {
        boolean isZeroPos = Maths.Approx(Position.lengthSquared(), 0d);
        boolean isIdentityRot = Maths.Approx(Orientation.x, 0f) && Maths.Approx(Orientation.y, 0f) && Maths.Approx(Orientation.z, 0f) && Maths.Approx(Orientation.w, 1f);
        boolean isOneScale = Maths.Approx(Scale.x, 1f) && Maths.Approx(Scale.y, 1f) && Maths.Approx(Scale.z, 1f);
        if(isZeroPos && isIdentityRot && isOneScale)
            return "{\"Dbg\":"+GetDebugInfo()+"}";
        else
        {
            StringBuilder output = new StringBuilder("{");
            if(!isZeroPos)
            {
                output.append("\"Pos\":[")
                    .append(Runtime.format(Position.x, FLOAT_FORMAT)).append(", ")
                    .append(Runtime.format(Position.y, FLOAT_FORMAT)).append(", ")
                    .append(Runtime.format(Position.z, FLOAT_FORMAT)).append("]");
            }
            if(!isIdentityRot)
            {
                if(!isZeroPos)
                    output.append(',');
                Vector3f euler = ToEuler(Orientation);
                output.append("\"Rot\":[")
                    .append(Runtime.format(euler.x, ANGLE_FORMAT)).append(", ")
                    .append(Runtime.format(euler.y, ANGLE_FORMAT)).append(", ")
                    .append(Runtime.format(euler.z, ANGLE_FORMAT)).append("]");
            }
            if(!isOneScale)
            {
                if(!isZeroPos || !isIdentityRot)
                    output.append(',');
                if(Maths.Approx(Scale.x, Scale.y) && Maths.Approx(Scale.y, Scale.z))
                {
                    output.append("\"Scl\":").append(Runtime.format(Scale.x, FLOAT_FORMAT));
                }
                else
                {
                    output.append("\"Scl\":[")
                        .append(Runtime.format(Scale.x, FLOAT_FORMAT)).append(", ")
                        .append(Runtime.format(Scale.y, FLOAT_FORMAT)).append(", ")
                        .append(Runtime.format(Scale.z, FLOAT_FORMAT)).append("]");
                }
            }
            output.append(", \"Dbg\":").append(GetDebugInfo()).append("}");
            return output.toString();
        }
    }


    public static void RunTests()
    {
        // -----------------------------------------------------------------------------------------
        // - Test Section #1 - Check ToEuler and FromEuler are inverse with single parameter input -
        Vector3f eulerYaw90 = new Vector3f(0f, 90f, 0f);
        Vector3f eulerYaw135 = new Vector3f(0f, 135f, 0f);
        Vector3f eulerPitchUp90 = new Vector3f(-90f, 0f, 0f);
        Vector3f eulerPitchDown45 = new Vector3f(45f, 0f, 0f);
        Vector3f eulerPitchDown90 = new Vector3f(90f, 0f, 0f);
        Vector3f eulerRollLeft45 = new Vector3f(0f, 0f, -45f);
        Vector3f eulerRollRight45 = new Vector3f(0f, 0f, 45f);
        Vector3f eulerRollRight60 = new Vector3f(0f, 0f, 60f);
        Vector3f eulerRollRight90 = new Vector3f(0f, 0f, 90f);
        Quaternionf yaw90 = QuatFromEuler(eulerYaw90);
        Quaternionf yaw135 = QuatFromEuler(eulerYaw135);
        Quaternionf pitchUp90 = QuatFromEuler(eulerPitchUp90);
        Quaternionf pitchDown45 = QuatFromEuler(eulerPitchDown45);
        Quaternionf pitchDown90 = QuatFromEuler(eulerPitchDown90);
        Quaternionf rollLeft45 = QuatFromEuler(eulerRollLeft45);
        Quaternionf rollRight45 = QuatFromEuler(eulerRollRight45);
        Quaternionf rollRight60 = QuatFromEuler(eulerRollRight60);
        Quaternionf rollRight90 = QuatFromEuler(eulerRollRight90);
        AssertEqual(eulerYaw90, ToEuler(yaw90), "ToEuler != FromEuler^-1");
        AssertEqual(eulerYaw135, ToEuler(yaw135), "ToEuler != FromEuler^-1");
        AssertEqual(eulerPitchUp90, ToEuler(pitchUp90), "ToEuler != FromEuler^-1");
        AssertEqual(eulerPitchDown45, ToEuler(pitchDown45), "ToEuler != FromEuler^-1");
        AssertEqual(eulerPitchDown90, ToEuler(pitchDown90), "ToEuler != FromEuler^-1");
        AssertEqual(eulerRollRight60, ToEuler(rollRight60), "ToEuler != FromEuler^-1");
        AssertEqual(eulerRollLeft45, ToEuler(rollLeft45), "ToEuler != FromEuler^-1");
        AssertEqual(eulerRollRight45, ToEuler(rollRight45), "ToEuler != FromEuler^-1");
        AssertEqual(eulerRollRight90, ToEuler(rollRight90), "ToEuler != FromEuler^-1");

        // ------------------------------------------------------------------------------------
        // - Test Section #2 - Check multi-parameter euler angles -
        Vector3f eulerYaw90Pitch45 = new Vector3f(45f, 90f, 0f);
        Vector3f eulerYaw90Pitch45Roll45 = new Vector3f(45f, 90f, 45f);
        Vector3f eulerPitch45Roll60 = new Vector3f(45f, 0f, 60f);
        Quaternionf yaw90Pitch45 = QuatFromEuler(eulerYaw90Pitch45);
        Quaternionf yaw90Pitch45Roll45 = QuatFromEuler(eulerYaw90Pitch45Roll45);
        Quaternionf pitch45Roll60 = QuatFromEuler(eulerPitch45Roll60);
        AssertEqual(eulerYaw90Pitch45, ToEuler(yaw90Pitch45), "ToEuler != FromEuler^-1");
        AssertEqual(eulerYaw90Pitch45Roll45, ToEuler(yaw90Pitch45Roll45), "ToEuler != FromEuler^-1");
        AssertEqual(eulerPitch45Roll60, ToEuler(pitch45Roll60), "ToEuler != FromEuler^-1");



        // ------------------------------------------------------------------------------------------------
        // - Test Section #3 - Check that these Quaternions do expected things to the Minecraft unit axes -
        Vector3f mcNorth = new Vector3f(0f, 0f, -1f);
        Vector3f mcEast = new Vector3f(1f, 0f, 0f);
        Vector3f mcSouth = new Vector3f(0f, 0f, 1f);
        Vector3f mcWest = new Vector3f(-1f, 0f, 0f);
        Vector3f mcUp = new Vector3f(0f, 1f, 0f);
        Vector3f mcDown = new Vector3f(0f, -1f, 0f);
        // Yaw 90 tests
        AssertEqual(Rotate(mcSouth, yaw90), 	    mcWest, 	"South * Yaw90 != West");
        AssertEqual(Rotate(mcWest, yaw90),  	    mcNorth, 	"West * Yaw90 != North");
        AssertEqual(Rotate(mcNorth, yaw90), 	    mcEast, 	"North * Yaw90 != East");
        AssertEqual(Rotate(mcEast, yaw90),  	    mcSouth, 	"East * Yaw90 != South");
        AssertEqual(Rotate(mcUp, yaw90), 		    mcUp, 		"Up * Yaw90 != Up");
        AssertEqual(Rotate(mcDown, yaw90),  	    mcDown, 	"Down * Yaw90 != Down");
        // Pitch up 90 tests
        AssertEqual(Rotate(mcNorth, pitchUp90), 	mcUp, 	    "North * PitchUp90 != Up");
        AssertEqual(Rotate(mcEast, pitchUp90), 	    mcEast,     "East * PitchUp90 != East");
        AssertEqual(Rotate(mcUp, pitchUp90), 	    mcSouth,    "Up * PitchUp90 != South");
        // Pitch down 90 tests
        AssertEqual(Rotate(mcNorth, pitchDown90), 	mcDown,     "North * PitchDown90 != Down");
        AssertEqual(Rotate(mcEast, pitchDown90), 	mcEast,     "East * PitchDown90 != East");
        AssertEqual(Rotate(mcUp, pitchDown90), 	    mcNorth,    "Up * PitchDown90 != North");
        // Roll tests
        AssertEqual(Rotate(mcNorth, rollRight90), 	mcNorth,    "North * RollRight90 != North");
        AssertEqual(Rotate(mcEast, rollRight90), 	mcDown,     "East * PitchDown90 != Down");
        AssertEqual(Rotate(mcUp, rollRight90), 	    mcEast,     "Up * PitchDown90 != East");



        // Composition tests
        Vector3f mcDownNorth = new Vector3f(0f, -Maths.CosF(Maths.TauF / 8f), -Maths.SinF(Maths.TauF / 8f));
        Vector3f mcDownEast = new Vector3f(Maths.SinF(Maths.TauF / 8f), -Maths.CosF(Maths.TauF / 8f), 0f);
        AssertEqual(Rotate(mcNorth, pitchDown45), mcDownNorth, "Pitch north not as expected");
        AssertEqual(Rotate(Rotate(mcNorth, pitchDown45), yaw90), mcDownEast, "Pitch east not as expected");

        AssertEqual(
            yaw90Pitch45.transform(mcNorth, new Vector3f()),
            yaw90.transform(pitchDown45.transform(mcNorth, new Vector3f()), new Vector3f()),//Rotate(Rotate(mcNorth, pitchDown45), yaw90),
            "YawPitch composition incorrect");


        // ------------------------------------------------------------------------------------
        // - Test Section #4 - Check Quaternion composition matches our expected behaviour -
        // Minecraft applies Roll(Z), then Pitch(X), then Yaw(Y)
        AssertEqual(Compose(pitchDown45, yaw90), yaw90Pitch45, "Composition unexpected");
        AssertEqual(Compose(rollRight45, pitchDown45, yaw90), yaw90Pitch45Roll45, "Composition unexpected");
        AssertEqual(Compose(rollRight60, pitchDown45), pitch45Roll60, "Composition unexpected");

        // ----------------------------------------------------------------------------------------------
        // - Test Section #5 - Validate some basic Transforms
        AssertEqual(Transform.IDENTITY.ForwardVec3(), mcNorth, "IDENTITY.Forward() not North");
        AssertEqual(Transform.IDENTITY.UpVec3(), mcUp, "IDENTITY.Up() not Up");
        AssertEqual(Transform.IDENTITY.RightVec3(), mcEast, "IDENTITY.Right() not East");
        // Test the properties of translations
        Transform offsetXAxis = Transform.FromPos(1d, 0d, 0d, () -> "X Axis");
        Transform offsetYAxis = Transform.FromPos(0d, 1d, 0d, () -> "Y Axis");
        Transform offsetZAxis = Transform.FromPos(0d, 0d, 1d, () -> "Z Axis");
        VerifyCommutative(offsetXAxis, offsetYAxis);
        VerifyCommutative(offsetXAxis, offsetZAxis);
        VerifyAssociative(offsetXAxis, offsetYAxis, offsetZAxis);
        // Test the properties of rotations
        Transform tYaw90 = Transform.FromEuler(eulerYaw90, () -> "Yaw90");
        Transform tYaw135 = Transform.FromEuler(eulerYaw135, () -> "Yaw135");
        Transform tPitchUp90 = Transform.FromEuler(eulerPitchUp90, () -> "PitchUp90");
        Transform tPitchDown45 = Transform.FromEuler(eulerPitchDown45, () -> "PitchDown45");
        Transform tRollRight45 = Transform.FromEuler(eulerRollRight45, () -> "RollRight45");
        Transform tRollRight90 = Transform.FromEuler(eulerRollRight90, () -> "RollRight90");
        AssertEqual(TransformStack.of(tYaw90, tYaw90, tYaw90, tYaw90).Top(), Transform.IDENTITY, "4 turns != identity");
        VerifyAssociative(tRollRight45, tPitchUp90, tYaw90);
        VerifyCommutative(tYaw90, tYaw135);
        VerifyCommutative(tRollRight45, tRollRight90);
        VerifyCommutative(tPitchUp90, tPitchDown45);
        // Interaction between translation and rotation
        VerifyCommutative(offsetXAxis, tPitchUp90);
        VerifyCommutative(offsetYAxis, tYaw90);
        VerifyCommutative(offsetZAxis, tRollRight45);


        // Test Section #6 - Look Along
        AssertEqual(Transform.FromLookDirection(tYaw90.ForwardVec3(), tYaw90.UpVec3()), tYaw90, "Look along failed");
        AssertEqual(Transform.FromLookDirection(tPitchDown45.ForwardVec3(), tPitchDown45.UpVec3()), tPitchDown45, "Look along failed");
        AssertEqual(Transform.FromLookDirection(tRollRight90.ForwardVec3(), tRollRight90.UpVec3()), tRollRight90, "Look along failed");

        // Test Section #7 - Non-uniform scale
        Transform flipTest = Transform.FromPosAndEuler(new Vec3(30d, 31.3d, -12d), 45f, 43f, 13f, () -> "FlipTest");
        AssertEqual(flipTest, flipTest.Reflect(true, false, false).Reflect(true, false, false), "FlipX not self-inverse");
        AssertEqual(flipTest, flipTest.Reflect(false, true, false).Reflect(false, true, false), "FlipY not self-inverse");
        AssertEqual(flipTest, flipTest.Reflect(false, false, true).Reflect(false, false, true), "FlipZ not self-inverse");

        Transform composed = TransformStack.of(tYaw90, tRollRight45, tPitchDown45).Top();
        AssertEqual(Transform.FromLookDirection(composed.ForwardVec3(), composed.UpVec3()), composed, "Look along failed");

    }
    private static void VerifyAssociative(@Nonnull Transform a, @Nonnull Transform b, @Nonnull Transform c)
    {
        AssertEqual(TransformStack.of(TransformStack.of(a, b).Top(), c).Top(),
                    TransformStack.of(a, TransformStack.of(b, c).Top()).Top(),
                    "Transforms not associative");
    }
    private static void VerifyCommutative(@Nonnull Transform a, @Nonnull Transform b)
    {
        AssertEqual(
            TransformStack.of().andThen(a).andThen(b).Top(),
            TransformStack.of().andThen(b).andThen(a).Top(),
            "Transforms not commutative");
    }

    private static final float Epsilon = 0.03f;
    private static void AssertEqual(@Nonnull Transform a, @Nonnull Transform b, @Nonnull String error)
    {
        Vector3f eulerA = ToEuler(a.Orientation);
        Vector3f eulerB = ToEuler(b.Orientation);
        if(!Maths.Approx(eulerA.x, eulerB.x, Epsilon)
        || !Maths.Approx(eulerA.y, eulerB.y, Epsilon)
        || !Maths.Approx(eulerA.z, eulerB.z, Epsilon)
        || !Maths.Approx(a.Position.x, b.Position.x, Epsilon)
        || !Maths.Approx(a.Position.y, b.Position.y, Epsilon)
        || !Maths.Approx(a.Position.z, b.Position.z, Epsilon)
        || !Maths.Approx(a.Scale.x, b.Scale.x, Epsilon)
        || !Maths.Approx(a.Scale.y, b.Scale.y, Epsilon)
        || !Maths.Approx(a.Scale.z, b.Scale.z, Epsilon))
        {
            FlansMod.LOGGER.error(error);
        }
    }


    private static void AssertEqual(@Nonnull Quaternionf a, @Nonnull Quaternionf b, @Nonnull String error)
    {
        if(!Maths.Approx(a.x, b.x, Epsilon)
            || !Maths.Approx(a.y, b.y, Epsilon)
            || !Maths.Approx(a.z, b.z, Epsilon)
            || !Maths.Approx(a.w, b.w, Epsilon))
        {
            Vector3f eulerA = ToEuler(a);
            Vector3f eulerB = ToEuler(b);
            FlansMod.LOGGER.error(error + ":" + eulerA + "," + eulerB);
        }
    }

    private static void AssertEqual(@Nonnull Vec3 a, @Nonnull Vec3 b, @Nonnull String error)
    {
        if(!Maths.Approx(a.x, b.x, Epsilon)
            || !Maths.Approx(a.y, b.y, Epsilon)
            || !Maths.Approx(a.z, b.z, Epsilon))
        {
            FlansMod.LOGGER.error(error);
        }
    }
    private static void AssertEqual(@Nonnull Vec3 a, @Nonnull Vector3f b, @Nonnull String error)
    {
        if(!Maths.Approx(a.x, b.x, Epsilon)
            || !Maths.Approx(a.y, b.y, Epsilon)
            || !Maths.Approx(a.z, b.z, Epsilon))
        {
            FlansMod.LOGGER.error(error);
        }
    }

    private static void AssertEqual(@Nonnull Vector3f a, @Nonnull Vector3f b, @Nonnull String error)
    {
        if(!Maths.Approx(a.x, b.x, Epsilon)
            || !Maths.Approx(a.y, b.y, Epsilon)
            || !Maths.Approx(a.z, b.z, Epsilon))
        {
            FlansMod.LOGGER.error(error);
        }
    }

    private static void AssertEqual(float a, float b, @Nonnull String error)
    {
        if(!Maths.Approx(a, b, Epsilon))
        {
            FlansMod.LOGGER.error(error);
        }
    }
}
