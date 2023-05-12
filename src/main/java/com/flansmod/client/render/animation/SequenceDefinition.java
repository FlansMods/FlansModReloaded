package com.flansmod.client.render.animation;

import com.flansmod.common.types.JsonField;

public class SequenceDefinition
{
	public enum ESmoothSetting
	{
		instant,
		linear,
		smooth,
	}

	public static class SequenceEntry
	{
		@JsonField
		public int tick = 0;
		@JsonField
		public ESmoothSetting entry = ESmoothSetting.linear;
		@JsonField
		public ESmoothSetting exit = ESmoothSetting.linear;
		@JsonField
		public String frame = "";
	}

	@JsonField
	public String name = "";
	@JsonField
	public SequenceEntry[] frames = new SequenceEntry[0];
}
