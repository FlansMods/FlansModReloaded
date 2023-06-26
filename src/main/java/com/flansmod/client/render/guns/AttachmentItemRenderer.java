package com.flansmod.client.render.guns;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.models.TurboRig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class AttachmentItemRenderer extends FlanItemModelRenderer
{
	public AttachmentItemRenderer() { super(); }

	@Override
	protected void DoRender(Entity entity,
							ItemStack stack,
							TurboRig.Baked rig,
							ItemTransforms.TransformType transformType,
							PoseStack ms,
							Consumer<String> renderPart)
	{

	}
}
