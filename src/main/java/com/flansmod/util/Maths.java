package com.flansmod.util;

import com.flansmod.common.FlansMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Maths
{
    // Long maths
    public static long Max(long a, long b) { return a > b ? a : b; }
    public static long Min(long a, long b) { return a < b ? a : b; }

    // Integer maths
    public static int Modulo(int a, int b)
    {
        int c = a % b;
        if(c < 0)
            c += b;
        return c;
    }
    public static int Abs(int a) { return a < 0 ? -a : a; }
    public static int Clamp(int i, int min, int max) { return i > max ? max : (i < min ? min : i);  }
    public static int Max(int a, int b) { return a > b ? a : b; }
    public static int Min(int a, int b) { return a < b ? a : b; }
    public static int CeilLerp(int a, int b, float t) { return Maths.Ceil(a + (b-a)*t); }
    public static int FloorLerp(int a, int b, float t) { return Maths.Floor(a + (b-a)*t); }

    // Double maths
    public static final double Root2 = Sqrt(2);
    public static final double Epsilon = 0.000000000001d;
    public static final double EpsilonSq = Epsilon * Epsilon;
    public static double Abs(double a) { return a < 0 ? -a : a; }
    public static double Clamp(double i, double min, double max) { return i > max ? max : (i < min ? min : i);  }
    public static int Ceil(double d) { return (int)Math.ceil(d); }
    public static int Floor(double d) { return (int)Math.floor(d); }
    public static int Round(double d) { return (int)Math.round(d); }
    public static double Max(double a, double b) { return a > b ? a : b; }
    public static double Min(double a, double b) { return a < b ? a : b; }
    public static boolean Approx(double a, double b) { return Abs(a-b) < Epsilon; }
    public static boolean Approx(double a, double b, double epsilon) { return Abs(a-b) < epsilon; }
    public static double Lerp(double a, double b, double t) { return a + (b-a)*t; }
    public static double Sign(double d) { return d > 0.0d ? 1.0d : (d < 0.0d ? -1.0d : 0.0d); }
    public static double Exp(double d) { return Math.exp(d); }
    public static double Pow(double a, double b) { return Math.pow(a, b); }

    // Float maths
    public static final float Root2F = SqrtF(2f);
    public static final float EpsilonF = 0.000001f;
    public static float Clamp(float i, float min, float max)
    {
        return i > max ? max : (i < min ? min : i);
    }
    public static float Abs(float a) { return a < 0 ? -a : a; }
    public static float Max(float a, float b) { return a > b ? a : b; }
    public static float Max(float a, float b, float c) { return Max(Max(a, b), c); }
    public static float Min(float a, float b) { return a < b ? a : b; }
    public static int Ceil(float d) { return (int)Math.ceil(d); }
    public static int Floor(float d) { return (int)Math.floor(d); }
    public static int Round(float d) { return (int)Math.round(d); }
    public static boolean Approx(float a, float b) { return Abs(a-b) < EpsilonF; }
    public static boolean Approx(float a, float b, float epsilon) { return Abs(a-b) < epsilon; }
    public static float LerpF(float a, float b, float t) { return a + (b-a)*Maths.Clamp(t, 0f, 1f); }
    public static float Sign(float f) { return f > 0.0f ? 1.0f : (f < 0.0f ? -1.0f : 0.0f); }
    public static float ExpF(float f) { return (float)Math.exp(f); }
    public static float PowF(float a, float b) { return (float)Math.pow(a, b); }

    public static final double Pi = 3.1415926535897932384626433832795028841971d;
    public static final double Tau = 2.0d * Pi;
    public static final double DegToRad = Tau / 360.0d;
    public static final double RadToDeg = 360.0d / Tau;
    
    public static final float PiF = (float)Pi;
    public static final float TauF = (float)Tau;
    public static final float DegToRadF = (float)DegToRad;
    public static final float RadToDegF = (float)RadToDeg;

    public static double Sin(double f) { return Math.sin(f); }
    public static double Cos(double f) { return Math.cos(f); }
    public static double Sqrt(double f) { return Math.sqrt(f); }
    public static double Atan(double f) { return Math.atan(f); }
    public static double Atan2(double a, double b) { return Math.atan2(a, b); }

    public static float SinF(float f) { return (float)Math.sin(f); }
    public static float CosF(float f) { return (float)Math.cos(f); }
    public static float SqrtF(float f) { return (float)Math.sqrt(f); }
    public static float AtanF(float f) { return (float)Math.atan(f); }
    public static float Atan2F(float a, float b) { return (float)Math.atan2(a, b); }

    public static Vector3f Lerp(Vector3f a, Vector3f b, float t) { return new Vector3f(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t); }
    public static Vector3f Sub(Vector3f a, Vector3f b) { return new Vector3f(a.x - b.x, a.y - b.y, a.z - b.z); }
    public static Vector3f Add(Vector3f a, Vector3f b) { return new Vector3f(a.x + b.x, a.y + b.y, a.z + b.z); }
    public static Vector3f Cross(Vector3f a, Vector3f b) { return a.cross(b, new Vector3f()); }
    public static Vector3f IdentityPosF()
    {
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }

    public static Vector3d Lerp(Vector3d a, Vector3d b, float t) { return new Vector3d(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t); }
    public static Vector3d Sub(Vector3d a, Vector3d b) { return new Vector3d(a.x - b.x, a.y - b.y, a.z - b.z); }
    public static Vector3d Add(Vector3d a, Vector3d b) { return new Vector3d(a.x + b.x, a.y + b.y, a.z + b.z); }
    public static Vector3d Cross(Vector3d a, Vector3d b) { return a.cross(b, new Vector3d()); }
    public static Vector3d IdentityPosD()
    {
        return new Vector3d(0.0d, 0.0d, 0.0d);
    }

    public static Vec3 Lerp(Vec3 a, Vec3 b, float t) { return new Vec3(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t); }
    public static Vec3 Sub(Vec3 a, Vec3 b) { return new Vec3(a.x - b.x, a.y - b.y, a.z - b.z); }
    public static Vec3 Add(Vec3 a, Vec3 b) { return new Vec3(a.x + b.x, a.y + b.y, a.z + b.z); }
    public static Vec3 Cross(Vec3 a, Vec3 b) { return a.cross(b); }
    public static double LengthXYZ(Vec3 v) { return v.length(); }
    public static double LengthXZ(Vec3 v) { return Sqrt(v.x * v.x + v.z * v.z); }
    public static boolean Approx(Vec3 a, Vec3 b) { return (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y)+(a.z-b.z)*(a.z-b.z) < EpsilonSq; }


    public static Quaternionf Slerp(Quaternionf a, Quaternionf b, float t) { return a.slerp(b, t, new Quaternionf()); }
    public static Orientation Slerp(Orientation a, Orientation b, float t) { return a.Slerp(b, t); }

    public static float ClampDegrees(float f)
    {
        f %= 360.0f;
        if(f >= 180f) return f - 360f;
        if(f < -180f) return f + 360f;
        return f;
    }
    public static float LerpDegrees(float a, float b, float t)
    {
        float delta = ClampDegrees(b - a);
        return a + delta * t;
    }

    public static double CalculateParameter(Vec3 v0, Vec3 v1, Vec3 vT)
    {
        double tX = (vT.x - v0.x) / (v1.x - v0.x);
        if(Double.isFinite(tX))
            return tX;
        double tY = (vT.y - v0.y) / (v1.y - v0.y);
        if(Double.isFinite(tY))
            return tY;
        double tZ = (vT.z - v0.z) / (v1.z - v0.z);
        if(Double.isFinite(tZ))
            return tZ;

        FlansMod.LOGGER.error(vT + " is not between " + v0 + " and " + v1);
        return Double.NaN;
    }

    public static int BlockPosToPinpointData(BlockPos pos, int extraData)
    {
       return (extraData & 0xff) << 24
            | (pos.getX() & 0xff) << 16
            | (pos.getY() & 0xff) << 8
            | (pos.getZ() & 0xff);
    }

    public static int GetClosestDistanceModulo(int a, int b, int mod)
    {
        int delta = Modulo(a - b, mod);  // in [0, mod)
        int complement = delta - mod;       // in [-mod, 0)

        return Abs(complement) < Abs(delta) ? complement : delta;
    }

    public static Vec3 Reflect(Vec3 incident, Direction surface)
    {
        switch(surface)
        {
            case UP, DOWN -> { return new Vec3(incident.x, -incident.y, incident.z); }
            case NORTH, SOUTH -> { return new Vec3(incident.x, incident.y, -incident.z); }
            case EAST, WEST -> { return new Vec3(-incident.x, incident.y, incident.z); }
            default -> {return incident;}
        }
    }

    public static BlockPos ResolveBlockPos(BlockPos roughGuess, int pinpointData)
    {
        int xMod256 = roughGuess.getX() & 0xff;
        int yMod256 = roughGuess.getY() & 0xff;
        int zMod256 = roughGuess.getZ() & 0xff;

        int xPin = (pinpointData >> 16) & 0xff;
        int yPin = (pinpointData >> 8) & 0xff;
        int zPin = (pinpointData) & 0xff;

        int dx = GetClosestDistanceModulo(xMod256, xPin, 256);
        int dy = GetClosestDistanceModulo(yMod256, yPin, 256);
        int dz = GetClosestDistanceModulo(zMod256, zPin, 256);

        return new BlockPos(roughGuess.getX() + dx, roughGuess.getY() + dy, roughGuess.getZ() + dz);
    }


    public static String ToRomanNumerals(int n)
    {
        StringBuilder builder = new StringBuilder();
        if(n >= 10000)
        {
            builder.append(n/1000).append('M');
        }
        else
        {
            while (n >= 1000)
            {
                builder.append("M");
                n -= 1000;
            }
        }
        n = GenerateNumeralsForPow10(n, builder, 100, 'M', 'D', 'C');
        n = GenerateNumeralsForPow10(n, builder, 10, 'C', 'L', 'X');
        n = GenerateNumeralsForPow10(n, builder, 1, 'X', 'V', 'I');
        return builder.toString();
    }

    private static int GenerateNumeralsForPow10(int n, StringBuilder builder, int pow10, char ten, char five, char one)
    {
        // n should be < pow10 * 10
        if(n >= pow10 * 9)
        {
            builder.append(one).append(ten);
            n -= pow10 * 9;
        }
        else if(n >= pow10 * 5)
        {
            builder.append(five);
            while(n >= pow10 * 6)
            {
                builder.append(one);
                n -= pow10;
            }
            n -= pow10 * 5;
        }
        else if(n >= pow10 * 4)
        {
            builder.append(one).append(five);
            n -= pow10 * 4;
        }
        else
        {
            while(n >= pow10)
            {
                builder.append(one);
                n -= pow10;
            }
        }
        return n;
    }

    public static boolean RayBoxIntersect(@Nonnull Vec3 startPos,
                                          @Nonnull Vec3 endPos,
                                          @Nonnull Transform boxCenterTransform,
                                          @Nonnull Vector3f boxHalfExtents,
                                          @Nullable Vector3d outPos)
    {
        // Localise
        startPos = boxCenterTransform.GlobalToLocalPosition(startPos);
        endPos = boxCenterTransform.GlobalToLocalPosition(endPos);
        Vec3 motion = endPos.subtract(startPos);

        // We now have an AABB starting at -halfExtents and with dimensions 2*halfExtents and our ray in the same coordinate system
        // We are looking for a point at which the ray enters the box, so we need only consider faces that the ray can see. Partition the space into 3 areas in each axis

        // X - axis and faces x = -half.x, and x = half.x
        if(motion.x != 0F)
        {
            if(startPos.x < -boxHalfExtents.x) //Check face -half.x
            {
                double intersectTime = (-boxHalfExtents.x - startPos.x) / motion.x;
                double intersectY = startPos.y + motion.y * intersectTime;
                double intersectZ = startPos.z + motion.z * intersectTime;
                if(-boxHalfExtents.y <= intersectY && intersectY <= boxHalfExtents.y
                    && -boxHalfExtents.z <= intersectZ && intersectZ <= boxHalfExtents.z)
                {
                    if(outPos != null)
                    {
                        Vec3 globalIntersect = boxCenterTransform.LocalToGlobalPosition(new Vec3(-boxHalfExtents.x, intersectY, intersectZ));
                        outPos.set(globalIntersect.x, globalIntersect.y, globalIntersect.z);
                    }
                    return true;
                }
            }
            else if(startPos.x > boxHalfExtents.x) //Check face +half.x
            {
                double intersectTime = (boxHalfExtents.x - startPos.x) / motion.x;
                double intersectY = startPos.y + motion.y * intersectTime;
                double intersectZ = startPos.z + motion.z * intersectTime;
                if(-boxHalfExtents.y <= intersectY && intersectY <= boxHalfExtents.y
                    && -boxHalfExtents.z <= intersectZ && intersectZ <= boxHalfExtents.z)
                {
                    if (outPos != null)
                    {
                        Vec3 globalIntersect = boxCenterTransform.LocalToGlobalPosition(new Vec3(boxHalfExtents.x, intersectY, intersectZ));
                        outPos.set(globalIntersect.x, globalIntersect.y, globalIntersect.z);
                    }
                    return true;
                }
            }
        }

        // Z - axis and faces z = -half.z and z = half.z
        if(motion.z != 0F)
        {
            if(startPos.z < -boxHalfExtents.z) // Check face z = -half.z
            {
                double intersectTime = (-boxHalfExtents.z - startPos.z) / motion.z;
                double intersectX = startPos.x + motion.x * intersectTime;
                double intersectY = startPos.y + motion.y * intersectTime;
                if(-boxHalfExtents.x <= intersectX && intersectX <= boxHalfExtents.x
                    && -boxHalfExtents.y <= intersectY && intersectY <= boxHalfExtents.y)
                {
                    if (outPos != null)
                    {
                        Vec3 globalIntersect = boxCenterTransform.LocalToGlobalPosition(new Vec3(intersectX, intersectY, -boxHalfExtents.z));
                        outPos.set(globalIntersect.x, globalIntersect.y, globalIntersect.z);
                    }
                    return true;
                }
            }
            else if(startPos.z > boxHalfExtents.z) //Check face z = +half.z
            {
                double intersectTime = (boxHalfExtents.z - startPos.z) / motion.z;
                double intersectX = startPos.x + motion.x * intersectTime;
                double intersectY = startPos.y + motion.y * intersectTime;
                if(-boxHalfExtents.x <= intersectX && intersectX <= boxHalfExtents.x
                    && -boxHalfExtents.y <= intersectY && intersectY <= boxHalfExtents.y)
                {
                    if (outPos != null)
                    {
                        Vec3 globalIntersect = boxCenterTransform.LocalToGlobalPosition(new Vec3(intersectX, intersectY, boxHalfExtents.z));
                        outPos.set(globalIntersect.x, globalIntersect.y, globalIntersect.z);
                    }
                    return true;
                }
            }
        }

        // Y - axis and faces y = -half.y and y = +half.y
        if(motion.y != 0F)
        {
            if(startPos.y < -boxHalfExtents.y) // Check face y = -half.y
            {
                double intersectTime = (-boxHalfExtents.y - startPos.y) / motion.y;
                double intersectX = startPos.x + motion.x * intersectTime;
                double intersectZ = startPos.z + motion.z * intersectTime;
                if(-boxHalfExtents.x <= intersectX && intersectX <= boxHalfExtents.x
                    && -boxHalfExtents.z <= intersectZ && intersectZ <= boxHalfExtents.z)
                {
                    if (outPos != null)
                    {
                        Vec3 globalIntersect = boxCenterTransform.LocalToGlobalPosition(new Vec3(intersectX, -boxHalfExtents.y, intersectZ));
                        outPos.set(globalIntersect.x, globalIntersect.y, globalIntersect.z);
                    }
                    return true;
                }
            }
            else if(startPos.y > boxHalfExtents.y) // Check face y = +half.y
            {
                double intersectTime = (boxHalfExtents.y - startPos.y) / motion.y;
                double intersectX = startPos.x + motion.x * intersectTime;
                double intersectZ = startPos.z + motion.z * intersectTime;
                if(-boxHalfExtents.x <= intersectX && intersectX <= boxHalfExtents.x
                    && -boxHalfExtents.z <= intersectZ && intersectZ <= boxHalfExtents.z)
                {
                    if (outPos != null)
                    {
                        Vec3 globalIntersect = boxCenterTransform.LocalToGlobalPosition(new Vec3(intersectX, boxHalfExtents.y, intersectZ));
                        outPos.set(globalIntersect.x, globalIntersect.y, globalIntersect.z);
                    }
                    return true;
                }
            }
        }

        return false;
    }

}
