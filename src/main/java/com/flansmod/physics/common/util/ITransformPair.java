package com.flansmod.physics.common.util;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

// Why this interface? If you want t=0, there is no need to supply t=1. Let's cut the work up
public interface ITransformPair
{
	ITransformPair Identity = new ITransformPair()
	{
		@Override @Nonnull
		public Transform GetPrevious() { return Transform.identity(); }
		@Override @Nonnull
		public Transform GetCurrent() { return Transform.identity(); }
	};

	@Nonnull Transform GetPrevious();
	@Nonnull Transform GetCurrent();

	@Nonnull
	default Transform GetDelta(float dt)
	{
		if(Maths.Approx(dt, 0f))
			return GetPrevious();
		if(Maths.Approx(dt, 1f))
			return GetCurrent();
		return Transform.interpolate(GetPrevious(), GetCurrent(), dt);
	}

	@Nonnull
	static ITransformPair of(@Nonnull Supplier<Transform> prev, @Nonnull Supplier<Transform> current)
	{
		return new ITransformPair()
		{
			@Override @Nonnull public Transform GetPrevious() { return prev.get(); }
			@Override @Nonnull public Transform GetCurrent() { return current.get(); }
		};
	}
	@Nonnull
	static ITransformPair compose(@Nonnull ITransformPair a, @Nonnull ITransformPair b)
	{
		return new ITransformPair()
		{
			@Override @Nonnull public Transform GetPrevious() { return Transform.compose(a.GetPrevious(), b.GetPrevious()); }
			@Override @Nonnull public Transform GetCurrent() { return Transform.compose(a.GetCurrent(), b.GetCurrent()); }
		};
	}
}


