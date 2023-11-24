package com.flansmod.client.render;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class FlanClientItemExtensions implements IClientItemExtensions
{
    protected FlanItemModelRenderer renderer;

    protected FlanClientItemExtensions(FlanItemModelRenderer renderer)
    {
        this.renderer = renderer;
    }

    public static FlanClientItemExtensions create(Item item, FlanItemModelRenderer renderer)
    {
        FlanModelRegistration.PreRegisterRenderer(item, renderer);
        return new FlanClientItemExtensions(renderer);
    }

    @Override
    public FlanItemModelRenderer getCustomRenderer()
    {
        return renderer;
    }
}
