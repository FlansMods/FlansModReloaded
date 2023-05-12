package com.flansmod.client.render.animation;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.resources.ResourceLocation;

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


	@JsonField
	public KeyframeDefinition[] keyframes = new KeyframeDefinition[0];
	@JsonField
	public SequenceDefinition[] sequences = new SequenceDefinition[0];

}
