package com.flansmod.client.render.guns;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.animation.*;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.common.actions.*;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.guns.elements.AttachmentSettingsDefinition;
import com.flansmod.common.types.magazines.EAmmoLoadMode;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.util.MinecraftHelpers;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GunItemRenderer extends FlanItemModelRenderer
{
    public GunItemRenderer(@Nullable GunItem gunItem)
    {
        super(gunItem, true);
    }

    @Override
    protected void DoRender(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
    {
        if(stack == null)
            return;

        ShooterContext shooterContext = ShooterContext.of(heldByEntity);
        GunContext gunContext = shooterContext.IsValid()
            ? GunContext.of(shooterContext, MinecraftHelpers.GetHand(renderContext.TransformType))
            : GunContext.of(stack, EContextSide.Client);
        if(gunContext.IsValid())
        {
            ActionStack actionStack = gunContext.GetActionStack();
            // If there is a valid action stack applicable to this gun, scan it for animation actions
            if(heldByEntity instanceof LocalPlayer)
            {
                for (ActionGroupInstance group : actionStack.GetActiveActionGroups())
                    for (ActionInstance action : group.GetActions())
                        if (!action.ShouldRender(gunContext))
                            return;
            }

            // Find our animation set
            FlanimationDefinition animationSet = FlansModClient.ANIMATIONS.Get(new ResourceLocation(gunContext.CacheGunDefinition().animationSet));

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
                    innerRenderContext.Transforms.PushSaveState();
                    ApplyAnimations(innerRenderContext, animationSet, actionStack, partName);

                    if(partName.startsWith("grip") || partName.startsWith("barrel") || partName.startsWith("stock") || partName.startsWith("sights"))
                    {
                        EAttachmentType attachmentType = EAttachmentType.Generic;
                        int attachmentIndex = -1;
                        if(partName.contains("_"))
                        {
                            attachmentType = EAttachmentType.Parse(partName.split("_")[0]);
                            attachmentIndex = Integer.parseInt(partName.split("_")[1]);
                        }
                        else
                        {
                            attachmentType = EAttachmentType.Parse(partName);
                            attachmentIndex = 0;
                        }
                        return RenderAttachmentPoint(gunContext, innerRenderContext, partName, attachmentType, attachmentIndex);
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
                    innerRenderContext.Transforms.PopSaveState();
                });


        }
    }

    private boolean RenderAttachmentPoint(GunContext gunContext, RenderContext renderContext, String partName, EAttachmentType attachmentType, int attachmentIndex)
    {
        AttachmentSettingsDefinition attachmentSettings = gunContext.CacheGunDefinition().GetAttachmentSettings(attachmentType);
        boolean anyAttachmentsPresent = false;
        ItemStack attachmentStack = gunContext.GetAttachmentStack(attachmentType, attachmentIndex);
        AttachmentDefinition attachment = gunContext.GetAttachmentDefinition(attachmentType, attachmentIndex);

        // Then render the attachment if we have one
        if (attachment != AttachmentDefinition.INVALID)
        {
            anyAttachmentsPresent = true;

            FlanItemModelRenderer attachmentRenderer = FlansModelRegistry.forItem(attachmentStack);
            if (attachmentRenderer instanceof AttachmentItemRenderer attachmentItemRenderer)
            {
                renderContext.Transforms.PushSaveState();
                attachmentItemRenderer.RenderAsAttachment(renderContext, gunContext, attachmentType, attachmentIndex);
                renderContext.Transforms.PopSaveState();
            }
        }

        // Render the default mesh if we have no attachment or it is set to render anyway
        return !anyAttachmentsPresent || !attachmentSettings.hideDefaultMesh;
    }
}
