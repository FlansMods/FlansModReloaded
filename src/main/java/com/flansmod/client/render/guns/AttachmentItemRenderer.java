package com.flansmod.client.render.guns;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.gunshots.GunContext;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class AttachmentItemRenderer extends FlanItemModelRenderer
{
	public AttachmentItemRenderer() { super(false); }

	@Override
	protected void DoRender(Entity heldByEntity, ItemStack stack, RenderContext renderContext)
	{
		// Find our skin
		ResourceLocation skin = GetSkin(stack);

		renderContext.Poses.pushPose();
		RenderPartTexturedSolid("attachment", skin, renderContext);
		renderContext.Poses.popPose();
	}

	public void RenderAsAttachment(RenderContext gunRendererContext, GunContext context, EAttachmentType attachmentType, int attachmentSlot)
	{
		// The main difference here is that we do not apply transforms. They should have been applied for us already
		Render(context.GetShooter().Entity(),
			context.GetAttachmentStack(attachmentType, attachmentSlot),
			new RenderContext(
				gunRendererContext.Buffers,
				null,
				gunRendererContext.Poses,
				gunRendererContext.Light,
				gunRendererContext.Overlay));
	}
}
