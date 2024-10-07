package com.flansmod.client;

import com.flansmod.physics.common.util.Maths;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

public class RecoilManager
{
	private float RecoilPendingYaw = 0.0f;
	private float RecoilPendingPitch = 0.0f;

	private float RecoilStacksYaw = 0.0f;
	private float RecoilStacksPitch = 0.0f;
	private float RecoilStacksYawLast = 0.0f;
	private float RecoilStacksPitchLast = 0.0f;

	public float GetRecoilYaw(float dt) { return Maths.LerpF(RecoilStacksYawLast, RecoilStacksYaw, dt); }
	public float GetRecoilPitch(float dt) { return Maths.LerpF(RecoilStacksPitchLast, RecoilStacksPitch, dt); }

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
	public void OnCameraEvent(@Nonnull ViewportEvent.ComputeCameraAngles event)
	{
		event.setYaw(event.getYaw() + GetRecoilYaw(Minecraft.getInstance().getPartialTick()));
		event.setPitch(event.getPitch() - GetRecoilPitch(Minecraft.getInstance().getPartialTick()));
	}

	@SubscribeEvent
	public void OnRenderTick(@Nonnull TickEvent.RenderTickEvent event)
	{

	}

	@SubscribeEvent
	public void OnClientTick(@Nonnull TickEvent.ClientTickEvent event)
	{
		if(event.phase == TickEvent.Phase.END)
		{
			RecoilStacksYawLast = RecoilStacksYaw;
			RecoilStacksPitchLast = RecoilStacksPitch;

			float dYaw = RecoilPendingYaw * 0.5f;
			float dPitch = RecoilPendingPitch * 0.5f;
			//Player player = Minecraft.getInstance().player;
			//if (player != null)
			//{
			//	player.setXRot(player.getXRot() - dPitch);
			//	player.setYRot(player.getYRot() - dYaw);
			//}
			RecoilStacksYaw += dYaw;
			RecoilStacksPitch += dPitch;
			RecoilStacksYaw *= 0.75f;
			RecoilStacksPitch *= 0.75f;

			RecoilPendingYaw -= dYaw;
			RecoilPendingPitch -= dPitch;
		}
	}
}
