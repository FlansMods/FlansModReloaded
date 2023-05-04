package com.flansmod.client.render;

import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class FlanItemModelRenderer<M extends FlanItemModel> extends BlockEntityWithoutLevelRenderer
{
    public FlanItemModelRenderer()
    {
        super(null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay)
    {
        M mainModel = (M) Minecraft.getInstance()
                .getItemRenderer()
                .getModel(stack, null, null, 0);
        PartialRenderUtility renderer = PartialRenderUtility.of(stack, transformType, ms, buffer, overlay);

        ms.pushPose();
        ms.translate(0.5F, 0.5F, 0.5F);
        render(stack, mainModel, renderer, transformType, ms, buffer, light, overlay);
        ms.popPose();
    }

    protected abstract void render(ItemStack stack, M model, PartialRenderUtility renderer, ItemTransforms.TransformType transformType,
                                   PoseStack ms, MultiBufferSource buffer, int light, int overlay);

    public abstract M createModel(BakedModel originalModel, String modID, String modelName);
}
