package com.flansmod.physics.common.util;

import javax.annotation.Nonnull;

public interface ITransformEntity
{
	void syncTransformToEntity();
	void syncEntityToTransform();

	void teleportTo(@Nonnull Transform transform);
	@Nonnull ITransformPair getRootTransform();
}
