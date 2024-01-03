package com.flansmod.common.crafting;

import com.flansmod.common.item.AttachmentItem;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

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

	@Nonnull
	@Override
	public ItemStack getItem()
	{
		if(ParentSlot.isActive() && ParentSlot.getItem().getItem() instanceof FlanItem)
		{
			return FlanItem.GetAttachmentInSlot(ParentSlot.getItem(), AttachmentType, AttachmentIndex);
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
	@Nonnull
	@Override
	public ItemStack remove(int count)
	{
		if(ParentSlot.isActive() && ParentSlot.getItem().getItem() instanceof FlanItem)
		{
			return FlanItem.RemoveAttachmentFromSlot(ParentSlot.getItem(), AttachmentType, AttachmentIndex);
		}
		return ItemStack.EMPTY;
	}
	@Override
	public void set(@Nonnull ItemStack stack)
	{
		if(ParentSlot.isActive() && ParentSlot.getItem().getItem() instanceof FlanItem)
		{
			FlanItem.SetAttachmentInSlot(ParentSlot.getItem(), AttachmentType, AttachmentIndex, stack);
		}
		setChanged();
	}
	@Override
	public boolean mayPlace(ItemStack stack)
	{
		if(!isActive())
			return false;
		if(stack.isEmpty())
			return true;
		if(stack.getItem() instanceof AttachmentItem attachmentItem)
		{
			if(attachmentItem.Def().attachmentType == AttachmentType)
				if (ParentSlot.isActive())
					if (ParentSlot.getItem().getItem() instanceof FlanItem flanItem)
						return flanItem.CanAcceptAttachment(stack, AttachmentType, AttachmentIndex);
		}
		return false;
	}
}
