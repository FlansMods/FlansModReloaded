package com.flansmod.client.render.guns;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.animation.*;
import com.flansmod.client.render.animation.elements.KeyframeDefinition;
import com.flansmod.client.render.animation.elements.PoseDefinition;
import com.flansmod.client.render.animation.elements.SequenceDefinition;
import com.flansmod.client.render.animation.elements.SequenceEntryDefinition;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.actions.Action;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.AnimationAction;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.gunshots.ActionGroupContext;
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
import net.minecraft.client.renderer.MultiBufferSource;
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
        super(true);
    }

    @Override
    protected void DoRender(Entity heldByEntity, ItemStack stack, RenderContext renderContext)
    {
        ShooterContext shooterContext = ShooterContext.CreateFrom(heldByEntity);
        GunContext gunContext = shooterContext.IsValid() ?
            GunContext.CreateFrom(shooterContext, MinecraftHelpers.GetHand(renderContext.TransformType)) :
            GunContext.CreateFrom(stack);
        if(gunContext.IsValid())
        {
            // If there is a valid action stack applicable to this gun, scan it for animation actions
            ActionStack actionStack = gunContext.GetActionStack();
            for (Action action : actionStack.GetActions())
                if (!action.ShouldRender(gunContext))
                    return;

            // Find our animation set
            AnimationDefinition animationSet = FlansModClient.ANIMATIONS.Get(new ResourceLocation(gunContext.GunDef().animationSet));

            // Find our skin
            ResourceLocation skin = GetSkin(stack);



            RenderPart(gunContext, animationSet, actionStack, skin, renderContext, "body");

            // Special case for the body, we keep the animations applied for rendering other parts
            renderContext.Poses.pushPose();
            {
                ApplyAnimations(renderContext, animationSet, actionStack, "body");


                RenderPart(gunContext, animationSet, actionStack, skin, renderContext, "revolver");
                RenderPart(gunContext, animationSet, actionStack, skin, renderContext, "slide");
                RenderPart(gunContext, animationSet, actionStack, skin, renderContext, "pump");

                ActionGroupContext primaryContext = ActionGroupContext.CreateFrom(gunContext, EActionInput.PRIMARY);
                ActionGroupContext secondaryContext = ActionGroupContext.CreateFrom(gunContext, EActionInput.SECONDARY);

                for (int i = 0; i < primaryContext.GetMagazineSize(0); i++)
                    RenderPart(gunContext, animationSet, actionStack, skin, renderContext, "ammo_" + i);

                RenderPartOrAttachment(gunContext, animationSet, actionStack, skin, renderContext, "grip", EAttachmentType.Grip);
                RenderPartOrAttachment(gunContext, animationSet, actionStack, skin, renderContext, "barrel", EAttachmentType.Barrel);
                RenderPartOrAttachment(gunContext, animationSet, actionStack, skin, renderContext, "stock", EAttachmentType.Stock);
                RenderPartOrAttachment(gunContext, animationSet, actionStack, skin, renderContext, "sights", EAttachmentType.Sights);
            }
            renderContext.Poses.popPose();
        }
    }

    private void RenderPartOrAttachment(GunContext gunContext, AnimationDefinition animationSet, ActionStack actionStack, ResourceLocation skin, RenderContext renderContext, String partName, EAttachmentType attachmentType)
    {
        AttachmentSettingsDefinition attachmentSettings = gunContext.GunDef().GetAttachmentSettings(attachmentType);
        boolean anyAttachmentsPresent = false;
        for(int attachmentSlot = 0; attachmentSlot < attachmentSettings.numAttachmentSlots; attachmentSlot++)
        {
            ItemStack attachmentStack = gunContext.GetAttachmentStack(attachmentType, attachmentSlot);
            AttachmentDefinition attachment = gunContext.GetAttachmentDefinition(attachmentType, attachmentSlot);

            // Then render the attachment if we have one
            if (attachment != AttachmentDefinition.INVALID)
            {
                anyAttachmentsPresent = true;

                FlanItemModelRenderer attachmentRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(attachmentStack);
                if (attachmentRenderer instanceof AttachmentItemRenderer attachmentItemRenderer)
                {
                    renderContext.Poses.pushPose();
                    TurboRig.AttachPoint ap = UnbakedRig.GetAttachPoint(partName);
                    // TODO: Apply additional transform based on attach-to part
                    renderContext.Poses.translate(ap.Offset.x, ap.Offset.y, ap.Offset.z);
                    attachmentItemRenderer.RenderAsAttachment(renderContext, gunContext, attachmentType, attachmentSlot);
                    renderContext.Poses.popPose();
                }
            }
        }

        // Render the default mesh if we have no attachment or it is set to render anyway
        if (!anyAttachmentsPresent || !attachmentSettings.hideDefaultMesh)
        {
            RenderPart(gunContext, animationSet, actionStack, skin, renderContext, partName);
        }
    }

    private void RenderPart(GunContext gunContext, AnimationDefinition animationSet, ActionStack actionStack, ResourceLocation skin, RenderContext renderContext, String partName)
    {
        renderContext.Poses.pushPose();
        ApplyAnimations(renderContext, animationSet, actionStack, partName);
        RenderPartTexturedSolid(partName, skin, renderContext);
        renderContext.Poses.popPose();
    }
}
