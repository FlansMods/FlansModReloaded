package com.flansmod.client.render;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.animation.FlanimationDefinition;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.actions.nodes.AimDownSightAction;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
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
		if (NUM_LOAD_ANIMS_TO_PLAY > 0)
		{
			NUM_LOAD_ANIMS_TO_PLAY--;
			return true;
		}
		return false;
	}


	public static Transform GetModelSpaceAPTransform(@Nonnull GunContext gunContext,
													 @Nonnull ItemTransforms.TransformType transformType,
													 @Nonnull String apName)
	{
		TransformStack transformStack = new TransformStack();
		ApplyModelToAP(transformStack, gunContext, apName, true);
		return transformStack.Top();
	}

	public static Transform GetWorldSpaceAPTransform(@Nonnull GunContext gunContext,
													 @Nonnull ItemTransforms.TransformType transformType,
													 @Nonnull String apName)
	{
		TransformStack transformStack = new TransformStack();
		ApplyWorldToRoot(transformStack, gunContext, transformType);
		ApplyRootToModel(transformStack, gunContext, transformType);

		//transformStack.add(new Transform("ScaleToModelSpace", 1f/16f));
		ApplyModelToAP(transformStack, gunContext, apName, true);

		// Hack: APs with default orientation [0,0,0] on a default posed gun [0,90,0] are actually pointing sideways...
		// TODO: Go clean this up in the export process
		transformStack.add(new Transform("RotateFix[TODO]", Transform.FromEuler(0f, -90f, 0f)));


		//transformStack.DebugRender(1);

		return transformStack.Top();
	}

	public static Transform GetWorldSpaceRootTransform(@Nonnull GunContext gunContext,
											     	   @Nonnull ItemTransforms.TransformType transformType)
	{
		TransformStack transformStack = new TransformStack();
		ApplyWorldToRoot(transformStack, gunContext, transformType);
		return transformStack.Top();
	}

	public static Transform GetRootSpaceModelTransform(@Nonnull GunContext gunContext,
													   @Nonnull ItemTransforms.TransformType transformType)
	{
		TransformStack transformStack = new TransformStack();
		ApplyRootToModel(transformStack, gunContext, transformType);
		return transformStack.Top();
	}




	// This takes us from the Minecraft world coords to the Minecraft "Model Root", which might be
	// - the bottom left of an item frame (sort of)
	// - the third person entity arm (sort of)
	// - first person 0,0 (sort of)
	public static void ApplyWorldToRoot(@Nonnull TransformStack transformStack,
										@Nonnull GunContext gunContext,
										@Nonnull ItemTransforms.TransformType transformType)
	{
		float dt = Minecraft.getInstance().getPartialTick();
		switch (transformType)
		{
			case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
				Player player = Minecraft.getInstance().player;
				if(player != null)
				{
					// World space, so we want to put this just in front of the player
					// This is like shooterContext.GetShootOrigin, just with dt passed in
					transformStack.add(new Transform("PlayerEye", player.getEyePosition(dt)));
					transformStack.add(new Transform("PlayerLook", Transform.FromEuler(-player.getViewXRot(dt), 180f - player.getViewYRot(dt), 0f)));

					Vanilla_ApplyHurtBobbing(transformStack, dt);
					Vanilla_ApplyWalkBobbing(transformStack, dt);
					Vanilla_ApplyViewBobbing(transformStack, dt);
					Vanilla_ApplyHandAnimation(transformStack, transformType, dt);

					transformStack.add(new Transform("ItemRenderer.render", new Vec3(-0.5d, -0.5d, -0.5d)));
				}
				else transformStack.add(Transform.Error("Trying to render first person with no current player"));
			}
			default -> {
				transformStack.add(gunContext.GetShootOrigin());
			}
		}
	}

	private static final Transform MC_FIRST_PERSON_OFFSET_RIGHT = new Transform("MC 1st person R", new Vec3(-0.06d, 0.45d, 0.5d));
	private static final Transform MC_FIRST_PERSON_OFFSET_LEFT = new Transform("MC 1st person L", new Vec3(1.06d, 0.45d, 0.5d));
	private static final Transform MC_THIRD_PERSON_OFFSET = new Transform("MC 3rd person", new Vec3(0.5d, 0.5d, 0.5d), Transform.FromEuler(0f, 90f, 0f));
	private static final Transform MC_GROUND_OFFSET = new Transform("MC ground", new Vec3(0.5d, 0.5d, 0.5d));

	// This takes us from the Minecraft "Model Root" to the "body" piece of this model
	public static void ApplyRootToModel(@Nonnull TransformStack transformStack,
										@Nonnull GunContext gunContext,
										@Nonnull ItemTransforms.TransformType transformType)
	{
		float dt = Minecraft.getInstance().getPartialTick();
		FlanItemModelRenderer gunRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(gunContext.Stack);
		if(gunRenderer != null)
		{
			Transform defaultPose = new Transform(gunRenderer.BakedRig.GetTransform(transformType));
			switch(transformType)
			{
				case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
					if(transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
						transformStack.add(MC_FIRST_PERSON_OFFSET_RIGHT);
					else
						transformStack.add(MC_FIRST_PERSON_OFFSET_LEFT);
					// Special case first person, we need to blend with the ADS position
					Transform adsBlendTransform = CalculateADSPosition(defaultPose, gunContext, transformType);
					transformStack.add(adsBlendTransform);
				}
				case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
					transformStack.add(MC_THIRD_PERSON_OFFSET);
					transformStack.add(defaultPose);
				}
				case GROUND -> {
					transformStack.add(MC_GROUND_OFFSET);
					transformStack.add(defaultPose);
				}
				default -> {
					transformStack.add(defaultPose);
				}
			}
		}
		else transformStack.add(Transform.Error("Could not find gun renderer for " + gunContext));

	}


	public static void ApplyItemTransforms(@Nonnull TransformStack transformStack, @Nonnull GunContext gunContext)
	{
		FlanItemModelRenderer gunRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(gunContext.Stack);
		if(gunRenderer != null)
		{

		}
	}

	public static void ApplyModelToAP(@Nonnull TransformStack transformStack,
									  @Nonnull GunContext gunContext,
									  @Nonnull String apName,
									  boolean animated)
	{
		FlanItemModelRenderer gunRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(gunContext.Stack);
		if(gunRenderer != null)
		{
			FlanimationDefinition animationSet = animated ? FlansModClient.ANIMATIONS.Get(new ResourceLocation(gunContext.Def.animationSet)) : null;
			ActionStack actionStack = animated ? gunContext.GetActionStack() : null;

			EAttachmentType attachmentType = ActionGroupContext.GetAttachmentType(apName);
			int attachmentIndex = ActionGroupContext.GetAttachmentIndex(apName);
			String gunAPName = gunRenderer.GetAPKey(attachmentType, attachmentIndex);
			String childAPName = ActionGroupContext.GetActionGroupKey(apName);

			if(attachmentIndex == -1)
				gunRenderer.ApplyAPOffsetInternal(transformStack, apName, animationSet, actionStack);
			else
			{
				// First layer is finding the offset of this attachment's AP within the gun
				gunRenderer.ApplyAPOffsetInternal(transformStack, gunAPName, animationSet, actionStack);

				// Second layer is where the AP is within the attachment model
				AttachmentDefinition attachDef = gunContext.GetAttachmentDefinition(attachmentType, attachmentIndex);
				if (attachDef.IsValid())
				{
					FlanItemModelRenderer attachmentRenderer = FlansModClient.MODEL_REGISTRATION.GetModelRenderer(attachDef);
					if (attachmentRenderer != null)
					{
						attachmentRenderer.ApplyAPOffsetInternal(transformStack, childAPName, null, null);
					}
					else transformStack.add(Transform.Error("Could not find attachment renderer for " + attachDef + " at '" + attachDef + "'"));
				}
				else transformStack.add(Transform.Error("There is no attachment at path " + attachmentType + "/" + attachmentIndex));
			}
		}
		else transformStack.add(Transform.Error("Could not find gun renderer for " + gunContext.Def));
	}

	private static void Vanilla_ApplyViewBobbing(@Nonnull TransformStack transformStack, float dt)
	{
		if (Minecraft.getInstance().getCameraEntity() instanceof LocalPlayer playerCamera)
		{
			float f = playerCamera.getAttackAnim(dt);
			float f1 = Mth.lerp(dt, playerCamera.xRotO, playerCamera.getXRot());
			float f2 = Mth.lerp(dt, playerCamera.xBobO, playerCamera.xBob);
			float f3 = Mth.lerp(dt, playerCamera.yBobO, playerCamera.yBob);
			float xAngle = (playerCamera.getViewXRot(dt) - f2) * 0.1F;
			float yAngle = (playerCamera.getViewYRot(dt) - f3) * 0.1F;
			transformStack.add(new Transform("ItemInHandRenderer.renderHandsWithItems", Transform.FromEuler(new Vector3f(xAngle, yAngle, 0f))));
		}
	}

	private static void Vanilla_ApplyHurtBobbing(@Nonnull TransformStack transformStack, float dt)
	{
		if (Minecraft.getInstance().getCameraEntity() instanceof LivingEntity livingCamera)
		{
			float f = (float)livingCamera.hurtTime - dt;
			if (livingCamera.isDeadOrDying()) {
				float f1 = Math.min((float)livingCamera.deathTime + dt, 20.0F);
				transformStack.add(new Transform("GameRenderer.bobHurt["+f+"]", Transform.FromEuler(0f, 0f, 40.0F - 8000.0F / (f1 + 200.0F))));
			}

			if (f < 0.0F) {
				return;
			}

			f /= (float)livingCamera.hurtDuration;
			f = Mth.sin(f * f * f * f * (float)Math.PI);
			float f2 = livingCamera.hurtDir;
			transformStack.add(new Transform("GameRenderer.bobHurt["+f+"]",Transform.FromEuler(0f, -f2, 0f)));
			transformStack.add(new Transform("GameRenderer.bobHurt["+f+"]",Transform.FromEuler(0f, 0f, -f * 14.0F)));
			transformStack.add(new Transform("GameRenderer.bobHurt["+f+"]",Transform.FromEuler(0f, f2, 0f)));
		}
	}

	private static void Vanilla_ApplyWalkBobbing(@Nonnull TransformStack transformStack, float dt)
	{
		if(Minecraft.getInstance().options.bobView().get() && Minecraft.getInstance().getCameraEntity() instanceof Player player)
		{
			float f = player.walkDist - player.walkDistO;
			float f1 = -(player.walkDist + f * dt);
			float f2 = Mth.lerp(dt, player.oBob, player.bob);
			transformStack.add(new Transform("GameRenderer.bobView["+f2+"]",
				new Vec3(
					Mth.sin(f1 * (float) Math.PI) * f2 * 0.5F,
					-Math.abs(Mth.cos(f1 * (float) Math.PI) * f2),
					0.0F),
				Transform.FromEuler(
					Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F,
					0f,
					Mth.sin(f1 * (float) Math.PI) * f2 * 3.0F)));
		}

	}

	private static void Vanilla_ApplyHandAnimation(@Nonnull TransformStack transformStack, ItemTransforms.TransformType transformType, float dt)
	{
		boolean rightHand = transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
		int i = rightHand ? 1 : -1;

		// ItemInHandRenderer::renderArmWithItem
		float handRaise = (1.0F - FlansModClient.GetHandHeight(MinecraftHelpers.GetHand(transformType), dt));

		float f5 = -0.4F * Mth.sin(Mth.sqrt(handRaise) * (float) Math.PI);
		float f6 = 0.2F * Mth.sin(Mth.sqrt(handRaise) * ((float) Math.PI * 2F));
		float f10 = -0.2F * Mth.sin(handRaise * (float) Math.PI);
		transformStack.add(new Transform("ItemInHandRenderer.renderArmWithItem["+handRaise+"]", new Vec3(i * f5, f6, f10)));

		// ItemInHandRenderer::applyItemArmTransform
		transformStack.add(new Transform("ItemInHandRenderer.applyItemArmTransform["+handRaise+"]", new Vec3(i * 0.56F, -0.52F + handRaise * -0.6F, -0.72F)));

		// ItemInHandRenderer::applyItemArmAttackTransform
		{
			float f = Mth.sin(handRaise * handRaise * (float)Math.PI);
			transformStack.add(new Transform("ItemInHandRenderer.applyItemArmAttackTransform["+handRaise+"]", Transform.FromEuler(0f, i * (45.0F + f * -20.0F), 0f)));
			float f1 = Mth.sin(Mth.sqrt(handRaise) * (float)Math.PI);
			transformStack.add(new Transform("ItemInHandRenderer.applyItemArmAttackTransform["+handRaise+"]", Transform.FromEuler(f1 * -80.0F, i * -45.0F, i * f1 * -20.0F)));
		}
	}

	// Calculate in world coords
	private static Transform CalculateADSPosition(@Nonnull Transform defaultPose,
												  @Nonnull GunContext gunContext,
												  @Nonnull ItemTransforms.TransformType transformType)
	{
		List<Transform> posesAppliedToMe = new ArrayList<>();
		List<Float> weights = new ArrayList<>();
		List<Transform> posesAppliedToOthers = new ArrayList<>();
		for (AdsInstance instance : ADS_INSTANCES.values())
		{
			String eyeLinePath = instance.AttachmentIndex >= 0
				? ActionGroupContext.CreateGroupPath(instance.AttachmentType, instance.AttachmentIndex, "eye_line")
				: "eye_line";
			Transform eyeLine = GetModelSpaceAPTransform(gunContext, transformType, eyeLinePath);
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

		// Blend together all contributing ADS actions - in theory this should only really be one per gun at once
		TransformStack eyeLineStack = new TransformStack();
		Transform eyeLinePos = Transform.Interpolate(posesAppliedToMe, weightArray);

		float blendWeight = Maths.Clamp(totalWeight, 0f, 1f);
		if(posesAppliedToOthers.size() > 0)
		{
			blendWeight *= 0.5f;
			boolean leftHanded = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
			eyeLineStack.add(new Transform(leftHanded ? "DualWieldTiltLeft" : "DualWieldTiltRight", Transform.FromEuler(0f, 0f, leftHanded ? -30f : 30f)));
		}

		// Push it forwards, so there's some gap between the "eye" and the "eye_line" AP
		eyeLineStack.add(new Transform("EyeToEyeLineGap", new Vec3(0f, 0f, -0.5f)));
		// Hmm, use default orientation
		eyeLineStack.add(new Transform("EyeLineOri", defaultPose.Orientation));
		// Then, offset by the "eye_line" in world space
		eyeLineStack.add(new Transform("EyeLinePos", eyeLinePos.Position.mul(-1d/16d, new Vector3d())));

		Transform lookDownEyeLineTransform = eyeLineStack.Top();

		return Transform.Interpolate(defaultPose, lookDownEyeLineTransform, blendWeight);
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
								String key = transformType + "/" + actionGroup.Context.GroupPath;

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
					instance.BlendAmount = Maths.LerpF(instance.BlendAmount, 1.0f, FlansModClient.FrameDeltaSeconds() * 8.0f);
				else
					instance.BlendAmount = Maths.LerpF(instance.BlendAmount, 0.0f, FlansModClient.FrameDeltaSeconds() * 10.0f);

				if(instance.BlendAmount < 0.01f && !instance.IsActive)
					ADS_INSTANCES.remove(key);
			}



		}
	}
}
