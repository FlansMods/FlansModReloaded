package com.flansmod.common.crafting.temporary;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.AbstractWorkbench;
import com.flansmod.common.crafting.menus.WorkbenchMenu;
import com.flansmod.common.types.crafting.EWorkbenchInventoryType;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TemporaryWorkbench
{
	public final AbstractWorkbench Workbench;
	public final List<WorkbenchMenu> OpenMenus = new ArrayList<>();
	public final WorkbenchDefinition Def;

	public TemporaryWorkbench(@Nonnull ResourceLocation workbenchDefLoc, @Nonnull Function<Player, Boolean> stillValidFunc)
	{
		Def = FlansMod.WORKBENCHES.Get(workbenchDefLoc);
		Workbench = new AbstractWorkbench(Def, stillValidFunc);
	}

	public TemporaryWorkbench(@Nonnull ResourceLocation workbenchDefLoc)
	{
		this(workbenchDefLoc, (player) -> true);
	}

	public void Close(@Nullable Player player)
	{
		if(player != null && !player.level().isClientSide)
		{
			for (Container container : Workbench.GetContainers(EWorkbenchInventoryType.AllTypes))
			{
				for (int i = 0; i < container.getContainerSize(); i++)
				{
					ItemStack stack = container.getItem(i);
					if (!stack.isEmpty())
					{
						player.getInventory().placeItemBackInInventory(stack);
						container.setItem(i, ItemStack.EMPTY);
					}
				}
			}
		}
		OpenMenus.clear();
	}
}
