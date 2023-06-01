package com.flansmod.common.crafting;

import com.flansmod.common.item.AttachmentItem;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AttachmentSlot extends RestrictedSlot
{
	private final RestrictedSlot ParentSlot;
	private final EAttachmentType AttachmentType;
	private final int AttachmentIndex;

	public AttachmentSlot(RestrictedSlot parentSlot, EAttachmentType type, int attachmentIndex, Container container, int x, int y)
	{
		super(container, 0, x, y);

		ParentSlot = parentSlot;
		AttachmentType = type;
		AttachmentIndex = attachmentIndex;
	}

	@Override
	public boolean isActive()
	{
		return super.isActive()
			&& ParentSlot.isActive()
			&& ParentSlot.getItem().getItem() instanceof FlanItem flanItem
			&& flanItem.HasAttachmentSlot(AttachmentType, AttachmentIndex);
	}

	@Override
	public ItemStack getItem()
	{
		if(ParentSlot.isActive() && ParentSlot.getItem().getItem() instanceof FlanItem flanItem)
		{
			return flanItem.GetAttachmentInSlot(ParentSlot.getItem(), AttachmentType, AttachmentIndex);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
	@Override
	public void setChanged() {
		ParentSlot.setChanged();
	}
	@Override
	public ItemStack remove(int count)
	{
		if(ParentSlot.isActive() && ParentSlot.getItem().getItem() instanceof FlanItem flanItem)
		{
			return flanItem.RemoveAttachmentFromSlot(ParentSlot.getItem(), AttachmentType, AttachmentIndex);
		}
		return ItemStack.EMPTY;
	}
	@Override
	public void set(ItemStack stack)
	{
		if(ParentSlot.isActive() && ParentSlot.getItem().getItem() instanceof FlanItem flanItem)
		{
			flanItem.SetAttachmentInSlot(ParentSlot.getItem(), AttachmentType, AttachmentIndex, stack);
		}
	}
	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return isActive() && stack.getItem() instanceof AttachmentItem;
	}
	@Override
	public void initialize(ItemStack stack)
	{
		set(stack);
		setChanged();
	}
}
