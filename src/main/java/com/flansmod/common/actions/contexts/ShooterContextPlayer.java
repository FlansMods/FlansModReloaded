package com.flansmod.common.actions.contexts;

import com.flansmod.common.item.FlanItem;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ShooterContextPlayer extends ShooterContextLiving
{
	public final Player Player;

	public ShooterContextPlayer(@Nonnull Player player)
	{
		super(player);
		Player = player;
	}

	@Override
	public Container GetAttachedInventory() { return Player.getInventory(); }
	@Override
	public boolean IsCreative()
	{
		return Player.isCreative();
	}
	@Override
	@Nonnull
	public UUID GetGunID(@Nonnull InteractionHand hand)
	{
		ItemStack handStack = hand == InteractionHand.MAIN_HAND ? Player.getInventory().getSelected() : Player.getInventory().getItem(Inventory.SLOT_OFFHAND);
		return FlanItem.GetGunID(handStack);
	}
	@Override
	@Nonnull
	public UUID GetGunIDForSlot(int gunSlotIndex)
	{
		ItemStack stackInSlot = Player.getInventory().getItem(gunSlotIndex);
		return FlanItem.GetGunID(stackInSlot);
	}
	@Override
	@Nonnull
	public GunContext CreateContext(@Nonnull UUID gunID)
	{
		for(int i = 0; i < Player.getInventory().getContainerSize(); i++)
		{
			ItemStack stackInSlot = Player.getInventory().getItem(i);
			if(FlanItem.GetGunID(stackInSlot).equals(gunID))
				return new GunContextPlayer(this, i);
		}
		return GunContext.INVALID;
	}

	@Nonnull
	@Override
	public GunContext[] GetAllGunContexts()
	{
		GunContext[] contexts = new GunContext[2];
		contexts[0] = GunContext.of(this, Player.getInventory().selected);
		contexts[0].UpdateFromItemStack();
		contexts[1] = GunContext.of(this, Inventory.SLOT_OFFHAND);
		contexts[1].UpdateFromItemStack();
		return contexts;
	}

	// ---------------------------------------------------------
	//   Container interface
	// ---------------------------------------------------------
	@Override
	public int getContainerSize() { return Player.getInventory().getContainerSize(); }
	@Override
	public boolean isEmpty() { return Player.getInventory().isEmpty(); }
	@Override
	@Nonnull
	public ItemStack getItem(int slot) { return Player.getInventory().getItem(slot); }
	@Override
	@Nonnull
	public ItemStack removeItem(int slotIndex, int count) { return Player.getInventory().removeItem(slotIndex, count); }
	@Override
	@Nonnull
	public ItemStack removeItemNoUpdate(int slotIndex) { return Player.getInventory().removeItemNoUpdate(slotIndex); }
	@Override
	public void setItem(int slotIndex, @Nonnull ItemStack stack) { Player.getInventory().setItem(slotIndex, stack); }
	@Override
	public void setChanged() { Player.getInventory().setChanged(); }
	@Override
	public boolean stillValid(@Nonnull Player player) { return Player.getInventory().stillValid(player); }
	@Override
	public void clearContent() { Player.getInventory().clearContent(); }

	@Override
	public String toString()
	{
		return "Player:'"+Player.getName().getString()+"'['"+Dimension().location().getPath() + "']";
	}
}
