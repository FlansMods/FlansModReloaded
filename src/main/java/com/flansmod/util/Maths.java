package com.flansmod.util;

import com.flansmod.common.FlansMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class Maths
{
    public static int Modulo(int a, int b)
    {
        int c = a % b;
        if(c < 0)
            c += b;
        return c;
    }
    public static int Abs(int a) { return a < 0 ? -a : a; }
    public static int Clamp(int i, int min, int max)
    {
        return i > max ? max : (i < min ? min : i);
    }
    public static double Clamp(double i, double min, double max)
    {
        return i > max ? max : (i < min ? min : i);
    }
    public static float Clamp(float i, float min, float max)
    {
        return i > max ? max : (i < min ? min : i);
    }
    public static int Ceil(double d) { return (int)Math.ceil(d); }
    public static int Floor(double d) { return (int)Math.floor(d); }
    
    public static final double Pi = 3.1415926535897932384626433832795028841971d;
    public static final double Tau = 2.0d * Pi;
    public static final double DegToRad = Tau / 360.0d;
    public static final double RadToDeg = 360.0d / Tau;
    
    public static final float PiF = (float)Pi;
    public static final float TauF = (float)Tau;
    public static final float DegToRadF = (float)DegToRad;
    public static final float RadToDegF = (float)RadToDeg;

    public static float SinF(float f) { return (float)Math.sin(f); }
    public static float CosF(float f) { return (float)Math.cos(f); }
    public static float SqrtF(float f) { return (float)Math.sqrt(f); }

    public static float ClampDegrees(float f)
    {
        f %= 360.0f;
        if(f >= 180f) return f - 360f;
        if(f < -180f) return f + 360f;
        return f;
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
