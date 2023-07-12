package com.flansmod.client.render.guns;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.animation.*;
import com.flansmod.client.render.animation.elements.KeyframeDefinition;
import com.flansmod.client.render.animation.elements.PoseDefinition;
import com.flansmod.client.render.animation.elements.SequenceDefinition;
import com.flansmod.client.render.animation.elements.SequenceEntryDefinition;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.actions.Action;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.AnimationAction;
import com.flansmod.common.gunshots.ShooterContext;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.AttachmentSettingsDefinition;
import com.flansmod.common.gunshots.GunContext;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GunItemRenderer extends FlanItemModelRenderer
{
    public GunItemRenderer()
    {
        super();
    }

    @Override
    protected void DoRender(Entity entity,
                            ItemStack stack,
                            TurboRig.Baked rig,
                            ItemTransforms.TransformType transformType,
                            PoseStack ms,
                            Consumer<String> renderPart)
    {
        // Find a sensible GunContext
        ShooterContext shooter = ShooterContext.CreateFrom(entity);
        GunContext gunContext = shooter.IsValid() ?
            GunContext.CreateFrom(shooter, MinecraftHelpers.GetHand(transformType)) :
            GunContext.CreateFrom(stack);
        if(!gunContext.IsValid())
            return;

        // If there is a valid action stack applicable to this gun, scan it for animation actions
        ActionStack actions = gunContext.GetActionStack();
        if(actions != null)
        {
            for (Action action : actions.GetActions())
            {
                if (!action.ShouldRender(gunContext))
                    return;
            }
        }

        AnimationDefinition animationSet = FlansModClient.ANIMATIONS.Get(new ResourceLocation(gunContext.GunDef().animationSet));
        ms.pushPose();
        {
            //ApplyRootRotation(stack, actions, ms, transformType);
            Consumer<String> PushAnimateRenderPop = new Consumer<String>()
            {
                @Override
                public void accept(String partName)
                {
                    ms.pushPose();
                    ApplyActiveAnimations(stack, actions, animationSet, ms, transformType, partName);
                    renderPart.accept(partName);
                    ms.popPose();
                }
            };
            ms.pushPose();
            {
                // Apply the body transform, then apply other transforms as children
                ApplyActiveAnimations(stack, actions, animationSet, ms, transformType, "body");
                renderPart.accept("body");

                PushAnimateRenderPop.accept("revolver");
                PushAnimateRenderPop.accept("slide");
                PushAnimateRenderPop.accept("pump");
                for(int i = 0; i < gunContext.GunDef().numBullets; i++)
                    PushAnimateRenderPop.accept("ammo_" + i);

                RenderPartOrAttachment(gunContext, EAttachmentType.Barrel, PushAnimateRenderPop, "barrel");
                RenderPartOrAttachment(gunContext, EAttachmentType.Grip, PushAnimateRenderPop, "grip");
                RenderPartOrAttachment(gunContext, EAttachmentType.Sights, PushAnimateRenderPop, "scope");
                RenderPartOrAttachment(gunContext, EAttachmentType.Stock, PushAnimateRenderPop, "stock");

                PushAnimateRenderPop.accept("rightHand");
            }
            ms.popPose();
        }
        ms.popPose();
    }

    private void RenderPartOrAttachment(GunContext context, EAttachmentType attachmentType, Consumer<String> renderPartFunc, String partName)
    {
        AttachmentSettingsDefinition attachmentSettings = context.GunDef().GetAttachmentSettings(attachmentType);
        AttachmentDefinition attachment = context.GetAttachmentDefinition(attachmentType);

        // Render the default mesh if we have no attachment or it is set to render anyway
        if(attachment == AttachmentDefinition.INVALID || !attachmentSettings.hideDefaultMesh)
        {
            renderPartFunc.accept(partName);
        }

        // Then render the attachment if we have one
        if(attachment != AttachmentDefinition.INVALID)
        {
            // TODO: Go get AttachmentRenderer

        }
    }

    private void ApplyActiveAnimations(ItemStack stack, ActionStack actions, AnimationDefinition animSet, PoseStack ms, ItemTransforms.TransformType transformType, String part)
    {
        if(actions != null)
        {
            Action[] cache = new Action[actions.GetActions().size()];
            actions.GetActions().toArray(cache);
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
                SequenceDefinition sequence = animSet.GetSequence(animAction.actionDef.anim);
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
                    KeyframeDefinition from = animSet.GetKeyframe(segment[0]);
                    KeyframeDefinition to = animSet.GetKeyframe(segment[1]);
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

                        PoseDefinition fromPose = animSet.GetPoseForPart(from, part);
                        PoseDefinition toPose = animSet.GetPoseForPart(to, part);

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
                ms.translate(resultPose.position.x, resultPose.position.y, resultPose.position.z);
                ms.mulPose(resultPose.orientation);
            }
        }
    }

    private void ApplyRootRotation(ItemStack stack, ActionStack actions, PoseStack ms, ItemTransforms.TransformType transformType)
    {
        if(transformType.firstPerson())
        {
            if(actions != null)
            {
                for(Action action : actions.GetActions())
                {
                    if(action instanceof AnimationAction animAction)
                    {
                        //FlansMod.LOGGER.info(animAction.progress + " + " + Minecraft.getInstance().getPartialTick());
                        float progress = animAction.GetProgressTicks() + Minecraft.getInstance().getPartialTick();
                        float duration = animAction.GetDurationTicks();
                        if(duration < 1.0f)
                            duration = 1.0f;
                        GunAnimation anim = GunAnimations.GetGunAnimation(animAction.actionDef.anim);
                        if(anim != null && anim.AppliesToPart(UnbakedRig.GetFloatParams(), "body"))
                        {
                            anim.Transform(ms, UnbakedRig.GetFloatParams(), "body", progress, duration);
                        }
                    }
                }
            }
        }
    }
}
