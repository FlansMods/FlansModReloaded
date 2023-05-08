package com.flansmod.client.render.guns;

import com.flansmod.client.render.FlanItemModel;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public class GunModel extends FlanItemModel
{
    public GunModel(BakedModel template, String modID, String gunName)
    {
        super(template, modID, gunName);

        addParts("body",
            "scope",
            "stock",
            "grip",
            "barrel",
            "magazine"
            );

        //GunDefinition gunDef = FlansMod.GUNS.get(new ResourceLocation(modID, gunName));
        //if(gunDef != null)
        //{
        //    addParts(gunDef.modelParts);
        //}
    }
}
