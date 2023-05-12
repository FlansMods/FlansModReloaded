package com.flansmod.client.render.animation;

import com.flansmod.common.FlansMod;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

import java.util.Map;

public abstract class GunAnimation
{
	public abstract boolean AppliesToPart(Map<String, Float> floatParams, String partName);
	public abstract void Transform(PoseStack poses, Map<String, Float> floatParams, String partName, float animationProgress, float animationDuration);

	protected boolean GetBool(Map<String, Float> floatParams, String paramName)
	{
		return floatParams.containsKey(paramName) && (floatParams.get(paramName) > 0.0f);
	}

	public static class AnimationChain
	{
		private Transform target;
		private float parametricTime = 0.0f;

		public AnimationChain(Transform target, float parametricTime)
		{
			this.target = target;
			this.parametricTime = parametricTime;
		}

		public AnimationChain LerpToTransform(Transform lerpTo, float lerpDuration)
		{
			if(parametricTime >= 0.0f)
			{
				float scaledTime = Maths.Clamp(parametricTime / lerpDuration, 0f, 1f);
				target.position.lerp(lerpTo.position, scaledTime);
				target.orientation.slerp(lerpTo.orientation, scaledTime);
			}
			parametricTime -= lerpDuration;
			return this;
		}

		public AnimationChain LerpToIdentity(float lerpDuration)
		{
			LerpToTransform(Transform.Identity(), lerpDuration);
			return this;
		}

		public AnimationChain LerpToPos(Vector3f pos, float lerpDuration)
		{
			if(parametricTime >= 0.0f)
			{
				float scaledTime = Maths.Clamp(parametricTime / lerpDuration, 0f, 1f);
				target.position.lerp(new Vector3d(pos), scaledTime);
			}
			parametricTime -= lerpDuration;
			return this;
		}

		public AnimationChain Pause(float pauseDuration)
		{
			parametricTime -= pauseDuration;
			return this;
		}

		public void ApplyTo(PoseStack poses)
		{
			poses.translate(target.position.x, target.position.y, target.position.z);
			poses.mulPose(target.orientation);
		}
	}

	public static class SlideSemiAuto extends GunAnimation
	{
		@Override
		public boolean AppliesToPart(Map<String, Float> floatParams, String partName)
		{
			return partName.equals("slide")
			   || (partName.equals("scope") && GetBool(floatParams, "scopeIsOnSlide"));
		}
		@Override
		public void Transform(PoseStack poses, Map<String, Float> floatParams, String partName, float animationProgress, float animationDuration)
		{
			float posZ = 1.0f - (animationProgress / animationDuration);
			if(floatParams.containsKey("slideDistance"))
				posZ *= floatParams.get("slideDistance");
			poses.translate(0f, 0f, posZ);
		}
	}

	public static class PumpAction extends GunAnimation
	{
		@Override
		public boolean AppliesToPart(Map<String, Float> floatParams, String partName)
		{
			return partName.equals("pump")
				|| (partName.equals("grip") && GetBool(floatParams, "gripInOnPump"));
		}
		@Override
		public void Transform(PoseStack poses, Map<String, Float> floatParams, String partName, float animationProgress, float animationDuration)
		{
			// Linear out then in /\
			float posZ = 2.0f * (animationProgress / animationDuration);
			if(posZ > 1.0f)
				posZ = 2.0f - posZ;
			if(floatParams.containsKey("pumpDistance"))
				posZ *= floatParams.get("pumpDistance");
			poses.translate(0f, 0f, posZ);
		}
	}

	public static class Spin extends GunAnimation
	{
		@Override
		public boolean AppliesToPart(Map<String, Float> floatParams, String partName)
		{
			return partName.equals("body");
		}
		@Override
		public void Transform(PoseStack poses, Map<String, Float> floatParams, String partName, float animationProgress, float animationDuration)
		{
			//FlansMod.LOGGER.info("" + animationProgress);

			float parameter = (animationProgress / animationDuration);
			float spinParameter = parameter * (parameter - 0.2f) * (parameter + 0.2f) * (360.0f / (1.2f * 0.8f));

			poses.mulPose(Transform.QuaternionFromEuler(0f, 0f, spinParameter));
		}
	}

	public static class Inspect extends GunAnimation
	{
		@Override
		public boolean AppliesToPart(Map<String, Float> floatParams, String partName)
		{
			return partName.equals("body");
				//|| (partName.equals("grip") && GetBool(floatParams, "gripInOnPump"));
		}
		@Override
		public void Transform(PoseStack poses, Map<String, Float> floatParams, String partName, float animationProgress, float animationDuration)
		{
			float parameter = animationProgress / animationDuration;

			new AnimationChain(Transform.Identity(), parameter)
				.LerpToTransform(new Transform(new Vector3f(-0.5f, -0.5f, 0.0f), Transform.IdentityQuat()), 0.1f)
				.LerpToTransform(new Transform(new Vector3f(0f, 6f, 6f), Transform.QuaternionFromEuler(0f, 90f, 0f)), 0.2f)
				.Pause(0.2f)
				.LerpToTransform(new Transform(new Vector3f(0f, 6f, 6f), Transform.QuaternionFromEuler(0f, -90f, 0f)), 0.2f)
				.LerpToIdentity(0.1f)
				//.LerpToPos(new Vector3f(10.0f, 0.0f, 0.0f), 0.25f)
				//.Pause(0.5f)

				.ApplyTo(poses);
		}
	}

	public static class ReloadBottomClip extends GunAnimation
	{
		@Override
		public boolean AppliesToPart(Map<String, Float> floatParams, String partName)
		{
			return partName.equals("body") || partName.equals("ammo");
			//|| (partName.equals("grip") && GetBool(floatParams, "gripInOnPump"));
		}
		@Override
		public void Transform(PoseStack poses, Map<String, Float> floatParams, String partName, float animationProgress, float animationDuration)
		{
			float parameter = animationProgress / animationDuration;

			new AnimationChain(Transform.Identity(), parameter)
				.LerpToTransform(new Transform(new Vector3f(-0.5f, -0.5f, 0.0f), Transform.IdentityQuat()), 0.1f)
				.LerpToTransform(new Transform(new Vector3f(0f, 6f, 6f), Transform.QuaternionFromEuler(0f, 90f, 0f)), 0.2f)
				.Pause(0.2f)
				.LerpToTransform(new Transform(new Vector3f(0f, 6f, 6f), Transform.QuaternionFromEuler(0f, -90f, 0f)), 0.2f)
				.LerpToIdentity(0.1f)
				//.LerpToPos(new Vector3f(10.0f, 0.0f, 0.0f), 0.25f)
				//.Pause(0.5f)

				.ApplyTo(poses);
		}
	}
}
