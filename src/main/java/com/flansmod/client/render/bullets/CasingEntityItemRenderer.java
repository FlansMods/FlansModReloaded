package com.flansmod.client.render.bullets;

import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.models.ITurboRenderer;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.GunContextPlayer;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.actions.nodes.EjectCasingAction;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.projectiles.CasingEntity;
import com.flansmod.physics.client.DebugRenderer;
import com.flansmod.physics.common.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CasingEntityItemRenderer {
    public CasingEntityItemRenderer()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void OnRenderFirstPersonHands(@Nonnull RenderHandEvent event)
    {
        if (Minecraft.getInstance().level == null)
            return;
        ShooterContext playerContext = ShooterContext.of(Minecraft.getInstance().player);
        if (!playerContext.IsValid())
            return;

        for(CasingEntity e : Minecraft.getInstance().level.getEntitiesOfClass(CasingEntity.class, AABB.ofSize(Minecraft.getInstance().player.position(),5f,5f,5f))){
            if(e.GetContext().GetShooter().IsLocalPlayerOwner()){
                if(e.Action != null && e.lifeTime < 8){
                    RenderCasingFirstPerson(e, event.getPoseStack(),null,e.GetContext(), event.getPartialTick());
                }
            }
        }
    }

    public void RenderCasingFirstPerson(@Nonnull CasingEntity casing, @Nonnull PoseStack poseStack,
                                        @Nullable Camera camera,
                                        @Nonnull GunContext gunContext, @Nonnull float partialTick){

        ResourceLocation loc = new ResourceLocation(casing.GetContext().Def.casingModel);

        if (casing.GetContext().GetItemStack().getItem() instanceof GunItem gunItem)
        {
            if(!gunItem.GetMagazineType(casing.GetContext().GetItemStack(), casing.GetContext().GetActionGroupContextByHash(casing.GetActionGroupPathHash()).GroupPath, 0).casingModelOverride.isEmpty()){
                loc = new ResourceLocation(gunItem.GetMagazineType(casing.GetContext().GetItemStack(), casing.GetContext().GetActionGroupContextByHash(casing.GetActionGroupPathHash()).GroupPath, 0).casingModelOverride);
            }
        }

        CasingRenderer renderer = (CasingRenderer) FlansModelRegistry.GetItemRenderer(loc);
        if(renderer != null){
            ItemDisplayContext transformType = ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            if (gunContext instanceof GunContextPlayer playerGunContext)
            {
                if (!Minecraft.getInstance().options.getCameraType().isFirstPerson() || !gunContext.GetShooter().IsLocalPlayerOwner())
                {
                    transformType = MinecraftHelpers.getThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
                } else
                {
                    transformType = MinecraftHelpers.getFirstPersonTransformType(playerGunContext.GetHand());
                }
            }

            int light = camera != null ? camera.getBlockAtCamera().getLightEmission() : 0;

            poseStack.pushPose();
            //poseStack.scale(0.576f,0.576f,0.576f);

            RenderContext renderContext = new RenderContext(
                    Minecraft.getInstance().renderBuffers().bufferSource(),
                    transformType,
                    poseStack,
                    15728704,
                    655360);
            renderContext.Transforms.push();


            if(camera != null)
                renderContext.Transforms.add(Transform.fromPos(camera.getPosition().scale(-1d)));

            if(transformType.firstPerson())
            {
                FirstPersonManager.ApplyEyeToRoot(renderContext.Transforms, gunContext, transformType);
                FirstPersonManager.ApplyRootToModel(renderContext.Transforms, gunContext, transformType);
                FirstPersonManager.ApplyModelToAP(renderContext.Transforms, gunContext, casing.Action.EjectDirection(), true);
                renderContext.Transforms.add(renderer.getPosePublic(null, null, casing.Action.EjectDirection()));
                renderContext.Transforms.add(Transform.fromScale(2.0f*0.576f));
                //renderContext.Transforms.add(Transform.fromEuler(casing.getXRot(),casing.getYRot(),0));
            }
            else
            {
                renderContext.Transforms.add(
                        FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, casing.Action.AttachPoint()));
            }

            //renderContext.Transforms.add(Transform.fromPos(0d, 0d, -1d/16d));



            float dist = (float) (Mth.lerp(partialTick, casing.lifeTime-1, casing.lifeTime)*casing.getDeltaMovement().length());
            renderContext.Transforms.add(Transform.fromPos(new Vec3(0,0,-dist)));
            //renderer.renderDirect(null, ItemStack.EMPTY, renderContext);

            renderer.renderDirect(
                    casing,
                    ItemStack.EMPTY,
                    renderContext);
            renderContext.Transforms.pop();
            poseStack.popPose();
        }
    }
}
