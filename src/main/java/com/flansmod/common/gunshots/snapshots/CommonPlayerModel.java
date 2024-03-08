package com.flansmod.common.gunshots.snapshots;

import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.common.gunshots.EPlayerHitArea;
import com.flansmod.common.gunshots.PlayerSnapshot;
import com.flansmod.common.item.GunItem;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class CommonPlayerModel
{
	public enum ArmPose
	{
		EMPTY(false),
		ITEM(false),
		BLOCK(false),
		BOW_AND_ARROW(true),
		THROW_SPEAR(false),
		CROSSBOW_CHARGE(true),
		CROSSBOW_HOLD(true),
		SPYGLASS(false),
		TOOT_HORN(false),
		BRUSH(false);

		private final boolean twoHanded;

		ArmPose(boolean p_102896_) {
			this.twoHanded = p_102896_;
		}

		public boolean isTwoHanded() {
			return this.twoHanded;
		}
	}

	private static class SimpleCube
	{
		// Poses
		public float x, y, z;
		public float xRot, yRot, zRot;
		public boolean visible;

		// Box settings
		public Vector3f BoxMin;
		public Vector3f BoxDims;
		public boolean Mirror = false;

		public static SimpleCube create()
		{
			return new SimpleCube();
		}

		// Doesn't matter for collision testing
		@Nonnull
		public SimpleCube texOffs(int u, int v) { return this; }
		@Nonnull
		public SimpleCube addBox(float xMin, float yMin, float zMin, float w, float h, float d, @Nonnull CubeDeformation deform)
		{
			BoxMin = new Vector3f(xMin - deform.growX, yMin - deform.growY, zMin - deform.growZ);
			BoxDims = new Vector3f(w + deform.growX * 2f, h + deform.growY * 2f, d + deform.growZ * 2f);
			return this;
		}
		@Nonnull
		public SimpleCube mirror()
		{
			Mirror = !Mirror;
			return this;
		}
		@Nonnull
		public SimpleCube offset(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public void copyFrom(@Nonnull SimpleCube other) {
			this.BoxMin = other.BoxMin;
			this.BoxDims = other.BoxDims;
			this.Mirror = other.Mirror;
			this.xRot = other.xRot;
			this.yRot = other.yRot;
			this.zRot = other.zRot;
			this.x = other.x;
			this.y = other.y;
			this.z = other.z;
		}

		public void Pose(@Nonnull TransformStack stack)
		{
			stack.add(Transform.FromPos(x / 16f, y / 16f, z / 16f));
			stack.add(Transform.FromPosAndQuat(Vec3.ZERO, (new Quaternionf()).rotationZYX(zRot, yRot, xRot), () -> "ZYX Rot"));
			stack.add(Transform.FromPos(BoxMin.x / 16f + BoxDims.x / 32f, BoxMin.y / 16f + BoxDims.y / 32f, BoxMin.z / 16f + BoxDims.z / 32f));

		}

		@Nonnull
		public Transform GetCenter()
		{
			TransformStack stack = new TransformStack();
			Pose(stack);
			return stack.Top();
		}

		@Nonnull
		public Vector3f GetHalfExtents()
		{
			return new Vector3f(BoxDims.x / 32f, BoxDims.y / 32f, BoxDims.z / 32f);
		}
	}
	private static class CubeDeformation {
		public static final CubeDeformation NONE = new CubeDeformation(0.0F);
		final float growX, growY, growZ;

		public CubeDeformation(float x, float y, float z) { growX = x; growY = y; growZ = z; }
		public CubeDeformation(float expand) { this(expand, expand, expand); }
		public CubeDeformation extend(float expand) { return new CubeDeformation(growX + expand, growY + expand, growZ + expand); }
		public CubeDeformation extend(float x, float y, float z) { return new CubeDeformation(growX + x, growY + y, growZ + z); }
	}

	private final SimpleCube head;
	private final SimpleCube hat;
	private final SimpleCube body;
	private final SimpleCube rightArm;
	private final SimpleCube leftArm;
	private final SimpleCube rightLeg;
	private final SimpleCube leftLeg;

	private float swimAmount = 0.0f;
	private boolean riding = false;
	private boolean crouching = false;
	private ArmPose leftArmPose = ArmPose.EMPTY;
	private ArmPose rightArmPose = ArmPose.EMPTY;
	private float attackTime = 0.0f;




	public CommonPlayerModel()
	{
		CubeDeformation deform = new CubeDeformation(0f);
		float height = 0.0f;
		boolean isSlim = false;

		// RHS of these are copied from HumanoidModel.createMesh
		head = SimpleCube			.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, deform).offset(0.0F, 0.0F + height, 0.0F);
		hat = SimpleCube			.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, deform.extend(0.5F)).offset(0.0F, 0.0F + height, 0.0F);
		body = SimpleCube			.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, deform).offset(0.0F, 0.0F + height, 0.0F);
		//RightArm = SimpleCube		.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform).offset(-5.0F, 2.0F + height, 0.0F);
		//LeftArm = SimpleCube		.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform).offset(5.0F, 2.0F + height, 0.0F);
		rightLeg = SimpleCube		.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform).offset(-1.9F, 12.0F + height, 0.0F);
		//LeftLeg = SimpleCube		.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform).offset(1.9F, 12.0F + height, 0.0F);

		// RHS of these are copied from PlayerModel.createMesh
		//EarModel = SimpleCube		.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, deform), PartPose.ZERO);
		//CloakModel = SimpleCube	.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, deform, 1.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
		float f = 0.25F;
		if (isSlim) {
			leftArm = SimpleCube			.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deform).offset(5.0F, 2.5F, 0.0F);
			rightArm = SimpleCube			.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deform).offset(-5.0F, 2.5F, 0.0F);
			//LeftSleeve = SimpleCube		.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
			//RightSleeve  = SimpleCube		.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
		} else {
			leftArm = SimpleCube			.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform).offset(5.0F, 2.0F, 0.0F);
			//LeftSleeve = SimpleCube			.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
			//RightSleeve  = SimpleCube		.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

			// Not here? Copied from humanoid
			rightArm = SimpleCube			.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform).offset(-5.0F, 2.0F + height, 0.0F);
		}

		leftLeg = SimpleCube		.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform).offset(1.9F, 12.0F, 0.0F);
		//LeftPants = SimpleCube 	.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		//RightPants = SimpleCube 	.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
		//Jacket = SimpleCube 		.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.ZERO);

	}

	public void Snap(@Nonnull Player player, @Nonnull PlayerSnapshot snap)
	{
		snap.valid = false;
		TransformStack poseStack = new TransformStack();
		float p_115310_ = 0f; // Partial tick

		// Copied from EntityRenderDispatcher::render(E p_114385_, double p_114386_, double p_114387_, double p_114388_, float p_114389_, float p_114390_, PoseStack p_114391_, MultiBufferSource p_114392_, int p_114393_)
		Vec3 vec3 = this.getRenderOffset(player, p_115310_);
		double d2 = player.position().x + vec3.x();
		double d3 = player.position().y + vec3.y();
		double d0 = player.position().z + vec3.z();
		//p_115311_.pushPose();
		poseStack.translate(d2, d3, d0);

		// Copied from PlayerRenderer::render(AbstractClientPlayer p_117788_, float p_117789_, float p_117790_, PoseStack p_117791_, MultiBufferSource p_117792_, int p_117793_)
		setModelProperties(player);

		// -- Copied from: --
		// LivingEntityRenderer::render(T p_115308_, float p_115309_, float p_115310_, PoseStack p_115311_, MultiBufferSource p_115312_, int p_115313_)
		//p_115311_.pushPose();

		this.attackTime = player.getAttackAnim(p_115310_);

		boolean shouldSit = player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit());
		this.riding = shouldSit;
		//this.young = player.isBaby();
		float f = Mth.rotLerp(p_115310_, player.yBodyRotO, player.yBodyRot);
		float f1 = Mth.rotLerp(p_115310_, player.yHeadRotO, player.yHeadRot);
		float f2 = f1 - f;
		if (shouldSit && player.getVehicle() instanceof LivingEntity) {
			LivingEntity livingentity = (LivingEntity)player.getVehicle();
			f = Mth.rotLerp(p_115310_, livingentity.yBodyRotO, livingentity.yBodyRot);
			f2 = f1 - f;
			float f3 = Mth.wrapDegrees(f2);
			if (f3 < -85.0F) {
				f3 = -85.0F;
			}

			if (f3 >= 85.0F) {
				f3 = 85.0F;
			}

			f = f1 - f3;
			if (f3 * f3 > 2500.0F) {
				f += f3 * 0.2F;
			}

			f2 = f1 - f;
		}

		float f6 = Mth.lerp(p_115310_, player.xRotO, player.getXRot());
		//if (isEntityUpsideDown(p_115308_)) {
		//	f6 *= -1.0F;
		//	f2 *= -1.0F;
		//}

		if (player.hasPose(Pose.SLEEPING)) {
			Direction direction = player.getBedOrientation();
			if (direction != null) {
				float f4 = player.getEyeHeight(Pose.STANDING) - 0.1F;
				poseStack.translate((float)(-direction.getStepX()) * f4, 0.0F, (float)(-direction.getStepZ()) * f4);
			}
		}

		float f7 = this.getBob(player, p_115310_);

		// Renamed to playerSetupRotations to simulate our super.
		this.playerSetupRotations(player, poseStack, f7, f, p_115310_);

		//poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
		this.scale(poseStack);
		poseStack.translate(0.0F, -1.501F, 0.0F);
		float f8 = 0.0F;
		float f5 = 0.0F;
		if (!shouldSit && player.isAlive()) {
			f8 = player.walkAnimation.speed(p_115310_);
			f5 = player.walkAnimation.position(p_115310_);
			if (player.isBaby()) {
				f5 *= 3.0F;
			}

			if (f8 > 1.0F) {
				f8 = 1.0F;
			}
		}

		this.prepareMobModel(player, f5, f8, p_115310_);
		this.livingSetupAnim(player, f5, f8, f7, f2, f6);

		// Now use all this good stuff to build a snapshot
		poseStack.PushSaveState();
		head.Pose(poseStack);
		snap.UpdateHitbox(EPlayerHitArea.HEAD, poseStack.Top(), head.GetHalfExtents());
		poseStack.PopSaveState();

		poseStack.PushSaveState();
		body.Pose(poseStack);
		snap.UpdateHitbox(EPlayerHitArea.BODY, poseStack.Top(), body.GetHalfExtents());
		poseStack.PopSaveState();

		poseStack.PushSaveState();
		leftLeg.Pose(poseStack);
		snap.UpdateHitbox(EPlayerHitArea.LEFTLEG, poseStack.Top(), leftLeg.GetHalfExtents());
		poseStack.PopSaveState();

		poseStack.PushSaveState();
		rightLeg.Pose(poseStack);
		snap.UpdateHitbox(EPlayerHitArea.RIGHTLEG, poseStack.Top(), rightLeg.GetHalfExtents());
		poseStack.PopSaveState();

		poseStack.PushSaveState();
		leftArm.Pose(poseStack);
		snap.UpdateHitbox(EPlayerHitArea.LEFTARM, poseStack.Top(), leftArm.GetHalfExtents());
		poseStack.PopSaveState();

		poseStack.PushSaveState();
		rightArm.Pose(poseStack);
		snap.UpdateHitbox(EPlayerHitArea.RIGHTARM, poseStack.Top(), rightArm.GetHalfExtents());
		poseStack.PopSaveState();

		snap.valid = true;
	}
	// Copied from PlayerRenderer::getRenderOffset(AbstractClientPlayer p_117785_, float p_117786_)
	@Nonnull
	public Vec3 getRenderOffset(@Nonnull Player p_117785_, float p_117786_) {
		return p_117785_.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
	}
	// Copied from HumanoidModel::prepareMobModel(T p_102861_, float p_102862_, float p_102863_, float p_102864_)
	public void prepareMobModel(@Nonnull Player p_102861_, float p_102862_, float p_102863_, float p_102864_) {
		this.swimAmount = p_102861_.getSwimAmount(p_102864_);
	}
	// Copied from LivingEntityRenderer::setupRotations(...)
	protected void livingSetupRotations(@Nonnull Player p_115317_, @Nonnull TransformStack poseStack, float p_115319_, float p_115320_, float p_115321_) {
		if (this.isShaking(p_115317_)) {
			p_115320_ += (float)(Math.cos((double)p_115317_.tickCount * 3.25D) * Math.PI * (double)0.4F);
		}

		if (!p_115317_.hasPose(Pose.SLEEPING)) {
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - p_115320_));
		}

		if (p_115317_.deathTime > 0) {
			float f = ((float)p_115317_.deathTime + p_115321_ - 1.0F) / 20.0F * 1.6F;
			f = Mth.sqrt(f);
			if (f > 1.0F) {
				f = 1.0F;
			}

			poseStack.mulPose(Axis.ZP.rotationDegrees(f * this.getFlipDegrees(p_115317_)));
		} else if (p_115317_.isAutoSpinAttack()) {
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - p_115317_.getXRot()));
			poseStack.mulPose(Axis.YP.rotationDegrees(((float)p_115317_.tickCount + p_115321_) * -75.0F));
		} else if (p_115317_.hasPose(Pose.SLEEPING)) {
			Direction direction = p_115317_.getBedOrientation();
			float f1 = direction != null ? sleepDirectionToRotation(direction) : p_115320_;
			poseStack.mulPose(Axis.YP.rotationDegrees(f1));
			poseStack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees(p_115317_)));
			poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
		} //else if (isEntityUpsideDown(p_115317_)) {
		//	p_115318_.translate(0.0F, p_115317_.getBbHeight() + 0.1F, 0.0F);
		//	p_115318_.mulPose(Axis.ZP.rotationDegrees(180.0F));
		//}
	}
	// Copied from PlayerRenderer::setupRotations(AbstractClientPlayer p_117802_, PoseStack p_117803_, float p_117804_, float p_117805_, float p_117806_)
	protected void playerSetupRotations(@Nonnull Player p_117802_, @Nonnull TransformStack poseStack, float p_117804_, float p_117805_, float p_117806_) {
		float f = p_117802_.getSwimAmount(p_117806_);
		if (p_117802_.isFallFlying()) {
			// -- This is "super.setupRotations" --
			livingSetupRotations(p_117802_, poseStack, p_117804_, p_117805_, p_117806_);
			float f1 = (float)p_117802_.getFallFlyingTicks() + p_117806_;
			float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
			if (!p_117802_.isAutoSpinAttack()) {
				poseStack.mulPose(Axis.XP.rotationDegrees(f2 * (-90.0F - p_117802_.getXRot())));
			}

			Vec3 vec3 = p_117802_.getViewVector(p_117806_);
			Vec3 vec31 = p_117802_.getDeltaMovement(); // (This was deltaMovementLerped)
			double d0 = vec31.horizontalDistanceSqr();
			double d1 = vec3.horizontalDistanceSqr();
			if (d0 > 0.0D && d1 > 0.0D) {
				double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
				double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
				poseStack.mulPose(Axis.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
			}
		} else if (f > 0.0F) {
			// -- This is "super.setupRotations" --
			livingSetupRotations(p_117802_, poseStack, p_117804_, p_117805_, p_117806_);
			float f3 = p_117802_.isInWater() || p_117802_.isInFluidType((fluidType, height) -> p_117802_.canSwimInFluidType(fluidType)) ? -90.0F - p_117802_.getXRot() : -90.0F;
			float f4 = Mth.lerp(f, 0.0F, f3);
			poseStack.mulPose(Axis.XP.rotationDegrees(f4));
			if (p_117802_.isVisuallySwimming()) {
				poseStack.translate(0.0F, -1.0F, 0.3F);
			}
		} else {
			// -- This is "super.setupRotations" --
			livingSetupRotations(p_117802_, poseStack, p_117804_, p_117805_, p_117806_);
		}

	}
	// Copied from LivingEntityRenderer::sleepDirectionToRotation(Direction p_115329_)
	private static float sleepDirectionToRotation(@Nonnull Direction p_115329_) {
		return switch (p_115329_)
		{
			case SOUTH -> 90.0F;
			case WEST -> 0.0F;
			case NORTH -> 270.0F;
			case EAST -> 180.0F;
			default -> 0.0F;
		};
	}
	// Copied from LivingEntityRenderer::getFlipDegrees(T p_115337_)
	protected float getFlipDegrees(@Nonnull Player p_115337_) { return 90.0F; }
	// Copied from LivingEntityRenderer::isShaking(T p_115304_)
	protected boolean isShaking(@Nonnull Player p_115304_) {
		return p_115304_.isFullyFrozen();
	}
	// Copied from LivingEntityRenderer::getBob(T p_115305_, float p_115306_)
	protected float getBob(@Nonnull Player p_115305_, float p_115306_) {
		return (float)p_115305_.tickCount + p_115306_;
	}
	// Copied from PlayerRenderer::scale(AbstractClientPlayer p_117798_, PoseStack p_117799_, float p_117800_)
	protected void scale(@Nonnull TransformStack poseStack) {
		float f = 0.9375F;
		poseStack.scale(0.9375F, 0.9375F, 0.9375F);
	}
	// Copied from PlayerRenderer::setModelProperties(AbstractClientPlayer p_117819_)
	private void setModelProperties(@Nonnull Player p_117819_)
	{
		if (p_117819_.isSpectator()) {
			setAllVisible(false);
			head.visible = true;
			hat.visible = true;
		} else {
			setAllVisible(true);
			hat.visible = p_117819_.isModelPartShown(PlayerModelPart.HAT);
			//jacket.visible = p_117819_.isModelPartShown(PlayerModelPart.JACKET);
			//leftPants.visible = p_117819_.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
			//rightPants.visible = p_117819_.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
			//leftSleeve.visible = p_117819_.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
			//rightSleeve.visible = p_117819_.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
			crouching = p_117819_.isCrouching();
			ArmPose humanoidmodel$armpose = getArmPose(p_117819_, InteractionHand.MAIN_HAND);
			ArmPose humanoidmodel$armpose1 = getArmPose(p_117819_, InteractionHand.OFF_HAND);
			if (humanoidmodel$armpose.isTwoHanded()) {
				humanoidmodel$armpose1 = p_117819_.getOffhandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
			}

			if (p_117819_.getMainArm() == HumanoidArm.RIGHT) {
				rightArmPose = humanoidmodel$armpose;
				leftArmPose = humanoidmodel$armpose1;
			} else {
				rightArmPose = humanoidmodel$armpose1;
				leftArmPose = humanoidmodel$armpose;
			}
		}
	}
	// Copied from PlayerRenderer::getArmPose(AbstractClientPlayer p_117795_, InteractionHand p_117796_)
	private static ArmPose getArmPose(@Nonnull Player p_117795_, @Nonnull InteractionHand p_117796_) {
		ItemStack itemstack = p_117795_.getItemInHand(p_117796_);
		if (itemstack.isEmpty()) {
			return ArmPose.EMPTY;
		} else {
			if (p_117795_.getUsedItemHand() == p_117796_ && p_117795_.getUseItemRemainingTicks() > 0) {
				UseAnim useanim = itemstack.getUseAnimation();
				if (useanim == UseAnim.BLOCK) {
					return ArmPose.BLOCK;
				}

				if (useanim == UseAnim.BOW) {
					return ArmPose.BOW_AND_ARROW;
				}

				if (useanim == UseAnim.SPEAR) {
					return ArmPose.THROW_SPEAR;
				}

				if (useanim == UseAnim.CROSSBOW && p_117796_ == p_117795_.getUsedItemHand()) {
					return ArmPose.CROSSBOW_CHARGE;
				}

				if (useanim == UseAnim.SPYGLASS) {
					return ArmPose.SPYGLASS;
				}

				if (useanim == UseAnim.TOOT_HORN) {
					return ArmPose.TOOT_HORN;
				}

				if (useanim == UseAnim.BRUSH) {
					return ArmPose.BRUSH;
				}
			} else if (!p_117795_.swinging && itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack)) {
				return ArmPose.CROSSBOW_HOLD;
			} else if(itemstack.getItem() instanceof GunItem gun)
			{
				return ArmPose.BOW_AND_ARROW;
			}

			return ArmPose.ITEM;
		}
	}

	// Note: PlayerModel::setupAnim only handles capes and stuff we don't need
	// Copied from HumanoidModel::setupAnim(T p_102866_, float p_102867_, float p_102868_, float p_102869_, float p_102870_, float p_102871_) {
	public void livingSetupAnim(@Nonnull Player player, float p_102867_, float p_102868_, float p_102869_, float p_102870_, float p_102871_)
	{
		boolean flag = player.getFallFlyingTicks() > 4;
		boolean flag1 = player.isVisuallySwimming();
		this.head.yRot = p_102870_ * ((float)Math.PI / 180F);
		if (flag) {
			this.head.xRot = (-(float)Math.PI / 4F);
		} else if (this.swimAmount > 0.0F) {
			if (flag1) {
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float)Math.PI / 4F));
			} else {
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, p_102871_ * ((float)Math.PI / 180F));
			}
		} else {
			this.head.xRot = p_102871_ * ((float)Math.PI / 180F);
		}

		this.body.yRot = 0.0F;
		this.rightArm.z = 0.0F;
		this.rightArm.x = -5.0F;
		this.leftArm.z = 0.0F;
		this.leftArm.x = 5.0F;
		float f = 1.0F;
		if (flag) {
			f = (float)player.getDeltaMovement().lengthSqr();
			f /= 0.2F;
			f *= f * f;
		}

		if (f < 1.0F) {
			f = 1.0F;
		}

		this.rightArm.xRot = Mth.cos(p_102867_ * 0.6662F + (float)Math.PI) * 2.0F * p_102868_ * 0.5F / f;
		this.leftArm.xRot = Mth.cos(p_102867_ * 0.6662F) * 2.0F * p_102868_ * 0.5F / f;
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightLeg.xRot = Mth.cos(p_102867_ * 0.6662F) * 1.4F * p_102868_ / f;
		this.leftLeg.xRot = Mth.cos(p_102867_ * 0.6662F + (float)Math.PI) * 1.4F * p_102868_ / f;
		this.rightLeg.yRot = 0.005F;
		this.leftLeg.yRot = -0.005F;
		this.rightLeg.zRot = 0.005F;
		this.leftLeg.zRot = -0.005F;
		if (this.riding) {
			this.rightArm.xRot += (-(float)Math.PI / 5F);
			this.leftArm.xRot += (-(float)Math.PI / 5F);
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = ((float)Math.PI / 10F);
			this.rightLeg.zRot = 0.07853982F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = (-(float)Math.PI / 10F);
			this.leftLeg.zRot = -0.07853982F;
		}

		this.rightArm.yRot = 0.0F;
		this.leftArm.yRot = 0.0F;
		boolean flag2 = player.getMainArm() == HumanoidArm.RIGHT;
		if (player.isUsingItem()) {
			boolean flag3 = player.getUsedItemHand() == InteractionHand.MAIN_HAND;
			if (flag3 == flag2) {
				this.poseRightArm(player);
			} else {
				this.poseLeftArm(player);
			}
		} else {
			boolean flag4 = flag2 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
			if (flag2 != flag4) {
				this.poseLeftArm(player);
				this.poseRightArm(player);
			} else {
				this.poseRightArm(player);
				this.poseLeftArm(player);
			}
		}

		this.setupAttackAnimation(player, p_102869_);
		if (this.crouching) {
			this.body.xRot = 0.5F;
			this.rightArm.xRot += 0.4F;
			this.leftArm.xRot += 0.4F;
			this.rightLeg.z = 4.0F;
			this.leftLeg.z = 4.0F;
			this.rightLeg.y = 12.2F;
			this.leftLeg.y = 12.2F;
			this.head.y = 4.2F;
			this.body.y = 3.2F;
			this.leftArm.y = 5.2F;
			this.rightArm.y = 5.2F;
		} else {
			this.body.xRot = 0.0F;
			this.rightLeg.z = 0.0F;
			this.leftLeg.z = 0.0F;
			this.rightLeg.y = 12.0F;
			this.leftLeg.y = 12.0F;
			this.head.y = 0.0F;
			this.body.y = 0.0F;
			this.leftArm.y = 2.0F;
			this.rightArm.y = 2.0F;
		}

		if (this.rightArmPose != ArmPose.SPYGLASS) {
			bobModelPart(this.rightArm, p_102869_, 1.0F);
		}

		if (this.leftArmPose != ArmPose.SPYGLASS) {
			bobModelPart(this.leftArm, p_102869_, -1.0F);
		}

		if (this.swimAmount > 0.0F) {
			float f5 = p_102867_ % 26.0F;
			HumanoidArm humanoidarm = this.getAttackArm(player);
			float f1 = humanoidarm == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			float f2 = humanoidarm == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			if (!player.isUsingItem()) {
				if (f5 < 14.0F) {
					this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, 0.0F);
					this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, 0.0F);
					this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float)Math.PI);
					this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float)Math.PI);
					this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, (float)Math.PI + 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F));
					this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, (float)Math.PI - 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F));
				} else if (f5 >= 14.0F && f5 < 22.0F) {
					float f6 = (f5 - 14.0F) / 8.0F;
					this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, ((float)Math.PI / 2F) * f6);
					this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, ((float)Math.PI / 2F) * f6);
					this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float)Math.PI);
					this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float)Math.PI);
					this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, 5.012389F - 1.8707964F * f6);
					this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, 1.2707963F + 1.8707964F * f6);
				} else if (f5 >= 22.0F && f5 < 26.0F) {
					float f3 = (f5 - 22.0F) / 4.0F;
					this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f3);
					this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f3);
					this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float)Math.PI);
					this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float)Math.PI);
					this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, (float)Math.PI);
					this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, (float)Math.PI);
				}
			}

			float f7 = 0.3F;
			float f4 = 0.33333334F;
			this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(p_102867_ * 0.33333334F + (float)Math.PI));
			this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(p_102867_ * 0.33333334F));
		}

		this.hat.copyFrom(this.head);
	}

	// Copied from:
	// HumanoidModel::poseRightArm(T p_102876_)
	private void poseRightArm(@Nonnull Player p_102876_) {
		switch (this.rightArmPose) {
			case EMPTY:
				this.rightArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
				this.rightArm.yRot = (-(float)Math.PI / 6F);
				break;
			case ITEM:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float)Math.PI / 10F);
				this.rightArm.yRot = 0.0F;
				break;
			case THROW_SPEAR:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float)Math.PI;
				this.rightArm.yRot = 0.0F;
				break;
			case BOW_AND_ARROW:
				this.rightArm.yRot = -0.1F + this.head.yRot;
				this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
				this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
				this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
				break;
			case CROSSBOW_CHARGE:
				animateCrossbowCharge(this.rightArm, this.leftArm, p_102876_, true);
				break;
			case CROSSBOW_HOLD:
				animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
				break;
			case BRUSH:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float)Math.PI / 5F);
				this.rightArm.yRot = 0.0F;
				break;
			case SPYGLASS:
				this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (p_102876_.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
				this.rightArm.yRot = this.head.yRot - 0.2617994F;
				break;
			case TOOT_HORN:
				this.rightArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
				this.rightArm.yRot = this.head.yRot - ((float)Math.PI / 6F);
		}

	}
	// Copied from:
	// HumanoidModel::poseLeftArm(T p_102879_)
	private void poseLeftArm(@Nonnull Player p_102879_) {
		switch (this.leftArmPose) {
			case EMPTY:
				this.leftArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
				this.leftArm.yRot = ((float)Math.PI / 6F);
				break;
			case ITEM:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float)Math.PI / 10F);
				this.leftArm.yRot = 0.0F;
				break;
			case THROW_SPEAR:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float)Math.PI;
				this.leftArm.yRot = 0.0F;
				break;
			case BOW_AND_ARROW:
				this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
				this.leftArm.yRot = 0.1F + this.head.yRot;
				this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
				this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
				break;
			case CROSSBOW_CHARGE:
				animateCrossbowCharge(this.rightArm, this.leftArm, p_102879_, false);
				break;
			case CROSSBOW_HOLD:
				animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
				break;
			case BRUSH:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float)Math.PI / 5F);
				this.leftArm.yRot = 0.0F;
				break;
			case SPYGLASS:
				this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (p_102879_.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
				this.leftArm.yRot = this.head.yRot + 0.2617994F;
				break;
			case TOOT_HORN:
				this.leftArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
				this.leftArm.yRot = this.head.yRot + ((float)Math.PI / 6F);
		}

	}

	// Copied from:
	// HumanoidModel::quadraticArmUpdate(float p_102834_)
	private float quadraticArmUpdate(float p_102834_) {
		return -65.0F * p_102834_ + p_102834_ * p_102834_;
	}

	// Copied from:
	// HumanoidModel::setupAttackAnimation(T p_102858_, float p_102859_)
	protected void setupAttackAnimation(@Nonnull Player p_102858_, float p_102859_) {
		if (!(this.attackTime <= 0.0F)) {
			HumanoidArm humanoidarm = this.getAttackArm(p_102858_);
			SimpleCube modelpart = this.getArm(humanoidarm);
			float f = this.attackTime;
			this.body.yRot = Mth.sin(Mth.sqrt(f) * ((float)Math.PI * 2F)) * 0.2F;
			if (humanoidarm == HumanoidArm.LEFT) {
				this.body.yRot *= -1.0F;
			}

			this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
			this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
			this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
			this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
			this.rightArm.yRot += this.body.yRot;
			this.leftArm.yRot += this.body.yRot;
			this.leftArm.xRot += this.body.yRot;
			f = 1.0F - this.attackTime;
			f *= f;
			f *= f;
			f = 1.0F - f;
			float f1 = Mth.sin(f * (float)Math.PI);
			float f2 = Mth.sin(this.attackTime * (float)Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
			modelpart.xRot -= f1 * 1.2F + f2;
			modelpart.yRot += this.body.yRot * 2.0F;
			modelpart.zRot += Mth.sin(this.attackTime * (float)Math.PI) * -0.4F;
		}
	}

	// Copied from HumanoidModel::getArm(HumanoidArm p_102852_)
	@Nonnull
	protected SimpleCube getArm(HumanoidArm p_102852_) {
		return p_102852_ == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
	}

	// Copied from HumanoidModel::getAttackArm(T p_102857_)
	private HumanoidArm getAttackArm(@Nonnull Player p_102857_) {
		HumanoidArm humanoidarm = p_102857_.getMainArm();
		return p_102857_.swingingArm == InteractionHand.MAIN_HAND ? humanoidarm : humanoidarm.getOpposite();
	}

	// Copied from:
	// HumanoidModel::rotlerpRad(float p_102836_, float p_102837_, float p_102838_)
	protected float rotlerpRad(float p_102836_, float p_102837_, float p_102838_) {
		float f = (p_102838_ - p_102837_) % ((float)Math.PI * 2F);
		if (f < -(float)Math.PI) {
			f += ((float)Math.PI * 2F);
		}

		if (f >= (float)Math.PI) {
			f -= ((float)Math.PI * 2F);
		}

		return p_102837_ + p_102836_ * f;
	}
	// Copied from:
	// HumanoidModel::setAllVisible(boolean p_102880_)
	public void setAllVisible(boolean p_102880_) {
		this.head.visible = p_102880_;
		this.hat.visible = p_102880_;
		this.body.visible = p_102880_;
		this.rightArm.visible = p_102880_;
		this.leftArm.visible = p_102880_;
		this.rightLeg.visible = p_102880_;
		this.leftLeg.visible = p_102880_;
	}

	// Copied from:
	// AnimationUtils::bobModelPart(ModelPart p_170342_, float p_170343_, float p_170344_)
	public static void bobModelPart(@Nonnull SimpleCube p_170342_, float p_170343_, float p_170344_) {
		p_170342_.zRot += p_170344_ * (Mth.cos(p_170343_ * 0.09F) * 0.05F + 0.05F);
		p_170342_.xRot += p_170344_ * Mth.sin(p_170343_ * 0.067F) * 0.05F;
	}
	// Copied from:
	// AnimationUtils::animateCrossbowHold(ModelPart p_102098_, ModelPart p_102099_, ModelPart p_102100_, boolean p_102101_)
	public static void animateCrossbowHold(@Nonnull SimpleCube p_102098_, @Nonnull SimpleCube p_102099_, @Nonnull SimpleCube p_102100_, boolean p_102101_) {
		SimpleCube modelpart = p_102101_ ? p_102098_ : p_102099_;
		SimpleCube modelpart1 = p_102101_ ? p_102099_ : p_102098_;
		modelpart.yRot = (p_102101_ ? -0.3F : 0.3F) + p_102100_.yRot;
		modelpart1.yRot = (p_102101_ ? 0.6F : -0.6F) + p_102100_.yRot;
		modelpart.xRot = (-(float)Math.PI / 2F) + p_102100_.xRot + 0.1F;
		modelpart1.xRot = -1.5F + p_102100_.xRot;
	}
	// Copied from:
	// AnimationUtils::animateCrossbowCharge(ModelPart p_102087_, ModelPart p_102088_, LivingEntity p_102089_, boolean p_102090_)
	public static void animateCrossbowCharge(@Nonnull SimpleCube p_102087_, @Nonnull SimpleCube p_102088_, @Nonnull LivingEntity p_102089_, boolean p_102090_) {
		SimpleCube modelpart = p_102090_ ? p_102087_ : p_102088_;
		SimpleCube modelpart1 = p_102090_ ? p_102088_ : p_102087_;
		modelpart.yRot = p_102090_ ? -0.8F : 0.8F;
		modelpart.xRot = -0.97079635F;
		modelpart1.xRot = modelpart.xRot;
		float f = (float) CrossbowItem.getChargeDuration(p_102089_.getUseItem());
		float f1 = Mth.clamp((float)p_102089_.getTicksUsingItem(), 0.0F, f);
		float f2 = f1 / f;
		modelpart1.yRot = Mth.lerp(f2, 0.4F, 0.85F) * (float)(p_102090_ ? 1 : -1);
		modelpart1.xRot = Mth.lerp(f2, modelpart1.xRot, (-(float)Math.PI / 2F));
	}
}
