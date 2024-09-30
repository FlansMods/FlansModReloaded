package com.flansmod.client.render;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.animation.FlanimationDefinition;
import com.flansmod.client.render.guns.AttachmentItemRenderer;
import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.models.ITurboRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.actions.nodes.AimDownSightAction;
import com.flansmod.common.gunshots.PlayerSnapshot;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import javax.annotation.Nonnull;
import java.lang.Math;
import java.util.*;

public class FirstPersonManager
{
	// ADS
	public static float ADS_BLEND = 0.0f;

	public static class AdsInstance
	{
		public ItemDisplayContext TransformType;
		public float BlendAmount;
		public EAttachmentType AttachmentType;
		public int AttachmentIndex;
		public boolean IsActive;
		public String EyeLineName;
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

	@Nonnull
	public static Transform GetModelSpaceAPTransform(@Nonnull GunContext gunContext,
													 @Nonnull ItemDisplayContext transformType,
													 @Nonnull String apName)
	{
		TransformStack transformStack = new TransformStack();
		ApplyModelToAP(transformStack, gunContext, apName, true);
		return transformStack.Top();
	}

	@Nonnull
	public static Transform GetWorldSpaceAPTransform(@Nonnull GunContext gunContext,
													 @Nonnull ItemDisplayContext transformType,
													 @Nonnull String apName)
	{
		float dt = Minecraft.getInstance().getPartialTick();
		TransformStack transformStack = new TransformStack();

		switch(transformType)
		{
			case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND ->
			{
				// From the world origin to the "eye" is all in world space coords
				ApplyWorldToEye(transformStack, gunContext, transformType);

				// Everything from this point onwards is in screen space, and needs perspective applied
				TransformStack inScreenSpace = new TransformStack();
				ApplyEyeToRoot(inScreenSpace, gunContext, transformType);

				ApplyRootToModel(inScreenSpace, gunContext, transformType);
				ApplyModelToAP(inScreenSpace, gunContext, apName, true);

				Transform eyeToAP = inScreenSpace.Top();
				Vec3 eyePos = eyeToAP.PositionVec3();

				// Project out of First Person space, and into World space
				Matrix4f firstPersonProjection = GetFirstPersonProjection();
				Matrix4f levelProjectionInv = GetLevelProjection().invert();

				Vec3 pos = ReprojectVector(eyePos, firstPersonProjection, levelProjectionInv);
				Vec3 fwd = ReprojectVector(eyeToAP.ForwardVec3().add(eyePos), firstPersonProjection, levelProjectionInv).subtract(pos).normalize();
				Vec3 up  = ReprojectVector(eyeToAP.UpVec3().add(eyePos), firstPersonProjection, levelProjectionInv).subtract(pos).normalize();

				transformStack.add(Transform.FromPositionAndLookDirection(
					pos, fwd, up,
					() -> "{\"EyeToAP\":"+eyeToAP.DebugInfo+",\"FromProjection\":\""+firstPersonProjection+"\",\"ToProjectionInv\":\""+levelProjectionInv+"\"}"));

				//Transform projectionFOV = Project(eyeToAP, fov, false);
				//transformStack.add(projectionFOV);
				//transformStack.add(new Transform(
				//	"\"FOV Fudge\"",
				//	projection70.Position.add(projectionFOV.Position),
				//	projection70.Orientation.mul(projectionFOV.Orientation)));
				//if(!Maths.Approx(Transform.ToEuler(transformStack.get(2).Orientation).y, 0f, 0.1f))
				//{
				//	FlansMod.LOGGER.info("Angle:" + Transform.ToEuler(transformStack.get(2).Orientation).y);
				//}

				//transformStack.DebugRender(1);
			}
			case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND ->
			{
				if(gunContext.GetShooter().Entity() instanceof Player player)
				{
					Level level = Minecraft.getInstance().level;
					if(level != null)
					{
						Raytracer raytracer = Raytracer.ForLevel(Minecraft.getInstance().level);
						PlayerSnapshot currentSnap = raytracer.GetSnapshot(player, 0);
						PlayerSnapshot prevSnap = raytracer.GetSnapshot(player, 1);
						if(currentSnap.valid && prevSnap.valid)
						{
							Transform currentArmRoot = currentSnap.GetArmTransform(MinecraftHelpers.GetArm(transformType));
							Transform previousArmRoot = prevSnap.GetArmTransform(MinecraftHelpers.GetArm(transformType));

							transformStack.push(Transform.Interpolate(previousArmRoot, currentArmRoot, dt));
							// 3.0F, 12.0F, 4.0F
							transformStack.push(Transform.FromEuler(-90f, 0f, 180f));
							transformStack.push(Transform.FromPos(-0.5d/16d, 2d/16d, -6d/16d));
							transformStack.push(Transform.FromPos(-0.5d, -0.5d, -0.5d));
							//transformStack.push(Transform.FromPos(0d, 0d, -0.75d));
							ApplyRootToModel(transformStack, gunContext, transformType);
							ApplyModelToAP(transformStack, gunContext, apName, true);
						}
					}
				}
			}
		}

		return transformStack.Top();
	}

