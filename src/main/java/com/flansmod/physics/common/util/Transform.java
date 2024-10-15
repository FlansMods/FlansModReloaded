package com.flansmod.physics.common.util;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.joml.*;
import org.joml.Runtime;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Transform
{
    private static final Vector3d       IDENTITY_POS    = new Vector3d();
    private static final Quaternionf    IDENTITY_QUAT   = new Quaternionf();
    private static final Vector3f       IDENTITY_SCALE  = new Vector3f(1f, 1f, 1f);
    public static final Transform IDENTITY = new Transform(() -> "\"Identity\"");

    // -- Fields --
    public static final String DEBUG_OFF = "";
    public static final Supplier<String> DEBUG_OFF_SUPP = () -> DEBUG_OFF;

    @Nullable
    public final Supplier<String> DebugInfo;
    @Nonnull
    public final Vector3d Position;
    @Nonnull
    public final Quaternionf Orientation;
    @Nonnull
    public final Vector3f Scale;

    private void ConstructorNaNCheck()
    {
        // Don't do in live, this is costly
        if(FMLEnvironment.production)
            return;

        if(Orientation.lengthSquared() < 0.01d)
        {
            FlansPhysicsMod.LOGGER.error("Transform has near-zero Quaternion");
        }
        if(hasNaN())
        {
            FlansPhysicsMod.LOGGER.error("Transform failed NaN check");
        }
    }



    private Transform(double x, double y, double z, float pitch, float yaw, float roll, float scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = DEBUG_OFF_SUPP;
        //DebugInfo = debugFunc;
        Position = new Vector3d(x, y, z);
        Orientation = quatFromEuler(pitch, yaw, roll);
        Scale = new Vector3f(scale, scale, scale);
        ConstructorNaNCheck();
    }
    private Transform(@Nonnull Vec3 pos,  @Nonnull Quaternionf rotation, @Nonnull Vector3f scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = DEBUG_OFF_SUPP;
        //DebugInfo = debugFunc;
        Position = new Vector3d(pos.x, pos.y, pos.z);
        Orientation = new Quaternionf(rotation);
        Scale = new Vector3f(scale);
        ConstructorNaNCheck();
    }
    private Transform(@Nonnull Vector3d pos,  @Nonnull Quaternionf rotation, @Nonnull Vector3f scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = DEBUG_OFF_SUPP;
        //DebugInfo = debugFunc;
        Position = new Vector3d(pos.x, pos.y, pos.z);
        Orientation = new Quaternionf(rotation);
        Scale = new Vector3f(scale);
        ConstructorNaNCheck();
    }
    private Transform(double x, double y, double z, @Nonnull Quaternionf rotation, float scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = DEBUG_OFF_SUPP;
        //DebugInfo = debugFunc;
        Position = new Vector3d(x, y, z);
        Orientation = new Quaternionf(rotation);
        Scale = new Vector3f(scale, scale, scale);
        ConstructorNaNCheck();
    }
    private Transform(double x, double y, double z, float scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = DEBUG_OFF_SUPP;
        //DebugInfo = debugFunc;
        Position = new Vector3d(x, y, z);
        Orientation = IDENTITY_QUAT;
        Scale = new Vector3f(scale, scale, scale);
        ConstructorNaNCheck();
    }
    private Transform(@Nonnull Vector3f pos, @Nonnull Quaternionf rotation, @Nonnull Vector3f scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = DEBUG_OFF_SUPP;
        //DebugInfo = debugFunc;
        Position = new Vector3d(pos.x, pos.y, pos.z);
        Orientation = new Quaternionf(rotation);
        Scale = new Vector3f(scale);
        ConstructorNaNCheck();
    }
    private Transform(float scale, @Nullable Supplier<String> debugFunc)
    {
        DebugInfo = DEBUG_OFF_SUPP;
        //DebugInfo = debugFunc;
        Position = IDENTITY_POS;
        Orientation = IDENTITY_QUAT;
        Scale = new Vector3f(scale, scale, scale);
        ConstructorNaNCheck();
    }
    private Transform(@Nullable Supplier<String> debugFunc)
    {
        DebugInfo = DEBUG_OFF_SUPP;
        //DebugInfo = debugFunc;
        Position = IDENTITY_POS;
        Orientation = IDENTITY_QUAT;
        Scale = IDENTITY_SCALE;
        ConstructorNaNCheck();
    }
    private Transform()
    {
        DebugInfo = DEBUG_OFF_SUPP;
        //DebugInfo = null;
        Position = IDENTITY_POS;
        Orientation = IDENTITY_QUAT;
        Scale = IDENTITY_SCALE;
        ConstructorNaNCheck();
    }

    // From complete transform
    private Transform(Transform other) { this(other.Position, other.Orientation, other.Scale, other.DebugInfo); }


    public static Transform compose(Transform ... transforms)
    {
        TransformStack stack = new TransformStack();
        stack.addAll(Arrays.asList(transforms));
        return stack.Top();
    }
    @Nonnull public static Transform identity()                                                                               { return new Transform((Supplier<String>) null); }
    @Nonnull public static Transform fromScale(float scale)                                                                   { return new Transform(scale, null);  }
    @Nonnull public static Transform fromPos(@Nonnull Vec3 pos)                                                               { return new Transform(pos.x, pos.y, pos.z, 1f, null); }
    @Nonnull public static Transform fromPos(double x, double y, double z)                                                    { return new Transform(x, y, z, 1f, null); }
    @Nonnull public static Transform fromPosAndEuler(@Nonnull Vec3 pos, @Nonnull Vector3f euler)                              { return new Transform(pos.x, pos.y, pos.z, euler.x, euler.y, euler.z, 1f, null); }
    @Nonnull public static Transform fromPosAndEuler(@Nonnull Vector3f pos, @Nonnull Vector3f euler)                          { return new Transform(pos.x, pos.y, pos.z, euler.x, euler.y, euler.z, 1f, null);}
    @Nonnull public static Transform fromPosAndEuler(@Nonnull Vec3 pos, float pitch, float yaw, float roll)                   { return new Transform(pos.x, pos.y, pos.z, pitch, yaw, roll, 1f, null);}
    @Nonnull public static Transform fromEuler(float pitch, float yaw, float roll)                                            { return new Transform(0d, 0d, 0d, quatFromEuler(pitch, yaw, roll), 1f, null);}
    @Nonnull public static Transform fromEulerRadians(float pitch, float yaw, float roll)                                     { return new Transform(0d, 0d, 0d, quatFromEulerRadians(pitch, yaw, roll), 1f, null); }
    @Nonnull public static Transform fromEuler(@Nonnull Vector3f euler)                                                       { return new Transform(0d, 0d, 0d, quatFromEuler(euler), 1f, null); }
    @Nonnull public static Transform fromLookDirection(@Nonnull Vec3 forward, @Nonnull Vec3 up)                               { return new Transform(0d, 0d, 0d, lookAlong(forward, up), 1f, null); }
    @Nonnull public static Transform fromPositionAndLookDirection(@Nonnull Vec3 pos, @Nonnull Vec3 forward, @Nonnull Vec3 up) { return new Transform(pos.x, pos.y, pos.z, lookAlong(forward, up), 1f, null); }
    @Nonnull public static Transform fromBlockPos(@Nonnull BlockPos blockPos)                                                 { return new Transform(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1f, null); }
    @Nonnull public static Transform fromPose(@Nonnull Matrix4f pose)                                                         { return new Transform(pose.transformPosition(new Vector3f()), pose.getUnnormalizedRotation(new Quaternionf()), getScale(pose), null); }
    @Nonnull public static Transform fromPose(@Nonnull PoseStack poseStack)                                                   { return fromPose(poseStack.last().pose(), null); }
    @Nonnull public static Transform fromEntity(@Nonnull Entity entity)                                                       { return fromPosAndEuler(entity.position(), entity.getXRot(), entity.getYRot(), 0f, () -> "From Ent:" + entity); }
    @Nonnull public static Transform copy(@Nonnull Transform other)                                                           { return new Transform(other); }

    @Nonnull public static Transform identity(@Nullable Supplier<String> debugFunc)                                                                                 { return new Transform(debugFunc); }
    @Nonnull public static Transform fromScale(float scale, @Nullable Supplier<String> debugFunc)                                                                   { return new Transform(scale, debugFunc);  }
    @Nonnull public static Transform fromScale(@Nonnull Vector3f scale, @Nullable Supplier<String> debugFunc)                                                       { return new Transform(IDENTITY_POS, IDENTITY_QUAT, scale, debugFunc);  }
    @Nonnull public static Transform fromPos(@Nonnull Vec3 pos, @Nullable Supplier<String> debugFunc)                                                               { return new Transform(pos.x, pos.y, pos.z, 1f, debugFunc); }
    @Nonnull public static Transform fromPos(double x, double y, double z, @Nullable Supplier<String> debugFunc)                                                    { return new Transform(x, y, z, 1f, debugFunc); }
    @Nonnull public static Transform fromPosAndEuler(@Nonnull Vec3 pos, @Nonnull Vector3f euler, @Nullable Supplier<String> debugFunc)                              { return new Transform(pos.x, pos.y, pos.z, euler.x, euler.y, euler.z, 1f, debugFunc); }
    @Nonnull public static Transform fromPosAndQuat(@Nonnull Vector3d pos, @Nonnull Quaternionf ori, @Nullable Supplier<String> debugFunc)                          { return new Transform(pos.x, pos.y, pos.z, ori, 1f, debugFunc); }
    @Nonnull public static Transform fromPosAndQuat(@Nonnull Vec3 pos, @Nonnull Quaternionf ori, @Nullable Supplier<String> debugFunc)                              { return new Transform(pos.x, pos.y, pos.z, ori, 1f, debugFunc); }
    @Nonnull public static Transform fromPosAndEuler(@Nonnull Vector3f pos, @Nonnull Vector3f euler, @Nullable Supplier<String> debugFunc)                          { return new Transform(pos.x, pos.y, pos.z, euler.x, euler.y, euler.z, 1f, debugFunc);}
    @Nonnull public static Transform fromPosAndEuler(@Nonnull Vec3 pos, float pitch, float yaw, float roll, @Nullable Supplier<String> debugFunc)                   { return new Transform(pos.x, pos.y, pos.z, pitch, yaw, roll, 1f, debugFunc);}
    @Nonnull public static Transform fromEuler(float pitch, float yaw, float roll, @Nullable Supplier<String> debugFunc)                                            { return new Transform(0d, 0d, 0d, quatFromEuler(pitch, yaw, roll), 1f, debugFunc);}
    @Nonnull public static Transform fromEulerRadians(float pitch, float yaw, float roll, @Nullable Supplier<String> debugFunc)                                     { return new Transform(0d, 0d, 0d, quatFromEulerRadians(pitch, yaw, roll), 1f, debugFunc); }
    @Nonnull public static Transform fromEuler(@Nonnull Vector3f euler, @Nullable Supplier<String> debugFunc)                                                       { return new Transform(0d, 0d, 0d, quatFromEuler(euler), 1f, debugFunc); }
    @Nonnull public static Transform fromLookDirection(@Nonnull Vec3 forward, @Nonnull Vec3 up, @Nullable Supplier<String> debugFunc)                               { return new Transform(0d, 0d, 0d, lookAlong(forward, up), 1f, debugFunc); }
    @Nonnull public static Transform fromPositionAndLookDirection(@Nonnull Vec3 pos, @Nonnull Vec3 forward, @Nonnull Vec3 up, @Nullable Supplier<String> debugFunc) { return new Transform(pos.x, pos.y, pos.z, lookAlong(forward, up), 1f, debugFunc); }
    @Nonnull public static Transform fromItem(@Nonnull ItemTransform itemTransform, @Nullable Supplier<String> debugFunc)                                           { return new Transform(itemTransform.translation.x, itemTransform.translation.y, itemTransform.translation.z, quatFromEuler(itemTransform.rotation), itemTransform.scale.x, debugFunc); }
    @Nonnull public static Transform fromPose(@Nonnull Matrix4f pose, @Nullable Supplier<String> debugFunc)                                                         { return new Transform(pose.transformPosition(new Vector3f()), pose.getUnnormalizedRotation(new Quaternionf()), getScale(pose), debugFunc); }
    @Nonnull public static Transform fromPose(@Nonnull PoseStack poseStack, @Nullable Supplier<String> debugFunc)                                                   { return fromPose(poseStack.last().pose(), debugFunc); }
    @Nonnull public static Transform fromBlockPos(@Nonnull BlockPos blockPos, @Nullable Supplier<String> debugFunc)                                                 { return new Transform(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1f, debugFunc); }
    @Nonnull public static Transform debug(@Nullable Supplier<String> debugFunc)                                                                                    { return new Transform(debugFunc); }
    @Nonnull public static Transform error(@Nonnull String errorMessage)                                                                                            { return new Transform(() -> errorMessage); }
    @Nonnull
    public static Transform extractOrientation(@Nonnull Transform from, boolean invert, @Nullable Supplier<String> debugFunc)
    {
        Quaternionf ori = invert ? from.Orientation.invert(new Quaternionf()) : from.Orientation;
        return new Transform(0d, 0d, 0d, ori, 1f, debugFunc == null ? () -> "{\"OriFrom\":"+from.DebugInfo+"}" : debugFunc);
    }
    @Nonnull
    public static Transform extractPosition(@Nonnull Transform from, double scale, @Nullable Supplier<String> debugFunc)
    {
        Vector3d pos = from.Position.mul(scale, new Vector3d());
        return new Transform(pos.x, pos.y, pos.z, IDENTITY_QUAT, 1f, debugFunc == null ? () -> "{\"PosFrom\":"+from.DebugInfo+"}" : debugFunc);
    }
    @Nonnull
    public static Transform flatten(@Nonnull Consumer<TransformStack> func)
    {
        TransformStack stack = new TransformStack();
        func.accept(stack);
        return stack.Top();
    }
    @Nonnull
    public CompoundTag toTag(boolean storePos, boolean storeOri, boolean storeScale)
    {
        CompoundTag tags = new CompoundTag();
        if(storePos)
        {
            tags.putDouble("px", Position.x);
            tags.putDouble("py", Position.y);
            tags.putDouble("pz", Position.z);
        }
        if(storeOri)
        {
            Vector3f euler = toEuler(Orientation);
            tags.putFloat("rp", euler.x);
            tags.putFloat("ry", euler.y);
            tags.putFloat("rr", euler.z);
        }
        if(storeScale)
        {
            tags.putFloat("sx", Scale.x);
            tags.putFloat("sy", Scale.y);
            tags.putFloat("sz", Scale.z);
        }

        return tags;
    }
    @Nonnull
    public CompoundTag toPosTag() { return toTag(true, false, false); }
    @Nonnull
    public CompoundTag toPosAndOriTag() { return toTag(true, true, false); }
    @Nonnull
    public static Transform fromTag(@Nonnull CompoundTag tags, @Nullable Vec3 withPos, @Nullable Quaternionf withOri, @Nullable Vector3f withScale)
    {
        if(withPos == null)
        {
            double x = tags.getDouble("px");
            double y = tags.getDouble("py");
            double z = tags.getDouble("pz");
            withPos = new Vec3(x, y, z);
        }
        if(withOri == null)
        {
            float pitch = tags.getFloat("rp");
            float yaw = tags.getFloat("ry");
            float roll = tags.getFloat("rr");
            withOri = quatFromEuler(pitch, yaw, roll);
        }
        if(withScale == null)
        {
            float scaleX = tags.getFloat("sx");
            float scaleY = tags.getFloat("sy");
            float scaleZ = tags.getFloat("sz");
            withScale = new Vector3f(scaleX, scaleY, scaleZ);
        }

        return new Transform(withPos, withOri, withScale, ()->"Loaded from Tag");
    }
    @Nonnull
    public static Transform fromTag(@Nonnull CompoundTag tags) {  return fromTag(tags, null, null, null); }
    @Nonnull
    public static Transform fromPosAndOriTag(@Nonnull CompoundTag tags) {  return fromTag(tags, null, null, new Vector3f(1f, 1f, 1f)); }
    @Nonnull
    public static Transform fromTagWithScale(@Nonnull CompoundTag tags, @Nonnull Vector3f scale) {  return fromTag(tags, null, null, scale); }
    @Nonnull
    public Transform inverse()
    {
        return localToGlobalTransform(Transform.IDENTITY);
    }

    // ----------------------------------------------------------------------------------------
    // --- Minecraft RenderSystem / JOML interface ---
    // ----------------------------------------------------------------------------------------
    public void applyToPoseStack(@Nonnull PoseStack poseStack)
    {
        poseStack.translate(Position.x, Position.y, Position.z);
        poseStack.mulPose(Orientation);
        poseStack.scale(Scale.x, Scale.y, Scale.z);
    }
    @Nonnull
    public PoseStack toNewPoseStack()
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
    public static Quaternionf quatFromEuler(@Nonnull Vector3f euler)
    {
        return new Quaternionf()
            .rotateY(-euler.y * Maths.DegToRadF)
            .rotateX(-euler.x * Maths.DegToRadF)
            .rotateZ(-euler.z * Maths.DegToRadF);
    }
    @Nonnull
    public static Quaternionf quatFromEulerRadians(float pitch, float yaw, float roll)
    {
        return new Quaternionf()
            .rotateY(-yaw)
            .rotateX(-pitch)
            .rotateZ(-roll);
    }
    @Nonnull
    public static Quaternionf quatFromEuler(float pitch, float yaw, float roll)
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
    public Transform reflect(boolean inX, boolean inY, boolean inZ)
    {
        Vec3 reflectedPos = new Vec3(inX ? -Position.x : Position.x, inY ? -Position.y : Position.y, inZ ? -Position.z : Position.z);
        Vec3 fwd = forward();
        Vec3 reflectedFwd = new Vec3(inX ? -fwd.x : fwd.x, inY ? -fwd.y : fwd.y, inZ ? -fwd.z : fwd.z);
        Vec3 up = up();
        Vec3 reflectedUp = new Vec3(inX ? -up.x : up.x, inY ? -up.y : up.y, inZ ? -up.z : up.z);
        return Transform.fromPositionAndLookDirection(reflectedPos, reflectedFwd, reflectedUp);
    }
    @Nonnull
    public Transform withEulerAngles(float pitch, float yaw, float roll)
    {
        return new Transform(Position, quatFromEuler(pitch, yaw, roll), Scale, () -> "SetEulers[P:"+pitch+", Y:"+yaw+", R:"+roll+"]");
    }
    @Nonnull
    public Transform withPosition(double x, double y, double z)
    {
        return new Transform(new Vec3(x, y, z), Orientation, Scale, () -> "SetPos["+x+","+y+","+z+"]");
    }
    @Nonnull
    public Transform withPosition(@Nonnull Vec3 pos)
    {
        return new Transform(pos, Orientation, Scale, () -> "SetPos["+pos+"]");
    }
    @Nonnull
    public Transform translated(@Nonnull Vec3 pos)
    {
        return new Transform(new Vector3d(Position.x + pos.x, Position.y + pos.y, Position.z + pos.z), Orientation, Scale, () -> "AddPos["+pos+"]");
    }
    @Nonnull
    public Transform withYaw(float yaw)
    {
        Vector3f euler = euler();
        return new Transform(Position, quatFromEuler(euler.x, yaw, euler.z), Scale, () -> "SetYaw["+yaw+"]");
    }
    @Nonnull
    public Transform withPitch(float pitch)
    {
        Vector3f euler = euler();
        return new Transform(Position, quatFromEuler(pitch, euler.y, euler.z), Scale, () -> "SetPitch["+pitch+"]");
    }
    @Nonnull
    public Transform withRoll(float roll)
    {
        Vector3f euler = euler();
        return new Transform(Position, quatFromEuler(euler.x, euler.y, roll), Scale, () -> "SetRoll["+roll+"]");
    }
    @Nonnull
    public Transform rotateYaw(float dYaw)  { return new Transform(Position, Orientation.mul(quatFromEuler(0, dYaw, 0), new Quaternionf()), Scale, () -> "AddYaw"); }
    @Nonnull
    public Transform rotatePitch(float dPitch)  { return new Transform(Position, Orientation.mul(quatFromEuler(dPitch, 0, 0), new Quaternionf()), Scale, () -> "AddPitch"); }
    @Nonnull
    public Transform rotateRoll(float dRoll)  { return new Transform(Position, Orientation.mul(quatFromEuler(0, 0, dRoll), new Quaternionf()), Scale, () -> "AddRoll"); }

    @Nonnull
    public static Vector3f getScale(@Nonnull Matrix4f mat)
    {
        Vector3f result = new Vector3f();
        mat.getScale(result);
        if(mat.determinant() < 0.0f)
            result.mul(-1f);
        return result;
    }

    @Nonnull
    public static Vector3f toEuler(@Nonnull Quaternionf quat)
    {
        Vector3f euler = quat.getEulerAnglesYXZ(new Vector3f());
        return euler.mul(-Maths.RadToDegF, -Maths.RadToDegF, -Maths.RadToDegF);
    }
    @Nonnull
    public static Quaternionf lookAlong(@Nonnull Vec3 forward, @Nonnull Vec3 up)
    {
        return new Quaternionf().lookAlong(
            (float)forward.x, (float)forward.y, (float)forward.z,
            (float)up.x, (float)up.y, (float)up.z)
            .invert();
    }
    @Nonnull
    public static Quaternionf compose(@Nonnull Quaternionf firstA, @Nonnull Quaternionf thenB)
    {
        return thenB.mul(firstA, new Quaternionf());
    }
    @Nonnull
    public static Quaternionf compose(@Nonnull Quaternionf firstA, @Nonnull Quaternionf thenB, @Nonnull Quaternionf thenC)
    {
        return thenC.mul(thenB, new Quaternionf()).mul(firstA, new Quaternionf());
    }
    @Nonnull
    public static Vector3f rotate(@Nonnull Vector3f vec, @Nonnull Quaternionf around)
    {
        return around.transform(vec, new Vector3f());
    }
    // ----------------------------------------------------------------------------------------


    // ----------------------------------------------------------------------------------------
    // -------- Positional Operations, inc. conversion to/from MC coords and composing --------
    // ----------------------------------------------------------------------------------------
    @Nonnull
    public BlockPos blockPos() { return new BlockPos(Maths.Floor(Position.x), Maths.Floor(Position.y), Maths.Floor(Position.z)); }
    @Nonnull
    public Vec3 positionVec3() { return new Vec3(Position.x, Position.y, Position.z); }
    @Nonnull public Vec3 forward()  { return localToGlobalDirection(new Vec3(0d, 0d, -1d)); }
    @Nonnull public Vec3 back()     { return localToGlobalDirection(new Vec3(0d, 0d, 1d)); }
    @Nonnull public Vec3 up()       { return localToGlobalDirection(new Vec3(0d, 1d, 0d)); }
    @Nonnull public Vec3 down()     { return localToGlobalDirection(new Vec3(0d, -1d, 0d)); }
    @Nonnull public Vec3 right()    { return localToGlobalDirection(new Vec3(1d, 0d, 0d)); }
    @Nonnull public Vec3 left()     { return localToGlobalDirection(new Vec3(-1d, 0d, 0d)); }
    @Nonnull public Vec3 directionVec(@Nonnull Direction dir) { return localToGlobalDirection(new Vec3(dir.step())); }



    public boolean isIdentity() {
        return Position.lengthSquared() <= Maths.Epsilon
            && Orientation.equals(IDENTITY_QUAT, Maths.EpsilonF)
            && Maths.Approx(Scale.x, 1f) && Maths.Approx(Scale.y, 1f) && Maths.Approx(Scale.z, 1f);
    }
    @Nonnull
    public Vector3f euler() { return toEuler(Orientation); }
    public float yaw() { return toEuler(Orientation).y; }
    public float pitch() { return toEuler(Orientation).x; }
    public float roll() { return toEuler(Orientation).z; }
    @Nonnull
    public Matrix3f oriMatrix() {
        //float[] floats = new float[9];
        FloatBuffer buf = MemoryUtil.memAllocFloat(9);
        //ByteBuffer buf = ByteBuffer.allocateDirect(9*4);
        //FloatBuffer buf = FloatBuffer(9);
        buf.mark();
        Orientation.getAsMatrix3f(buf);
        buf.reset();
        return new Matrix3f(buf);

    }
    public boolean hasNaN()
    {

        return Double.isNaN(Position.x) || Double.isNaN(Position.y) || Double.isNaN(Position.z) ||
            Double.isNaN(Orientation.x) || Double.isNaN(Orientation.y) || Double.isNaN(Orientation.z) || Double.isNaN(Orientation.w) ||
            Double.isNaN(Scale.x) || Double.isNaN(Scale.y) || Double.isNaN(Scale.z);
    }


    // ----------------------------------------------------------------------------------------
    // -------- Transformations i.e. Convert between this space and the parent space ----------
    // ----------------------------------------------------------------------------------------
    //  Applied as follows: Rotate, then translate, then scale.
    @Nonnull
    public Vec3 localToGlobalPosition(@Nonnull Vec3 localPos)
    {
        Vector3d scratch = new Vector3d(localPos.x, localPos.y, localPos.z);
        Orientation.transform(scratch);
        scratch.mul(Scale);
        scratch.add(Position);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    @Nonnull
    public Vec3 globalToLocalPosition(@Nonnull Vec3 globalPos)
    {
        Vector3d scratch = new Vector3d(globalPos.x, globalPos.y, globalPos.z);
        scratch.sub(Position);
        Orientation.transformInverse(scratch);
        scratch.mul(1.0f / Scale.x, 1.0f / Scale.y, 1.0f / Scale.z);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    //  In this case, we no longer care about the position offset
    @Nonnull
    public Vec3 localToGlobalVelocity(@Nonnull Vec3 localVelocity)
    {
        Vector3d scratch = new Vector3d(localVelocity.x, localVelocity.y, localVelocity.z);
        Orientation.transform(scratch);
        scratch.mul(Scale);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    @Nonnull
    public Vec3 globalToLocalVelocity(@Nonnull Vec3 globalVelocity)
    {
        Vector3d scratch = new Vector3d(globalVelocity.x, globalVelocity.y, globalVelocity.z);
        scratch.mul(1.0f / Scale.x, 1.0f / Scale.y, 1.0f / Scale.z);
        Orientation.transformInverse(scratch);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    //  In this case, we also don't want to scale. If it's normalized in, it should be normalized out
    @Nonnull
    public Vec3 localToGlobalDirection(@Nonnull Vec3 localDirection)
    {
        Vector3d scratch = new Vector3d(localDirection.x, localDirection.y, localDirection.z);
        Orientation.transform(scratch);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    @Nonnull
    public Vec3 globalToLocalDirection(@Nonnull Vec3 globalDirection)
    {
        Vector3d scratch = new Vector3d(globalDirection.x, globalDirection.y, globalDirection.z);
        Orientation.transformInverse(scratch);
        return new Vec3(scratch.x, scratch.y, scratch.z);
    }
    @Nonnull
    public Quaternionf localToGlobalOrientation(@Nonnull Quaternionf localOri)
    {
        // Scale CAN affect rotations, iff it is negative in one or more axes
        boolean flipX = Scale.x < 0.0f;
        boolean flipY = Scale.y < 0.0f;
        boolean flipZ = Scale.z < 0.0f;
        if(flipX || flipY || flipZ)
            return reflect(flipX, flipY, flipZ).Orientation.mul(localOri, new Quaternionf());

        return Orientation.mul(localOri, new Quaternionf()).normalize();
    }
    @Nonnull
    public Quaternionf globalToLocalOrientation(@Nonnull Quaternionf globalOri)
    {
        // Scale CAN affect rotations, iff it is negative in one or more axes
        boolean flipX = Scale.x < 0.0f;
        boolean flipY = Scale.y < 0.0f;
        boolean flipZ = Scale.z < 0.0f;
        if(flipX || flipY || flipZ)
            return reflect(flipX, flipY, flipZ).Orientation.invert(new Quaternionf()).mul(globalOri, new Quaternionf()).normalize();

        return Orientation.invert(new Quaternionf()).mul(globalOri, new Quaternionf());
    }
    @Nonnull
    public Vector3f localToGlobalScale(@Nonnull Vector3f localScale)
    {
        return localScale.mul(Scale, new Vector3f());
    }
    @Nonnull
    public Vector3f globalToLocalScale(@Nonnull Vector3f  globalScale)
    {
        return globalScale.div(Scale, new Vector3f());
    }
    @Nonnull
    public Transform localToGlobalTransform(@Nonnull Transform localTransform)
    {
        return new Transform(
            localToGlobalPosition(localTransform.positionVec3()),
            localToGlobalOrientation(localTransform.Orientation),
            localToGlobalScale(localTransform.Scale),
            () -> "{\"Local\":"+localTransform+",\"Apply\":"+this+"}");
    }
    @Nonnull
    public Transform globalToLocalTransform(@Nonnull Transform globalTransform)
    {
        return new Transform(
            globalToLocalPosition(globalTransform.positionVec3()),
            globalToLocalOrientation(globalTransform.Orientation),
            globalToLocalScale(globalTransform.Scale),
            () -> "{\"Globl\":"+globalTransform+",\"Apply\":"+this+"}");
    }
    @Nonnull
    public AABB localToGlobalBounds(@Nonnull AABB localBounds)
    {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for(int x = 0; x < 2; x++)
            for(int y = 0; y < 2; y++)
                for(int z = 0; z < 2; z++)
                {
                    Vec3 rotated = localToGlobalPosition(
                        new Vec3(localBounds.minX + localBounds.getXsize() * x,
                                localBounds.minY + localBounds.getYsize() * y,
                                localBounds.minZ + localBounds.getZsize() * z));
                    minX = Maths.Min(minX, rotated.x);
                    minY = Maths.Min(minY, rotated.y);
                    minZ = Maths.Min(minZ, rotated.z);
                    maxX = Maths.Max(maxX, rotated.x);
                    maxY = Maths.Max(maxY, rotated.y);
                    maxZ = Maths.Max(maxZ, rotated.z);

                }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
    // ----------------------------------------------------------------------------------------


    // ----------------------------------------------------------------------------------------
    // ------------------------------- Misc Transform functions -------------------------------
    // ----------------------------------------------------------------------------------------
    @Nonnull
    public static Transform interpolate(@Nonnull Transform a, @Nonnull Transform b, float t)
    {
        return new Transform(
            a.Position.lerp(b.Position, t, new Vector3d()),
            a.Orientation.slerp(b.Orientation, t, new Quaternionf()),
            a.Scale.lerp(b.Scale, t, new Vector3f()),
            () -> "{\"Interpolate\":["+a.DebugInfo+","+b.DebugInfo+"]}");
    }
    @Nonnull
    public static Transform interpolate(@Nonnull List<Transform> transforms)
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

        return Transform.fromPosAndQuat(
            position.mul(1d / transforms.size()),
            (Quaternionf) Quaternionf.slerp(orientations, weights, new Quaternionf()),
            debugInfoBuilder);
    }
    @Nonnull
    public static Transform interpolate(@Nonnull List<Transform> transforms, @Nonnull float[] weights)
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

        return Transform.fromPosAndQuat(
            position.mul(1d / totalWeight),
            (Quaternionf) Quaternionf.slerp(orientations, weights, new Quaternionf()),
            debugInfoBuilder);
    }


    private static final NumberFormat FLOAT_FORMAT = new DecimalFormat("#.##");
    private static final NumberFormat ANGLE_FORMAT = new DecimalFormat("#");



    @Nonnull
    public String getDebugInfo()
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
            return "{\"Dbg\":"+ getDebugInfo()+"}";
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
                Vector3f euler = toEuler(Orientation);
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
            output.append(", \"Dbg\":").append(getDebugInfo()).append("}");
            return output.toString();
        }
    }



}
