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
import java.util.List;

public class Transform
{
    private static final Vector3d ZERO_POS = new Vector3d();
    private static final Quaternionf IDENTITY_QUAT = new Quaternionf();
    public static final Transform IDENTITY = new Transform("\"Identity\"");
    public static Transform Error(String errorMessage){
        return new Transform() {
            @Override
            public String toString()
            {
                return "{\"ERROR\":\"" + errorMessage + "\"}";
            }
        };
    }

    // -- Fields --
    public final String DebugInfo;
    public final Vector3d Position;
    public final Quaternionf Orientation;
    public final float Scale;

    private Transform(String debugInfo, double x, double y, double z, float pitch, float yaw, float roll, float scale)
    {
        DebugInfo = debugInfo;
        Position = new Vector3d(x, y, z);
        Orientation = FromEuler(pitch, yaw, roll);
        Scale = scale;
    }
    private Transform(String debugInfo, double x, double y, double z, Quaternionf rotation, float scale)
    {
        DebugInfo = debugInfo;
        Position = new Vector3d(x, y, z);
        Orientation = new Quaternionf(rotation);
        Scale = scale;
    }
    private Transform(String debugInfo, double x, double y, double z, float scale)
    {
        DebugInfo = debugInfo;
        Position = new Vector3d(x, y, z);
        Orientation = IDENTITY_QUAT;
        Scale = scale;
    }
    private Transform(String debugInfo, float scale)
    {
        DebugInfo = debugInfo;
        Position = ZERO_POS;
        Orientation = IDENTITY_QUAT;
        Scale = scale;
    }

    // From complete transform
    private Transform(Transform other) { this(other.DebugInfo, other.Position, other.Orientation, other.Scale); }
    private Transform(String debugInfo, Matrix4f pose)
    {
        this(debugInfo,
             pose.transformPosition(new Vector3f()),
             pose.getUnnormalizedRotation(new Quaternionf()),
             pose.getScale(new Vector3f()).x);
    }
    private Transform(String debugInfo, ItemTransform itemTransform) { this(debugInfo, itemTransform.translation, FromEuler(itemTransform.rotation), itemTransform.scale.x); }

