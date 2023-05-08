package com.flansmod.common.item;

import com.flansmod.common.types.JsonDefinition;
import net.minecraft.world.item.Item;

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
}
