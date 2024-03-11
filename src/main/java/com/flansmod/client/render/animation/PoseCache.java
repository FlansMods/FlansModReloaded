package com.flansmod.client.render.animation;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.animation.elements.KeyframeDefinition;
import com.flansmod.client.render.animation.elements.PoseDefinition;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.json.Json;
import java.util.*;

public class PoseCache
{
	private static final Map<ResourceLocation, Map<ResourceLocation, Map<String, Map<String, Transform>>>> Cache = new HashMap<>();

	@Nonnull
	public static Transform Lerp(@Nonnull ResourceLocation itemID,
								 @Nonnull ResourceLocation animationID,
								 @Nonnull String keyframeA,
								 @Nonnull String keyframeB,
								 @Nonnull String partName,
								 float t)
	{
		Transform poseA = GetPose(itemID, animationID, keyframeA, partName);
		Transform poseB = GetPose(itemID, animationID, keyframeB, partName);
		return Transform.Interpolate(poseA, poseB, Maths.Clamp(t, 0f, 1f));
	}

	@Nonnull
	public static Transform GetPose(@Nonnull ResourceLocation itemID,
									@Nonnull ResourceLocation animationID,
									@Nonnull String keyframeID,
									@Nonnull String partName)
	{
		if(!Cache.containsKey(itemID))
			Cache.put(itemID, new HashMap<>());

		var cacheForItem = Cache.get(itemID);
		if(cacheForItem != null)
		{
			if (!cacheForItem.containsKey(animationID))
				cacheForItem.put(animationID, BuildCache(itemID, animationID));

			var cacheForAnim = cacheForItem.get(animationID);
			if(cacheForAnim != null)
			{
				var cacheForKeyframe = cacheForAnim.get(keyframeID);
				return cacheForKeyframe.getOrDefault(partName, Transform.IDENTITY);
			}
			else
			{
				// This is odd, we should have built a cache here.
				return Transform.IDENTITY;
			}
		}
		else
		{
			FlansMod.LOGGER.error("We failed to get the Cache we either already had or just created??");
			return Transform.IDENTITY;
		}
	}

	@Nonnull
	private static Map<String, Map<String, Transform>> BuildCache(@Nonnull ResourceLocation itemID,
																  @Nonnull ResourceLocation animationID)
	{
		// Create cache
		FlansMod.LOGGER.info(">> Creating cache for [model:"+itemID+", anim:"+ animationID+"] <<");
		Map<String, Map<String, Transform>> cache = new HashMap<>();
		FlanItemModelRenderer renderer = FlansModelRegistry.forItem(itemID);
		FlanimationDefinition anim = FlansModClient.ANIMATIONS.Get(animationID);
		if(renderer == null)
		{
			FlansMod.LOGGER.error(">> Cache creation failed for [model:"+itemID+", anim:"+ animationID+"] <<");
			return cache;
		}

		for(KeyframeDefinition keyframe : anim.keyframes)
		{
			cache.put(keyframe.name, new HashMap<>());
			var poseCache = cache.get(keyframe.name);
			BuildPoseCache(renderer, anim, keyframe, poseCache);
		}

		return cache;
	}
	private static void BuildPoseCache(@Nonnull FlanItemModelRenderer renderer,
									   @Nonnull FlanimationDefinition anim,
									   @Nonnull KeyframeDefinition keyframe,
									   @Nonnull Map<String, Transform> poseCache)
	{
		for (PoseDefinition pose : keyframe.poses)
			if (!poseCache.containsKey(pose.applyTo))
				poseCache.put(pose.applyTo, pose.Resolve(renderer.GetParameters()));

		for(String parent : keyframe.parents)
			if(parent != null && !parent.isEmpty())
			{
				KeyframeDefinition parentKeyframe = anim.GetKeyframe(parent);
				if(parentKeyframe != null)
					BuildPoseCache(renderer, anim, parentKeyframe, poseCache);
			}
	}
}
