package com.flansmod.client.render.models.baked;

import com.flansmod.physics.common.util.Transform;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record BakedAttachPoint(@Nullable String parent, @Nonnull Transform offset, boolean physics)
{
	public static final BakedAttachPoint invalid = new BakedAttachPoint(null, Transform.identity(), false);
}
