package com.flansmod.client.render.debug;

import com.flansmod.common.FlansMod;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.Input;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.checkerframework.checker.units.qual.A;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class DebugModelPoser
{
	public static class Keyframe
	{
		public Transform transform;
		public int numTicks;

		public Keyframe(Transform t, int n)
		{
			transform = t;
			numTicks = n;
		}
	}

	public static boolean active = true;
	public static Quaternionf rotation = new Quaternionf();
	public static Vector3f eulerAngles = new Vector3f();
	public static Vector3f translation = new Vector3f();

	public static List<Keyframe> keyframes = new ArrayList<>();
	public static int editingKeyframe = 0;

	public static boolean isPlaying = false;
	public static long startedOnFrame = 0l;

	private static Axis Forward = new Axis(InputConstants.KEY_UP, InputConstants.KEY_DOWN);
	private static Axis Right = new Axis(InputConstants.KEY_RIGHT, InputConstants.KEY_LEFT);
	private static Axis Up = new Axis(InputConstants.KEY_RSHIFT, InputConstants.KEY_RCONTROL);

	private static Axis Yaw = new Axis(InputConstants.KEY_DELETE, InputConstants.KEY_PAGEDOWN);
	private static Axis Pitch = new Axis(InputConstants.KEY_HOME, InputConstants.KEY_END);
	private static Axis Roll = new Axis(InputConstants.KEY_PAGEUP, InputConstants.KEY_INSERT);



	private static Axis SelectKeyframe = new Axis(InputConstants.KEY_EQUALS, InputConstants.KEY_MINUS);
	private static Axis CreateKeyframe = new Axis(InputConstants.KEY_K, 0);
	private static Axis SwapKeyframe = new Axis(InputConstants.KEY_L, 0);
	private static Axis DeleteKeyframe = new Axis(InputConstants.KEY_BACKSPACE, 0);

	private static Axis Reset = new Axis(InputConstants.KEY_COMMA, 0);
	private static Axis ToggleActive = new Axis(InputConstants.KEY_PERIOD, 0);
	private static Axis PlayPause = new Axis(InputConstants.KEY_P, InputConstants.KEY_O);

	public void Init()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static Transform GetDebugRenderPose()
	{
		long time = Minecraft.getInstance().level.getGameTime();
		long delta = time - startedOnFrame;
		if(delta < 1000000)
		{
			float partial = Minecraft.getInstance().getPartialTick();

			float animProgress = partial + (float) delta;

			for(int i = 0; i < keyframes.size(); i++)
			{
				Keyframe currentFrame = keyframes.get(i);
				if(animProgress > currentFrame.numTicks)
				{
					animProgress -= currentFrame.numTicks;
				}
				else // We are between this frame and the next
				{
					if(i + 1 < keyframes.size())
					{
						Keyframe nextFrame = keyframes.get(i+1);

						return Transform.Interpolate(currentFrame.transform, nextFrame.transform, animProgress / currentFrame.numTicks);
					}
					break;
				}
			}
		}

		return new Transform(translation, rotation);
	}

	@SubscribeEvent
	public void OnRenderGUI(RenderGuiOverlayEvent event)
	{
		if(active)
		{
			Font font = Minecraft.getInstance().font;
			if (event.getOverlay().id().getPath().equals("crosshair"))
			{
				int spacing = 8;
				int cursorY = 4;
				int cursorX = 4;

				font.draw(event.getPoseStack(), "Animation Pose Tool", cursorX, cursorY, -1);
				cursorY += spacing;

				font.draw(event.getPoseStack(), "Position: " + translation.x + ", " + translation.y + ", " + translation.z, cursorX, cursorY += spacing, -1);
				font.draw(event.getPoseStack(), "Euler   : " + eulerAngles.x + ", " + eulerAngles.y + ", " + eulerAngles.z, cursorX, cursorY += spacing, -1);

				cursorY += spacing;
				font.draw(event.getPoseStack(), "Keyframes [" + keyframes.size() + "] [+/-] to select, [Backspace] to delete", cursorX, cursorY += spacing, -1);
				for (int i = 0; i < keyframes.size() + 1; i++)
				{
					if (i < keyframes.size())
					{
						if (editingKeyframe == i)
							font.draw(event.getPoseStack(), ">> " + i + " | " + keyframes.get(i).numTicks, cursorX, cursorY += spacing, 0x00ff00);
						else
							font.draw(event.getPoseStack(), "#  " + i + " | " + keyframes.get(i).numTicks, cursorX, cursorY += spacing, -1);
					} else
					{
						font.draw(event.getPoseStack(), "-- Press [K] to make a snapshot or [L] to overwrite --", cursorX, cursorY += spacing, -1);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void OnRenderLast(RenderLevelStageEvent event)
	{
		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)
		{
			ToggleActive.Update();
			if(ToggleActive.Pressed())
				active = !active;


			if(active)
			{
				Forward.Update();
				Right.Update();
				Up.Update();
				Yaw.Update();
				Pitch.Update();
				Roll.Update();
				SelectKeyframe.Update();
				CreateKeyframe.Update();
				DeleteKeyframe.Update();
				Reset.Update();
				SwapKeyframe.Update();
				PlayPause.Update();

				if (SelectKeyframe.GetMotion() != 0f)
				{
					if (SelectKeyframe.GetMotion() > 0f)
						editingKeyframe++;
					if (SelectKeyframe.GetMotion() < 0f)
						editingKeyframe--;

					if (editingKeyframe > keyframes.size())
						editingKeyframe = 0;
					if (editingKeyframe < 0)
						editingKeyframe = keyframes.size();
				}

				if (PlayPause.GetMotion() > 0.0f)
				{
					startedOnFrame = Minecraft.getInstance().level.getGameTime();
					isPlaying = true;
				}


				if (Reset.Pressed())
				{
					eulerAngles = new Vector3f(0f, 90f, 0f);
					translation = new Vector3f(0f, -8f, -8f);
				}

				float forward = Forward.GetMotion();
				float right = Right.GetMotion();
				float up = Up.GetMotion();

				float yaw = 5f * Yaw.GetMotion();
				float pitch = 5f * Pitch.GetMotion();
				float roll = 5f * Roll.GetMotion();

				if (CheckKey(InputConstants.KEY_LSHIFT))
				{
					forward *= 0.25f;
					right *= 0.25f;
					up *= 0.25f;
				}
				if (CheckKey(InputConstants.KEY_SEMICOLON))
				{
					Minecraft.getInstance().keyboardHandler.setClipboard("hi");
				}

				translation.add(right, up, -forward);
				eulerAngles.add(roll, yaw, pitch);
				//rotation.rotateXYZ(roll * Maths.DegToRadF, yaw * Maths.DegToRadF, pitch * Maths.DegToRadF);

				rotation = new Quaternionf().rotateXYZ(eulerAngles.x * Maths.DegToRadF,
					eulerAngles.y * Maths.DegToRadF,
					eulerAngles.z * Maths.DegToRadF);

				if (CreateKeyframe.Pressed())
				{
					keyframes.add(new Keyframe(new Transform(translation, rotation), 10));
					editingKeyframe = keyframes.size();
				} else if (SwapKeyframe.Pressed())
				{
					if (editingKeyframe < keyframes.size())
					{
						keyframes.set(editingKeyframe, new Keyframe(new Transform(translation, rotation), 10));
					}
				}
				if (DeleteKeyframe.Pressed())
				{
					if (editingKeyframe < keyframes.size())
					{
						keyframes.remove(editingKeyframe);
						editingKeyframe = keyframes.size();
					}
				}
			}
		}
	}

	private static class Axis
	{
		private final int positive;
		private final int negative;

		private float valueThisFrame = 0.0f;
		private float valueLastFrame = 0.0f;
		private float timeSpentPressed = 0.0f;

		public Axis(int pos, int neg)
		{
			positive = pos;
			negative = neg;
		}

		public boolean Pressed()
		{
			return valueThisFrame != 0.0f && valueLastFrame == 0.0f;
		}

		public float GetMotion()
		{
			if(valueLastFrame == valueThisFrame)
				return 0.0f;
			else
				return valueThisFrame;
		}

		public void Update()
		{
			valueLastFrame = valueThisFrame;
			valueThisFrame = CheckAxis(positive, negative);
		}
	}

	private static float CheckAxis(int pos, int neg)
	{
		float value = 0.0f;
		if(pos != 0)
			value += (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), pos) ? 1f : 0f);
		if(neg != 0)
			value += (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), neg) ? -1f : 0f);
		return value;
	}

	private static boolean CheckKey(int key)
	{
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
	}
}
