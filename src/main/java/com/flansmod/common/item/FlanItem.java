package com.flansmod.common.item;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.abilities.CraftingTraitDefinition;
import com.flansmod.common.types.abilities.elements.CraftingTraitProviderDefinition;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.guns.elements.AttachmentSettingsDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.physics.common.util.Maths;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class FlanItem extends Item implements IForgeItem
{
    private static final List<FlanItem> ALL_ITEMS = new ArrayList<>(256);

    public static Iterable<FlanItem> GetAllItems()
    {
        return ALL_ITEMS;
    }
    public abstract JsonDefinition Def();
    @Nonnull
    public final ResourceLocation DefinitionLocation;

    public FlanItem(@Nonnull ResourceLocation definitionLocation, @Nonnull Properties props)
    {
        super(props);
        DefinitionLocation = definitionLocation;
        ALL_ITEMS.add(this);
    }

    @Nonnull
    public PaintableDefinition GetPaintDef()
    {
        if(Def() instanceof GunDefinition gunDef)
            return gunDef.paints;

        return PaintableDefinition.Invalid;
    }


    public boolean ShouldRenderAsIcon(@Nonnull ItemDisplayContext transformType)
    {
        return transformType == ItemDisplayContext.GUI;
    }

    public boolean CanBeCraftedFromParts() { return true; }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nullable Level level,
                                @Nonnull List<Component> tooltips,
                                @Nonnull TooltipFlag flags)
    {
        boolean expanded = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_RSHIFT)
            || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT);

        if(CanBeCraftedFromParts())
        {
            if (!expanded)
            {
                tooltips.add(Component.translatable("tooltip.flansmod.hold_shift_for_more"));
            } else
            {
                PartDefinition[] craftedFromParts = GetCraftingInputs(stack);
                if (craftedFromParts.length == 0)
                {
                    tooltips.add(Component.translatable("tooltip.crafted_from_nothing"));
                } else
                {
                    for (PartDefinition craftedFrom : craftedFromParts)
                    {
                        tooltips.add(Component.translatable(
                            "tooltip.crafted_from",
                            Component.translatable("item." + craftedFrom.GetLocation().getNamespace() + "." + craftedFrom.GetLocation().getPath())
                        ));
                        for (ModifierDefinition modDef : craftedFrom.modifiers)
                        {
                            for (Component modString : modDef.GetModifierStrings())
                                tooltips.add(Component.translatable("tooltip.crafted_from.modifier_format", modString));
                        }
                    }
                }
            }
        }

        for(var kvp : GetTraits(stack).entrySet())
        {
            tooltips.add(CreateTraitComponent(kvp.getKey(), kvp.getValue(), expanded));
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
        if(stack.isEmpty())
            return InvalidGunUUID;
        if(stack.getTag() == null || !stack.getTag().contains("id"))
            return InvalidGunUUID;
        if(stack.getTag().get("id").getType() != IntArrayTag.TYPE){ //Fix crashing when installed alongside tetra
            return InvalidGunUUID;
        }
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
    private static String GetSlotKey(@Nonnull EAttachmentType type, int slot) { return type.name() + "_" + slot; }
    @Nonnull
    public static ItemStack GetAttachmentInSlot(@Nonnull ItemStack stack, @Nonnull EAttachmentType type, int slot)
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
    public static List<AttachmentDefinition> GetAttachmentDefinitions(@Nonnull ItemStack stack)
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
    public static List<ItemStack> GetAttachmentStacks(@Nonnull ItemStack stack)
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

    public static void SetAttachmentInSlot(@Nonnull ItemStack stack, @Nonnull EAttachmentType type, int slot, @Nonnull ItemStack attachmentStack)
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
    public static ItemStack RemoveAttachmentFromSlot(@Nonnull ItemStack stack, @Nonnull EAttachmentType type, int slot)
    {
        ItemStack ret = GetAttachmentInSlot(stack, type, slot);
        SetAttachmentInSlot(stack, type, slot, ItemStack.EMPTY);
        return ret;
    }

    @Nonnull
    public static String GetPaintjobName(@Nonnull ItemStack stack)
    {
        if(stack.hasTag() && stack.getOrCreateTag().contains("paint"))
        {
            return stack.getOrCreateTag().getString("paint");
        }
        return "default";
    }

    public static void SetPaintjobName(@Nonnull ItemStack stack, @Nonnull String paint)
    {
        stack.getOrCreateTag().putString("paint", paint);
    }

    // Only remember parts that we used, not arbitrary item stacks with NBT
    @Nonnull
    public static PartDefinition[] GetCraftingInputs(@Nonnull ItemStack stack)
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
    public static void SetCraftingInputs(@Nonnull ItemStack stack, @Nonnull List<ItemStack> partStacks)
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
    public static void SetCraftingInputs(@Nonnull ItemStack stack, @Nonnull ItemStack[] partStacks)
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
    protected void CollectAbilities(@Nonnull ItemStack stack, @Nonnull Map<CraftingTraitDefinition, Integer> abilityMap)
    {

    }
    @Nonnull
    public static Map<CraftingTraitDefinition, Integer> GetTraits(@Nonnull ItemStack stack)
    {
        Map<CraftingTraitDefinition, Integer> abilityMap = new HashMap<>();
        for(PartDefinition part : GetCraftingInputs(stack))
        {
            for(CraftingTraitProviderDefinition abilityProvider : part.traits)
            {
                CraftingTraitDefinition ability = abilityProvider.GetAbility();
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
            for(CraftingTraitProviderDefinition abilityProvider : attachment.abilities)
            {
                CraftingTraitDefinition ability = abilityProvider.GetAbility();
                if(ability.IsValid())
                {
                    int newLevel = abilityMap.getOrDefault(ability, 0) + abilityProvider.level;
                    newLevel = Maths.Min(newLevel, ability.maxLevel);
                    abilityMap.put(ability, newLevel);
                }
            }
        }
        if(stack.getItem() instanceof FlanItem flanItem)
            flanItem.CollectAbilities(stack, abilityMap);
        return abilityMap;
    }
    @Nonnull
    public static String GetModeValue(@Nonnull ItemStack stack, @Nonnull String modeKey, @Nonnull String defaultValue)
    {
        if(stack.getOrCreateTag().contains("modes"))
        {
            return stack.getOrCreateTag().getCompound("modes").getString(modeKey);
        }
        return defaultValue;
    }
    public static void SetModeValue(@Nonnull ItemStack stack, @Nonnull String modeKey, @Nonnull String modeValue)
    {
        if(!stack.getOrCreateTag().contains("modes"))
            stack.getOrCreateTag().put("modes", new CompoundTag());

        stack.getOrCreateTag().getCompound("modes").putString(modeKey, modeValue);
    }

    @Nonnull
    public static Component ListOf(@Nonnull String prefix, @Nonnull String[] strings)
    {
        Component commaList = Component.translatable(prefix + strings[0]);
        for(int i = 1; i < strings.length; i++)
        {
            commaList = Component.translatable(
                i == strings.length - 1 ? "comma_list.append_last" : "comma_list.append_one",
                commaList,
                Component.translatable(prefix + strings[i]));
        }
        return commaList;
    }
    @Nonnull
    public static Component ListOf(@Nonnull Component[] strings)
    {
        Component commaList = strings[0];
        for(int i = 1; i < strings.length; i++)
        {
            commaList = Component.translatable(
                i == strings.length - 1 ? "comma_list.append_last" : "comma_list.append_one",
                commaList,
                strings[i]);
        }
        return commaList;
    }

    @Nonnull
    public static Component CreateTraitComponent(@Nonnull CraftingTraitDefinition trait, int level, boolean expanded)
    {
        Component abilityString = (trait.maxLevel == 1)
            ? (Component.translatable("tooltip.ability_without_level",
            Component.translatable("ability." + trait.Location.getNamespace() + "." + trait.Location.getPath())))
            : (Component.translatable("tooltip.ability_with_level",
            Component.translatable("ability." + trait.Location.getNamespace() + "." + trait.Location.getPath()),
            Component.translatable(Maths.ToRomanNumerals(level))));

        Component abilityColour = Component.translatable("ability." + trait.Location.getNamespace() + "." + trait.Location.getPath() + ".colour");


        if(!expanded)
        {
            return Component.literal("\u00A7" + abilityColour.getString() + abilityString.getString());
        }
        else
        {
            Component abilityTooltipString = Component.translatable(
                "ability."  + trait.Location.getNamespace() +"." + trait.Location.getPath() + ".tooltip." + level);
           return Component.literal("\u00A7" + abilityColour.getString() + abilityString.getString() + " - " + abilityTooltipString.getString());
        }
    }
}
