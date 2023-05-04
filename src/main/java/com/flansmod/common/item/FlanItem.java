package com.flansmod.common.item;

import com.flansmod.common.types.JsonDefinition;
import net.minecraft.world.item.Item;

public abstract class FlanItem extends Item
{
    public abstract JsonDefinition Def();

    public FlanItem(Properties props)
    {
        super(props);
    }
}
