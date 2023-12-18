package com.flansmod.client.render.models;

import com.flansmod.client.render.RenderContext;
import com.flansmod.common.FlansMod;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TurboRenderUtility
{
	public static boolean USE_MODELVIEW_MATRIX_RENDER_MODE = false;
	public static boolean USE_BAKED_TURBO_MODELS = true;

	public static void Render(@Nullable TurboRig unbaked, @Nullable TurboRig.Baked baked, @Nonnull String partName, @Nonnull TransformStack transformStack, @Nonnull VertexConsumer vc, int light, int overlay)
	{
		if(USE_BAKED_TURBO_MODELS && baked != null)
		{
			TurboModel.Baked bakedSection = baked.GetPart(partName);
			if(bakedSection != null)
				Render(bakedSection, vc, transformStack, light, overlay);
		}
		else if(unbaked != null)
		{
			TurboModel unbakedSection = unbaked.GetPart(partName);
			if(unbakedSection != null)
				Render(unbakedSection, vc, transformStack, light, overlay, 1f/16f);
		}
		else
		{
			// Render a missing model?
		}
	}

	public static void Render(TurboModel.Baked model, VertexConsumer vc, TransformStack transformStack, int light, int overlay)
	{
		Minecraft.getInstance().getItemRenderer().renderQuadList(
			transformStack.Top().ToNewPoseStack(),
			vc,
			model.getQuads(
				null,
				null,
				Minecraft.getInstance().font.random),
			ItemStack.EMPTY,
			light,
			overlay
		);
	}

	public static void Render(TurboModel model, VertexConsumer vc, TransformStack transformStack, int light, int overlay, float scale)
	{
		Transform topPose = transformStack.Top();
		if(USE_MODELVIEW_MATRIX_RENDER_MODE)
		{
			// Copy our pose into the model view matrix and upload it
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			modelViewStack.setIdentity();
			modelViewStack.mulPoseMatrix(transformStack.Top().ToNewPoseStack().last().pose());
			RenderSystem.applyModelViewMatrix();

			// Render without transformation on the CPU
			// Render direct to the Tesselator, because this method does not work when deferred?



			// Return the model view matrix to how it was before
			modelViewStack.popPose();
			RenderSystem.applyModelViewMatrix();
		}
		else
		{
			// The regular Minecraft way of rendering. Transform all our vertices on the CPU and don't use ModelView matrix...
			for (TurboElement element : model.GetElements())
			{
				for (Direction direction : Direction.values())
				{
					TurboFace face = element.GetFace(direction);
					Vector3f[] positions = element.GetFaceVertices(direction, true);
					Vector3f normal = element.GetNormal(direction, true);
					Vec3 nPosed = topPose.LocalToGlobalDirection(new Vec3(normal.x, normal.y, normal.z));

					for (int i = 0; i < 4; i++)
					{
						Vec3 vPosed = topPose.LocalToGlobalPosition(new Vec3(
							positions[i].x * scale,
							positions[i].y * scale,
							positions[i].z * scale));
						vc.vertex(
							(float)vPosed.x,
							(float)vPosed.y,
							(float)vPosed.z,
							1.0f, 1.0f, 1.0f, 1.0f,
							face.uvData.getU(i),
							face.uvData.getV(i),
							overlay, // overlayCoords
							light, // uv2
							(float)nPosed.x,
							(float)nPosed.y,
							(float)nPosed.z);
					}
				}
			}
		}
	}
}
