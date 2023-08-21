package com.flansmod.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;

public class RenderContext
{
	public final MultiBufferSource Buffers;
	public final ItemTransforms.TransformType TransformType;
	public final PoseStack Poses;
	public final int Light;
	public final int Overlay;

	public RenderContext(MultiBufferSource buffers,
						 ItemTransforms.TransformType transformType,
						 PoseStack poses,
						 int light,
						 int overlay)
	{
		Buffers = buffers;
		TransformType = transformType;
		Poses = poses;
		Light = light;
		Overlay = overlay;
	}
}
