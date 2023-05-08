package com.flansmod.client.render.models;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;

public class TurboTextureAtlasSprite extends TextureAtlasSprite
{

	protected TurboTextureAtlasSprite(
		ResourceLocation skin)
	{
		super(skin,
			new SpriteContents(
				skin,
				new FrameSize(1,1),
				new NativeImage(1,1, false),
				AnimationMetadataSection.EMPTY,
				null),
			1,
			1,
			0,
			0
			);
	}
}
