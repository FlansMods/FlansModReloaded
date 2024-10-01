package com.flansmod.client.render.tile;

import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.models.ITurboRenderer;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.blocks.TurretBlock;
import com.flansmod.common.blocks.TurretBlockEntity;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TurretBlockEntityRenderer implements BlockEntityRenderer<TurretBlockEntity>
{
    public TurretBlockEntityRenderer(@Nonnull BlockEntityRendererProvider.Context context)
    {

    }

    @Override
    public void render(@Nonnull TurretBlockEntity turretBlockEntity,
                       float v,
                       @Nonnull PoseStack poseStack,
                       @Nonnull MultiBufferSource multiBufferSource,
                       int i,
                       int i1)
    {

        for(UUID gunID : turretBlockEntity.GetAllGunIDs())
        {
            GunContext gunContext= turretBlockEntity.CreateContext(gunID);
            if (gunContext.IsValid())
            {
                ITurboRenderer renderer = FlansModelRegistry.GetItemRenderer(gunContext.Stack);
                if (renderer instanceof GunItemRenderer gunRenderer)
                {
                    poseStack.pushPose();

                    poseStack.translate(0.5d, 0.5d, 0.5d);
                    poseStack.mulPose(turretBlockEntity.getBlockState().getValue(TurretBlock.FACING).getRotation());
                    poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90f));

                    RenderContext renderContext = new RenderContext(
                            multiBufferSource,
                            ItemDisplayContext.FIXED,
                            poseStack,
                            i, i1);

                    gunRenderer.RenderDirect(null, gunContext.Stack, renderContext);
                    poseStack.popPose();
                }
            }
        }
    }
}
