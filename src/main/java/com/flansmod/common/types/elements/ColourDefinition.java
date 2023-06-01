package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ColourDefinition
{
	public static final ColourDefinition white = new ColourDefinition(1f, 1f, 1f);
	public static final ColourDefinition red = new ColourDefinition(1f, 0f, 0f);
	public static final ColourDefinition green = new ColourDefinition(0f, 1f, 0f);
	public static final ColourDefinition blue = new ColourDefinition(0f, 0f, 1f);
	public static final ColourDefinition yellow = new ColourDefinition(1f, 1f, 0f);
	public static final ColourDefinition cyan = new ColourDefinition(0f, 1f, 1f);
	public static final ColourDefinition magenta = new ColourDefinition(1f, 0f, 1f);
	public static final ColourDefinition black = new ColourDefinition(0f, 0f, 0f);


	@JsonField
	public int value;

	public ColourDefinition()
	{
		value = 0xffffff;
	}

	public ColourDefinition(int i)
	{
		value = i;
	}

	private static final float ratio = 1.0f / 255.0f;
	public ColourDefinition(float r, float g, float b)
	{
		value =   ((int)(r * ratio) & 0xff) << 16
				| ((int)(g * ratio) & 0xff) << 8
				| ((int)(b * ratio) & 0xff);
	}
	public ColourDefinition(float a, float r, float g, float b)
	{
		value =   ((int)(a * ratio) & 0xff) << 24
				| ((int)(r * ratio) & 0xff) << 16
				| ((int)(g * ratio) & 0xff) << 8
				| ((int)(b * ratio) & 0xff);
	}

	public static ColourDefinition ParseFunc(Object ref, JsonElement jNode, JsonField annot)
	{
		if(jNode.isJsonArray())
		{
			JsonArray jVec = jNode.getAsJsonArray();
			boolean hasAlpha = jVec.size() >= 4;
			float a = hasAlpha ? jVec.get(0).getAsFloat() : 0.0f;
			float r = jVec.get(hasAlpha ? 1 : 0).getAsFloat();
			float g = jVec.get(hasAlpha ? 2 : 1).getAsFloat();
			float b = jVec.get(hasAlpha ? 3 : 2).getAsFloat();
			return new ColourDefinition(a, r, g, b);
		}
		return (ColourDefinition)ref;
	}
}
