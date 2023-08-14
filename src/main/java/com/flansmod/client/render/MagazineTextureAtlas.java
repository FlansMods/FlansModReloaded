package com.flansmod.client.render;

import com.flansmod.common.FlansMod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MagazineTextureAtlas implements PreparableReloadListener
{
	private static final ResourceLocation ATLAS_LOCATION = new ResourceLocation(FlansMod.MODID, "atlases/magazines");
	private static final ResourceLocation ATLAS_INFO_LOCATION = new ResourceLocation(FlansMod.MODID, "magazines");
	private static final FileToIdConverter MAGAZINE_LISTER = FileToIdConverter.json("magazines");
	private final TextureAtlas Atlas;
	private final HashMap<ResourceLocation, TextureAtlasSprite> Sprites;

	public MagazineTextureAtlas()
	{
		Atlas = new TextureAtlas(ATLAS_LOCATION);
		Sprites = new HashMap<>();
	}

	public void Init()
	{
		Minecraft.getInstance().getTextureManager().register(ATLAS_LOCATION, Atlas);
	}

	public TextureAtlasSprite GetIcon(ResourceLocation magLoc)
	{
		if(magLoc.getPath().contains("magazine"))
			return Atlas.getSprite(magLoc);
		else
		{
			return Atlas.getSprite(magLoc.withPrefix("magazine/"));
		}
	}

	@Override
	public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller p_10640_, ProfilerFiller p_10641_, Executor executor, Executor altExecutor)
	{
		CompletableFuture<SpriteLoader.Preparations> preparations =
			SpriteLoader.create(Atlas)
			.loadAndStitch(resourceManager, ATLAS_INFO_LOCATION, 0, executor)
			.thenCompose(SpriteLoader.Preparations::waitForUpload);

		return CompletableFuture.allOf(preparations)
			.thenCompose(barrier::wait)
			.thenAcceptAsync((var) -> {
				SpriteLoader.Preparations completedPrep = preparations.join();
				Atlas.upload(completedPrep);
				TextureAtlasSprite missingSprite = completedPrep.missing();
				Sprites.clear();
				Sprites.putAll(completedPrep.regions());
				FlansMod.LOGGER.info("Magazine Atlas has " + Sprites.size() + " sprites");
			}, altExecutor);
	}
}
