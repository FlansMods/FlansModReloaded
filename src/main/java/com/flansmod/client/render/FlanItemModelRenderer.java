package com.flansmod.client.render;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.flansmod.client.render.animation.FlanimationDefinition;
import com.flansmod.client.render.models.*;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.actions.contexts.EContextSide;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class FlanItemModelRenderer extends BlockEntityWithoutLevelRenderer implements ITurboRenderer
{

    public final boolean ShouldRenderWhenHeld;
    @Nullable
    public final FlanItem Item;
    @Nullable
    private TurboRenderUtility TurboRenderHelper;
    @Nonnull
    public TurboRenderUtility GetTurboRigWrapper()
    {
        if(TurboRenderHelper == null)
        {
            if(Item == null)
                TurboRenderHelper = TurboRenderUtility.of();
            else
                TurboRenderHelper = FlansModelRegistry.GetRigWrapperFor(Item.DefinitionLocation);
        }
        return TurboRenderHelper;
    }


    public FlanItemModelRenderer(@Nullable FlanItem flanItem, boolean shouldRenderWhenHeld)
    {
        super(null, null);
        Item = flanItem;
        ShouldRenderWhenHeld = shouldRenderWhenHeld;
        TurboRenderHelper = null;
    }

    // Entry point for vanilla render calls
    @Override
    public void renderByItem(@Nonnull ItemStack stack,
                             @Nonnull ItemDisplayContext transformType,
                             @Nonnull PoseStack ms,
                             @Nonnull MultiBufferSource buffers,
                             int light,
                             int overlay)
    {
        TransformStack transformStack = new TransformStack(Transform.FromPose(ms));
        boolean shouldRenderIcon = false;
        if(stack.getItem() instanceof FlanItem flanItem && flanItem.ShouldRenderAsIcon(transformType))
            shouldRenderIcon = true;
        if(shouldRenderIcon)
        {
            String skin = FlanItem.GetPaintjobName(stack);
            BakedModel iconModel = GetTurboRigWrapper().GetIconModel(skin);
            if(iconModel != null)
            {
                PoseStack poseStack = null;
                switch(transformType)
                {
                    case GUI -> {
                        transformStack.add(Transform.FromEuler(0f, 0f, 180f));
                        transformStack.add(Transform.FromPos(1d, 0d, 0d));
                        //transformStack.add(Transform.FromScale(new Vector3f(-2.0f, -2.0f, 2.0f), () -> "\"Scale to GUI size\""));
                        poseStack = transformStack.Top().ToNewPoseStack();
                        poseStack.scale(-1f, 1f, 1f);
                        Lighting.setupForFlatItems();
                    }
                    default -> {
                        poseStack = transformStack.Top().ToNewPoseStack();
                        poseStack.scale(0.55f, 0.55f, 0.55f);
                        poseStack.translate(0.4f, 0.5f, 0.5f);
                    }
                }

                VertexConsumer buff = buffers.getBuffer(RenderType.cutout());
                buff.color(1,1,1,1); //Probably not needed
                Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                    poseStack.last(),
                    buff,
                    null,
                    iconModel,
                    1f, 1f, 1f,
                    light,
                    overlay);
            }
            else
                shouldRenderIcon = false;
        }

        if(!shouldRenderIcon)
        {
            FirstPersonManager.ApplyRootToModel(
                transformStack,
                GunContext.of(stack, EContextSide.Client),
                transformType);

            // Render item
            Entity heldBy = transformType.firstPerson() ? Minecraft.getInstance().player : null;
            DoRender(heldBy, stack, new RenderContext(buffers, transformType, transformStack, light, overlay));
        }
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

    // The specifics handled by each render type, gun etc.
    protected abstract void DoRender(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext);

    // Then a bunch of functions you can call while in the render func
    protected void ApplyAnimations(@Nonnull RenderContext renderContext,
                                   @Nonnull FlanimationDefinition animationSet,
                                   @Nullable ActionStack actionStack,
                                   @Nonnull String partName)
    {
        renderContext.Transforms.add(GetPose(animationSet, actionStack, partName));
    }

    public boolean HasPart(@Nonnull String partName)
    {
        return GetTurboRigWrapper().HasPart(partName);
    }

    @Nonnull
    protected Transform GetPose(@Nonnull FlanimationDefinition animationSet, @Nullable ActionStack actionStack, @Nonnull String partName)
    {
        return GetTurboRigWrapper().GetPose(partName,
            GetDefLoc(),
            animationSet,
            actionStack);
    }

    private void ApplyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress)
    {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }
    @Nonnull
    public ResourceLocation GetSkin(@Nullable ItemStack stack)
    {
        String skin = stack != null ? FlanItem.GetPaintjobName(stack) : "default";
        return GetTurboRigWrapper().GetSkinLocation(skin);
    }
    @Nonnull
    public Map<String, Float> GetParameters()
    {
        return GetTurboRigWrapper().GetParameters();
    }
    private static final ResourceLocation UnknownModelLocation = new ResourceLocation(FlansMod.MODID, "models/unknown");
    @Nonnull
    public ResourceLocation GetDefLoc()
    {
        return Item != null ? Item.DefinitionLocation : UnknownModelLocation;
    }


    protected void RenderFirstPersonArm(PoseStack poseStack)
    {
        //if(partName.equals("rightHand") || partName.equals("leftHand"))
        //{
        //    ResourceLocation skinLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(Minecraft.getInstance().getUser().getGameProfile());
        //    RenderSystem.setShaderTexture(0, skinLocation);
        //}
    }

    protected void RenderPartIteratively(@Nonnull RenderContext renderContext,
                                         @Nonnull String partName,
                                         @Nonnull Function<String, ResourceLocation> textureFunc,
                                         @Nonnull BiFunction<String, RenderContext, Boolean> preRenderFunc,
                                         @Nonnull BiConsumer<String, RenderContext> postRenderFunc)
    {
        GetTurboRigWrapper().RenderPartIteratively(renderContext, partName, textureFunc, preRenderFunc, postRenderFunc);
    }

    protected void RenderAttachedEffect(String attachPointName, ResourceLocation texture, ResourceLocation model, RenderContext renderContext)
    {

    }

    public void ApplyAPOffsetInternal(@Nonnull TransformStack transformStack,
                                       @Nonnull String apName,
                                       @Nullable FlanimationDefinition animationSet,
                                       @Nullable ActionStack actionStack)
    {
        TurboRig.AttachPoint.Baked ap = GetTurboRigWrapper().GetAP(apName);
        if(ap != null)
        {
            // Resolve the AP that we are attached to first
            if (ap.Parent != null)
            {
                ApplyAPOffsetInternal(transformStack, ap.Parent.PartName, animationSet, actionStack);
            }

            // Then offset by our AP
            transformStack.add(ap.Offset);
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

    @Nonnull
    public String GetAPKey(@Nonnull EAttachmentType attachmentType, int attachmentIndex)
    {
        return GetTurboRigWrapper().GetAPKey(attachmentType, attachmentIndex);
    }


}
