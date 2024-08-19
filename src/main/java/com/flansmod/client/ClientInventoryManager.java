package com.flansmod.client;

import com.flansmod.common.crafting.temporary.TemporaryWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ClientInventoryManager
{
	private TemporaryWorkbench OpenWorkbench = null;

	@Nonnull
	public TemporaryWorkbench GetTemporaryInventory(@Nonnull ResourceLocation workbenchDef)
	{
		if(OpenWorkbench != null)
		{
			if(OpenWorkbench.Def.Location.equals(workbenchDef))
			{
				return OpenWorkbench;
			}
			else
			{
				OpenWorkbench.Close(Minecraft.getInstance().player);
			}
		}

		OpenWorkbench = new TemporaryWorkbench(workbenchDef);
		return OpenWorkbench;
	}

	public void CloseTemporaryInventory()
	{
		if(OpenWorkbench != null)
		{
			OpenWorkbench.Close(Minecraft.getInstance().player);
			OpenWorkbench = null;
		}
	}
}
