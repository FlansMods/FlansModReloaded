package com.flansmod.common.network;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.gunshots.GunContext;
import com.flansmod.common.gunshots.ShooterContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import java.util.UUID;

public abstract class FlansModMessage
{
	public abstract void Encode(FriendlyByteBuf buf);
	public abstract void Decode(FriendlyByteBuf buf);
}
