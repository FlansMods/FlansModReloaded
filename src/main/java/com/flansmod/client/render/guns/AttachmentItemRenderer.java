package com.flansmod.client.render.guns;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.RenderContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.item.AttachmentItem;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class AttachmentItemRenderer extends FlanItemModelRenderer
{
	public AttachmentItemRenderer() { super(false); }

	@Override
	protected void DoRender(Entity heldByEntity, ItemStack stack, RenderContext renderContext)
	{
		// Find our skin
		ResourceLocation skin = GetSkin(stack);

		renderContext.Transforms.PushSaveState();
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

		renderContext.Transforms.PopSaveState();


	}

	public void RenderAsAttachment(RenderContext gunRendererContext, GunContext context, EAttachmentType attachmentType, int attachmentSlot)
	{
		// The main difference here is that we do not apply transforms. They should have been applied for us already
		RenderDirect(context.GetShooter().Entity(),
			context.GetAttachmentStack(attachmentType, attachmentSlot),
			new RenderContext(
				gunRendererContext.Buffers,
				null,
				gunRendererContext.Transforms,
				gunRendererContext.Light,
				gunRendererContext.Overlay));
	}
}
