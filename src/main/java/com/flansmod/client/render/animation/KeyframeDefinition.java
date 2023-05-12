package com.flansmod.client.render.animation;


import com.flansmod.common.types.JsonField;
import org.joml.Vector3f;

public class KeyframeDefinition
{
	@JsonField
	public String name = "";
	@JsonField
	public PoseDefinition[] poses = new PoseDefinition[0];
}
