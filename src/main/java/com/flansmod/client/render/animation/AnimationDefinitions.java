package com.flansmod.client.render.animation;

import com.flansmod.common.types.Definitions;

public class AnimationDefinitions extends Definitions<AnimationDefinition>
{
	public AnimationDefinitions()
	{
		super(AnimationDefinition.TYPE,
			AnimationDefinition.class,
			AnimationDefinition.INVALID,
			AnimationDefinition::new);
	}


}
