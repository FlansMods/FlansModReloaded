package com.flansmod.common.item;

import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.AttachmentSettingsDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.elements.PaintjobDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public final ResourceLocation DefinitionLocation;

    public FlanItem(ResourceLocation definitionLocation, Properties props)
    {
        super(props);
        DefinitionLocation = definitionLocation;
        ALL_ITEMS.add(this);
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                @Nullable Level level,
                                List<Component> tooltips,
                                TooltipFlag flags)
    {
        for (ItemStack attachmentStack : GetAttachmentStacks(stack))
        {
            tooltips.add(Component.translatable("tooltip.format.attached", attachmentStack.getHoverName()));
        }
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

    public List<ItemStack> GetAttachmentStacks(ItemStack stack)
    {
        List<ItemStack> attachmentStacks = new ArrayList<>();
        if(stack.hasTag())
        {
            CompoundTag tags = stack.getTag();
            if(tags.contains("attachments"))
            {
                CompoundTag attachTags = tags.getCompound("attachments");
                for(String key : attachTags.getAllKeys())
                {
                    ItemStack attachmentStack = ItemStack.of(attachTags.getCompound(key));
                    if(!attachmentStack.isEmpty())
                        attachmentStacks.add(attachmentStack);
                }
            }
        }
        return attachmentStacks;
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

    @Nonnull
    public PaintableDefinition GetPaintDef()
    {
        if(Def() instanceof GunDefinition gunDef)
            return gunDef.paints;

        return PaintableDefinition.Invalid;
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