    private Transform(String debugInfo, Vec3 pos, Quaternionf ori, float scale) { this(debugInfo, pos.x, pos.y, pos.z, ori, scale); }
    private Transform(String debugInfo, Vector3d pos, Quaternionf ori, float scale) { this(debugInfo, pos.x, pos.y, pos.z, ori, scale); }
    private Transform(String debugInfo, Vector3f pos, Quaternionf ori, float scale) { this(debugInfo, pos.x, pos.y, pos.z, ori, scale); }
    private Transform(String debugInfo, Vec3 pos, Vector3f euler, float scale) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), scale); }
    private Transform(String debugInfo, Vector3d pos, Vector3f euler, float scale) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), scale); }
    private Transform(String debugInfo, Vector3f pos, Vector3f euler, float scale) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), scale); }

    // From pos and ori
    private Transform(String debugInfo, Vec3 pos, Quaternionf ori) { this(debugInfo, pos.x, pos.y, pos.z, ori, 1f); }
    private Transform(String debugInfo, Vector3d pos, Quaternionf ori) { this(debugInfo, pos.x, pos.y, pos.z, ori, 1f); }
    private Transform(String debugInfo, Vector3f pos, Quaternionf ori) { this(debugInfo, pos.x, pos.y, pos.z, ori, 1f); }
    private Transform(String debugInfo, Vec3 pos, Vector3f euler) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), 1f); }
    private Transform(String debugInfo, Vector3d pos, Vector3f euler) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), 1f); }
    private Transform(String debugInfo, Vector3f pos, Vector3f euler) { this(debugInfo, pos.x, pos.y, pos.z, FromEuler(euler), 1f); }

    // From just ori
    public Transform(String debugInfo, Quaternionf ori) { this(debugInfo, 0d, 0d, 0d, ori, 1f); }

    // From pos and scale
    private Transform(String debugInfo, Vec3 pos, float scale) { this(debugInfo, pos.x, pos.y, pos.z, scale); }
    private Transform(String debugInfo, Vector3i pos, float scale) { this(debugInfo, pos.x, pos.y, pos.z, scale); }
    private Transform(String debugInfo, Vector3d pos, float scale) { this(debugInfo, pos.x, pos.y, pos.z, scale); }
    private Transform(String debugInfo, Vector3f pos, float scale) { this(debugInfo, pos.x, pos.y, pos.z, scale); }

    // From just pos
    private Transform(String debugInfo, Vec3 pos) { this(debugInfo, pos.x, pos.y, pos.z, 1f); }
    private Transform(String debugInfo, Vector3i pos) { this(debugInfo, pos.x, pos.y, pos.z, 1f); }
    private Transform(String debugInfo, Vector3d pos) { this(debugInfo, pos.x, pos.y, pos.z, 1f); }
    private Transform(String debugInfo, Vector3f pos) { this(debugInfo, pos.x, pos.y, pos.z, 1f); }

    private Transform(String debugInfo) { this(debugInfo, 1f); }
    private Transform() { this("{}", 1f); }
    public static Transform Identity(@Nonnull String debugInfo) {
        return new Transform(debugInfo);
    }
    @Nonnull
    public static Transform FromScale(@Nonnull String debugInfo, float scale) {
        return new Transform(debugInfo, scale);
    }
    @Nonnull
    public static Transform FromPos(@Nonnull String debugInfo, @Nonnull Vec3 pos) {
        return new Transform(debugInfo, pos);
    }
    @Nonnull
    public static Transform FromPos(@Nonnull String debugInfo, double x, double y, double z) {
        return new Transform(debugInfo, x, y, z, 1.0f);
    }
    @Nonnull
    public static Transform FromPosAndEuler(@Nonnull String debugInfo, @Nonnull Vec3 pos, @Nonnull Vector3f euler) {
        return new Transform(debugInfo, pos, euler);
    }
    @Nonnull
    public static Transform FromPosAndEuler(@Nonnull String debugInfo, @Nonnull Vector3f pos, @Nonnull Vector3f euler) {
        return new Transform(debugInfo, pos, euler);
    }
    @Nonnull
    public static Transform FromPosAndEuler(@Nonnull String debugInfo, @Nonnull Vec3 pos, float pitch, float yaw, float roll) {
        return new Transform(debugInfo, pos.x, pos.y, pos.z, pitch, yaw, roll, 1f);
    }
    @Nonnull
    public static Transform FromEuler(@Nonnull String debugInfo, float pitch, float yaw, float roll) {
        return new Transform(debugInfo, FromEuler(pitch, yaw, roll));
    }
    @Nonnull
    public static Transform FromEuler(@Nonnull String debugInfo, @Nonnull Vector3f euler) {
        return new Transform(debugInfo, FromEuler(euler));
    }
    @Nonnull
    public static Transform FromLookDirection(@Nonnull String debugInfo, @Nonnull Vec3 forward, @Nonnull Vec3 up) {
        return new Transform(debugInfo, LookAlong(forward, up));
    }
    @Nonnull
    public static Transform FromPositionAndLookDirection(@Nonnull String debugInfo, @Nonnull Vec3 pos, @Nonnull Vec3 forward, @Nonnull Vec3 up) {
        return new Transform(debugInfo, pos, LookAlong(forward, up));
    }
    @Nonnull
    public static Transform ExtractOrientation(@Nullable String debugInfo, @Nonnull Transform from, boolean invert)
    {
        Quaternionf ori = invert ? from.Orientation.invert(new Quaternionf()) : from.Orientation;
        return debugInfo == null
            ? new Transform("{\"OriFrom\":"+from.DebugInfo+"}", ori)
            : new Transform(debugInfo, ori);
    }
    @Nonnull
    public static Transform ExtractPosition(@Nullable String debugInfo, @Nonnull Transform from, double scale)
    {
        Vector3d pos = from.Position.mul(scale, new Vector3d());
        return debugInfo == null
            ? new Transform("{\"PosFrom\":"+from.DebugInfo+"}", pos)
            : new Transform(debugInfo, pos);
    }

    // ----------------------------------------------------------------------------------------
    // --- Minecraft RenderSystem / JOML interface ---
    // ----------------------------------------------------------------------------------------
    @Nonnull
    public static Transform FromPoseStack(@Nonnull String sourceName, @Nonnull PoseStack poseStack) {
        return new Transform("\"PoseStack[" + sourceName +"]\"", poseStack.last().pose());
    }
    @Nonnull
    public static Transform FromItemTransform(@Nonnull String sourceName, @Nonnull ItemTransform itemTransform)
    {
        return new Transform("{\"ItemTransform\":\""+sourceName+"\",\"Transform\":\""+itemTransform+"\"}", itemTransform);
    }
    public void ApplyToPoseStack(@Nonnull PoseStack poseStack)
    {
        poseStack.translate(Position.x, Position.y, Position.z);
        poseStack.mulPose(Orientation);
        poseStack.scale(Scale, Scale, Scale);
    }
    @Nonnull
    public PoseStack ToNewPoseStack()
    {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(Position.x, Position.y, Position.z);
        poseStack.mulPose(Orientation);
        poseStack.scale(Scale, Scale, Scale);
        return poseStack;
    }
    // ----------------------------------------------------------------------------------------

    // ----------------------------------------------------------------------------------------
    // --------- Angular Operations, inc. conversion to/from MC eulers and composing ----------
    // ----------------------------------------------------------------------------------------
    @Nonnull
    private static Quaternionf FromEuler(@Nonnull Vector3f euler)
    {
        return new Quaternionf()
            .rotateY(-euler.y * Maths.DegToRadF)
            .rotateX(-euler.x * Maths.DegToRadF)
            .rotateZ(-euler.z * Maths.DegToRadF);
    }
    @Nonnull
    private static Quaternionf FromEuler(float pitch, float yaw, float roll)
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
    public static Transform FromBlockPos(@Nonnull String debugInfo, @Nonnull BlockPos blockPos) { return new Transform(debugInfo, new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())); }
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
    public Quaternionf LocalToGlobalOrientation(Quaternionf localOri)
    {
        return Orientation.mul(localOri, new Quaternionf());
    }
    public Quaternionf GlobalToLocalOrientation(Quaternionf globalOri)
    {
        return Orientation.invert(new Quaternionf()).mul(globalOri, new Quaternionf());
    }
    public float LocalToGlobalScale(float localScale)
    {
        return localScale * Scale;
    }
    public float GlobalToLocalScale(float globalScale)
    {
        return globalScale / Scale;
    }
    public Transform LocalToGlobalTransform(Transform localTransform)
    {
        return new Transform(
            "{\"Local\":" + localTransform.toString() +
            ",\"Apply\":" + toString() + "}",
            LocalToGlobalPosition(localTransform.PositionVec3()),
            LocalToGlobalOrientation(localTransform.Orientation),
            LocalToGlobalScale(localTransform.Scale));
    }
    public Transform GlobalToLocalTransform(Transform globalTransform)
    {
        return new Transform(
            "{\"Globl\":" + globalTransform.toString() +
            ",\"Apply\":" + toString() + "}",
            GlobalToLocalPosition(globalTransform.PositionVec3()),
            GlobalToLocalOrientation(globalTransform.Orientation),
            GlobalToLocalScale(globalTransform.Scale));
    }
    // ----------------------------------------------------------------------------------------


    // ----------------------------------------------------------------------------------------
    // ------------------------------- Misc Transform functions -------------------------------
    // ----------------------------------------------------------------------------------------
    @Nonnull
    public static Transform Interpolate(@Nonnull Transform a, @Nonnull Transform b, float t)
    {
        return new Transform(
            "{\"Interpolate\":["+a.DebugInfo+","+b.DebugInfo+"]}",
            a.Position.lerp(b.Position, t, new Vector3d()),
            a.Orientation.slerp(b.Orientation, t, new Quaternionf()),
            Maths.LerpF(a.Scale, b.Scale, t));
    }
    @Nonnull
    public static Transform Interpolate(@Nonnull List<Transform> transforms)
    {
        if(transforms.size() <= 0)
            return Transform.IDENTITY;
        if(transforms.size() == 1)
            return transforms.get(0);

        StringBuilder debugInfo = new StringBuilder("{\"Interpolate\":[");
        Vector3d position = new Vector3d();
        Quaternionf[] orientations = new Quaternionf[transforms.size()];
        float[] weights = new float[transforms.size()];
        for(int i = 0; i < transforms.size(); i++)
        {
            debugInfo.append(transforms.get(i).DebugInfo);
            position.add(transforms.get(i).Position);
            orientations[i] = transforms.get(i).Orientation;
            weights[i] = 1f / transforms.size();
            if(i != transforms.size() - 1)
                debugInfo.append(',');
        }

        return new Transform(
            debugInfo.append("]}").toString(),
            position.mul(1d / transforms.size()),
            (Quaternionf) Quaternionf.slerp(orientations, weights, new Quaternionf()));
    }
    @Nonnull
    public static Transform Interpolate(@Nonnull List<Transform> transforms, @Nonnull float[] weights)
    {
        if(transforms.size() <= 0)
            return Transform.IDENTITY;
        if(transforms.size() == 1)
            return transforms.get(0);

        StringBuilder debugInfo = new StringBuilder("{\"Interpolate\":[");
        Vector3d position = new Vector3d();
        Quaternionf[] orientations = new Quaternionf[transforms.size()];
        float totalWeight = 0.0f;
        for(int i = 0; i < transforms.size(); i++)
        {
            debugInfo.append('{').append("\"Weight\":").append(weights[i]).append(", \"Trans\":").append(transforms.get(i).DebugInfo).append('}');
            position.add(transforms.get(i).Position.mul(weights[i], new Vector3d()));
            orientations[i] = transforms.get(i).Orientation;
            totalWeight += weights[i];
            if(i != transforms.size() - 1)
                debugInfo.append(',');
        }

        return new Transform(
            debugInfo.append("]}").toString(),
            position.mul(1d / totalWeight),
            (Quaternionf) Quaternionf.slerp(orientations, weights, new Quaternionf()));
    }



    private static final NumberFormat FLOAT_FORMAT = new DecimalFormat("#.##");
    private static final NumberFormat ANGLE_FORMAT = new DecimalFormat("#");

    @Override
    public String toString()
    {
        boolean isZeroPos = Maths.Approx(Position.lengthSquared(), 0d);
        boolean isIdentityRot = Maths.Approx(Orientation.x, 0f) && Maths.Approx(Orientation.y, 0f) && Maths.Approx(Orientation.z, 0f) && Maths.Approx(Orientation.w, 1f);
        boolean isOneScale = Maths.Approx(Scale, 1f);
        if(isZeroPos && isIdentityRot && isOneScale)
            return "{\"Dbg\":"+DebugInfo+"}";
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
                output.append("\"Scl\":").append(Runtime.format(Scale, FLOAT_FORMAT));
            }
            output.append(", \"Dbg\":").append(DebugInfo).append("}");
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
        Quaternionf yaw90 = FromEuler(eulerYaw90);
        Quaternionf yaw135 = FromEuler(eulerYaw135);
        Quaternionf pitchUp90 = FromEuler(eulerPitchUp90);
        Quaternionf pitchDown45 = FromEuler(eulerPitchDown45);
        Quaternionf pitchDown90 = FromEuler(eulerPitchDown90);
        Quaternionf rollLeft45 = FromEuler(eulerRollLeft45);
        Quaternionf rollRight45 = FromEuler(eulerRollRight45);
        Quaternionf rollRight60 = FromEuler(eulerRollRight60);
        Quaternionf rollRight90 = FromEuler(eulerRollRight90);
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
        Quaternionf yaw90Pitch45 = FromEuler(eulerYaw90Pitch45);
        Quaternionf yaw90Pitch45Roll45 = FromEuler(eulerYaw90Pitch45Roll45);
        Quaternionf pitch45Roll60 = FromEuler(eulerPitch45Roll60);
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
        Transform offsetXAxis = Transform.FromPos("X", 1d, 0d, 0d);
        Transform offsetYAxis = Transform.FromPos("Y", 0d, 1d, 0d);
        Transform offsetZAxis = Transform.FromPos("Z", 0d, 0d, 1d);
        VerifyCommutative(offsetXAxis, offsetYAxis);
        VerifyCommutative(offsetXAxis, offsetZAxis);
        VerifyAssociative(offsetXAxis, offsetYAxis, offsetZAxis);
        // Test the properties of rotations
        Transform tYaw90 = Transform.FromEuler("Yaw90", eulerYaw90);
        Transform tYaw135 = Transform.FromEuler("Yaw135", eulerYaw135);
        Transform tPitchUp90 = Transform.FromEuler("PitchUp90", eulerPitchUp90);
        Transform tPitchDown45 = Transform.FromEuler("PitchDown45", eulerPitchDown45);
        Transform tRollRight45 = Transform.FromEuler("RollRight45", eulerRollRight45);
        Transform tRollRight90 = Transform.FromEuler("RollRight90", eulerRollRight90);
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
        AssertEqual(Transform.FromLookDirection("look", tYaw90.ForwardVec3(), tYaw90.UpVec3()), tYaw90, "Look along failed");
        AssertEqual(Transform.FromLookDirection("look", tPitchDown45.ForwardVec3(), tPitchDown45.UpVec3()), tPitchDown45, "Look along failed");
        AssertEqual(Transform.FromLookDirection("look", tRollRight90.ForwardVec3(), tRollRight90.UpVec3()), tRollRight90, "Look along failed");

        Transform composed = TransformStack.of(tYaw90, tRollRight45, tPitchDown45).Top();
        AssertEqual(Transform.FromLookDirection("look", composed.ForwardVec3(), composed.UpVec3()), composed, "Look along failed");

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
        || !Maths.Approx(a.Scale, b.Scale, Epsilon))
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
