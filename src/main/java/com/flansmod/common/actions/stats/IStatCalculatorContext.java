package com.flansmod.common.actions.stats;

public interface IStatCalculatorContext
{
	class OutOfContextStats implements IStatCalculatorContext {
		@Override
		public int GetNumAttachments() { return 0; }
		@Override
		public float GetMagFullness() { return 0f; }
	}
	OutOfContextStats Invalid = new OutOfContextStats();

	default int GetNumAttachments() { return 0; }
	default float GetMagFullness() { return 0.0f; }
}
