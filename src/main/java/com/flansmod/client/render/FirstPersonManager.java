package com.flansmod.client.render;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.GunContextCache;
import com.flansmod.common.actions.nodes.AimDownSightAction;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.GunContextPlayer;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;

import java.util.*;

public class FirstPersonManager
{
	// ADS
	public static float ADS_BLEND = 0.0f;
	public static class AdsInstance
	{
		public ItemTransforms.TransformType TransformType;
		public float BlendAmount;
		public EAttachmentType AttachmentType;
		public int AttachmentIndex;
		public boolean IsActive;
	}
	public static HashMap<String, AdsInstance> ADS_INSTANCES = new HashMap<>();


	// I don't like this, but server keeps sending us an updated ItemStack and we're like "guess I don't need to play an anim"
	public static int NUM_LOAD_ANIMS_TO_PLAY = 0;


	public static void LocalPlayerStartReload(int count)
	{
		NUM_LOAD_ANIMS_TO_PLAY = count;
	}

	public static boolean ConsumeLoadOne()
	{
		if(NUM_LOAD_ANIMS_TO_PLAY > 0)
		{
			NUM_LOAD_ANIMS_TO_PLAY--;
			return true;
		}
		return false;
	}

	public static Transform GetWorldSpaceAttachmentTransform(ItemStack stack, ItemTransforms.TransformType transformType, String apName)
	{
		Player player = Minecraft.getInstance().player;
		float dt = Minecraft.getInstance().getPartialTick();
		if(player != null)
		{
			Transform firstPersonPos = GetFirstPersonAttachmentTransform(stack, transformType, apName).ScalePosition(-1f/16f, 1f/16f, -1f/16f);

			firstPersonPos = firstPersonPos.RotateGlobalPitch(player.getViewXRot(dt));
			firstPersonPos = firstPersonPos.RotateGlobalYaw(player.getViewYRot(dt));

			//Transform playerPos = Transform.FromPosAndEuler(
			//	player.getEyePosition().toVector3f(),
			//	new Vector3f(0.0f,
			//				-player.getViewYRot(),
			//				player.getViewXRot(Minecraft.getInstance().getPartialTick())));
			//return firstPersonPos.RightMultiply(playerPos);

			return new Transform(
				new Vector3d(
					player.getEyePosition(dt).x +firstPersonPos.position.x,
					player.getEyePosition(dt).y +firstPersonPos.position.y,
					player.getEyePosition(dt).z +firstPersonPos.position.z),
				firstPersonPos.orientation);
		}
		return Transform.Identity();
	}

	public static Transform GetFirstPersonAttachmentTransform(ItemStack stack, ItemTransforms.TransformType transformType, String apName)
	{
		Transform rootPos = GetFirstPersonRenderPos(stack, transformType);
		FlanItemModelRenderer gunRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(stack);
		if(gunRenderer != null)
		{
			Transform apPos = gunRenderer.GetDefaultTransform(apName);
			apPos.ScalePosition(1f/16f);
			return apPos.RightMultiply(rootPos);
		}
		return rootPos;
	}

	public static Transform GetGunRelativeEyeLine(ItemStack stack, EAttachmentType attachmentType, int attachmentIndex)
	{
		FlanItemModelRenderer gunRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(stack);
		if(gunRenderer != null)
		{
			if(attachmentIndex >= 0)
			{
				GunContext gunContext = GunContextCache.Get(true).Create(stack);
				if(gunContext.IsValid())
				{
					Transform attachTransform = gunRenderer.GetDefaultTransform(attachmentType, attachmentIndex);
					AttachmentDefinition attachDef = gunContext.GetAttachmentDefinition(attachmentType, attachmentIndex);
					if (attachDef.IsValid())
					{
						FlanItemModelRenderer attachmentRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(attachDef);
						Transform eyeLineTransform = attachmentRenderer.GetDefaultTransform("eye_line");
						return attachTransform.RightMultiply(eyeLineTransform);
					}
				}
			}

			return gunRenderer.GetDefaultTransform("eye_line");
		}

		return Transform.Identity();
	}

