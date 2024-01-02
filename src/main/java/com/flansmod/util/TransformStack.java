package com.flansmod.util;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.common.FlansMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

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
public class TransformStack extends Stack<Transform>
{
	public final Stack<Integer> Pushes = new Stack<>();

	@Nonnull public Transform Top() { return LocalToGlobalTransform(Transform.IDENTITY); }
	@Nonnull public Vec3 Forward() { return LocalToGlobalDirection(new Vec3(0d, 0d, -1d)); }
	@Nonnull public Vec3 Right() { return LocalToGlobalDirection(new Vec3(1d, 0d, 0d)); }
	@Nonnull public Vec3 Up() { return LocalToGlobalDirection(new Vec3(0d, 1d, 0d)); }

	public TransformStack() {
		super();
	}
	public TransformStack(Transform initial)
	{
		super();
		add(initial);
	}

	public static TransformStack of()
	{
		return new TransformStack();
	}
	public static TransformStack of(Transform transform)
	{
		return new TransformStack(transform);
	}
	public static TransformStack of(Transform ... transforms)
	{
		TransformStack stack = new TransformStack();
		stack.addAll(Arrays.asList(transforms));
		return stack;
	}
	public TransformStack andThen(Transform transform)
	{
		add(transform);
		return this;
	}

	public void PushSaveState()
	{
		Pushes.add(size());
	}
	public void PopSaveState()
	{
		if(!Pushes.empty())
		{
			int savedState = Pushes.pop();
			while(size() > savedState)
				pop();
		}
		else
			FlansMod.LOGGER.error("Uneven push/pop in TransformStack");
	}

	@Nonnull
	public Vec3 LocalToGlobalDirection(@Nonnull Vec3 localDirection)
	{
		for(int i = size() - 1; i >= 0; i--)
			localDirection = get(i).LocalToGlobalDirection(localDirection);
		return localDirection;
	}
	@Nonnull
	public Vec3 GlobalToLocalDirection(@Nonnull Vec3 globalDirection)
	{
		for(int i = 0; i < size(); i++)
			globalDirection = get(i).GlobalToLocalDirection(globalDirection);
		return globalDirection;
	}
	@Nonnull
	public Vec3 LocalToGlobalPosition(@Nonnull Vec3 localPosition)
	{
		for(int i = size() - 1; i >= 0; i--)
			localPosition = get(i).LocalToGlobalPosition(localPosition);
		return localPosition;
	}
	@Nonnull
	public Vec3 GlobalToLocalPosition(@Nonnull Vec3 globalPosition)
	{
		for(int i = 0; i < size(); i++)
			globalPosition = get(i).GlobalToLocalPosition(globalPosition);
		return globalPosition;
	}
	@Nonnull
	public Quaternionf LocalToGlobalOrientation(@Nonnull Quaternionf localOri)
	{
		for(int i = size() - 1; i >= 0; i--)
			localOri = get(i).LocalToGlobalOrientation(localOri);
		return localOri;
	}
	@Nonnull
	public Quaternionf GlobalToLocalOrientation(@Nonnull Quaternionf globalOri)
	{
		for(int i = 0; i < size(); i++)
			globalOri = get(i).GlobalToLocalOrientation(globalOri);
		return globalOri;
	}
	@Nonnull
	public Transform LocalToGlobalTransform(@Nonnull Transform localTransform)
	{
		for(int i = size() - 1; i >= 0; i--)
			localTransform = get(i).LocalToGlobalTransform(localTransform);
		return localTransform;
	}
	@Nonnull
	public Transform GlobalToLocalTransform(@Nonnull Transform globalTransform)
	{

		for(int i = 0; i < size(); i++)
			globalTransform = get(i).GlobalToLocalTransform(globalTransform);
		return globalTransform;
	}

	public void ApplyToPoseStack(@Nonnull PoseStack poseStack)
	{
		for(int i = 0; i < size(); i++)
		{
			Transform t = get(i);
			poseStack.translate(t.Position.x, t.Position.y, t.Position.z);
			poseStack.mulPose(t.Orientation);
			poseStack.scale(t.Scale, t.Scale, t.Scale);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void DebugRender(int ticks)
	{
		Vec3 prevAxesPos = Vec3.ZERO;
		for(int i = 0; i < size(); i++)
		{
			Transform debug = Transform.IDENTITY;
			for(int j = i; j >= 0; j--)
			{
				debug = get(j).LocalToGlobalTransform(debug);
			}
			DebugRenderer.RenderAxes(debug, ticks, new Vector4f());
			DebugRenderer.RenderLine(prevAxesPos, 1, new Vector4f(1f, 1f, 0f, 1f), debug.PositionVec3().subtract(prevAxesPos));
			prevAxesPos = debug.PositionVec3();
		}
	}
}
