package com.flansmod.physics.common.util;

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
    public static long max(long a, long b) { return a > b ? a : b; }
    public static long min(long a, long b) { return a < b ? a : b; }
    public static long ceilLerp(long a, long b, double t) { return ceilL(a + (b-a)*t); }
    public static long floorLerp(long a, long b, double t) { return floorL(a + (b-a)*t); }
    public static long roundLerp(long a, long b, double t) { return roundL(a + (b-a)*t); }

    // Integer maths
    public static int modulo(int a, int b)
    {
        int c = a % b;
        if(c < 0)
            c += b;
        return c;
    }
    public static int abs(int a) { return a < 0 ? -a : a; }
    public static int clamp(int i, int min, int max) { return i > max ? max : (i < min ? min : i);  }
    public static int max(int a, int b) { return a > b ? a : b; }
    public static int min(int a, int b) { return a < b ? a : b; }
    public static int ceilLerp(int a, int b, float t) { return ceil(a + (b-a)*t); }
    public static int floorLerp(int a, int b, float t) { return floor(a + (b-a)*t); }
    public static int roundLerp(int a, int b, float t) { return round(a + (b-a)*t); }

    // Double maths
    public static final double Root2 = sqrt(2);
    public static final double Epsilon = 0.0000001d;
    public static final double EpsilonSq = Epsilon * Epsilon;
    public static double abs(double a) { return a < 0 ? -a : a; }
    public static double clamp(double i, double min, double max) { return i > max ? max : (i < min ? min : i);  }
    public static int ceil(double d) { return (int)Math.ceil(d); }
    public static int floor(double d) { return (int)Math.floor(d); }
    public static int round(double d) { return (int)Math.round(d); }
    public static long ceilL(double d) { return (long)Math.ceil(d); }
    public static long floorL(double d) { return (long)Math.floor(d); }
    public static long roundL(double d) { return (long)Math.round(d); }
    public static double max(double a, double b) { return a > b ? a : b; }
    public static double min(double a, double b) { return a < b ? a : b; }

    public static boolean absApprox(double a, double b) { return approx(a, b) || approx(a, -b); }
    public static boolean absApprox(double a, double b, double epsilon) { return approx(a, b, epsilon) || approx(a, -b, epsilon); }
    public static boolean approx(double a, double b) { return abs(a-b) < Epsilon; }
    public static boolean approx(double a, double b, double epsilon) { return abs(a-b) < epsilon; }
    public static double lerp(double a, double b, double t) { return a + (b-a)*t; }
    public static double sign(double d) { return d > 0.0d ? 1.0d : (d < 0.0d ? -1.0d : 0.0d); }
    public static double exp(double d) { return Math.exp(d); }
    public static double pow(double a, double b) { return Math.pow(a, b); }

    // Float maths
    public static final float Root2F = sqrtF(2f);
    public static final float EpsilonF = 0.000001f;
    public static float clamp(float i, float min, float max)
    {
        return i > max ? max : (i < min ? min : i);
    }
    public static float abs(float a) { return a < 0 ? -a : a; }
    public static float max(float a, float b) { return a > b ? a : b; }
    public static float max(float a, float b, float c) { return max(max(a, b), c); }
    public static float min(float a, float b) { return a < b ? a : b; }
    public static int ceil(float d) { return (int)Math.ceil(d); }
    public static int floor(float d) { return (int)Math.floor(d); }
    public static int round(float d) { return (int)Math.round(d); }
    public static boolean approx(float a, float b) { return abs(a-b) < EpsilonF; }
    public static boolean approx(float a, float b, float epsilon) { return abs(a-b) < epsilon; }
    public static float lerpF(float a, float b, float t) { return a + (b-a)*Maths.clamp(t, 0f, 1f); }
    public static float sign(float f) { return f > 0.0f ? 1.0f : (f < 0.0f ? -1.0f : 0.0f); }
    public static float expF(float f) { return (float)Math.exp(f); }
    public static float powF(float a, float b) { return (float)Math.pow(a, b); }

    public static final double Pi = 3.1415926535897932384626433832795028841971d;
    public static final double Tau = 2.0d * Pi;
    public static final double DegToRad = Tau / 360.0d;
    public static final double RadToDeg = 360.0d / Tau;
    
    public static final float PiF = (float)Pi;
    public static final float TauF = (float)Tau;
    public static final float DegToRadF = (float)DegToRad;
    public static final float RadToDegF = (float)RadToDeg;

    public static double sin(double f) { return Math.sin(f); }
    public static double cos(double f) { return Math.cos(f); }
    public static double sqrt(double f) { return Math.sqrt(f); }
    public static double atan(double f) { return Math.atan(f); }
    public static double atan2(double a, double b) { return Math.atan2(a, b); }

    public static float sinF(float f) { return (float)Math.sin(f); }
    public static float cosF(float f) { return (float)Math.cos(f); }
    public static float sqrtF(float f) { return (float)Math.sqrt(f); }
    public static float atanF(float f) { return (float)Math.atan(f); }
    public static float atan2F(float a, float b) { return (float)Math.atan2(a, b); }

    public static Vector3f lerp(Vector3f a, Vector3f b, float t) { return new Vector3f(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t); }
    public static Vector3f sub(Vector3f a, Vector3f b) { return new Vector3f(a.x - b.x, a.y - b.y, a.z - b.z); }
    public static Vector3f add(Vector3f a, Vector3f b) { return new Vector3f(a.x + b.x, a.y + b.y, a.z + b.z); }
    public static Vector3f cross(Vector3f a, Vector3f b) { return a.cross(b, new Vector3f()); }
    public static Vector3f identityPosF()
    {
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }

    public static Vector3d lerp(Vector3d a, Vector3d b, float t) { return new Vector3d(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t); }
    public static Vector3d sub(Vector3d a, Vector3d b) { return new Vector3d(a.x - b.x, a.y - b.y, a.z - b.z); }
    public static Vector3d add(Vector3d a, Vector3d b) { return new Vector3d(a.x + b.x, a.y + b.y, a.z + b.z); }
    public static Vector3d cross(Vector3d a, Vector3d b) { return a.cross(b, new Vector3d()); }
    public static Vector3d identityPosD()
    {
        return new Vector3d(0.0d, 0.0d, 0.0d);
    }

    public static Vec3 lerp(Vec3 a, Vec3 b, float t) { return new Vec3(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t); }
    public static Vec3 sub(Vec3 a, Vec3 b) { return new Vec3(a.x - b.x, a.y - b.y, a.z - b.z); }
    public static Vec3 add(Vec3 a, Vec3 b) { return new Vec3(a.x + b.x, a.y + b.y, a.z + b.z); }
    public static Vec3 cross(Vec3 a, Vec3 b) { return a.cross(b); }
    public static double lengthSqr(double x, double y, double z) { return x*x+y*y+z*z; }
    public static double lengthXYZ(Vec3 v) { return v.length(); }
    public static double lengthXZ(Vec3 v) { return sqrt(v.x * v.x + v.z * v.z); }
    public static boolean approx(Vec3 a, Vec3 b) { return (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y)+(a.z-b.z)*(a.z-b.z) < EpsilonSq; }
    public static boolean approx(Vec3 a, Vec3 b, double epsilon) { return (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y)+(a.z-b.z)*(a.z-b.z) < epsilon * epsilon; }
    public static Vec3 clamp(Vec3 v, double min, double max) { return new Vec3(clamp(v.x, min, max), clamp(v.y, min, max), clamp(v.z, min, max)); }


    public static Quaternionf slerp(Quaternionf a, Quaternionf b, float t) { return a.slerp(b, t, new Quaternionf()); }

    public static float clampDegrees(float f)
    {
        f %= 360.0f;
        if(f >= 180f) return f - 360f;
        if(f < -180f) return f + 360f;
        return f;
    }
    public static float lerpDegrees(float a, float b, float t)
    {
        float delta = clampDegrees(b - a);
        return a + delta * t;
    }

    public static double calculateParameter(Vec3 v0, Vec3 v1, Vec3 vT)
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

        //FlansPhysicsMod.LOGGER.error(vT + " is not between " + v0 + " and " + v1);
        return Double.NaN;
    }

    public static int blockPosToPinpointData(BlockPos pos, int extraData)
    {
       return (extraData & 0xff) << 24
            | (pos.getX() & 0xff) << 16
            | (pos.getY() & 0xff) << 8
            | (pos.getZ() & 0xff);
    }

    public static int getClosestDistanceModulo(int a, int b, int mod)
    {
        int delta = modulo(a - b, mod);  // in [0, mod)
        int complement = delta - mod;       // in [-mod, 0)

        return abs(complement) < abs(delta) ? complement : delta;
    }

    public static Vec3 reflect(Vec3 incident, Direction surface)
    {
        switch(surface)
        {
            case UP, DOWN -> { return new Vec3(incident.x, -incident.y, incident.z); }
            case NORTH, SOUTH -> { return new Vec3(incident.x, incident.y, -incident.z); }
            case EAST, WEST -> { return new Vec3(-incident.x, incident.y, incident.z); }
            default -> {return incident;}
        }
    }

    public static BlockPos resolveBlockPos(BlockPos roughGuess, int pinpointData)
    {
        int xMod256 = roughGuess.getX() & 0xff;
        int yMod256 = roughGuess.getY() & 0xff;
        int zMod256 = roughGuess.getZ() & 0xff;

        int xPin = (pinpointData >> 16) & 0xff;
        int yPin = (pinpointData >> 8) & 0xff;
        int zPin = (pinpointData) & 0xff;

        int dx = getClosestDistanceModulo(xMod256, xPin, 256);
        int dy = getClosestDistanceModulo(yMod256, yPin, 256);
        int dz = getClosestDistanceModulo(zMod256, zPin, 256);

        return new BlockPos(roughGuess.getX() + dx, roughGuess.getY() + dy, roughGuess.getZ() + dz);
    }


    public static String toRomanNumerals(int n)
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
        n = generateNumeralsForPow10(n, builder, 100, 'M', 'D', 'C');
        n = generateNumeralsForPow10(n, builder, 10, 'C', 'L', 'X');
        n = generateNumeralsForPow10(n, builder, 1, 'X', 'V', 'I');
        return builder.toString();
    }

    private static int generateNumeralsForPow10(int n, StringBuilder builder, int pow10, char ten, char five, char one)
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

    public static boolean rayBoxIntersect(@Nonnull Vec3 startPos,
                                          @Nonnull Vec3 endPos,
                                          @Nonnull Transform boxCenterTransform,
                                          @Nonnull Vector3f boxHalfExtents,
                                          @Nullable Vector3d outPos)
    {
        // Localise
        startPos = boxCenterTransform.globalToLocalPosition(startPos);
        endPos = boxCenterTransform.globalToLocalPosition(endPos);
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
                        Vec3 globalIntersect = boxCenterTransform.localToGlobalPosition(new Vec3(-boxHalfExtents.x, intersectY, intersectZ));
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
                        Vec3 globalIntersect = boxCenterTransform.localToGlobalPosition(new Vec3(boxHalfExtents.x, intersectY, intersectZ));
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
                        Vec3 globalIntersect = boxCenterTransform.localToGlobalPosition(new Vec3(intersectX, intersectY, -boxHalfExtents.z));
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
                        Vec3 globalIntersect = boxCenterTransform.localToGlobalPosition(new Vec3(intersectX, intersectY, boxHalfExtents.z));
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
                        Vec3 globalIntersect = boxCenterTransform.localToGlobalPosition(new Vec3(intersectX, -boxHalfExtents.y, intersectZ));
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
                        Vec3 globalIntersect = boxCenterTransform.localToGlobalPosition(new Vec3(intersectX, boxHalfExtents.y, intersectZ));
                        outPos.set(globalIntersect.x, globalIntersect.y, globalIntersect.z);
                    }
                    return true;
                }
            }
        }

        return false;
    }


}
