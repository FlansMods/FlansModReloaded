package com.flansmod.client.render.bullets;

import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.models.ITurboRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.GunContextPlayer;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.actions.nodes.AttachEffectAction;
import com.flansmod.common.actions.nodes.EjectCasingAction;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.projectiles.CasingEntity;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.physics.common.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CasingEntityRenderer extends EntityRenderer<CasingEntity> {
    public CasingEntityRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }


    @Override
    public ResourceLocation getTextureLocation(CasingEntity casing) {
        return new ResourceLocation(casing.GetContext().Def.casingModel);
    }

    @Override
    public void render(@Nonnull CasingEntity casing, float yaw, float partialTick, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffers, int light)
    {
        GunDefinition def = casing.GetContext().Def;
        ResourceLocation loc = new ResourceLocation(casing.GetContext().Def.casingModel);

        if (casing.GetContext().GetItemStack().getItem() instanceof GunItem gunItem)
        {
            if(!gunItem.GetMagazineType(casing.GetContext().GetItemStack(), casing.GetContext().GetActionGroupContextByHash(casing.GetActionGroupPathHash()).GroupPath, 0).casingModelOverride.isEmpty()){
                loc = new ResourceLocation(gunItem.GetMagazineType(casing.GetContext().GetItemStack(), casing.GetContext().GetActionGroupContextByHash(casing.GetActionGroupPathHash()).GroupPath, 0).casingModelOverride);
            }
        }

        CasingRenderer casingRenderer = (CasingRenderer) FlansModelRegistry.GetItemRenderer(loc);

        boolean noRender = false;

        if(casingRenderer != null)
        {
            pose.pushPose();
            ItemDisplayContext display = ItemDisplayContext.FIXED;
            if(Minecraft.getInstance().options.getCameraType().isFirstPerson() && casing.GetContext().GetShooter().IsLocalPlayerOwner()) {
                pose.translate(casing.firstPersonOffset.x(), casing.firstPersonOffset.y(), casing.firstPersonOffset.z());
                display = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
                if(casing.lifeTime < 8){
                    noRender = true;
                }
            }
            float y = Mth.lerp(partialTick, casing.yRotO, casing.getYRot());
            float x = -Mth.lerp(partialTick, casing.xRotO, casing.getXRot());
            pose.mulPose(Axis.YP.rotationDegrees(y));
            pose.mulPose(Axis.ZP.rotationDegrees(x));


            RenderContext context = new RenderContext(
                    buffers,
                    display,
                    pose,
                    light,
                    655360);

            if(!noRender) {
                casingRenderer.renderDirect(
                        casing,
                        ItemStack.EMPTY,
                        context);
            }

            pose.popPose();
        }
        else FlansMod.LOGGER.warn("Could not find bullet renderer for " + casing);
    }
}
