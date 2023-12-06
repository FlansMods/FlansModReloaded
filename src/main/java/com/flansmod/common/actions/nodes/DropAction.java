package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.ShooterContextPlayer;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DropAction extends ActionInstance
{
	public DropAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public void OnTriggerClient(int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{
		if(Group.Context.Gun.GetShooter() instanceof ShooterContextPlayer playerContext)
		{
			playerContext.Player.drop(playerContext.Player.getMainHandItem(), false);
			playerContext.Player.getInventory().setItem(playerContext.Player.getInventory().selected, ItemStack.EMPTY);
		}
	}
}
