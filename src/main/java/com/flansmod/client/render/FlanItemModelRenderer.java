package com.flansmod.client.render;

import java.util.function.Consumer;

import com.flansmod.client.render.debug.DebugModelPoser;
import com.flansmod.client.render.models.TurboElement;
import com.flansmod.client.render.models.TurboFace;
import com.flansmod.client.render.models.TurboModel;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.FlansMod;
import com.flansmod.common.item.FlanItem;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public abstract class FlanItemModelRenderer extends BlockEntityWithoutLevelRenderer
{
    private final RandomSource random = RandomSource.create();
    protected TurboRig UnbakedRig;
    protected TurboRig.Baked BakedRig;

    public FlanItemModelRenderer()
    {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack,
                             ItemTransforms.TransformType transformType,
                             PoseStack ms,
                             MultiBufferSource buffers,
                             int light,
                             int overlay)
    {
        RenderSystem.enableDepthTest();

        if(transformType == ItemTransforms.TransformType.GROUND)
        {
            BakedRig.ApplyTransform(transformType, ms, false);
        }


        ms.pushPose();
        RenderItem(null, transformType, stack, ms, buffers, light, overlay);
        ms.popPose();
    }

    public void RenderFirstPerson(Entity entity,
                                  ItemStack stack,
                                  HumanoidArm arm,
                                  ItemTransforms.TransformType transformType,
                                  PoseStack ms,
                                  MultiBufferSource buffers,
                                  int light,
                                  int overlay,
                                  float equipProgress)
    {
        if(BakedRig == null)
        {
            FlansMod.LOGGER.error("Could not render Flan's Item " + stack + " because rig was null");
            return;
        }

        ms.pushPose();
        BakedRig.ApplyTransform(transformType, ms, false);
        RenderItem(entity, transformType, stack, ms, buffers, light, overlay);
        ms.popPose();
    }

    protected void RenderItem(Entity entity,
                              ItemTransforms.TransformType transformType,
                              ItemStack stack,
                              PoseStack requestedPoseStack,
                              MultiBufferSource buffers,
                              int light,
                              int overlay)
    {

        // Default minecraft doesn't use the ModelView matrix properly?!
        // We are going to load up our transformations into it
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        {
            modelViewStack.mulPoseMatrix(requestedPoseStack.last().pose());

            // Bind texture
            FlanItem flanItem = stack.getItem() instanceof FlanItem ? (FlanItem)stack.getItem() : null;
            String skin = "default";
            if(flanItem != null)
                skin = flanItem.GetPaintjobName(stack);
            ResourceLocation texture = BakedRig.GetTexture(skin);
            if (texture != null)
            {
                Minecraft.getInstance().textureManager.getTexture(texture).setFilter(false, false);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, texture);
            }

            // Render item
            DoRender(entity, stack, BakedRig, transformType, modelViewStack, (partName) -> {
                RenderPartSolid(stack, buffers, partName, light, overlay);
            });
        }
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public void OnUnbakedModelLoaded(TurboRig unbaked)
    {
        UnbakedRig = unbaked;
    }

    public void OnBakeComplete(TurboRig.Baked baked)
    {
        BakedRig = baked;
    }

    protected abstract void DoRender(Entity entity, ItemStack stack, TurboRig.Baked rig, ItemTransforms.TransformType transformType, PoseStack ms, Consumer<String> renderPartFunc);

    protected void RenderFirstPersonArm(PoseStack poseStack)
    {

    }

    private void ApplyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress)
    {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    protected void RenderPartTransparent(ItemStack stack, MultiBufferSource buffer, String partName, int light, int overlay)
    {
        RenderPart(stack, buffer, partName, light, overlay, true);
    }

    protected void RenderPartSolid(ItemStack stack, MultiBufferSource buffer, String partName, int light, int overlay)
    {
        RenderPart(stack, buffer, partName, light, overlay, false);
    }

    protected void RenderPart(ItemStack stack,
                              MultiBufferSource buffer,
                              String partName,
                              int light,
                              int overlay,
                              boolean transparent)
    {
        ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
        ModelData data = ModelData.EMPTY;

        if(partName.equals("rightHand") || partName.equals("leftHand"))
        {
            ResourceLocation skinLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(Minecraft.getInstance().getUser().getGameProfile());
            RenderSystem.setShaderTexture(0, skinLocation);
        }
        else
        {
            BakedModel model = BakedRig.GetPart(partName);

            // unbaked run?
            TurboModel unbaked = UnbakedRig.GetPart(partName);
            if(unbaked != null)
            {
                // We should be loading up poses into the modelViewMatrix already
                RenderSystem.applyModelViewMatrix();

                for (TurboElement element : unbaked.GetElements())
                {
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder tessBuffer = tesselator.getBuilder();
                    if (transparent)
                        tessBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    else
                        tessBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

                    for (Direction direction : Direction.values())
                    {
                        TurboFace face = element.GetFace(direction);
                        Vector3f[] positions = element.GetFaceVertices(direction, true);
                        for (int i = 0; i < 4; i++)
                        {

                            tessBuffer.vertex(positions[i].x, positions[i].y, positions[i].z);
                            if (transparent)
                            {
                                float r = ((light >> 16) & 0xff) / 255f;
                                float g = ((light >> 8) & 0xff) / 255f;
                                float b = ((light) & 0xff) / 255f;
                                tessBuffer.color(r, g, b, 0.5f);
                            } else
                            {
                                tessBuffer.uv(face.uvData.getU(i), face.uvData.getV(i));
                            }

                            tessBuffer.endVertex();
                        }
                    }
                    tesselator.end();
                }
            }
        }
    }
}
