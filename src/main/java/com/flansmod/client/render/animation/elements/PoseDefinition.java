package com.flansmod.client.render.animation.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.VecWithOverride;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.Map;

public class PoseDefinition
{
	public static PoseDefinition Identity() { return new PoseDefinition(); }

	@JsonField
	public String applyTo = "";
	@JsonField
	public VecWithOverride position = new VecWithOverride();
	@JsonField
	public VecWithOverride rotation = new VecWithOverride();
	@JsonField
	public Vector3f scale = new Vector3f(1f, 1f, 1f);

	@Nonnull
	public Transform Resolve(@Nonnull Map<String, Float> parameters)
	{
		return Transform.FromPosAndEuler("{\"Pose\":\""+applyTo+"\"}",
			position.ResolveF(parameters).mul(1f/16f),
			rotation.ResolveF(parameters));
	}
}
