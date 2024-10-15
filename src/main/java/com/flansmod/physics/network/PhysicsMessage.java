package com.flansmod.physics.network;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public abstract class PhysicsMessage
{
    public abstract void encode(@Nonnull FriendlyByteBuf buf);
    public abstract void decode(@Nonnull FriendlyByteBuf buf);
}
