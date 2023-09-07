package com.flansmod.client;

import com.flansmod.util.Maths;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RecoilManager
{
	private float RecoilPendingYaw = 0.0f;
	private float RecoilPendingPitch = 0.0f;

	public RecoilManager()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void AddRecoil(float magYaw, float magPitch)
	{
		RecoilPendingYaw += magYaw;
		RecoilPendingPitch += magPitch;
	}

	public void OnPlayerLookInput(Vec2 delta)
	{

	}
	@SubscribeEvent
	public void OnClientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase == TickEvent.Phase.END)
		{
			float dYaw = RecoilPendingYaw * 0.5f;
			float dPitch = RecoilPendingPitch * 0.5f;
			Player player = Minecraft.getInstance().player;
			if (player != null)
			{
				player.setXRot(player.getXRot() - dPitch);
				player.setYRot(player.getYRot() - dYaw);
			}
			RecoilPendingYaw -= dYaw;
			RecoilPendingPitch -= dPitch;
		}
	}
}
