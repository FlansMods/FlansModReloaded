package com.flansmod.packs.clockwork.client;

import com.flansmod.packs.clockwork.ClockworkCalamityMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class ChipModel<T extends LivingEntity> extends HumanoidModel<T>
{
	public static final ModelLayerLocation MODEL_LAYER_LOCATION =
		new ModelLayerLocation(new ResourceLocation(ClockworkCalamityMod.MODID, "chip"), "main");

	public ChipModel(ModelPart modelPart)
	{
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer()
	{
		final float BODY_OFFSET = -14.0F;

		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, -14.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();


		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(@Nonnull T chip, float x, float y, float z, float p_102592_, float p_102593_)
	{
		super.setupAnim(chip, x, y, z, p_102592_, p_102593_);


	}
}
