package com.flansmod.client.render.guns;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.animation.*;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Action;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.AnimationAction;
import com.flansmod.common.types.guns.GunContext;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
        ActionStack actions = FlansModClient.GUNSHOTS_CLIENT.GetActionStack(Minecraft.getInstance().cameraEntity);
        AnimationDefinition animationSet = FlansModClient.ANIMATIONS.get(new ResourceLocation("flansmod", "bolt_action_rifle"));
        GunContext context = GunContext.TryCreateFromEntity(entity, MinecraftHelpers.GetHand(transformType));

        if(actions != null)
        {
            for (Action action : actions.GetActions())
            {
                if (!action.ShouldRender(context))
                    return;
            }
        }

        ms.pushPose();
        {
            //ApplyRootRotation(stack, actions, ms, transformType);
            ApplyActiveAnimations(stack, actions, animationSet, ms, transformType, "body");
            renderPart.accept("body");



            renderPart.accept("grip");
            renderPart.accept("scope");
            renderPart.accept("slide");
            renderPart.accept("revolver");

            ms.pushPose();
            {
                ms.translate(0f, 0f, 0f);
                renderPart.accept("stock");
            }
            ms.popPose();

            renderPart.accept("rightHand");
        }
        ms.popPose();
    }

    private void ApplyActiveAnimations(ItemStack stack, ActionStack actions, AnimationDefinition animSet, PoseStack ms, ItemTransforms.TransformType transformType, String part)
    {
        if(actions != null)
        {
            Action[] cache = new Action[actions.GetActions().size()];
            actions.GetActions().toArray(cache);
            for(Action action : cache)
            {
                if(action instanceof AnimationAction animAction)
                {
                    SequenceDefinition sequence = animSet.GetSequence(animAction.actionDef.anim);
                    if(sequence == null)
                        continue;

                    // Make sure we scale the sequence (which can be played at any speed) with the target duration of this specific animation action
                    float progress = animAction.GetProgressTicks() + Minecraft.getInstance().getPartialTick();
                    float animMultiplier = sequence.Duration() / animAction.GetDurationTicks();
                    progress *= animMultiplier;

                    // Find the segment of this animation that we need
                    SequenceDefinition.SequenceEntry[] segment = sequence.GetSegment(progress);
                    float segmentDuration = segment[1].tick - segment[0].tick;

                    // If it is valid, let's animate it
                    if(segmentDuration > 0.0f)
                    {
                        KeyframeDefinition from = animSet.GetKeyframe(segment[0]);
                        KeyframeDefinition to = animSet.GetKeyframe(segment[1]);
                        if (from != null && to != null)
                        {
                            float linearParameter = (progress - segment[0].tick) / segmentDuration;
                            linearParameter = Maths.Clamp(linearParameter, 0f, 1f);
                            float outputParameter = linearParameter;

                            // Instant transitions take priority first
                            if(segment[0].exit == SequenceDefinition.ESmoothSetting.instant)
                                outputParameter = 1.0f;
                            if(segment[1].entry == SequenceDefinition.ESmoothSetting.instant)
                                outputParameter = 0.0f;

                            // Then apply smoothing?
                            if(segment[0].exit == SequenceDefinition.ESmoothSetting.smooth)
                            {
                                // Smoothstep function
                                if(linearParameter < 0.5f)
                                    outputParameter = linearParameter * linearParameter * (3f - 2f * linearParameter);
                            }
                            if(segment[1].entry == SequenceDefinition.ESmoothSetting.smooth)
                            {
                                // Smoothstep function
                                if(linearParameter > 0.5f)
                                    outputParameter = linearParameter * linearParameter * (3f - 2f * linearParameter);
                            }

                            PoseDefinition fromPose = animSet.GetPoseForPart(from, part);
                            PoseDefinition toPose = animSet.GetPoseForPart(to, part);

                            Vector3f pos = PoseDefinition.LerpPosition(UnbakedRig.GetFloatParams(), fromPose, toPose, outputParameter);
                            Quaternionf rotation = PoseDefinition.LerpRotation(UnbakedRig.GetFloatParams(), fromPose, toPose, outputParameter);

                            //FlansMod.LOGGER.info("Frame " + progress + ": From " + from.name + " to " + to.name + " at t=" + outputParameter);

                            ms.translate(pos.x, pos.y, pos.z);
                            ms.mulPose(rotation);
                        }
                    }
                }
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
