package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.gunshots.ShooterContextPlayer;
import com.flansmod.common.types.elements.ActionDefinition;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DropAction extends Action
{
	public DropAction(@NotNull ActionGroup group, @NotNull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public void OnTriggerClient(ActionGroupContext context, int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(ActionGroupContext context, int triggerIndex)
	{
		if(context.Shooter() instanceof ShooterContextPlayer playerContext)
		{
			playerContext.Player.drop(playerContext.Player.getMainHandItem(), false);
			playerContext.Player.getInventory().setItem(playerContext.Player.getInventory().selected, ItemStack.EMPTY);
		}
	}
}
