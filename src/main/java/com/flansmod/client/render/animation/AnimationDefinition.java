package com.flansmod.client.render.animation;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.util.Transform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;

import javax.swing.text.html.Option;
import java.util.Map;
import java.util.Optional;

public class AnimationDefinition extends JsonDefinition
{
	public static final String TYPE = "animation";
	public static final AnimationDefinition INVALID = new AnimationDefinition(new ResourceLocation(FlansMod.MODID, "animations/null"));


	public AnimationDefinition(ResourceLocation srcLoc)
	{
		super(srcLoc);
	}

	@Override
	public String GetTypeName() { return TYPE; }

	public SequenceDefinition GetSequence(String name)
	{
		for(int i = 0; i < sequences.length; i++)
		{
			if(sequences[i].name.equals(name))
				return sequences[i];
		}
		return null;
	}

	public KeyframeDefinition GetKeyframe(SequenceDefinition.SequenceEntry entry)
	{
		return entry == null ? null : GetKeyframe(entry.frame);
	}

	public KeyframeDefinition GetKeyframe(String name)
	{
		for(int i = 0; i < keyframes.length; i++)
		{
			if(keyframes[i].name.equals(name))
				return keyframes[i];
		}
		return null;
	}

	public PoseDefinition GetPoseForPart(KeyframeDefinition keyframe, String partName)
	{
		return GetPoseForPartOptional(keyframe, partName).orElse(PoseDefinition.Identity());
	}

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
