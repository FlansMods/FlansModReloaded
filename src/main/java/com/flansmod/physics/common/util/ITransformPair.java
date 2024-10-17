package com.flansmod.physics.common.util;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

// Why this interface? If you want t=0, there is no need to supply t=1. Let's cut the work up
public interface ITransformPair
{
	ITransformPair Identity = new ITransformPair()
	{
		@Override @Nonnull
		public Transform previous() { return Transform.identity(); }
		@Override @Nonnull
		public Transform current() { return Transform.identity(); }
	};

	@Nonnull Transform previous();
	@Nonnull Transform current();

	@Nonnull
	default Transform delta(float dt)
	{
		if(Maths.approx(dt, 0f))
			return previous();
		if(Maths.approx(dt, 1f))
			return current();
		return Transform.interpolate(previous(), current(), dt);
	}

	@Nonnull
	static ITransformPair of(@Nonnull Supplier<Transform> prev, @Nonnull Supplier<Transform> current)
	{
		return new ITransformPair()
		{
			@Override @Nonnull public Transform previous() { return prev.get(); }
			@Override @Nonnull public Transform current() { return current.get(); }
		};
	}
	@Nonnull
	static ITransformPair compose(@Nonnull ITransformPair a, @Nonnull ITransformPair b)
	{
		return new ITransformPair()
		{
			@Override @Nonnull public Transform previous() { return Transform.compose(a.previous(), b.previous()); }
			@Override @Nonnull public Transform current() { return Transform.compose(a.current(), b.current()); }
		};
	}
}


