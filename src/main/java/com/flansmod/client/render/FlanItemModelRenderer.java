package com.flansmod.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.animation.AnimationDefinition;
import com.flansmod.client.render.animation.ESmoothSetting;
import com.flansmod.client.render.animation.elements.KeyframeDefinition;
import com.flansmod.client.render.animation.elements.PoseDefinition;
import com.flansmod.client.render.animation.elements.SequenceDefinition;
import com.flansmod.client.render.animation.elements.SequenceEntryDefinition;
import com.flansmod.client.render.guns.AttachmentItemRenderer;
import com.flansmod.client.render.models.*;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.gunshots.GunContextPlayer;
import com.flansmod.common.gunshots.ShooterContext;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.*;

import javax.annotation.Nonnull;

public abstract class FlanItemModelRenderer extends BlockEntityWithoutLevelRenderer
{
    protected static final RenderStateShard.ShaderStateShard GUN_CUTOUT_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunCutoutShader);
    private static class RenderTypeFlanItem extends RenderType {
        protected static final Function<ResourceLocation, RenderType> GUN_CUTOUT = Util.memoize((p_173204_) -> {
            RenderType.CompositeState rendertype$compositestate =
                RenderType.CompositeState.builder()
                    .setShaderState(GUN_CUTOUT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(p_173204_, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(CULL)
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

    protected abstract void DoRender(Entity heldByEntity, ItemStack stack, RenderContext renderContext);

    @Override
    public void renderByItem(@Nonnull ItemStack stack,
                             @Nonnull ItemTransforms.TransformType transformType,
                             @Nonnull PoseStack ms,
                             @Nonnull MultiBufferSource buffers,
                             int light,
                             int overlay)
    {
        RenderSystem.enableDepthTest();
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
            return;

        RenderItem(entity, transformType, stack, ms, buffers, light, overlay);
    }

    public void RenderDirect(Entity heldByEntity, ItemStack stack, RenderContext renderContext)
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

    protected void RenderItem(Entity entity,
                              ItemTransforms.TransformType transformType,
                              ItemStack stack,
                              PoseStack requestedPoseStack,
                              MultiBufferSource buffers,
                              int light,
                              int overlay)
    {
        requestedPoseStack.pushPose();
        {
            FlanItem flanItem = stack.getItem() instanceof FlanItem ? (FlanItem)stack.getItem() : null;
            String skin = "default";
            if(flanItem != null)
                skin = flanItem.GetPaintjobName(stack);

            boolean shouldRenderRig = true;
            if(transformType == ItemTransforms.TransformType.GUI)
            {
                BakedModel iconModel = BakedRig.GetIconModel(skin);
                if(iconModel != null)
                {
                    shouldRenderRig = false;
                    requestedPoseStack.setIdentity();
                    requestedPoseStack.translate(-0.5f, -0.5f, 0f);
                    Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                        requestedPoseStack.last(),
                        buffers.getBuffer(Sheets.cutoutBlockSheet()),
                        null,
                        iconModel,
                        1f, 1f, 1f,
                        light,
                        overlay);
                }
            }

            if(shouldRenderRig)
            {
                BakedRig.ApplyTransform(transformType, requestedPoseStack, false);

                // Bind texture
                ResourceLocation texture = BakedRig.GetTexture(skin);

                // Find the right buffer
                VertexConsumer vc = buffers.getBuffer(flanItemRenderType(texture));

                // Render item
                DoRender(entity, stack, new RenderContext(buffers, transformType, requestedPoseStack, light, overlay));
            }
        }
        requestedPoseStack.popPose();
    }

    protected void ApplyAnimations(RenderContext renderContext, AnimationDefinition animationSet, ActionStack actionStack, String partName)
    {
        if(UnbakedRig == null)
            return;

        if(actionStack != null)
        {
            List<AnimationAction> animActions = new ArrayList<>();
            for(ActionGroupInstance group : actionStack.GetActiveActionGroups())
                for(ActionInstance action : group.GetActions())
                {
                    if (action instanceof AnimationAction animAction)
                        animActions.add(animAction);
                }

            List<Transform> poses = new ArrayList<>();
            for (AnimationAction animAction : animActions)
            {

                SequenceDefinition sequence = animationSet.GetSequence(animAction.Def.anim);
                if (sequence == null)
                {
                    FlansMod.LOGGER.warn("Could not find animation sequence " + animAction.Def.anim + " in anim set " + animationSet.Location);
                    continue;
                }

                // Make sure we scale the sequence (which can be played at any speed) with the target duration of this specific animation action
                float progress = animAction.AnimFrame + Minecraft.getInstance().getPartialTick();
                float animMultiplier = sequence.Duration() / (animAction.Def.duration * 20f);
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

            float adsBlend = renderContext.TransformType.firstPerson() ? FlansModClient.ADS_BLEND : 0.0f;
            if(poses.size() > 0)
            {
                Transform resultPose = Transform.Interpolate(poses);
                if(adsBlend > 0.0f && partName.equals("body"))
                {
                    Transform eyeLine = GetEyeLine(renderContext.TransformType);
                    Vector3d blendedPos = resultPose.position.lerp(eyeLine.position, adsBlend);
                    Quaternionf blendedOri = resultPose.orientation.slerp(eyeLine.orientation, adsBlend);
                    renderContext.Poses.translate(blendedPos.x, blendedPos.y, blendedPos.z);
                    renderContext.Poses.mulPose(blendedOri);
                }
                else
                {
                    renderContext.Poses.translate(resultPose.position.x, resultPose.position.y, resultPose.position.z);
                    renderContext.Poses.mulPose(resultPose.orientation);
                }
            }
            else
            {
                if(adsBlend > 0.0f && partName.equals("body"))
                {
                    Transform eyeLine = GetEyeLine(renderContext.TransformType);
                    renderContext.Poses.translate(eyeLine.position.x * adsBlend, eyeLine.position.y * adsBlend, eyeLine.position.z * adsBlend);
                    renderContext.Poses.mulPose(eyeLine.orientation.slerp(new Quaternionf(), adsBlend, new Quaternionf()));
                }
            }

            // Apply the model offset after animating
            if(UnbakedRig != null)
            {
                TurboModel model = UnbakedRig.GetPart(partName);
                if (model != null)
                {
                    renderContext.Poses.translate(model.offset.x, model.offset.y, model.offset.z);
                }
            }
        }
    }

    private void ApplyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress)
    {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    protected Transform GetEyeLine(ItemTransforms.TransformType transformType)
    {
        if(UnbakedRig == null)
            return Transform.Identity();

        List<Transform> otherEyeLines = new ArrayList<>();
        List<Transform> myEyeLines = new ArrayList<>();
        ShooterContext shooterContext = ShooterContext.GetOrCreate(Minecraft.getInstance().player);
        if(shooterContext.IsValid())
        {
            for(GunContext gunContext : shooterContext.GetAllActiveGunContexts())
            {
                if(gunContext.IsValid() && gunContext instanceof GunContextPlayer gunContextPlayer)
                {
                    boolean isThisHand = MinecraftHelpers.GetHand(transformType) == gunContextPlayer.GetHand();
                    for(ActionGroupInstance groupInstance : gunContext.GetActionStack().GetActiveActionGroups())
                    {
                        boolean hasADS = false;
                        for(ActionInstance actionInstance : groupInstance.GetActions())
                        {
                            if(actionInstance instanceof AimDownSightAction adsAction)
                            {
                                hasADS = true;
                            }
                        }

                        if(hasADS)
                        {
                            if(groupInstance.Context.IsAttachment())
                            {
                                EAttachmentType attachmentType = groupInstance.Context.GetAttachmentType();
                                int attachmentIndex = groupInstance.Context.GetAttachmentIndex();

                                TurboRig.AttachPoint attachmentRoot = UnbakedRig.GetAttachPoint(attachmentType, attachmentIndex);
                                Transform attachmentRootTransform = Transform.FromPosAndEuler(attachmentRoot.Offset, attachmentRoot.Euler);
                                ItemStack attachmentStack = gunContext.GetAttachmentStack(attachmentType, attachmentIndex);
                                FlanItemModelRenderer attachmentRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(attachmentStack);
                                if (attachmentRenderer instanceof AttachmentItemRenderer attachmentItemRenderer)
                                {
                                    if(attachmentRenderer.UnbakedRig != null)
                                    {
                                        TurboRig.AttachPoint ap = attachmentRenderer.UnbakedRig.GetAttachPoint("eye_line");
                                        if(ap != null)
                                        {
                                            Transform eyeLineTransform = Transform.FromPosAndEuler(ap.Offset, ap.Euler);
                                            if (isThisHand)
                                                myEyeLines.add(attachmentRootTransform.RightMultiply(eyeLineTransform));
                                            else
                                                otherEyeLines.add(attachmentRootTransform.RightMultiply(eyeLineTransform));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                TurboRig.AttachPoint ap = UnbakedRig.GetAttachPoint("eye_line");
                                if(ap != null)
                                {
                                    Transform eyeLineTransform = Transform.FromPosAndEuler(ap.Offset, ap.Euler);
                                    if (isThisHand)
                                        myEyeLines.add(eyeLineTransform);
                                    else
                                        otherEyeLines.add(eyeLineTransform);
                                }
                            }
                        }
                    }
                }
            }
        }

        if(myEyeLines.size() > 0)
        {
            Vector3f srcOffset = UnbakedRig.GetTransforms(transformType).translation;
            boolean leftHanded = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
            Vector3d returnToCenter = new Vector3d(0f, -srcOffset.y*16f, leftHanded ? 8f : -8f);

            Vector3d targetDelta = returnToCenter.sub(myEyeLines.get(0).position);
            Quaternionf targetRotation = myEyeLines.get(0).orientation;

            if(otherEyeLines.size() > 0)
            {
                // Both guns are ADS, go halfway
                targetDelta.mul(0.5f);
            }

            return new Transform(targetDelta, targetRotation);
        }

        return Transform.Identity();
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
        //if(partName.equals("rightHand") || partName.equals("leftHand"))
        //{
        //    ResourceLocation skinLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(Minecraft.getInstance().getUser().getGameProfile());
        //    RenderSystem.setShaderTexture(0, skinLocation);
        //}
    }

    protected void RenderPartIteratively(RenderContext renderContext,
                                         String partName,
                                         Function<String, ResourceLocation> textureFunc,
                                         BiFunction<String, RenderContext, Boolean> preRenderFunc,
                                         BiConsumer<String, RenderContext> postRenderFunc)
    {
        renderContext.Poses.pushPose();
        {
            boolean shouldRender = preRenderFunc.apply(partName, renderContext);
            if(shouldRender)
            {
                RenderPartTexturedSolid(partName, textureFunc.apply(partName), renderContext);
                if(UnbakedRig != null)
                {
                    for (var kvp : UnbakedRig.GetAttachmentPoints())
                    {
                        if (kvp.getValue().AttachTo.equals(partName))
                        {
                            renderContext.Poses.pushPose();
                            renderContext.Poses.translate(kvp.getValue().Offset.x, kvp.getValue().Offset.y, kvp.getValue().Offset.z);
                            RenderPartIteratively(renderContext, kvp.getKey(), textureFunc, preRenderFunc, postRenderFunc);
                            renderContext.Poses.popPose();
                        }
                    }
                }
            }
            postRenderFunc.accept(partName, renderContext);
        }
        renderContext.Poses.popPose();
    }

    protected void RenderPartTexturedSolid(String partName, ResourceLocation withTexture, RenderContext renderContext)
    {
        VertexConsumer vc = renderContext.Buffers.getBuffer(flanItemRenderType(withTexture));
        if(UnbakedRig != null)
        {
            TurboModel unbaked = UnbakedRig.GetPart(partName);
            if (unbaked != null)
            {
                TurboRenderUtility.Render(unbaked, vc, renderContext.Poses, renderContext.Light, renderContext.Overlay);
            }
        }
    }

    public Vector3f GetAttachPoint(String apName)
    {
        if(UnbakedRig != null)
        {
            TurboRig.AttachPoint ap = UnbakedRig.GetAttachPoint(apName);
            if(ap != null)
                return ap.Offset;
        }
        return new Vector3f();
    }
}
