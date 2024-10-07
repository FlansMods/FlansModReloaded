package com.flansmod.packs.worldwars.client;

import com.flansmod.packs.worldwars.WorldWarsMod;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class JanModel<T extends LivingEntity> extends HumanoidModel<T>
{
	public static final ModelLayerLocation MODEL_LAYER_LOCATION =
		new ModelLayerLocation(new ResourceLocation(WorldWarsMod.MODID, "jan"), "main");

	public JanModel(ModelPart modelPart)
	{
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer()
	{
		final float BODY_OFFSET = -14.0F;

		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, -14.0F);
		PartDefinition root = meshdefinition.getRoot();

		root.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(0, 0)
			.addBox(-4f, -8f, -4f, 8f, 8f, 8f),
			PartPose.ZERO);

		root.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(0, 16)
			.addBox(-4f, 0f, -2f, 8, 16, 4),
			PartPose.ZERO);

		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 36)
				.addBox(-1.5F, -1.5F, -1.5F, 3.0F, 15.0F, 3.0F),
			PartPose.offset(-5.0F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(12, 36)
				.mirror()
				.addBox(-1.5F, -1.5F, -1.5F, 3.0F, 15.0F, 3.0F),
			PartPose.offset(5.0F, 12.0F, 8.0F));

		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(24, 36)
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F),
			PartPose.offset(-2.0F, -5.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(36, 36)
				.mirror()
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F),
			PartPose.offset(2.0F, -5.0F, 0.0F));



		root.getChild("body").addOrReplaceChild("pack_0", CubeListBuilder.create()
				.texOffs(0,60)
				.addBox(-3.25f, 3f, -3f, 2, 3, 1),
			PartPose.ZERO);
		root.getChild("body").addOrReplaceChild("pack_1", CubeListBuilder.create()
				.texOffs(7,60)
				.addBox(-1.0f, 3f, -3f, 2, 3, 1),
			PartPose.ZERO);
		root.getChild("body").addOrReplaceChild("pack_2", CubeListBuilder.create()
				.texOffs(14,60)
				.addBox(1.25f, 3f, -3f, 2, 3, 1),
			PartPose.ZERO);

		root.getChild("body").addOrReplaceChild("pack_3", CubeListBuilder.create()
				.texOffs(0,54)
				.addBox(-3.25f, 7f, -3f, 2, 4, 1),
			PartPose.ZERO);
		root.getChild("body").addOrReplaceChild("pack_4", CubeListBuilder.create()
				.texOffs(7,54)
				.addBox(-1.0f, 7f, -3f, 2, 4, 1),
			PartPose.ZERO);
		root.getChild("body").addOrReplaceChild("pack_5", CubeListBuilder.create()
				.texOffs(14,54)
				.addBox(1.25f, 7f, -3f, 2, 4, 1),
			PartPose.ZERO);



		root.getChild("body").addOrReplaceChild("trenchcoat_top", CubeListBuilder.create()
			.texOffs(32,19)
			.addBox(-4.5f, 0f, 2f, 9, 16, 1),
			PartPose.rotation(5.0f * Maths.DegToRadF, 0f, 0f));
		root.getChild("body").addOrReplaceChild("trenchcoat_top_left", CubeListBuilder.create()
				.texOffs(24,19)
				.addBox(-1.5f, 0f, 2f, 3, 16, 1),
			PartPose.offsetAndRotation(2.75f, 0f, -0.25f,
				5.0f * Maths.DegToRadF, 45f * Maths.DegToRadF, 0f));
		root.getChild("body").addOrReplaceChild("trenchcoat_top_right", CubeListBuilder.create()
				.texOffs(52,19)
				.mirror()
				.addBox(-1.5f, 0f, 2f, 3, 16, 1),
			PartPose.offsetAndRotation(-2.75f, 0f, -0.25f,
				5.0f * Maths.DegToRadF, -45f * Maths.DegToRadF, 0f));

		root.getChild("body").addOrReplaceChild("trenchcoat_tail", CubeListBuilder.create()
				.texOffs(42,0)
				.addBox(-4.5f, 0f, 2f, 9, 10, 1),
			PartPose.offsetAndRotation(0f, 16f, 1.5f,
				10.0f * Maths.DegToRadF, 0f, 0f));
		root.getChild("body").addOrReplaceChild("trenchcoat_left_tail", CubeListBuilder.create()
				.texOffs(32,0)
				.addBox(-2f, 0f, 2f, 4, 10, 1),
			PartPose.offsetAndRotation(3.25f, 16f, 0.75f,
				10.0f * Maths.DegToRadF, 45f * Maths.DegToRadF, 0f));
		root.getChild("body").addOrReplaceChild("trenchcoat_right_tail", CubeListBuilder.create()
				.texOffs(50,38)
				.mirror()
				.addBox(-2f, 0f, 2f, 4, 10, 1),
			PartPose.offsetAndRotation(-3.25f, 16f, 0.75f,
				10.0f * Maths.DegToRadF, -45f * Maths.DegToRadF, 0f));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(@Nonnull T jan, float x, float y, float z, float p_102592_, float p_102593_)
	{
		super.setupAnim(jan, x, y, z, p_102592_, p_102593_);
		head.visible = true;
		hat.visible = false;

		head.y = -8.0f;
		body.y = -8.0f;

		rightLeg.y = 8.0F;
		leftLeg.y = 8.0F;
		rightArm.setPos(-4.5F, -6.0F, 0.0F);
		leftArm.setPos(4.5F, -6.0F, 0.0F);
	}
}
