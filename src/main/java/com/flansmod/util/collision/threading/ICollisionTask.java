package com.flansmod.util.collision.threading;

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
