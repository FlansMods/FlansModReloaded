package com.flansmod.client.render.animation;

import com.flansmod.client.render.animation.elements.*;
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


	public FlanimationDefinition(@Nonnull ResourceLocation srcLoc)
	{
		super(srcLoc);
	}

	@Override
	public String GetTypeName() { return TYPE; }
	@Nullable
	public ModalSequenceDefinition GetModalSequence(@Nonnull String modeName)
	{
		for (ModalSequenceDefinition modal : modalSequences)
		{
			if (modal.modeName.equalsIgnoreCase(modeName))
				return modal;
		}
		return null;
	}
	@Nullable
	public SequenceDefinition GetSequence(@Nonnull String name)
	{
		for (SequenceDefinition sequence : sequences)
		{
			if (sequence.name.equalsIgnoreCase(name))
				return sequence;
		}
		return null;
	}
	@Nullable
	public KeyframeDefinition GetKeyframe(@Nullable SequenceEntryDefinition entry)
	{
		return entry == null ? null : GetKeyframe(entry.frame);
	}
	@Nullable
	public KeyframeDefinition GetKeyframe(@Nonnull String name)
	{
		for (KeyframeDefinition keyframe : keyframes)
		{
			if (keyframe.name.equals(name))
				return keyframe;
		}
		return null;
	}
	@Nonnull
	public PoseDefinition GetPoseForPart(@Nonnull KeyframeDefinition keyframe, @Nonnull String partName)
	{
		return GetPoseForPartOptional(keyframe, partName).orElse(PoseDefinition.Identity());
	}
	@Nonnull
	private Optional<PoseDefinition> GetPoseForPartOptional(@Nonnull KeyframeDefinition keyframe, @Nonnull String partName)
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
	@JsonField
	public ModalSequenceDefinition[] modalSequences = new ModalSequenceDefinition[0];
}
