package com.flansmod.client.render.guns;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.animation.*;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.actions.*;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.gunshots.ShooterContext;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.guns.elements.AttachmentSettingsDefinition;
import com.flansmod.common.types.magazines.EAmmoLoadMode;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.util.MinecraftHelpers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GunItemRenderer extends FlanItemModelRenderer
{
    public GunItemRenderer()
    {
        super(true);
    }

    @Override
    protected void DoRender(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
    {
        if(stack == null)
            return;

        ShooterContext shooterContext = ShooterContext.GetOrCreate(heldByEntity);
        GunContext gunContext = shooterContext.IsValid() ?
            GunContext.GetGunContext(shooterContext, MinecraftHelpers.GetHand(renderContext.TransformType)) :
            GunContext.GetGunContext(stack);
        if(gunContext.IsValid())
        {
            // If there is a valid action stack applicable to this gun, scan it for animation actions
            ActionStack actionStack = gunContext.GetActionStack();
            for(ActionGroupInstance group : actionStack.GetActiveActionGroups())
                for (ActionInstance action : group.GetActions())
                    if (!action.ShouldRender(gunContext))
                        return;

            // Find our animation set
            AnimationDefinition animationSet = FlansModClient.ANIMATIONS.Get(new ResourceLocation(gunContext.CacheGunDefinition().animationSet));

            // Find our skin
            ResourceLocation skin = GetSkin(stack);

            RenderPartIteratively(
                renderContext,
                "body",
                // Texture Func
                (partName) -> {
                    return skin;
                },
                // Pre-Func
                (partName, innerRenderContext) -> {
                    innerRenderContext.Poses.pushPose();
                    ApplyAnimations(innerRenderContext, animationSet, actionStack, partName);

                    if(partName.equals("grip") || partName.equals("barrel") || partName.equals("stock") || partName.equals("sights"))
                    {
                        return RenderAttachmentPoint(gunContext, innerRenderContext, partName, EAttachmentType.Parse(partName));
                    }
                    else if(partName.startsWith("ammo_"))
                    {
                        ActionGroupContext primaryContext = ActionGroupContext.CreateFrom(gunContext, Actions.DefaultPrimaryActionKey);
                        if(primaryContext.IsValid())
                        {
                            String indexString = partName.substring("ammo_".length());
                            int magIndex = 0;
                            int bulletIndex = 0;
                            try
                            {
                                // If we are specifying further between mags, we need to split again
                                if (indexString.contains("_"))
                                {
                                    String[] parts = indexString.split("_");
                                    if(parts.length == 2)
                                    {
                                        magIndex = Integer.parseInt(parts[0]);
                                        bulletIndex = Integer.parseInt(parts[1]);
                                    }
                                } else
                                {
                                    bulletIndex = Integer.parseInt(indexString);
                                }
                            }
                            catch (Exception ignored)
                            { }


                            MagazineDefinition magDef = primaryContext.GetMagazineType(magIndex);
                            if(magDef.ammoLoadMode == EAmmoLoadMode.OneBulletAtATime_Revolver)
                                return !primaryContext.GetBulletAtIndex(magIndex, bulletIndex).isEmpty();

                            return true;
                        }
                    }

                    return true;
                },
                // Post-Func
                (partName, innerRenderContext) -> {
                    innerRenderContext.Poses.popPose();
                });


        }
    }

    private boolean RenderAttachmentPoint(GunContext gunContext, RenderContext renderContext, String partName, EAttachmentType attachmentType)
    {
        AttachmentSettingsDefinition attachmentSettings = gunContext.CacheGunDefinition().GetAttachmentSettings(attachmentType);
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
                    attachmentItemRenderer.RenderAsAttachment(renderContext, gunContext, attachmentType, attachmentSlot);
                    renderContext.Poses.popPose();
                }
            }
        }

        // Render the default mesh if we have no attachment or it is set to render anyway
        return !anyAttachmentsPresent || !attachmentSettings.hideDefaultMesh;
    }


    private void RenderPartOrAttachment(GunContext gunContext, AnimationDefinition animationSet, ActionStack actionStack, ResourceLocation skin, RenderContext renderContext, String partName, EAttachmentType attachmentType)
    {
        AttachmentSettingsDefinition attachmentSettings = gunContext.CacheGunDefinition().GetAttachmentSettings(attachmentType);
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
