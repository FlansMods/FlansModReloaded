package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ColourDefinition
{
	@JsonField
	public float alpha = 1.0f;
	@JsonField
	public float red = 1.0f;
	@JsonField
	public float green = 1.0f;
	@JsonField
	public float blue = 1.0f;
}
