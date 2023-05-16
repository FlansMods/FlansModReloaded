package com.flansmod.client.render.animation;

import com.flansmod.common.types.JsonField;
import org.jetbrains.annotations.NotNull;

public class SequenceDefinition
{
	public enum ESmoothSetting
	{
		instant,
		linear,
		smooth,
	}

	public static class SequenceEntry implements Comparable<SequenceEntry>
	{
		@JsonField
		public int tick = 0;
		@JsonField
		public ESmoothSetting entry = ESmoothSetting.linear;
		@JsonField
		public ESmoothSetting exit = ESmoothSetting.linear;
		@JsonField
		public String frame = "";

		@Override
		public int compareTo(@NotNull SequenceDefinition.SequenceEntry other)
		{
			return Integer.compare(tick, other.tick);
		}
	}

	public SequenceEntry[] GetSegment(float tickPlusPartial)
	{
		SequenceEntry[] entries = new SequenceEntry[2];
		entries[0] = frames[0];
		entries[1] = frames[frames.length - 1];

		for(int i = 0; i < frames.length; i++)
		{
			// If this is the closest above or below our current time, set it
			if(frames[i].tick <= tickPlusPartial && frames[i].tick > entries[0].tick)
				entries[0] = frames[i];

			if(frames[i].tick > tickPlusPartial && frames[i].tick < entries[1].tick)
				entries[1] = frames[i];
		}

		return entries;
	}

	public float Duration()
	{
		return ticks;
	}

	@JsonField
	public String name = "";
	@JsonField(Min = 1)
	public int ticks = 20;
	@JsonField
	public SequenceEntry[] frames = new SequenceEntry[0];
}
