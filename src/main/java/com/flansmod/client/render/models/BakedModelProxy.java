package com.flansmod.client.render.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;

public class BakedModelProxy implements BakedModel
{
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState p_235039_, @Nullable Direction p_235040_, RandomSource p_235041_)
	{
		return Collections.emptyList();
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return false;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean usesBlockLight()
	{
		return false;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return null;
	}

	@Override
	public ItemOverrides getOverrides()
	{
		return null;
	}
}