	private static @Nonnull Vec3 ReprojectVector(@Nonnull Vec3 src, @Nonnull Matrix4f from, @Nonnull Matrix4f toInverse)
	{
		Vector4f scratch = new Vector4f((float)src.x, (float)src.y, (float)src.z, 1.0f);
		// Project and normalize into clip-space
		scratch.mul(from);
		float w = scratch.w;
		if(w > Maths.EpsilonF)
		{
			scratch.mul(1f / w);

			// Calculate the new w' that will get us to w=1 when we unproject
			float w2 = 1f / (scratch.z * toInverse.m23() + toInverse.m33());
			scratch.mul(w2);

			// Unproject to world space
			scratch.mul(toInverse);
			if (!Maths.Approx(scratch.w, 1.0f, 0.001f))
				FlansMod.LOGGER.warn("Un-projection to world space was not normalized. w=" + scratch.w);

			return new Vec3(scratch.x, scratch.y, scratch.z);
		}
		else
		{
			// If z ~ 0, then this point is in the camera, so it will be invariant under projection
			return src;
		}
	}

	private static Matrix4f GetFirstPersonProjection()
	{
		// From GameRenderer::renderItemInHand
		float dt = Minecraft.getInstance().getPartialTick();
		double fov = FlansModClient.GetFOV(Minecraft.getInstance().gameRenderer.getMainCamera(), dt, false);
		return Minecraft.getInstance().gameRenderer.getProjectionMatrix(fov);
	}

	private static Matrix4f GetLevelProjection()
	{
		// From GameRenderer::renderLevel
		float dt = Minecraft.getInstance().getPartialTick();
		PoseStack posestack = new PoseStack();
		double d0 = FlansModClient.GetFOV(Minecraft.getInstance().gameRenderer.getMainCamera(), dt, true);
		posestack.mulPoseMatrix(Minecraft.getInstance().gameRenderer.getProjectionMatrix(d0));
		TransformStack transformStack = new TransformStack();
		//Vanilla_ApplyHurtBobbing(transformStack, dt);
		//Vanilla_ApplyViewBobbing(transformStack, dt);
		// TODO: Confusion effects
		//transformStack.ApplyToPoseStack(posestack);
		return posestack.last().pose();
	}

	public static Transform GetWorldSpaceRootTransform(@Nonnull GunContext gunContext,
													   @Nonnull ItemDisplayContext transformType)
	{
		TransformStack transformStack = new TransformStack();
		ApplyWorldToEye(transformStack, gunContext, transformType);
		ApplyEyeToRoot(transformStack, gunContext, transformType);
		return transformStack.Top();
	}

	public static Transform GetRootSpaceModelTransform(@Nonnull GunContext gunContext,
													   @Nonnull ItemDisplayContext transformType)
	{
		TransformStack transformStack = new TransformStack();
		ApplyRootToModel(transformStack, gunContext, transformType);
		return transformStack.Top();
	}


	public static void ApplyWorldToEye(@Nonnull TransformStack transformStack,
									   @Nonnull GunContext gunContext,
									   @Nonnull ItemDisplayContext transformType)
	{
		float dt = Minecraft.getInstance().getPartialTick();

		switch(transformType)
		{
			case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
				Player player = Minecraft.getInstance().player;
				if (player != null)
				{
					// World space, so we want to put this just in front of the player
					// This is like shooterContext.GetShootOrigin, just with dt passed in

					transformStack.add(Transform.FromPos(player.getEyePosition(dt), () -> "\"PlayerEye\""));
					transformStack.add(Transform.FromEuler(player.getViewXRot(dt), 180f + player.getViewYRot(dt), 0f, () -> "\"PlayerLook\""));
				}
			}
			default -> {
				transformStack.add(gunContext.GetShootOrigin(dt));
			}
		}
	}

