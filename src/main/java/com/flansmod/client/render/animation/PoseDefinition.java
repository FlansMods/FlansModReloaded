package com.flansmod.client.render.animation;

import com.flansmod.common.types.JsonField;
import org.joml.Vector3f;

public class PoseDefinition
{
	@JsonField
	public String applyTo = "";
	@JsonField
	public Vector3f position = new Vector3f();
	@JsonField
	public Vector3f rotation = new Vector3f();
	@JsonField
	public Vector3f scale = new Vector3f(1f, 1f, 1f);
}
