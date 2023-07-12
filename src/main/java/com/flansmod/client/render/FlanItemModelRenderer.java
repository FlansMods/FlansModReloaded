package com.flansmod.client.render;

import java.util.function.Consumer;
import java.util.function.Function;

import com.flansmod.client.FlansModClient;
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

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
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
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public abstract class FlanItemModelRenderer extends BlockEntityWithoutLevelRenderer
{
    private final RandomSource random = RandomSource.create();


    protected static final RenderStateShard.ShaderStateShard GUN_CUTOUT_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunCutoutShader);
    private static class RenderTypeFlanItem extends RenderType {
        protected static final Function<ResourceLocation, RenderType> GUN_CUTOUT = Util.memoize((p_173204_) -> {
            RenderType.CompositeState rendertype$compositestate =
                RenderType.CompositeState.builder()
                    .setShaderState(GUN_CUTOUT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(p_173204_, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setOverlayState(OVERLAY)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(true);
            return create("flan_gun_item",
                DefaultVertexFormat.BLOCK,
                VertexFormat.Mode.QUADS,
                256,
                true,
                false,
                rendertype$compositestate);
        });

        public RenderTypeFlanItem(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_)
        {
            super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        }
    }
    public static RenderType flanItemRenderType(ResourceLocation texture) {
        return RenderTypeFlanItem.GUN_CUTOUT.apply(texture);
    }



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

        RenderSystem.enableDepthTest();

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

            // Find the right buffer
            VertexConsumer vc = buffers.getBuffer(flanItemRenderType(texture));

            /*
            private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT = Util.memoize((p_173202_) -> {
                RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(p_173202_, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true);
            });
   private static final Function<ResourceLocation, RenderType> ENTITY_SOLID = Util.memoize((p_173204_) -> {
      RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
          .setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
          .setTextureState(new RenderStateShard.TextureStateShard(p_173204_, false, false))
          .setTransparencyState(NO_TRANSPARENCY)
          .setLightmapState(LIGHTMAP)
          .setOverlayState(OVERLAY)
          .createCompositeState(true);
   });

            // Good colours

               private static final Function<ResourceLocation, RenderType> ITEM_ENTITY_TRANSLUCENT_CULL = Util.memoize((p_173200_) -> {
      RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_173200_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE).createCompositeState(true);
      return create("item_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
   });
            private static final Function<ResourceLocation, RenderType> ENTITY_SHADOW = Util.memoize((p_173188_) -> {
                RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_SHADOW_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(p_173188_, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false);
                return create("entity_shadow",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    false,
                    rendertype$compositestate);
            });
   private static final Function<ResourceLocation, RenderType> DRAGON_EXPLOSION_ALPHA = Util.memoize((p_173255_) -> {
      RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
      .setShaderState(RENDERTYPE_ENTITY_ALPHA_SHADER)
      .setTextureState(new RenderStateShard.TextureStateShard(p_173255_, false, false))
      .setCullState(NO_CULL)
      .createCompositeState(true);
   });
               private static final Function<ResourceLocation, RenderType> EYES = Util.memoize((p_173253_) -> {
      RenderStateShard.TextureStateShard renderstateshard$texturestateshard = new RenderStateShard.TextureStateShard(p_173253_, false, false);
      return create("eyes", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_EYES_SHADER).setTextureState(renderstateshard$texturestateshard).setTransparencyState(ADDITIVE_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
   });
   private static final Function<ResourceLocation, RenderType> ENTITY_NO_OUTLINE = Util.memoize((p_173190_) -> {
      RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_NO_OUTLINE_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_173190_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setWriteMaskState(COLOR_WRITE).createCompositeState(false);
      return create("entity_no_outline", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, rendertype$compositestate);
   });
            */

            /*
            if (texture != null)
            {
                Minecraft.getInstance().textureManager.getTexture(texture).setFilter(false, false);
                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
                RenderSystem.setShaderTexture(0, texture);
            }

            RenderSystem.setupShaderLights(RenderSystem.getShader());
            */

            // Render item
            DoRender(entity, stack, BakedRig, transformType, modelViewStack, (partName) -> {
                RenderPartSolid(modelViewStack, stack, vc, partName, light, overlay);
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

    protected void RenderPartTransparent(PoseStack ms, ItemStack stack, VertexConsumer vc, String partName, int light, int overlay)
    {
        RenderPart(ms, stack, vc, partName, light, overlay, true);
    }

    protected void RenderPartSolid(PoseStack ms, ItemStack stack, VertexConsumer vc, String partName, int light, int overlay)
    {
        RenderPart(ms, stack, vc, partName, light, overlay, false);
    }

    protected void RenderPart(PoseStack ms,
                              ItemStack stack,
                              VertexConsumer vc,
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
/*
            BakedModel model = BakedRig.GetPart(partName);
            if(model != null)
            {
                for (BakedQuad quad : model.getQuads(null, null, RandomSource.create()))
                {


                    vc.putBulkData(ms.last(),
                        quad,
                        1f,
                        1f,
                        1f,
                        1f,
                        light,
                        overlay,
                        false);
                }
            }*/

            // unbaked run?
            TurboModel unbaked = UnbakedRig.GetPart(partName);
            if(unbaked != null)
            {
                // We should be loading up poses into the modelViewMatrix already
                RenderSystem.applyModelViewMatrix();

                for (TurboElement element : unbaked.GetElements())
                {
                    /*
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder tessBuffer = tesselator.getBuilder();
                    if (transparent)
                        tessBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    else
                        tessBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
*/
                    for (Direction direction : Direction.values())
                    {
                        TurboFace face = element.GetFace(direction);
                        Vector3f[] positions = element.GetFaceVertices(direction, true);
                        Vector3f normal = element.GetNormal(direction, true);
                        Vector3f nPosed = ms.last().normal().transform(normal);

                        for (int i = 0; i < 4; i++)
                        {
                            Vector4f vPosed = ms.last().pose().transform(new Vector4f(positions[i].x, positions[i].y, positions[i].z, 1.0F));
                            vc.vertex(
                                vPosed.x,
                                vPosed.y,
                                vPosed.z,
                                1.0f, 1.0f, 1.0f, 1.0f,
                                face.uvData.getU(i),
                                face.uvData.getV(i),
                                0, // overlayCoords
                                0, // uv2
                                nPosed.x,
                                nPosed.y,
                                nPosed.z);
                        }
                    }
                }
            }

        }
    }
}
