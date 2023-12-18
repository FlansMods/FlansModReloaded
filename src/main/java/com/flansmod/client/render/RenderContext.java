package com.flansmod.client.render;

import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RenderContext
{
	@Nonnull
	public final MultiBufferSource Buffers;
	@Nullable
	public final ItemTransforms.TransformType TransformType;
	@Nonnull
	public final TransformStack Transforms;
	public final int Light;
	public final int Overlay;

	public RenderContext(@Nonnull MultiBufferSource buffers,
						 @Nullable ItemTransforms.TransformType transformType,
						 @Nonnull TransformStack transforms,
						 int light,
						 int overlay)
	{
		Buffers = buffers;
		TransformType = transformType;
		Transforms = transforms;
		Light = light;
		Overlay = overlay;
	}
	public RenderContext(@Nonnull MultiBufferSource buffers,
						 @Nullable ItemTransforms.TransformType transformType,
						 @Nonnull PoseStack poses,
						 int light,
						 int overlay)
	{
		Buffers = buffers;
		TransformType = transformType;
		Transforms = new TransformStack(new Transform("FromPoseStack", poses.last().pose()));
		Light = light;
		Overlay = overlay;
	}
	public RenderContext(@Nonnull MultiBufferSource buffers,
						 @Nullable ItemTransforms.TransformType transformType,
						 int light,
						 int overlay)
	{
		Buffers = buffers;
		TransformType = transformType;
		Transforms = new TransformStack();
		Light = light;
		Overlay = overlay;
	}
}
