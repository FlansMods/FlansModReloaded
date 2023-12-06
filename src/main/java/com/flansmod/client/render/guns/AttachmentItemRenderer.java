package com.flansmod.client.render.guns;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.attachments.EAttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class AttachmentItemRenderer extends FlanItemModelRenderer
{
	public AttachmentItemRenderer() { super(false); }

	@Override
	protected void DoRender(Entity heldByEntity, ItemStack stack, RenderContext renderContext)
	{
		// Find our skin
		ResourceLocation skin = GetSkin(stack);

		renderContext.Poses.pushPose();
		RenderPartIteratively(renderContext,
			"body",
			// Texture func
			(partName) -> { return skin; },
			// Pre-func
			(partName, innerRenderContext) -> {
				return true;
			},
			// Post-func
			(partName, innerRenderContext) -> {
			}
		);
		//RenderPartTexturedSolid("attachment", skin, renderContext);
		renderContext.Poses.popPose();
	}

	public void RenderAsAttachment(RenderContext gunRendererContext, GunContext context, EAttachmentType attachmentType, int attachmentSlot)
	{
		// The main difference here is that we do not apply transforms. They should have been applied for us already
		RenderDirect(context.GetShooter().Entity(),
			context.GetAttachmentStack(attachmentType, attachmentSlot),
			new RenderContext(
				gunRendererContext.Buffers,
				null,
				gunRendererContext.Poses,
				gunRendererContext.Light,
				gunRendererContext.Overlay));
	}
}
