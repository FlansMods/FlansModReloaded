package com.flansmod.client.render;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
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



        //PartialRenderUtility renderer = PartialRenderUtility.of(stack, transformType, ms, buffer, overlay);

        ms.pushPose();
        ms.translate(0.5F, 0.5F, 0.5F);
        render(stack, mainModel, transformType, ms, buffer, light, overlay);




        ms.popPose();
    }

    private final RandomSource random = RandomSource.create();
    protected void RenderPart(ItemStack stack,
                              PoseStack ms,
                              MultiBufferSource buffer,
                              FlanItemModel rootModel,
                              String partName,
                              int light,
                              int overlay)
    {
        ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
        ModelData data = ModelData.EMPTY;
        BakedModel model = rootModel.getModelPart(partName);

        ResourceLocation texture = rootModel.GetTextureForPart(partName);
        Minecraft.getInstance().textureManager.getTexture(texture).setFilter(false, false);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.disableTexture();


        for (RenderType renderType : model.getRenderTypes(stack, false))
        {
            List<BakedQuad> quads = model.getQuads(null, null, random, data, renderType);
            VertexConsumer vc = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());
            random.setSeed(42L);
           // ir.renderQuadList(ms, vc, quads, stack, light, overlay);
        }

    }

    protected abstract void render(ItemStack stack, M model, ItemTransforms.TransformType transformType,
                                   PoseStack ms, MultiBufferSource buffer, int light, int overlay);

    public abstract M createModel(BakedModel originalModel, String modID, String modelName);
}
