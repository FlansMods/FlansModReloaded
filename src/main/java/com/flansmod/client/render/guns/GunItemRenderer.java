package com.flansmod.client.render.guns;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.PartialRenderUtility;
import com.flansmod.client.render.animation.GunAnimation;
import com.flansmod.client.render.animation.GunAnimations;
import com.flansmod.client.render.guns.GunModel;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Action;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.AnimationAction;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GunItemRenderer extends FlanItemModelRenderer
{
    public GunItemRenderer()
    {
        super();
    }

    @Override
    protected void DoRender(ItemStack stack,
                            TurboRig.Baked rig,
                            ItemTransforms.TransformType transformType,
                            PoseStack ms,
                            Consumer<String> renderPart)
    {
        ActionStack actions = FlansMod.GUNSHOTS.GetActionStack(Minecraft.getInstance().cameraEntity);


        ms.pushPose();
        {
            ApplyRootRotation(stack, actions, ms, transformType);
            renderPart.accept("body");

            FlansModClient.ANIMATIONS.get(new ResourceLocation("flansmod", "bolt_action_rifle"));


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
                        float progress = animAction.progress + Minecraft.getInstance().getPartialTick();
                        float duration = animAction.duration;
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
