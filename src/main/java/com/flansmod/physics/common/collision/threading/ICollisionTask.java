package com.flansmod.physics.common.collision.threading;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICollisionTask<TInputType, TOutputType>
{
	void Prepare(@Nonnull TInputType input);
	boolean CanRun();
	void Run();
	boolean IsComplete();
	boolean CanCancel();
	void Cancel();
	@Nullable TOutputType GetResult();
}