	public static Transform GetFirstPersonRenderPos(ItemStack stack, ItemTransforms.TransformType transformType)
	{
		FlanItemModelRenderer gunRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(stack);
		if(gunRenderer != null)
		{
			List<Transform> posesAppliedToMe = new ArrayList<>();
			List<Float> weights = new ArrayList<>();
			List<Transform> posesAppliedToOthers = new ArrayList<>();
			for (AdsInstance instance : ADS_INSTANCES.values())
			{
				Transform eyeLine = GetGunRelativeEyeLine(stack, instance.AttachmentType, instance.AttachmentIndex);
				if (instance.TransformType == transformType)
				{
					posesAppliedToMe.add(eyeLine);
					weights.add(instance.BlendAmount);
				}
				else
					posesAppliedToOthers.add(eyeLine);
			}

			float[] weightArray = new float[weights.size()];
			float totalWeight = 0.0f;
			for(int i = 0; i < weights.size(); i++)
			{
				weightArray[i] = weights.get(i);
				totalWeight += weights.get(i);
			}

			// Then blend with the default pose
			Transform defaultPose = Transform.FromItemTransform(gunRenderer.UnbakedRig.GetTransforms(transformType));
			defaultPose = defaultPose.ScalePosition(16f);

			// Blend together all contributing ADS actions - in theory this should only really be one per gun at once
			Transform averageEyeLine = Transform.Interpolate(posesAppliedToMe, weightArray);
			boolean leftHanded = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
			Transform lookDownEyeLineTransform = averageEyeLine.ScalePosition(-1f).RotateGlobal(defaultPose.orientation).Translate(0f, 0f, -8f);
			float blendWeight = Maths.Clamp(totalWeight, 0f, 1f);
			if(posesAppliedToOthers.size() > 0)
			{
				blendWeight *= 0.5f;
				lookDownEyeLineTransform = lookDownEyeLineTransform.RotateLocalRoll(leftHanded ? -30f : 30f);
			}

			return Transform.Interpolate(defaultPose, lookDownEyeLineTransform, blendWeight);

			//Vector3f srcOffset = gunRenderer.UnbakedRig.GetTransforms(transformType).translation;
			//boolean leftHanded = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
			//Vector3d returnToCenter = new Vector3d(0f, -srcOffset.y * 16f, leftHanded ? 8f : -8f);
		}
		return Transform.Identity();
	}

	public static void RenderTick()
	{
		if(Minecraft.getInstance().player != null)
		{
			ShooterContext playerContext = ShooterContext.GetOrCreate(Minecraft.getInstance().player);

			// "Deactivate" all, then reactivate the ones that are still there
			for(AdsInstance instance : ADS_INSTANCES.values())
				instance.IsActive = false;

			for(GunContext gunContext : playerContext.GetAllGunContexts(true))
			{
				if(gunContext.IsValid())
				{
					for (ActionGroupInstance actionGroup : gunContext.GetActionStack().GetActiveActionGroups())
					{
						for (ActionInstance action : actionGroup.GetActions())
						{
							if (action instanceof AimDownSightAction adsAction)
							{
								ItemTransforms.TransformType transformType = actionGroup.Context.Gun instanceof GunContextPlayer gunContextPlayer ? MinecraftHelpers.GetFirstPersonTransformType(gunContextPlayer.GetHand()) : ItemTransforms.TransformType.FIXED;
								String key = transformType + "_" + actionGroup.Context.GroupPath;

								// We don't know about it, must be new
								if(!ADS_INSTANCES.containsKey(key))
								{
									AdsInstance newInst = new AdsInstance();
									newInst.IsActive = true;
									newInst.BlendAmount = 0.0f;
									newInst.TransformType = transformType;
									if(actionGroup.Context.IsAttachment())
									{
										newInst.AttachmentType = actionGroup.Context.GetAttachmentType();
										newInst.AttachmentIndex = actionGroup.Context.GetAttachmentIndex();
									}
									else
									{
										newInst.AttachmentType = EAttachmentType.Generic;
										newInst.AttachmentIndex = -1;
									}
									ADS_INSTANCES.put(key, newInst);
								}
								// Otherwise, keep it active
								else
								{
									ADS_INSTANCES.get(key).IsActive = true;
								}
							}
						}
					}
				}
			}

			List<String> keys = new ArrayList<>(ADS_INSTANCES.keySet());
			for(String key : keys)
			{
				AdsInstance instance = ADS_INSTANCES.get(key);
				if(instance.IsActive)
					instance.BlendAmount = Maths.Lerp(instance.BlendAmount, 1.0f, FlansModClient.FrameDeltaSeconds() * 8.0f);
				else
					instance.BlendAmount = Maths.Lerp(instance.BlendAmount, 0.0f, FlansModClient.FrameDeltaSeconds() * 10.0f);

				if(instance.BlendAmount < 0.01f && !instance.IsActive)
					ADS_INSTANCES.remove(key);
			}



		}
	}
}
