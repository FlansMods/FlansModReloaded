package com.flansmod.client.render;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.flansmod.common.FlansMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
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
import org.joml.Vector3f;

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
        mainModel.getOriginalModel().applyTransform(transformType, ms, false);
        //ms.translate(5F, 2F, 0.5F);
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
        //RenderSystem.disableTexture();
        //RenderSystem.applyModelViewMatrix();
        //RenderSystem.disableDepthTest();
        //RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        //RenderSystem.setShader(GameRenderer::getRendertypeEntityAlphaShader);
        RenderSystem.enableCull();
       // ms.setIdentity();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder tessBuffer = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
        tessBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        //FlansMod.LOGGER.info("Render" + stack.toString());
        for (RenderType renderType : model.getRenderTypes(stack, false))
        {
            List<BakedQuad> quads = model.getQuads(null, null, random, data, renderType);
            //VertexConsumer vc = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());
            VertexConsumer vc = ItemRenderer.getFoilBuffer(buffer, renderType, true, false);
            random.setSeed(43L);
            for(int i = 0; i < quads.size(); i++)
            {
                Vector3f[] extracted = new Vector3f[4];
                for(int j = 0; j < 4; j++)
                {
                    float x = Float.intBitsToFloat(quads.get(i).getVertices()[j * 8 + 0]);
                    float y = Float.intBitsToFloat(quads.get(i).getVertices()[j * 8 + 1]);
                    float z = Float.intBitsToFloat(quads.get(i).getVertices()[j * 8 + 2]);
                    extracted[j] = new Vector3f(x, y, z);
                    //FlansMod.LOGGER.info("Vert at " + x + "," + y + "," + z);
                }
                extracted = extracted;
            }

            ir.renderQuadList(ms, vc, quads, stack, light, overlay);
        }

        tesselator.end();

    }

    protected abstract void render(ItemStack stack, M model, ItemTransforms.TransformType transformType,
                                   PoseStack ms, MultiBufferSource buffer, int light, int overlay);

    public abstract M createModel(BakedModel originalModel, String modID, String modelName);
}
