package com.flansmod.client.render.animation.elements;

import com.flansmod.client.render.animation.ESmoothSetting;
import com.flansmod.common.types.JsonField;
import org.jetbrains.annotations.NotNull;

public class SequenceEntryDefinition implements Comparable<SequenceEntryDefinition>
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
	public int compareTo(@NotNull SequenceEntryDefinition other)
	{
		return Integer.compare(tick, other.tick);
	}
}