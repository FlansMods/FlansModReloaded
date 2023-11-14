package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.guns.elements.AttachmentSettingsDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class FlanItem extends Item
{
    private static final List<FlanItem> ALL_ITEMS = new ArrayList<>(256);

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
    public void appendHoverText(@NotNull ItemStack stack,
                                @Nullable Level level,
                                @NotNull List<Component> tooltips,
                                @NotNull TooltipFlag flags)
    {
        PartDefinition[] craftedFromParts = GetCraftingInputs(stack);
        for(PartDefinition craftedFrom : craftedFromParts)
        {
            tooltips.add(Component.translatable(
                "tooltip.crafted_from",
                Component.translatable(craftedFrom.GetLocationString() + ".name")
            ));
            for(ModifierDefinition modDef : craftedFrom.modifiers)
            {
                tooltips.add(Component.translatable("tooltip.crafted_from.modifier_format", modDef.GetModifierString()));
            }
        }

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
            CompoundTag tags = stack.getOrCreateTag();
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
            CompoundTag tags = stack.getOrCreateTag();
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

        if(!tags.contains("attachments"))
            tags.put("attachments", attachTags);
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
        if(stack.hasTag() && stack.getOrCreateTag().contains("paint"))
        {
            return stack.getOrCreateTag().getString("paint");
        }
        return "default";
    }

    public void SetPaintjobName(ItemStack stack, String paint)
    {
        stack.getOrCreateTag().putString("paint", paint);
        stack.getOrCreateTag().putInt("CustomModelData", 0);
        if (!paint.equals("default") && GetPaintDef().IsValid())
        {
            for (int i = 0; i < GetPaintDef().paintjobs.length; i++)
                if (GetPaintDef().paintjobs[i].textureName.equals(paint))
                    stack.getOrCreateTag().putInt("CustomModelData", i+1);
        }
    }

    // Only remember parts that we used, not arbitrary item stacks with NBT
    public PartDefinition[] GetCraftingInputs(ItemStack stack)
    {
        if(stack.hasTag() && stack.getOrCreateTag().contains("parts"))
        {
            CompoundTag craftingTags = stack.getOrCreateTag().getCompound("parts");
            PartDefinition[] parts = new PartDefinition[craftingTags.getAllKeys().size()];
            int index = 0;
            for(String key : craftingTags.getAllKeys())
            {
                ResourceLocation resLoc = new ResourceLocation(craftingTags.getString(key));
                parts[index] = FlansMod.PARTS.Get(resLoc);
                index++;
            }
            return parts;
        }
        return new PartDefinition[0];
    }
    public void SetCraftingInputs(ItemStack stack, List<ItemStack> partStacks)
    {
        CompoundTag craftingTags = new CompoundTag();
        int index = 0;
        for(ItemStack partStack : partStacks)
        {
            if(partStack.isEmpty())
                continue;
            if(partStack.getItem() instanceof PartItem part)
            {
                craftingTags.putString(Integer.toString(index), part.DefinitionLocation.toString());
            }
            index++;
        }
        stack.getOrCreateTag().put("parts", craftingTags);
    }
    public void SetCraftingInputs(ItemStack stack, ItemStack[] partStacks)
    {
        CompoundTag craftingTags = new CompoundTag();
        int index = 0;
        for(ItemStack partStack : partStacks)
        {
            if(partStack.isEmpty())
                continue;
            if(partStack.getItem() instanceof PartItem part)
            {
                craftingTags.putString(Integer.toString(index), part.DefinitionLocation.toString());
            }
            index++;
        }
        stack.getOrCreateTag().put("parts", craftingTags);
    }
}
