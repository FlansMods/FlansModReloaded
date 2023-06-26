package com.flansmod.common.item;

import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.AttachmentSettingsDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class FlanItem extends Item
{
    private static List<FlanItem> ALL_ITEMS = new ArrayList(256);

    public static Iterable<FlanItem> GetAllItems()
    {
        return ALL_ITEMS;
    }

    public abstract JsonDefinition Def();

    public FlanItem(Properties props)
    {
        super(props);
        ALL_ITEMS.add(this);
    }

    public boolean HasAttachmentSlot(EAttachmentType type, int slot)
    {
        AttachmentSettingsDefinition attachSettings = null;
        if(Def() instanceof GunDefinition gunDef)
        {
            attachSettings = gunDef.GetAttachmentSettings(type);
        }
        if(attachSettings != null)
            return attachSettings.numAttachmentSlots > slot;

        return false;
    }

    private String GetSlotKey(EAttachmentType type, int slot) { return type.name() + "_" + slot; }

    public ItemStack GetAttachmentInSlot(ItemStack stack, EAttachmentType type, int slot)
    {
        if(stack.hasTag())
        {
            CompoundTag tags = stack.getTag();
            if(tags.contains("attachments"))
            {
                CompoundTag attachTags = tags.getCompound("attachments");
                if(attachTags.contains(GetSlotKey(type, slot)))
                {
                    return ItemStack.of(attachTags.getCompound(GetSlotKey(type, slot)));
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public void SetAttachmentInSlot(ItemStack stack, EAttachmentType type, int slot, ItemStack attachmentStack)
    {
        CompoundTag tags = stack.getOrCreateTag();
        CompoundTag attachTags = tags.getCompound("attachments");
        CompoundTag saveTags = new CompoundTag();
        attachmentStack.save(saveTags);
        attachTags.put(GetSlotKey(type, slot), saveTags);
    }

    public ItemStack RemoveAttachmentFromSlot(ItemStack stack, EAttachmentType type, int slot)
    {
        ItemStack ret = GetAttachmentInSlot(stack, type, slot);
        SetAttachmentInSlot(stack, type, slot, ItemStack.EMPTY);
        return ret;
    }

    public String GetPaintjobName(ItemStack stack)
    {
        if(stack.hasTag() && stack.getTag().contains("paint"))
        {
            return stack.getTag().getString("paint");
        }
        return "default";
    }

    public void SetPaintjobName(ItemStack stack, String paint)
    {
        stack.getOrCreateTag().putString("paint", paint);
    }
}
