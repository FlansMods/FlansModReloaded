package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.abilities.AbilityDefinition;
import com.flansmod.common.types.abilities.elements.AbilityProviderDefinition;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.guns.elements.AttachmentSettingsDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.util.Maths;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

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
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nullable Level level,
                                @Nonnull List<Component> tooltips,
                                @Nonnull TooltipFlag flags)
    {
        PartDefinition[] craftedFromParts = GetCraftingInputs(stack);
        for(PartDefinition craftedFrom : craftedFromParts)
        {
            tooltips.add(Component.translatable(
                "tooltip.crafted_from",
                Component.translatable("item." + craftedFrom.GetLocation().getNamespace() + "." + craftedFrom.GetLocation().getPath())
            ));
            for(ModifierDefinition modDef : craftedFrom.modifiers)
            {
                tooltips.add(Component.translatable("tooltip.crafted_from.modifier_format", modDef.GetModifierString()));
            }
        }

        for(var kvp : GetAbilities(stack).entrySet())
        {
            Component abilityString = Component.translatable("tooltip.ability_with_level",
                Component.translatable("ability." + kvp.getKey().Location.getNamespace() + "." + kvp.getKey().Location.getPath()),
                Component.translatable("enchantment.level." + kvp.getValue()));

            Component abilityColour = Component.translatable("ability." + kvp.getKey().Location.getNamespace() + "." + kvp.getKey().Location.getPath() + ".colour");

            tooltips.add(Component.literal("\u00A7" + abilityColour.getString() + abilityString.getString()));
        }



        for (ItemStack attachmentStack : GetAttachmentStacks(stack))
        {
            tooltips.add(Component.translatable("tooltip.format.attached", attachmentStack.getHoverName()));
        }
    }

    public static final UUID InvalidGunUUID = new UUID(0L, 0L);
    @Nonnull
    public static UUID Server_GetOrSetNewGunID(@Nonnull ItemStack stack)
    {
        if(stack.getTag() == null || !stack.getTag().contains("id"))
        {
            UUID newUUID = UUID.randomUUID();
            FlansMod.LOGGER.info("GunUUID: Created UUID " + newUUID + " for " + stack);
            stack.getOrCreateTag().putUUID("id", newUUID);
            return newUUID;
        }
        return stack.getTag().getUUID("id");
    }
    @Nonnull
    public static UUID GetGunID(@Nonnull ItemStack stack)
    {
        if(stack.getTag() == null || !stack.getTag().contains("id"))
            return InvalidGunUUID;
        return stack.getTag().getUUID("id");
    }

    public boolean HasAttachmentSlot(@Nonnull EAttachmentType type, int slot)
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

    public boolean CanAcceptAttachment(@Nonnull ItemStack attachmentStack, @Nonnull EAttachmentType slotType, int slotIndex)
    {
        if(Def() instanceof GunDefinition gunDef)
        {
            AttachmentSettingsDefinition attachSettings = gunDef.GetAttachmentSettings(slotType);
            if(slotIndex < attachSettings.numAttachmentSlots)
            {
                if(attachSettings.matchNames.length == 0 && attachSettings.matchTags.length == 0)
                    return true;

                if(attachSettings.matchNames.length > 0)
                    for(String matchName : attachSettings.matchNames)
                        if(attachmentStack.getItem().builtInRegistryHolder().is(new ResourceLocation(matchName)))
                            return true;
                if(attachSettings.matchTags.length > 0)
                    for(String matchTag : attachSettings.matchTags)
                        if(attachmentStack.getItem().builtInRegistryHolder().containsTag(TagKey.create(Registries.ITEM, new ResourceLocation(matchTag))))
                            return true;
            }
        }
        return false;
    }
    @Nonnull
    private String GetSlotKey(@Nonnull EAttachmentType type, int slot) { return type.name() + "_" + slot; }
    @Nonnull
    public ItemStack GetAttachmentInSlot(@Nonnull ItemStack stack, @Nonnull EAttachmentType type, int slot)
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
    @Nonnull
    public List<AttachmentDefinition> GetAttachmentDefinitions(@Nonnull ItemStack stack)
    {
        List<AttachmentDefinition> defs = new ArrayList<>();
        for(ItemStack attachmentStack : GetAttachmentStacks(stack))
        {
            if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
                defs.add(attachmentItem.Def());
        }
        return defs;
    }
    @Nonnull
    public List<ItemStack> GetAttachmentStacks(@Nonnull ItemStack stack)
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

    public void SetAttachmentInSlot(@Nonnull ItemStack stack, @Nonnull EAttachmentType type, int slot, @Nonnull ItemStack attachmentStack)
    {
        CompoundTag tags = stack.getOrCreateTag();
        CompoundTag attachTags = tags.getCompound("attachments");
        CompoundTag saveTags = new CompoundTag();
        attachmentStack.save(saveTags);
        attachTags.put(GetSlotKey(type, slot), saveTags);

        if(!tags.contains("attachments"))
            tags.put("attachments", attachTags);
    }

    @Nonnull
    public ItemStack RemoveAttachmentFromSlot(@Nonnull ItemStack stack, @Nonnull EAttachmentType type, int slot)
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

    public String GetPaintjobName(@Nonnull ItemStack stack)
    {
        if(stack.hasTag() && stack.getOrCreateTag().contains("paint"))
        {
            return stack.getOrCreateTag().getString("paint");
        }
        return "default";
    }

    public void SetPaintjobName(@Nonnull ItemStack stack, @Nonnull String paint)
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
    @Nonnull
    public PartDefinition[] GetCraftingInputs(@Nonnull ItemStack stack)
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
    public void SetCraftingInputs(@Nonnull ItemStack stack, @Nonnull List<ItemStack> partStacks)
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
    public void SetCraftingInputs(@Nonnull ItemStack stack, @Nonnull ItemStack[] partStacks)
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
    @Nonnull
    public Map<AbilityDefinition, Integer> GetAbilities(@Nonnull ItemStack stack)
    {
        Map<AbilityDefinition, Integer> abilityMap = new HashMap<>();
        for(PartDefinition part : GetCraftingInputs(stack))
        {
            for(AbilityProviderDefinition abilityProvider : part.abilities)
            {
                AbilityDefinition ability = abilityProvider.GetAbility();
                if(ability.IsValid())
                {
                    int newLevel = abilityMap.getOrDefault(ability, 0) + abilityProvider.level;
                    newLevel = Maths.Min(newLevel, ability.maxLevel);
                    abilityMap.put(ability, newLevel);
                }
            }
        }
        for(AttachmentDefinition attachment : GetAttachmentDefinitions(stack))
        {
            for(AbilityProviderDefinition abilityProvider : attachment.abilities)
            {
                AbilityDefinition ability = abilityProvider.GetAbility();
                if(ability.IsValid())
                {
                    int newLevel = abilityMap.getOrDefault(ability, 0) + abilityProvider.level;
                    newLevel = Maths.Min(newLevel, ability.maxLevel);
                    abilityMap.put(ability, newLevel);
                }
            }
        }
        return abilityMap;
    }
}
