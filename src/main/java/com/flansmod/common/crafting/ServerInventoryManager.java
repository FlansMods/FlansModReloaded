package com.flansmod.common.crafting;

import com.flansmod.common.crafting.menus.WorkbenchMenu;
import com.flansmod.common.crafting.temporary.TemporaryWorkbench;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerInventoryManager
{
	private Map<Player, TemporaryWorkbench> OpenWorkbenches = new HashMap<>();

	public boolean OpenTemporaryInventory(@Nonnull Player player, @Nonnull ResourceLocation workbenchDef)
	{
		if(OpenWorkbenches.containsKey(player))
		{
			TemporaryWorkbench existingWorkbench = OpenWorkbenches.get(player);
			if(existingWorkbench.Def.Location.equals(workbenchDef))
			{
				// If we already have the workbench that we are asking for open, just show it
				if(player instanceof ServerPlayer serverPlayer)
					NetworkHooks.openScreen(serverPlayer, existingWorkbench.Workbench, BlockPos.ZERO);
				return true;
			}
			else
			{
				CloseTemporaryInventory(player);
			}
		}

		TemporaryWorkbench temp = new TemporaryWorkbench(workbenchDef);
		OpenWorkbenches.put(player, temp);
		if(player instanceof ServerPlayer serverPlayer)
			NetworkHooks.openScreen(serverPlayer, temp.Workbench, BlockPos.ZERO);
		return true;
	}

	public void CloseTemporaryInventory(@Nonnull Player player)
	{
		if(OpenWorkbenches.containsKey(player))
		{
			TemporaryWorkbench existingWorkbench = OpenWorkbenches.get(player);
			existingWorkbench.Close(player);
			OpenWorkbenches.remove(player);
		}
	}
}
