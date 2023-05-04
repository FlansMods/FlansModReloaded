package com.flansmod.client.render.guns;

import com.flansmod.client.render.FlanItemModel;
import net.minecraft.client.resources.model.BakedModel;

public class GunModel extends FlanItemModel
{
    public GunModel(BakedModel template, String modID, String gunName)
    {
        super(template, modID, gunName);
        addParts("body",
                "scope");
    }
}
