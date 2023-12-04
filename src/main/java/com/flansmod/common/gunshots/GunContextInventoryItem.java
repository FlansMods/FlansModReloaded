package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.GunContext;
import com.flansmod.util.Transform;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class GunContextInventoryItem extends GunContext
{
	@Nullable
	private final Vec3 Position;
	private final Container Inv;
	private final int Slot;
	public GunContextInventoryItem(Container inv, int slot, @Nullable Vec3 position, @Nullable net.minecraft.world.level.Level level)
	{
		super(inv.getItem(slot), level);
		Position = position;
		Inv = inv;
		Slot = slot;
	}
	@Override
	public void OnItemStackChanged(ItemStack stack)
	{
		Inv.setItem(Slot, stack);
	}
	@Override
	public boolean UpdateFromItemStack()
	{
		boolean invalidate = StackUpdateWouldInvalidate(Inv.getItem(Slot));
		if(!invalidate)
			Stack = Inv.getItem(Slot).copy();
		return invalidate;
	}
	@Override
	public DamageSource CreateDamageSource() { return DamageSource.GENERIC; }
	@Override
	@Nonnull
	public ShooterContext GetShooter() { return ShooterContext.INVALID; }
	@Override
	public Transform GetShootOrigin() { return new Transform(Position); }
	@Override
	public Container GetAttachedInventory() { return Inv; }
	@Override
	public boolean CanPerformActions() { return false; }
	@Override
	@Nonnull
	public ActionStack GetActionStack() { return ActionStack.Invalid; }
	@Override
	public boolean CanPerformTwoHandedAction() { return false; }

	@Override
	public int hashCode()
	{
		return Objects.hash(Inv, Slot);
	}
	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof GunContextInventoryItem otherContext)
		{
			return Slot == otherContext.Slot && Inv == otherContext.Inv;
		}
		return false;
	}
	@Override
	public String toString()
	{
		return "Gun:" + GetItemStack().toString() + " in slot " + Slot + " in inventory " + Inv.toString();
	}
	// There are no modifiers to apply right now
	@Override
	public int HashModifierSources() { return 0; }
	@Override
	public void RecalculateModifierCache() {}
}
