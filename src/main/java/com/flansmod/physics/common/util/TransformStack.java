package com.flansmod.physics.common.util;

import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.physics.common.FlansPhysicsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Consumer;

// Stack is built up from "global" space to "local" space
// For example
// - Empty Stack -> "Global", xyz axes
// - (0) Player Position
// - (1) Player Look Rotation
// - (2) First Person Item Root Offset
// - (3) Body Default Position
// - (4) Body Animation
// - (5) Barrel Attach Position
// - (6) Barrel Animation
//
public class TransformStack
{
	private record Layer(@Nonnull Stack<Transform> transforms, @Nullable String debugInfo)
	{
		@Nonnull public Vec3 localToGlobalDirection(@Nonnull Vec3 localDirection) { return iterateDown(Transform::localToGlobalDirection, localDirection); }
		@Nonnull public Vec3 globalToLocalDirection(@Nonnull Vec3 globalDirection) { return iterateUp(Transform::globalToLocalDirection, globalDirection); }
		@Nonnull public Vec3 localToGlobalPosition(@Nonnull Vec3 localPosition) { return iterateDown(Transform::localToGlobalPosition, localPosition); }
		@Nonnull public Vec3 globalToLocalPosition(@Nonnull Vec3 globalPosition) { return iterateUp(Transform::globalToLocalPosition, globalPosition); }
		@Nonnull public Quaternionf localToGlobalOrientation(@Nonnull Quaternionf localOri) { return iterateDown(Transform::localToGlobalOrientation, localOri); }
		@Nonnull public Quaternionf globalToLocalOrientation(@Nonnull Quaternionf globalOri) { return iterateUp(Transform::globalToLocalOrientation, globalOri); }
		@Nonnull public Transform localToGlobalTransform(@Nonnull Transform localTransform) { return iterateDown(Transform::localToGlobalTransform, localTransform); }
		@Nonnull public Transform globalToLocalTransform(@Nonnull Transform globalTransform) { return iterateUp(Transform::globalToLocalTransform, globalTransform); }

		private <T> T iterateUp(@Nonnull BiFunction<Transform, T, T> func, @Nonnull T target)
		{
			for(int i = 0; i < transforms.size(); i++)
				target = func.apply(transforms.get(i), target);
			return target;
		}
		private void iterateUp(@Nonnull Consumer<Transform> func)
		{
			for(int i = 0; i < transforms.size(); i++)
				func.accept(transforms.get(i));
		}
		private <T> T iterateDown(@Nonnull BiFunction<Transform, T, T> func, @Nonnull T target)
		{
			for(int i = transforms.size() - 1; i >= 0; i--)
				target = func.apply(transforms.get(i), target);
			return target;
		}
		private void iterateDown(@Nonnull Consumer<Transform> func)
		{
			for(int i = transforms.size() - 1; i >= 0; i--)
				func.accept(transforms.get(i));
		}
	}
	private final Stack<Layer> Layers = new Stack<>();

	@Nonnull public Transform top() { return localToGlobalTransform(Transform.IDENTITY); }
	@Nonnull public Vec3 forward() { return localToGlobalDirection(new Vec3(0d, 0d, -1d)); }
	@Nonnull public Vec3 right() { return localToGlobalDirection(new Vec3(1d, 0d, 0d)); }
	@Nonnull public Vec3 up() { return localToGlobalDirection(new Vec3(0d, 1d, 0d)); }

	private TransformStack()
	{
		Layers.push(new Layer(new Stack<>(), null));
	}
	@Nonnull
	public static TransformStack empty() { return new TransformStack(); }
	@Nonnull
	public static TransformStack of() { return new TransformStack(); }
	@Nonnull
	public static TransformStack of(@Nonnull Transform transform) { return new TransformStack().and(transform); }
	@Nonnull
	public static TransformStack of(@Nonnull Transform ... transforms) { return new TransformStack().and(transforms); }

	@Nonnull
	public TransformStack and(@Nonnull Transform transform)
	{
		add(transform);
		return this;
	}
	@Nonnull
	public TransformStack and(@Nonnull Transform ... transforms)
	{
		addAll(transforms);
		return this;
	}

