package com.flansmod.client.render.models;

import com.flansmod.client.render.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class TurboRenderUtility
{
	public static boolean USE_MODELVIEW_MATRIX_RENDER_MODE = false;
	public static void Render(TurboModel model, VertexConsumer vc, PoseStack pose, int light, int overlay)
	{
		if(USE_MODELVIEW_MATRIX_RENDER_MODE)
		{
			// Copy our pose into the model view matrix and upload it
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			modelViewStack.setIdentity();
			modelViewStack.mulPoseMatrix(pose.last().pose());
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
					Vector3f nPosed = pose.last().normal().transform(normal);

					for (int i = 0; i < 4; i++)
					{
						Vector4f vPosed = pose.last().pose().transform(new Vector4f(positions[i].x, positions[i].y, positions[i].z, 1.0F));
						vc.vertex(
							vPosed.x,
							vPosed.y,
							vPosed.z,
							1.0f, 1.0f, 1.0f, 1.0f,
							face.uvData.getU(i),
							face.uvData.getV(i),
							overlay, // overlayCoords
							light, // uv2
							nPosed.x,
							nPosed.y,
							nPosed.z);
					}
				}
			}
		}
	}
}
