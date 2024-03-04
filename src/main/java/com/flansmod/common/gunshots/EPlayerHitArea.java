package com.flansmod.common.gunshots;

public enum EPlayerHitArea
{
    BODY,
    HEAD,
    LEFTLEG,
    RIGHTLEG,
    LEFTARM,
    RIGHTARM,
    LEFTITEM,
    RIGHTITEM;

    public static final int NUM_AREAS = EPlayerHitArea.values().length;

    public float DamageMultiplier()
    {
        return switch (this)
        {
            case HEAD -> 1.6f;
            case LEFTARM, RIGHTARM -> 0.6f;
            default -> 1.0f;
        };
    }
}
