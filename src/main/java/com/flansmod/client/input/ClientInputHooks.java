package com.flansmod.client.input;

import com.flansmod.common.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class ClientInputHooks
{
	public ClientInputHooks()
	{
		MinecraftForge.EVENT_BUS.addListener(this::onClickInput);
	}

	public void onClickInput(InputEvent.InteractionKeyMappingTriggered event)
	{
		Player player = Minecraft.getInstance().player;

		ItemStack stack = player.getItemInHand(event.getHand());
		if(stack.getItem() instanceof GunItem gun)
		{
			gun.ClientHandleMouse(player, stack, event);
		}
	}
}
