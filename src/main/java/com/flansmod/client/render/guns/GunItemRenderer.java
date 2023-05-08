package com.flansmod.client.render.guns;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.PartialRenderUtility;
import com.flansmod.client.render.guns.GunModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

public class GunItemRenderer extends FlanItemModelRenderer<GunModel>
{
    @Override
    protected void render(ItemStack stack, GunModel model, ItemTransforms.TransformType transformType,
                          PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        //renderer.render(model.getOriginalModel(), light);

        RenderPart(stack, ms, buffer, model, "body", light, overlay);

        float xOffset = -1/16f;
        ms.translate(-xOffset, 0, 0);
        ms.mulPose(Axis.YP.rotationDegrees(0));
        ms.translate(xOffset, 0, 0);

        RenderPart(stack, ms, buffer, model, "scope", light, overlay);

    }

    @Override
    public GunModel createModel(BakedModel originalModel, String modID, String modelName)
    {
        return new GunModel(originalModel, modID, modelName);
    }
}
