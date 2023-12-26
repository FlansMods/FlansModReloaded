package com.flansmod.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.animation.FlanimationDefinition;
import com.flansmod.client.render.animation.ESmoothSetting;
import com.flansmod.client.render.animation.elements.KeyframeDefinition;
import com.flansmod.client.render.animation.elements.PoseDefinition;
import com.flansmod.client.render.animation.elements.SequenceDefinition;
import com.flansmod.client.render.animation.elements.SequenceEntryDefinition;
import com.flansmod.client.render.models.*;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.GunContextCache;
import com.flansmod.common.actions.nodes.AnimationAction;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.util.*;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

public abstract class FlanItemModelRenderer extends BlockEntityWithoutLevelRenderer
{
    protected static final RenderStateShard.ShaderStateShard GUN_SOLID_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunSolidShader);
    protected static final RenderStateShard.ShaderStateShard GUN_CUTOUT_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunCutoutShader);
    protected static final RenderStateShard.ShaderStateShard GUN_EMISSIVE_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunEmissiveShader);
    protected static final RenderStateShard.ShaderStateShard GUN_TRANSPARENT_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunTransparentShader);

    private static class RenderTypeFlanItem extends RenderType {

        protected static RenderType.CompositeState.CompositeStateBuilder BaseState(ResourceLocation texture)
        {
            return RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setCullState(CULL)
                .setOverlayState(OVERLAY)
                .setLightmapState(LIGHTMAP)
                .setDepthTestState(LEQUAL_DEPTH_TEST);
        }

        protected static final Function<ResourceLocation, RenderType> GUN_CUTOUT = Util.memoize((texture) -> {
            RenderType.CompositeState compositeState =
                RenderType.CompositeState.builder()
                    .setShaderState(GUN_CUTOUT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
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
                compositeState);
        });


        protected static final Function<ResourceLocation, RenderType> GUN_EMISSIVE =
            Util.memoize((texture) ->
                create("flan_gun_emissive",
                    DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.QUADS,
                    256,
                    true,
                    false,
                    BaseState(texture)
                        .setShaderState(GUN_EMISSIVE_SHADER)
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)));
        protected static final Function<ResourceLocation, RenderType> GUN_TRANSPARENT =
            Util.memoize((texture) ->
                create("flan_gun_transparent",
                    DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.QUADS,
                    256,
                    true,
                    false,
                    BaseState(texture)
                        .setShaderState(GUN_TRANSPARENT_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .createCompositeState(true)));
        protected static final Function<ResourceLocation, RenderType> GUN_SOLID =
            Util.memoize((texture) ->
                create("flan_gun_solid",
                DefaultVertexFormat.BLOCK,
                VertexFormat.Mode.QUADS,
                256,
                true,
                false,
                BaseState(texture)
                    .setShaderState(GUN_SOLID_SHADER)
                    .setTransparencyState(NO_TRANSPARENCY)
                    .createCompositeState(true)));

        public RenderTypeFlanItem(String name, VertexFormat vf, VertexFormat.Mode vfm, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupFunc, Runnable cleanupFunc)
        {
            super(name, vf, vfm, bufferSize, affectsCrumbling, sortOnUpload, setupFunc, cleanupFunc);
        }
    }
    @Nonnull
    public static RenderType flanItemRenderType(@Nullable ResourceLocation texture, @Nonnull ETurboRenderMaterial material)
    {
        if(texture == null)
            texture = MissingTextureAtlasSprite.getLocation();
        return switch (material)
        {
            case Solid -> RenderTypeFlanItem.GUN_SOLID.apply(texture);
            case Cutout -> RenderTypeFlanItem.GUN_CUTOUT.apply(texture);
            case Emissive -> RenderTypeFlanItem.GUN_EMISSIVE.apply(texture);
            case Transparent -> RenderTypeFlanItem.GUN_TRANSPARENT.apply(texture);
        };
    }


    protected TurboRig UnbakedRig;
    protected TurboRig.Baked BakedRig;
    public final boolean ShouldRenderWhenHeld;

    public FlanItemModelRenderer(boolean shouldRenderWhenHeld)
    {
        super(null, null);
        ShouldRenderWhenHeld = shouldRenderWhenHeld;
    }
    public void OnUnbakedModelLoaded(TurboRig unbaked)
    {
        UnbakedRig = unbaked;
    }
    public void OnBakeComplete(TurboRig.Baked baked)
    {
        BakedRig = baked;
    }
    // Entry point for vanilla render calls
    @Override
    public void renderByItem(@Nullable ItemStack stack,
                             @Nonnull ItemDisplayContext transformType,
                             @Nonnull PoseStack ms,
                             @Nonnull MultiBufferSource buffers,
                             int light,
                             int overlay)
    {
        RenderSystem.enableDepthTest();
        ms.pushPose();
        RenderItem(null, transformType, stack, new TransformStack(Transform.FromPoseStack("ItemPose", ms)), buffers, light, overlay);
        ms.popPose();
    }
    // Not sure why you need to do this but another way in
    public void RenderDirect(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
    {
        renderContext.Transforms.PushSaveState();
        {
            // Apply root transform
            //if(renderContext.TransformType != null)
            //   BakedRig.ApplyTransform(renderContext.TransformType, renderContext.Transforms, false);

            DoRender(heldByEntity, stack, renderContext);
        }
        renderContext.Transforms.PopSaveState();
    }




    protected void RenderItem(@Nullable Entity entity,
                              @Nonnull ItemDisplayContext transformType,
                              @Nullable ItemStack stack,
                              @Nonnull TransformStack transformStack,
                              @Nonnull MultiBufferSource buffers,
                              int light,
                              int overlay)
    {
        transformStack.PushSaveState();
        {
            boolean shouldRenderRig = true;
            if(transformType == ItemDisplayContext.GUI)
            {
                String skin = "default";
                if(stack != null)
                    skin = FlanItem.GetPaintjobName(stack);
                BakedModel iconModel = BakedRig.GetIconModel(skin);
                if(iconModel != null)
                {

                    shouldRenderRig = false;
                    //PoseStack poseStack = new PoseStack();
                    //transformStack.add(new Transform("Offset to GUI origin", new Vec3(-0.5d, -0.5d, 0d)));
                    transformStack.add(new Transform("Scale to GUI size", -2.0f));
                    PoseStack poseStack = transformStack.Top().ToNewPoseStack();
                    poseStack.scale(-1f, 1f, 1f);
                    Lighting.setupForFlatItems();
                    Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                        poseStack.last(),
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
                FirstPersonManager.ApplyRootToModel(
                        transformStack,
                        GunContextCache.Get(true).Create(stack),
                        transformType);

                // Render item
                DoRender(entity, stack, new RenderContext(buffers, transformType, transformStack, light, overlay));
            }
        }
        transformStack.PopSaveState();
    }

    // The specifics handled by each render type, gun etc.
    protected abstract void DoRender(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext);

    // Then a bunch of functions you can call while in the render func
    protected void ApplyAnimations(@Nonnull RenderContext renderContext,
                                   @Nullable FlanimationDefinition animationSet,
                                   @Nullable ActionStack actionStack,
                                   @Nonnull String partName)
    {
        renderContext.Transforms.add(GetPose(animationSet, actionStack, partName));
    }

    protected void ApplyAnimations(@Nonnull TransformStack transformStack,
                                   @Nullable FlanimationDefinition animationSet,
                                   @Nullable ActionStack actionStack,
                                   @Nonnull String partName)
    {
        transformStack.add(GetPose(animationSet, actionStack, partName));
    }

    protected Transform GetPose(FlanimationDefinition animationSet, ActionStack actionStack, String partName)
    {
        if(UnbakedRig == null)
            return Transform.Error("Unbaked Rig Missing");

        if(actionStack != null)
        {
            if(!animationSet.IsValid())
                return Transform.Error("Missing animation set");

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

                        //!
                        Transform test = new Transform("Pose["+from.name+"-"+to.name+"]", pos.mul(1f/16f), rotation);
                        poses.add(test);
                    }
                }
            }

            Transform resultPose = poses.size() > 0 ? Transform.Interpolate(poses) : new Transform("Anim no-op");
            TurboModel model = UnbakedRig.GetPart(partName);
            if (model != null)
            {
                //return resultPose.Translate(model.offset.x, model.offset.y, model.offset.z);
            }
            // else
            return resultPose;
        }

        return new Transform("No Anims");
    }

    private void ApplyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress)
    {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    public ResourceLocation GetSkin(@Nullable ItemStack stack)
    {
        String skin = "default";
        if(stack != null && stack.getItem() instanceof FlanItem flanItem)
        {
            skin = flanItem.GetPaintjobName(stack);
        }
        return BakedRig.GetTexture(skin);
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
        renderContext.Transforms.PushSaveState();
        {
            boolean shouldRender = preRenderFunc.apply(partName, renderContext);
            if(shouldRender)
            {
                TurboModel unbakedModel = UnbakedRig.GetPart(partName);
                ETurboRenderMaterial material = unbakedModel != null ? unbakedModel.material : ETurboRenderMaterial.Cutout;
                RenderPart(partName, material, textureFunc.apply(partName), renderContext);
                if(BakedRig != null)
                {
                    for (var kvp : BakedRig.GetAttachPoints())
                    {
                        if (kvp.getValue().Parent != null && kvp.getValue().Parent.PartName.equals(partName))
                        {
                            renderContext.Transforms.PushSaveState();
                            renderContext.Transforms.add(new Transform("Offset[" + kvp.getKey() + "]", kvp.getValue().Offset, kvp.getValue().Rotation));
                            RenderPartIteratively(renderContext, kvp.getKey(), textureFunc, preRenderFunc, postRenderFunc);
                            renderContext.Transforms.PopSaveState();
                        }
                    }
                }
            }
            postRenderFunc.accept(partName, renderContext);
        }
        renderContext.Transforms.PopSaveState();
    }

    protected void RenderPartTexturedSolid(String partName, ResourceLocation withTexture, RenderContext renderContext)
    {
        VertexConsumer vc = renderContext.Buffers.getBuffer(flanItemRenderType(withTexture, ETurboRenderMaterial.Cutout));
        TurboRenderUtility.Render(UnbakedRig, BakedRig, partName, renderContext.Transforms, vc, renderContext.Light, renderContext.Overlay);
    }

    protected void RenderPart(@Nonnull String partName,
                              @Nonnull ETurboRenderMaterial material,
                              @Nullable ResourceLocation withTexture,
                              @Nonnull RenderContext renderContext)
    {
        VertexConsumer vc = renderContext.Buffers.getBuffer(flanItemRenderType(withTexture, material));
        TurboRenderUtility.Render(UnbakedRig, BakedRig, partName, renderContext.Transforms, vc, renderContext.Light, renderContext.Overlay);
    }

    protected void RenderAttachedEffect(String attachPointName, ResourceLocation texture, ResourceLocation model, RenderContext renderContext)
    {

    }

    public void ApplyAPOffsetInternal(@Nonnull TransformStack transformStack,
                                       @Nonnull String apName,
                                       @Nullable FlanimationDefinition animationSet,
                                       @Nullable ActionStack actionStack)
    {
        TurboRig.AttachPoint.Baked ap = BakedRig.GetAttachPoint(apName);
        if(ap != null)
        {
            // Resolve the AP that we are attached to first
            if (ap.Parent != null)
            {
                ApplyAPOffsetInternal(transformStack, ap.Parent.PartName, animationSet, actionStack);
            }

            // Then offset by our AP
            transformStack.add(new Transform("AP Offset["+apName+"]", ap.Offset, ap.Rotation, 1.0f));
            // Then offset by our animation
            if(animationSet != null && actionStack != null)
            {
                transformStack.add(GetPose(animationSet, actionStack, apName));
            }
        }
        else
        {
            transformStack.add(Transform.Error("Could not find AP '" + apName + "'"));
        }
    }



    public String GetAPKey(EAttachmentType attachmentType, int attachmentIndex)
    {
        String apKey = attachmentType.toString().toLowerCase() + "_" + attachmentIndex;
        if(BakedRig.GetAttachPoint(apKey) != null)
            return apKey;

        // Backup, try without the index i.e. "barrel" instead of "barrel_0"
        if(attachmentIndex == 0)
        {
            apKey = attachmentType.toString().toLowerCase();
            if(BakedRig.GetAttachPoint(apKey) != null)
                return apKey;
        }
        return "";
    }


}
