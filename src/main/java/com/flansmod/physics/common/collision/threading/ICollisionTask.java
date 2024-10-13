package com.flansmod.physics.common.collision.threading;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICollisionTask<TInputType, TOutputType>
{
	void prepare(@Nonnull TInputType input);
	boolean canRun();
	void run();
	boolean isComplete();
	boolean canCancel();
	void cancel();
	@Nullable TOutputType getResult();
}
