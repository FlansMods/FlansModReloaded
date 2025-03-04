package com.flansmod.client.render;

import java.util.Map;
import java.util.function.*;

import com.flansmod.client.render.animation.FlanimationDefinition;
import com.flansmod.client.render.models.*;
import com.flansmod.client.render.models.baked.BakedAttachPoint;
import com.flansmod.client.render.models.baked.BakedTurboRig;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.physics.common.util.EContextSide;
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
    public static final ResourceLocation invalidModelLoc = new ResourceLocation(FlansMod.MODID, "models/unknown");
    public static final Supplier<ResourceLocation> invalidModelLocGetter = () -> invalidModelLoc;
    public final boolean ShouldRenderWhenHeld;
    @Nullable
    public final FlanItem Item;
    protected final TurboRigWrapper rigWrapper;
    private ResourceLocation locationOverride;

    public FlanItemModelRenderer(@Nullable FlanItem flanItem, boolean shouldRenderWhenHeld)
    {
        super(null, null);
        Item = flanItem;
        ShouldRenderWhenHeld = shouldRenderWhenHeld;
        rigWrapper = new TurboRigWrapper(Item != null ? Item::getDefinitionLocation : invalidModelLocGetter);
    }

    public FlanItemModelRenderer(@Nullable FlanItem flanItem, boolean shouldRenderWhenHeld, ResourceLocation loc)
    {
        super(null, null);
        Item = flanItem;
        ShouldRenderWhenHeld = shouldRenderWhenHeld;
        locationOverride = loc;
        rigWrapper = new TurboRigWrapper(this::getLocationOverride);
    }

    public ResourceLocation getLocationOverride(){
        return locationOverride;
    }

    protected void ifRigFound(@Nonnull Consumer<BakedTurboRig> func) { rigWrapper.ifRigFound(func); }
    protected void ifAnyModelFound(@Nonnull Consumer<BakedModel> func) { rigWrapper.ifAnyModelFound(func); }
    protected void ifRigOrOtherwise(@Nonnull Consumer<BakedTurboRig> rigFunc, @Nonnull Consumer<BakedModel> backupFunc) { rigWrapper.ifRigOrOtherwise(rigFunc, backupFunc); }
    @Nullable protected <T> T getOrDefault(@Nonnull Function<BakedTurboRig, T> getFunc, @Nullable T defaultValue) { return rigWrapper.getOrDefault(getFunc, defaultValue); }
    @Nullable protected <T> T getRigOrOtherwise(@Nonnull Function<BakedTurboRig, T> rigGetFunc, @Nonnull Function<BakedModel, T> backupGetFunc, @Nullable T defaultValue) { return rigWrapper.getRigOrOtherwise(rigGetFunc, backupGetFunc, defaultValue); }

    // Entry point for vanilla render calls
    @Override
    public void renderByItem(@Nonnull ItemStack stack,
                             @Nonnull ItemDisplayContext transformType,
                             @Nonnull PoseStack ms,
                             @Nonnull MultiBufferSource buffers,
                             int light,
                             int overlay)
    {
        TransformStack transformStack = TransformStack.of(ms);
        boolean shouldRenderIcon = false;
        if(stack.getItem() instanceof FlanItem flanItem && flanItem.ShouldRenderAsIcon(transformType))
            shouldRenderIcon = true;
        if(shouldRenderIcon)
        {
            String skin = FlanItem.GetPaintjobName(stack);
            BakedModel iconModel = getRigOrOtherwise(
                    (rig) -> rig.getIconModel(skin),
                    (other) -> other,
                    null);
            if(iconModel != null)
            {
                PoseStack poseStack = null;
                switch(transformType)
                {
                    case GUI -> {
                        //transformStack.add(Transform.fromEuler(0f, 0f, 180f));
                        //transformStack.add(Transform.fromPos(1d, 0d, 0d));
                        //transformStack.add(Transform.FromScale(new Vector3f(-2.0f, -2.0f, 2.0f), () -> "\"Scale to GUI size\""));
                        poseStack = transformStack.top().toNewPoseStack();
                        //poseStack.scale(-1f, 1f, 1f);
                        Lighting.setupForFlatItems();
                    }
                    default -> {
                        poseStack = transformStack.top().toNewPoseStack();
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
            doRender(heldBy, stack, new RenderContext(buffers, transformType, transformStack, light, overlay));
        }
    }
    // Not sure why you need to do this but another way in
    public void renderDirect(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext)
    {
        renderContext.Transforms.push();
        {
            // Apply root transform
            //if(renderContext.TransformType != null)
            //   BakedRig.ApplyTransform(renderContext.TransformType, renderContext.Transforms, false);

            doRender(heldByEntity, stack, renderContext);
        }
        renderContext.Transforms.pop();
    }

    // The specifics handled by each render type, gun etc.
    protected abstract void doRender(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext);

    // Then a bunch of functions you can call while in the render func
    protected void applyAnimations(@Nonnull RenderContext renderContext,
                                   @Nonnull FlanimationDefinition animationSet,
                                   @Nullable ActionStack actionStack,
                                   @Nonnull String partName)
    {
        renderContext.Transforms.add(getPose(animationSet, actionStack, partName));
    }
    @Nonnull
    protected Transform getPose(@Nonnull FlanimationDefinition animationSet, @Nullable ActionStack actionStack, @Nonnull String sectionName)
    {
        return getOrDefault(rig -> rig.getPose(sectionName, getDefLoc(), animationSet, actionStack), Transform.IDENTITY);
    }

    @Nonnull
    public Transform getPosePublic (@Nonnull FlanimationDefinition animationSet, @Nullable ActionStack actionStack, @Nonnull String sectionName)
    {
        return getOrDefault(rig -> rig.getPose(sectionName, getDefLoc(), animationSet, actionStack), Transform.IDENTITY);
    }
    public boolean hasSection(@Nonnull String sectionName) { return getOrDefault(rig -> rig.hasSection(sectionName), false); }
    private void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress)
    {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }
    @Nonnull
    public ResourceLocation getSkin(@Nullable ItemStack stack)
    {
        String skin = stack != null ? FlanItem.GetPaintjobName(stack) : "default";
        return getOrDefault(rig -> rig.getSkin(skin), invalidModelLoc);
    }
    @Nonnull
    public Map<String, Float> getParameters() { return getOrDefault(BakedTurboRig::floatParameters, Map.of()); }
    @Nonnull
    public ResourceLocation getDefLoc()
    {
        return Item != null ? Item.DefinitionLocation : invalidModelLoc;
    }


    protected void RenderFirstPersonArm(PoseStack poseStack)
    {
        //if(partName.equals("rightHand") || partName.equals("leftHand"))
        //{
        //    ResourceLocation skinLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(Minecraft.getInstance().getUser().getGameProfile());
        //    RenderSystem.setShaderTexture(0, skinLocation);
        //}
    }

    protected void renderSectionIteratively(@Nonnull RenderContext renderContext,
                                            @Nonnull String sectionName,
                                            @Nonnull Function<String, ResourceLocation> textureFunc,
                                            @Nonnull BiFunction<String, RenderContext, Boolean> preRenderFunc,
                                            @Nonnull BiConsumer<String, RenderContext> postRenderFunc)
    {
        ifRigFound((rig) -> rig.renderSectionIteratively(renderContext, sectionName, textureFunc, preRenderFunc, postRenderFunc));
    }

    protected void RenderAttachedEffect(String attachPointName, ResourceLocation texture, ResourceLocation model, RenderContext renderContext)
    {

    }

    public void ApplyAPOffsetInternal(@Nonnull TransformStack transformStack,
                                      @Nonnull String apName,
                                      @Nullable FlanimationDefinition animationSet,
                                      @Nullable ActionStack actionStack)
    {
        ifRigFound((rig) ->
        {
            BakedAttachPoint ap = rig.getAP(apName);
            // Resolve the AP that we are attached to first
            if (ap.parent() != null)
            {
                ApplyAPOffsetInternal(transformStack, ap.parent(), animationSet, actionStack);
            }

            // Then offset by our AP
            transformStack.add(ap.offset());
            // Then offset by our animation
            if(animationSet != null && actionStack != null)
            {
                transformStack.add(getPose(animationSet, actionStack, apName));
            }
        });
    }

    @Nonnull
    public String getAPKey(@Nonnull EAttachmentType attachmentType, int attachmentIndex) { return getOrDefault((rig) -> rig.getAPKey(attachmentType, attachmentIndex), getDefaultAPKey(attachmentType, attachmentIndex)); }
    @Nonnull
    public String getDefaultAPKey(@Nonnull EAttachmentType attachmentType, int attachmentIndex) { return attachmentType.unindexedName(attachmentIndex); }
}
