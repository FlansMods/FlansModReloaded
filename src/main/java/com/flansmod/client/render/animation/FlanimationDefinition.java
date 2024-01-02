package com.flansmod.client.render.animation;

import com.flansmod.client.render.animation.elements.KeyframeDefinition;
import com.flansmod.client.render.animation.elements.PoseDefinition;
import com.flansmod.client.render.animation.elements.SequenceDefinition;
import com.flansmod.client.render.animation.elements.SequenceEntryDefinition;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class FlanimationDefinition extends JsonDefinition
{
	public static final String TYPE = "flanimation";
	public static final String FOLDER = "flanimations";
	public static final FlanimationDefinition INVALID = new FlanimationDefinition(new ResourceLocation(FlansMod.MODID, "animations/null"));


	public FlanimationDefinition(ResourceLocation srcLoc)
	{
		super(srcLoc);
	}

	@Override
	public String GetTypeName() { return TYPE; }
	@Nullable
	public SequenceDefinition GetSequence(String name)
	{
		for (SequenceDefinition sequence : sequences)
		{
			if (sequence.name.toLowerCase().equals(name.toLowerCase()))
				return sequence;
		}
		return null;
	}
	@Nullable
	public KeyframeDefinition GetKeyframe(SequenceEntryDefinition entry)
	{
		return entry == null ? null : GetKeyframe(entry.frame);
	}
	@Nullable
	public KeyframeDefinition GetKeyframe(String name)
	{
		for (KeyframeDefinition keyframe : keyframes)
		{
			if (keyframe.name.equals(name))
				return keyframe;
		}
		return null;
	}
	@Nonnull
	public PoseDefinition GetPoseForPart(KeyframeDefinition keyframe, String partName)
	{
		return GetPoseForPartOptional(keyframe, partName).orElse(PoseDefinition.Identity());
	}
	@Nonnull
	private Optional<PoseDefinition> GetPoseForPartOptional(KeyframeDefinition keyframe, String partName)
	{
		if(keyframe.HasPoseForPart(partName))
		{
			return Optional.of(keyframe.GetPoseForPart(partName));
		}
		else
		{
			for(String parent : keyframe.parents)
			{
				KeyframeDefinition parentKeyframe = GetKeyframe(parent);
				if(parentKeyframe != null)
				{
					Optional<PoseDefinition> poseFromParent = GetPoseForPartOptional(parentKeyframe, partName);
					if(poseFromParent.isPresent())
						return poseFromParent;
				}
			}
		}
		return Optional.empty();
	}

	@JsonField
	public KeyframeDefinition[] keyframes = new KeyframeDefinition[0];
	@JsonField
	public SequenceDefinition[] sequences = new SequenceDefinition[0];

}
