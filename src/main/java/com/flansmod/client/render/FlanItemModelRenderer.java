package com.flansmod.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.animation.AnimationDefinition;
import com.flansmod.client.render.animation.ESmoothSetting;
import com.flansmod.client.render.animation.elements.KeyframeDefinition;
import com.flansmod.client.render.animation.elements.PoseDefinition;
import com.flansmod.client.render.animation.elements.SequenceDefinition;
import com.flansmod.client.render.animation.elements.SequenceEntryDefinition;
import com.flansmod.client.render.models.*;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Action;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.AnimationAction;
import com.flansmod.common.item.FlanItem;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.*;

import javax.annotation.Nonnull;

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
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
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
    public static RenderType flanItemRenderType(ResourceLocation texture)
    {
        if(texture == null)
            texture = MissingTextureAtlasSprite.getLocation();
        return RenderTypeFlanItem.GUN_CUTOUT.apply(texture);
    }


    protected TurboRig UnbakedRig;
    protected TurboRig.Baked BakedRig;
    public final boolean ShouldRenderWhenHeld;

    public FlanItemModelRenderer(boolean shouldRenderWhenHeld)
    {
        super(null, null);
        ShouldRenderWhenHeld = shouldRenderWhenHeld;
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

    public void Render(Entity heldByEntity, ItemStack stack, RenderContext renderContext)
    {
        renderContext.Poses.pushPose();
        {
            // Apply root transform
            if(renderContext.TransformType != null)
                BakedRig.ApplyTransform(renderContext.TransformType, renderContext.Poses, false);

            DoRender(heldByEntity, stack, renderContext);
        }
        renderContext.Poses.popPose();
    }

    protected abstract void DoRender(Entity heldByEntity, ItemStack stack, RenderContext renderContext);

    protected void RenderPartTexturedSolid(String partName, ResourceLocation withTexture, RenderContext renderContext)
    {
        VertexConsumer vc = renderContext.Buffers.getBuffer(flanItemRenderType(withTexture));
        TurboModel unbaked = UnbakedRig.GetPart(partName);
        if(unbaked != null)
        {
            TurboRenderUtility.Render(unbaked, vc, renderContext.Poses, renderContext.Light, renderContext.Overlay);
        }
    }

    protected void ApplyAnimations(RenderContext renderContext, AnimationDefinition animationSet, ActionStack actionStack, String partName)
    {
        if(actionStack != null)
        {
            Action[] cache = new Action[actionStack.GetActions().size()];
            actionStack.GetActions().toArray(cache);
            List<AnimationAction> animActions = new ArrayList<>();
            for(Action action : cache)
            {
                if (action instanceof AnimationAction animAction)
                {
                    animActions.add(animAction);
                }
            }

            List<Transform> poses = new ArrayList<>();
            for (AnimationAction animAction : animActions)
            {
                SequenceDefinition sequence = animationSet.GetSequence(animAction.ActionDef.anim);
                if (sequence == null)
                    continue;

                // Make sure we scale the sequence (which can be played at any speed) with the target duration of this specific animation action
                float progress = animAction.GetProgressTicks() + Minecraft.getInstance().getPartialTick();
                float animMultiplier = sequence.Duration() / animAction.GetDurationTicks();
                progress *= animMultiplier;

                // Find the segment of this animation that we need
                SequenceEntryDefinition[] segment = sequence.GetSegment(progress);
                float segmentDuration = segment[1].tick - segment[0].tick;

                // If it is valid, let's animate it
                if (segmentDuration >= 0.0f)
                {
                    KeyframeDefinition from = animationSet.GetKeyframe(segment[0]);
                    KeyframeDefinition to = animationSet.GetKeyframe(segment[1]);
                    if (from != null && to != null)
                    {
                        float linearParameter = (progress - segment[0].tick) / segmentDuration;
                        linearParameter = Maths.Clamp(linearParameter, 0f, 1f);
                        float outputParameter = linearParameter;

                        // Instant transitions take priority first
                        if (segment[0].exit == ESmoothSetting.instant)
                            outputParameter = 1.0f;
                        if (segment[1].entry == ESmoothSetting.instant)
                            outputParameter = 0.0f;

                        // Then apply smoothing?
                        if (segment[0].exit == ESmoothSetting.smooth)
                        {
                            // Smoothstep function
                            if (linearParameter < 0.5f)
                                outputParameter = linearParameter * linearParameter * (3f - 2f * linearParameter);
                        }
                        if (segment[1].entry == ESmoothSetting.smooth)
                        {
                            // Smoothstep function
                            if (linearParameter > 0.5f)
                                outputParameter = linearParameter * linearParameter * (3f - 2f * linearParameter);
                        }

                        PoseDefinition fromPose = animationSet.GetPoseForPart(from, partName);
                        PoseDefinition toPose = animationSet.GetPoseForPart(to, partName);

                        Vector3f pos = PoseDefinition.LerpPosition(UnbakedRig.GetFloatParams(), fromPose, toPose, outputParameter);
                        Quaternionf rotation = PoseDefinition.LerpRotation(UnbakedRig.GetFloatParams(), fromPose, toPose, outputParameter);

                        Transform test = new Transform(pos, rotation);
                        poses.add(test);
                    }
                }
            }
            if(poses.size() > 0)
            {
                Transform resultPose = Transform.Interpolate(poses);
                renderContext.Poses.translate(resultPose.position.x, resultPose.position.y, resultPose.position.z);
                renderContext.Poses.mulPose(resultPose.orientation);
            }

            // Apply the model offset after animating
            TurboModel model = UnbakedRig.GetPart(partName);
            if(model != null)
            {
                renderContext.Poses.translate(model.offset.x, model.offset.y, model.offset.z);
            }
        }
    }

    protected ResourceLocation GetSkin(@Nonnull ItemStack stack)
    {
        String skin = "default";
        if(stack.getItem() instanceof FlanItem flanItem)
        {
            skin = flanItem.GetPaintjobName(stack);
        }
        return BakedRig.GetTexture(skin);
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

        Render(entity, stack, new RenderContext(buffers, transformType, ms, light, overlay));
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

            // Render item
            DoRender(entity, stack, new RenderContext(buffers, transformType, requestedPoseStack, light, overlay));
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