	// This takes us from the Minecraft world coords to the Minecraft "Model Root", which might be
	// - the bottom left of an item frame (sort of)
	// - the third person entity arm (sort of)
	// - first person 0,0 (sort of)
	public static void ApplyEyeToRoot(@Nonnull TransformStack transformStack,
										@Nonnull GunContext gunContext,
										@Nonnull ItemDisplayContext transformType)
	{
		float dt = Minecraft.getInstance().getPartialTick();
		switch (transformType)
		{
			case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
				Player player = Minecraft.getInstance().player;
				if(player != null)
				{
					Vanilla_ApplyHurtBobbing(transformStack, dt);
					Vanilla_ApplyWalkBobbing(transformStack, dt);
					Vanilla_ApplyViewBobbing(transformStack, dt);
					Vanilla_ApplyHandAnimation(transformStack, transformType, dt);
					transformStack.add(Transform.FromPos(new Vec3(-0.5d, -0.5d, -0.5d), () -> "\"ItemRenderer.render\""));
				}
				else transformStack.add(Transform.Error("Trying to render first person with no current player"));
			}
		}
	}

	private static final Transform MC_FIRST_PERSON_OFFSET_RIGHT = Transform.FromPos(new Vec3(-0.06d, 1.0d, 1.0d), () -> "\"MC 1st person R\"");
	private static final Transform MC_FIRST_PERSON_OFFSET_LEFT = Transform.FromPos(new Vec3(1.06d, 1.0d, 1.0d), () -> "\"MC 1st person L\"");
	private static final Transform MC_THIRD_PERSON_OFFSET = Transform.FromPosAndEuler(new Vec3(0.5d, 0.5d, 0.5d), new Vector3f(0f, 90f, 0f), () -> "\"MC 3rd person\"");
	private static final Transform MC_GROUND_OFFSET = Transform.FromPos(new Vec3(0.5d, 0.5d, 0.5d), () -> "\"MC ground\"");

	// This takes us from the Minecraft "Model Root" to the "body" piece of this model
	public static void ApplyRootToModel(@Nonnull TransformStack transformStack,
										@Nonnull GunContext gunContext,
										@Nonnull ItemDisplayContext transformType)
	{
		float dt = Minecraft.getInstance().getPartialTick();
		ITurboRenderer turboRenderer = FlansModelRegistry.GetItemRenderer(gunContext.Stack);
		if(turboRenderer instanceof GunItemRenderer gunRenderer)
		{
			Transform defaultPose = gunRenderer.GetTurboRigWrapper().GetTransform(transformType);
			switch(transformType)
			{
				case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
					if(transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
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
		ITurboRenderer gunRenderer = FlansModelRegistry.GetItemRenderer(gunContext.Stack);
		if(gunRenderer != null)
		{

		}
	}

	public static void ApplyModelToAP(@Nonnull TransformStack transformStack,
									  @Nonnull GunContext gunContext,
									  @Nonnull String apName,
									  boolean animated)
	{
		ITurboRenderer turboRenderer = FlansModelRegistry.GetItemRenderer(gunContext.Stack);
		if(turboRenderer instanceof GunItemRenderer gunRenderer)
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
					ITurboRenderer turboRenderer1 = FlansModelRegistry.GetItemRenderer(attachDef.Location);
					if (turboRenderer1 instanceof AttachmentItemRenderer attachmentRenderer)
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
		if (Minecraft.getInstance().options.bobView().get())
		{
			if (Minecraft.getInstance().getCameraEntity() instanceof LocalPlayer playerCamera)
			{
				float f = playerCamera.getAttackAnim(dt);
				float f1 = Mth.lerp(dt, playerCamera.xRotO, playerCamera.getXRot());
				float f2 = Mth.lerp(dt, playerCamera.xBobO, playerCamera.xBob);
				float f3 = Mth.lerp(dt, playerCamera.yBobO, playerCamera.yBob);
				final float xAngle = (playerCamera.getViewXRot(dt) - f2) * 0.1F;
				final float yAngle = (playerCamera.getViewYRot(dt) - f3) * 0.1F;
				if(!Maths.Approx(xAngle, 0.0f) && !Maths.Approx(yAngle, 0.0f))
					transformStack.add(Transform.FromEuler(-xAngle, -yAngle, 0f, () -> "\"ItemInHandRenderer.renderHandsWithItems["+xAngle+", "+yAngle+"]\""));
			}
		}
	}

	private static void Vanilla_ApplyHurtBobbing(@Nonnull TransformStack transformStack, float dt)
	{
		if (Minecraft.getInstance().getCameraEntity() instanceof LivingEntity livingCamera)
		{
			final float hurtTime = (float)livingCamera.hurtTime - dt;
			if (livingCamera.isDeadOrDying()) {
				float f1 = Math.min((float)livingCamera.deathTime + dt, 20.0F);
				transformStack.add(Transform.FromEuler(0f, 0f, 40.0F - 8000.0F / (f1 + 200.0F), () -> "\"GameRenderer.bobHurt["+hurtTime+"]\""));
			}

			if (hurtTime < 0.0F) {
				return;
			}

			final float hurtParameter = hurtTime / (float)livingCamera.hurtDuration;
			final float f = Mth.sin(hurtParameter * hurtParameter * hurtParameter * hurtParameter * (float)Math.PI);
			float f2 = livingCamera.getHurtDir();
			transformStack.add(Transform.FromEuler(0f, -f2, 0f, () -> "\"GameRenderer.bobHurt["+f+"]\""));
			transformStack.add(Transform.FromEuler(0f, 0f, -f * 14.0F, () -> "\"GameRenderer.bobHurt["+f+"]\""));
			transformStack.add(Transform.FromEuler(0f, f2, 0f, () -> "\"GameRenderer.bobHurt["+ f +"]\""));
		}
	}

	private static void Vanilla_ApplyWalkBobbing(@Nonnull TransformStack transformStack, float dt)
	{
		if(Minecraft.getInstance().options.bobView().get() && Minecraft.getInstance().getCameraEntity() instanceof Player player)
		{
			float f = player.walkDist - player.walkDistO;
			float f1 = -(player.walkDist + f * dt);
			final float f2 = Mth.lerp(dt, player.oBob, player.bob);

			transformStack.add(Transform.FromPos(Mth.sin(f1 * Maths.PiF) * f2 * 0.5F, -Math.abs(Mth.cos(f1 * Maths.PiF) * f2), 0.0F, () -> "\"GameRenderer.bobView["+f2+"]\""));
			transformStack.add(Transform.FromEuler(0F, 0f, Mth.sin(f1 * Maths.PiF) * f2 * 3.0F, () -> "\"GameRenderer.bobView["+f2+"]\""));
			transformStack.add(Transform.FromEuler(Math.abs(Mth.cos(f1 * Maths.PiF - 0.2F) * f2) * 5.0F, 0f, 0f, () -> "\"GameRenderer.bobView["+f2+"]\""));
		}

	}

	private static void Vanilla_ApplyHandAnimation(@Nonnull TransformStack transformStack, ItemDisplayContext transformType, float dt)
	{
		boolean rightHand = transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
		int i = rightHand ? 1 : -1;

		LocalPlayer player = Minecraft.getInstance().player;

		// ItemInHandRenderer::renderArmWithItem
		// handRaise = p_109376_
		float atkSwing = player.getAttackAnim(dt); // renderHandsWithItems::f
		InteractionHand attackingHand = MoreObjects.firstNonNull(player.swingingArm, InteractionHand.MAIN_HAND); // renderHandsWithItems::interactionhand
		if(attackingHand != MinecraftHelpers.GetHand(transformType))
			atkSwing = 0.0f; // renderHandsWithItems::f4/f6 == renderArmWithItem::p_109376_

		final float attackSwing = atkSwing;
		final float handRaise = (1.0F - FlansModClient.GetHandHeight(MinecraftHelpers.GetHand(transformType), dt)); // == renderHandsWithItems::f5/f7 == renderArmWithItem::p_109378_

		float f5 = -0.4F * Mth.sin(Mth.sqrt(handRaise) * (float) Math.PI);
		float f6 = 0.2F * Mth.sin(Mth.sqrt(handRaise) * ((float) Math.PI * 2F));
		float f10 = -0.2F * Mth.sin(handRaise * (float) Math.PI);
		transformStack.add(Transform.FromPos(i * f5, f6, f10, () -> "\"ItemInHandRenderer.renderArmWithItem["+handRaise+"]\""));

		// ItemInHandRenderer::applyItemArmTransform(p_109378_)
		transformStack.add(Transform.FromPos(i * 0.56F, -0.52F + handRaise * -0.6F, -0.72F, () -> "\"ItemInHandRenderer.applyItemArmTransform["+handRaise+"]\""));

		// ItemInHandRenderer::applyItemArmAttackTransform
		// handRaise = p_109376_
		{
			float f = Mth.sin(attackSwing * attackSwing * (float)Math.PI);
			transformStack.add(Transform.FromEuler(0f, i * (45.0F + f * -20.0F), 0f, () -> "\"ItemInHandRenderer.applyItemArmAttackTransform["+attackSwing+"]\""));
			float f1 = Mth.sin(Mth.sqrt(attackSwing) * (float)Math.PI);
			transformStack.add(Transform.FromEuler(f1 * -80.0F, i * -45.0F, i * f1 * -20.0F, () -> "\"ItemInHandRenderer.applyItemArmAttackTransform["+attackSwing+"]\""));
		}
	}

	// Calculate in world coords
	private static Transform CalculateADSPosition(@Nonnull Transform defaultPose,
												  @Nonnull GunContext gunContext,
												  @Nonnull ItemDisplayContext transformType)
	{
		List<Transform> posesAppliedToMe = new ArrayList<>();
		List<Float> weights = new ArrayList<>();
		List<Transform> posesAppliedToOthers = new ArrayList<>();
		for (AdsInstance instance : ADS_INSTANCES.values())
		{
			String eyeLinePath = instance.AttachmentIndex >= 0
				? ActionGroupContext.CreateGroupPath(instance.AttachmentType, instance.AttachmentIndex, instance.EyeLineName)
				: instance.EyeLineName;

			// This looks hacky. It is, but it looks nice. We want to hold the un-animated eye-line position to our eye,
			// then animate on top, so pass in a new context that only knows about attachments, no anims
			GunContext animlessContext = GunContext.of(gunContext.GetItemStack());
			Transform eyeLine = GetModelSpaceAPTransform(animlessContext, transformType, eyeLinePath);
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
			boolean leftHanded = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
			eyeLineStack.add(Transform.FromEuler(0f, 0f, leftHanded ? 30f : -30f, () -> leftHanded ? "\"DualWieldTiltLeft\"" : "\"DualWieldTiltRight\""));
		}

		// Push it forwards, so there's some gap between the "eye" and the "eye_line" AP
		eyeLineStack.add(Transform.FromPos(new Vec3(0f, 0f, -0.5f), () -> "\"EyeToEyeLineGap\""));
		// Hmm, use default orientation
		eyeLineStack.add(Transform.ExtractOrientation(defaultPose, false, () -> "\"DefaultOri\""));
		eyeLineStack.add(Transform.ExtractOrientation(eyeLinePos, true, () -> "\"InvEyeLineOri\""));
		// Then, offset by the "eye_line" in world space
		eyeLineStack.add(Transform.ExtractPosition(eyeLinePos, -1d, () -> "\"EyeLinePos\""));

		Transform lookDownEyeLineTransform = eyeLineStack.Top();

		return Transform.Interpolate(defaultPose, lookDownEyeLineTransform, blendWeight);
	}




	public static void RenderTick()
	{
		if(Minecraft.getInstance().player != null)
		{
			ShooterContext playerContext = ShooterContext.of(Minecraft.getInstance().player);

			// "Deactivate" all, then reactivate the ones that are still there
			for(AdsInstance instance : ADS_INSTANCES.values())
				instance.IsActive = false;

			for(GunContext gunContext : playerContext.GetAllGunContexts())
			{
				if(gunContext.IsValid())
				{
					for (ActionGroupInstance actionGroup : gunContext.GetActionStack().GetActiveActionGroups())
					{
						for (ActionInstance action : actionGroup.GetActions())
						{
							if (action instanceof AimDownSightAction adsAction)
							{
								ItemDisplayContext transformType = actionGroup.Context.Gun instanceof GunContextPlayer gunContextPlayer ? MinecraftHelpers.GetFirstPersonTransformType(gunContextPlayer.GetHand()) : ItemDisplayContext.FIXED;
								String key = transformType + "/" + actionGroup.Context.GroupPath;

								// We don't know about it, must be new
								if(!ADS_INSTANCES.containsKey(key))
								{
									AdsInstance newInst = new AdsInstance();
									newInst.IsActive = true;
									newInst.BlendAmount = 0.0f;
									newInst.TransformType = transformType;
									newInst.EyeLineName = adsAction.EyeLineName();
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