	public void add(@Nonnull Transform transform)
	{
		Layers.peek().transforms.add(transform);
	}
	public void addAll(@Nonnull Transform ... transforms)
	{
		Layers.peek().transforms.addAll(Arrays.asList(transforms));
	}

	public void push()
	{
		Layers.push(new Layer(new Stack<>(), null));
	}
	public void push(@Nonnull String debugInfo)
	{
		Layers.push(new Layer(new Stack<>(), debugInfo));
	}
	public void pop()
	{
		if(Layers.size() > 1)
			Layers.pop();
		else
			FlansPhysicsMod.LOGGER.error("Uneven push/pop in TransformStack");
	}

	@Nonnull public Vec3 localToGlobalDirection(@Nonnull Vec3 localDirection) { return iterateDown(Transform::localToGlobalDirection, localDirection); }
	@Nonnull public Vec3 globalToLocalDirection(@Nonnull Vec3 globalDirection) { return iterateUp(Transform::globalToLocalDirection, globalDirection); }
	@Nonnull public Vec3 localToGlobalPosition(@Nonnull Vec3 localPosition) { return iterateDown(Transform::localToGlobalPosition, localPosition); }
	@Nonnull public Vec3 globalToLocalPosition(@Nonnull Vec3 globalPosition) { return iterateUp(Transform::globalToLocalPosition, globalPosition); }
	@Nonnull public Quaternionf localToGlobalOrientation(@Nonnull Quaternionf localOri) { return iterateDown(Transform::localToGlobalOrientation, localOri); }
	@Nonnull public Quaternionf globalToLocalOrientation(@Nonnull Quaternionf globalOri) { return iterateUp(Transform::globalToLocalOrientation, globalOri); }
	@Nonnull public Transform localToGlobalTransform(@Nonnull Transform localTransform) { return iterateDown(Transform::localToGlobalTransform, localTransform); }
	@Nonnull public Transform globalToLocalTransform(@Nonnull Transform globalTransform) { return iterateUp(Transform::globalToLocalTransform, globalTransform); }

	private <T> T iterateUp(@Nonnull BiFunction<Transform, T, T> func, @Nonnull T target)
	{
		for(int i = 0; i < Layers.size(); i++)
			target = Layers.get(i).iterateUp(func, target);
		return target;
	}
	private void iterateUp(@Nonnull Consumer<Transform> func)
	{
		for(int i = 0; i < Layers.size(); i++)
			Layers.get(i).iterateUp(func);
	}
	private <T> T iterateDown(@Nonnull BiFunction<Transform, T, T> func, @Nonnull T target)
	{
		for(int i = Layers.size() - 1; i >= 0; i--)
			target = Layers.get(i).iterateDown(func, target);
		return target;
	}
	private void iterateDown(@Nonnull Consumer<Transform> func)
	{
		for(int i = Layers.size() - 1; i >= 0; i--)
			Layers.get(i).iterateDown(func);
	}

	public void applyToPoseStack(@Nonnull PoseStack poseStack)
	{
		iterateUp((t) -> {
			poseStack.translate(t.Position.x, t.Position.y, t.Position.z);
			poseStack.mulPose(t.Orientation);
			poseStack.scale(t.Scale.x, t.Scale.y, t.Scale.z);
		});
	}

	public void scale(float x, float y, float z) { add(Transform.fromScale(new Vector3f(x, y, z))); }
	public void translate(double x, double y, double z)
	{
		add(Transform.fromPos(x, y, z));
	}
	public void mulPose(@Nonnull Quaternionf rot)
	{
		add(Transform.fromPosAndQuat(new Vector3d(), rot));
	}

	@OnlyIn(Dist.CLIENT)
	public void debugRender(int ticks)
	{
		Vec3 prevAxesPos = Vec3.ZERO;


		//for(int i = 0; i < size(); i++)
		//{
		//	Transform debug = Transform.IDENTITY;
		//	for(int j = i; j >= 0; j--)
		//	{
		//		debug = get(j).localToGlobalTransform(debug);
		//	}
		//	DebugRenderer.RenderAxes(debug, ticks, new Vector4f());
		//	//DebugRenderer.RenderLine(prevAxesPos, 1, new Vector4f(1f, 1f, 0f, 1f), debug.PositionVec3().subtract(prevAxesPos));
		//	prevAxesPos = debug.positionVec3();
		//}
	}
}
