package com.flansmod.util;

import com.flansmod.common.FlansMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Maths
{
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
    public static final double Epsilon = 0.000000000001d;
    public static double Abs(double a) { return a < 0 ? -a : a; }
    public static double Clamp(double i, double min, double max) { return i > max ? max : (i < min ? min : i);  }
    public static int Ceil(double d) { return (int)Math.ceil(d); }
    public static int Floor(double d) { return (int)Math.floor(d); }
    public static int Round(double d) { return (int)Math.round(d); }
    public static double Max(double a, double b) { return a > b ? a : b; }
    public static double Min(double a, double b) { return a < b ? a : b; }
    public static boolean Approx(double a, double b) { return Abs(a-b) < Epsilon; }
    public static double Lerp(double a, double b, double t) { return a + (b-a)*t; }
    public static double Sign(double d) { return d > 0.0d ? 1.0d : (d < 0.0d ? -1.0d : 0.0d); }


    // Float maths
    public static final float EpsilonF = 0.000001f;
    public static float Clamp(float i, float min, float max)
    {
        return i > max ? max : (i < min ? min : i);
    }
    public static float Abs(float a) { return a < 0 ? -a : a; }
    public static float Max(float a, float b) { return a > b ? a : b; }
    public static float Min(float a, float b) { return a < b ? a : b; }
    public static int Ceil(float d) { return (int)Math.ceil(d); }
    public static int Floor(float d) { return (int)Math.floor(d); }
    public static int Round(float d) { return (int)Math.round(d); }
    public static boolean Approx(float a, float b) { return Abs(a-b) < EpsilonF; }
    public static float Lerp(float a, float b, float t) { return a + (b-a)*t; }
    public static float Sign(float f) { return f > 0.0f ? 1.0f : (f < 0.0f ? -1.0f : 0.0f); }

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


    public static Quaternionf IdentityQuat()
    {
        return new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
    }
    public static Quaternionf QuaternionFromEuler(float x, float y, float z) { return new Quaternionf().rotateXYZ(x* Maths.DegToRadF, y * Maths.DegToRadF, z* Maths.DegToRadF); }
    public static Quaternionf QuaternionFromEuler(Vector3f v) { return new Quaternionf().rotateXYZ(v.x* Maths.DegToRadF, v.y * Maths.DegToRadF, v.z* Maths.DegToRadF); }
    public static Quaternionf Slerp(Quaternionf a, Quaternionf b, float t) { return a.slerp(b, t, new Quaternionf()); }

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


}
