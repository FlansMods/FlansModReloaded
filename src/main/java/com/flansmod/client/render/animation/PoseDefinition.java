package com.flansmod.client.render.animation;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.VecWithOverride;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

public class PoseDefinition
{
	public static final PoseDefinition Identity() { return new PoseDefinition(); }

	@JsonField
	public String applyTo = "";
	@JsonField
	public VecWithOverride position = new VecWithOverride();
	@JsonField
	public VecWithOverride rotation = new VecWithOverride();
	@JsonField
	public Vector3f scale = new Vector3f(1f, 1f, 1f);

	public static Vector3f LerpPosition(Map<String, Float> parameters, PoseDefinition a, PoseDefinition b, float t)
	{
		t = Maths.Clamp(t, 0f, 1f);
		return Maths.Lerp(a.position.ResolveF(parameters), b.position.ResolveF(parameters), t);
	}

	public static Quaternionf LerpRotation(Map<String, Float> parameters, PoseDefinition a, PoseDefinition b, float t)
	{
		t = Maths.Clamp(t, 0f, 1f);
		Quaternionf quatA = Maths.QuaternionFromEuler(a.rotation.ResolveF(parameters));
		Quaternionf quatB = Maths.QuaternionFromEuler(b.rotation.ResolveF(parameters));
		return Maths.Slerp(quatA, quatB, t);
	}
}
