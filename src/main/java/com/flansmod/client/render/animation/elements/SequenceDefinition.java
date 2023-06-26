package com.flansmod.client.render.animation.elements;

import com.flansmod.common.types.JsonField;

public class SequenceDefinition
{
	public SequenceEntryDefinition[] GetSegment(float tickPlusPartial)
	{
		SequenceEntryDefinition[] entries = new SequenceEntryDefinition[2];
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
	public SequenceEntryDefinition[] frames = new SequenceEntryDefinition[0];
}
