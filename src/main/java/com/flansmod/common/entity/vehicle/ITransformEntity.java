package com.flansmod.common.entity.vehicle;

import com.flansmod.util.Transform;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITransformEntity
{
	@Nonnull default Transform GetLocal0() { return GetLocal(0f); }
	@Nonnull Transform GetLocal(float dt);
	@Nullable ITransformEntity GetParent();
}
