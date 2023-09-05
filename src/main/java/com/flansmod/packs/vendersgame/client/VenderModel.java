package com.flansmod.packs.vendersgame.client;

import com.flansmod.packs.vendersgame.VendersGameMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class VenderModel<T extends LivingEntity> extends HumanoidModel<T>
{
	public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(VendersGameMod.MODID, "vender"), "main");

	public VenderModel(ModelPart modelPart)
	{
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer()
	{
		final float BODY_OFFSET = -14.0F;

		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, -14.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartPose headPose = PartPose.offset(0.0F, -13.0F, 0.0F);
		partdefinition.addOrReplaceChild("hat",
			CubeListBuilder.create()
				.texOffs(0, 16)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)),
			headPose);
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
			headPose);
		partdefinition.addOrReplaceChild("body",
			CubeListBuilder.create()
				.texOffs(32, 16)
				.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 4.0F),
			PartPose.offset(0.0F, 10F, 0.0F));
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(56, 0)
				.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 18.0F, 2.0F),
			PartPose.offset(-5.0F, 12.0F, 0.0F));
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(56, 0)
				.mirror()
				.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 18.0F, 2.0F),
			PartPose.offset(5.0F, 12.0F, 8.0F));
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(56, 0)
				.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 15.0F, 2.0F),
			PartPose.offset(-2.0F, -5.0F, 81.0F));
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(56, 0)
				.mirror()
				.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 15.0F, 2.0F),
			PartPose.offset(2.0F, -5.0F, 0.0F));


		float bandolierAngle = 40.0f;
		partdefinition.getChild("body").addOrReplaceChild("bandolier_front", CubeListBuilder.create()
			.texOffs(32, 0)
			.addBox(-1, -5.5f, -1, 2, 11, 1),
			PartPose.offsetAndRotation(0f, 4f, -2f,
				0f, 0f, bandolierAngle));
		partdefinition.getChild("body").addOrReplaceChild("bandolier_bottom", CubeListBuilder.create()
				.texOffs(32, 9)
				.addBox(-1, -6.5f, -1, 2, 1, 6),
			PartPose.offsetAndRotation(0f, 4f, -2f,
				0f, 0f, bandolierAngle));
		partdefinition.getChild("body").addOrReplaceChild("bandolier_top", CubeListBuilder.create()
				.texOffs(32, 9)
				.addBox(-1, 5.5f, -1, 2, 1, 6),
			PartPose.offsetAndRotation(0f, 4f, -2f,
				0f, 0f, bandolierAngle));
		partdefinition.getChild("body").addOrReplaceChild("bandolier_back", CubeListBuilder.create()
				.texOffs(32, 0)
				.addBox(-1, -5.5f, -1, 2, 11, 1),
			PartPose.offsetAndRotation(0f, 4f, 3f,
				0f, 0f, bandolierAngle));
		partdefinition.getChild("body").addOrReplaceChild("shell_1", CubeListBuilder.create()
				.texOffs(25, 0)
				.addBox(-1.5f, -2.5f, -0.5f, 2, 1, 1),
			PartPose.offsetAndRotation(0f, 4f, -3f,
				0f, 0f, bandolierAngle));
		partdefinition.getChild("body").addOrReplaceChild("shell_2", CubeListBuilder.create()
				.texOffs(25, 0)
				.addBox(-1.5f, -1f, -0.5f, 2, 1, 1),
			PartPose.offsetAndRotation(0f, 4f, -3f,
				0f, 0f, bandolierAngle));
		partdefinition.getChild("body").addOrReplaceChild("shell_3", CubeListBuilder.create()
				.texOffs(25, 0)
				.addBox(-1.5f, 0.5f, -0.5f, 2, 1, 1),
			PartPose.offsetAndRotation(0f, 4f, -3f,
				0f, 0f, bandolierAngle));
		partdefinition.getChild("body").addOrReplaceChild("shell_4", CubeListBuilder.create()
				.texOffs(25, 0)
				.addBox(-1.5f, 2.0f, -0.5f, 2, 1, 1),
			PartPose.offsetAndRotation(0f, 4f, -3f,
				0f, 0f, bandolierAngle));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(@Nonnull T vender, float x, float y, float z, float p_102592_, float p_102593_)
	{
		super.setupAnim(vender, x, y, z, p_102592_, p_102593_);
		head.visible = true;
		int i = -6;
		body.xRot = 0.0F;
		body.y = 4.0F;
		body.z = -0.0F;
		rightLeg.xRot -= 0.0F;
		leftLeg.xRot -= 0.0F;
		rightArm.xRot *= 0.5F;
		leftArm.xRot *= 0.5F;
		rightLeg.xRot *= 0.5F;
		leftLeg.xRot *= 0.5F;
		float f = 0.4F;
		if (rightArm.xRot > 0.4F) {
			rightArm.xRot = 0.4F;
		}

		if (leftArm.xRot > 0.4F) {
			leftArm.xRot = 0.4F;
		}

		if (rightArm.xRot < -0.4F) {
			rightArm.xRot = -0.4F;
		}

		if (leftArm.xRot < -0.4F) {
			leftArm.xRot = -0.4F;
		}

		if (rightLeg.xRot > 0.4F) {
			rightLeg.xRot = 0.4F;
		}

		if (leftLeg.xRot > 0.4F) {
			leftLeg.xRot = 0.4F;
		}

		if (rightLeg.xRot < -0.4F) {
			rightLeg.xRot = -0.4F;
		}

		if (leftLeg.xRot < -0.4F) {
			leftLeg.xRot = -0.4F;
		}

		rightLeg.z = 0.0F;
		leftLeg.z = 0.0F;
		rightLeg.y = 9.0F;
		leftLeg.y = 9.0F;
		head.z = -0.0F;
		head.y = 4.0F;
		hat.x = head.x;
		hat.y = head.y;
		hat.z = head.z;
		hat.xRot = head.xRot;
		hat.yRot = head.yRot;
		hat.zRot = head.zRot;

		int j = -14;
		rightArm.setPos(-5.0F, 6.0F, 0.0F);
		leftArm.setPos(5.0F, 6.0F, 0.0F);
	}
}
