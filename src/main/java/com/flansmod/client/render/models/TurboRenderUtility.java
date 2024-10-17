package com.flansmod.client.render.models;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.RenderContext;
import com.flansmod.client.render.animation.ESmoothSetting;
import com.flansmod.client.render.animation.FlanimationDefinition;
import com.flansmod.client.render.animation.PoseCache;
import com.flansmod.client.render.animation.elements.KeyframeDefinition;
import com.flansmod.client.render.animation.elements.SequenceDefinition;
import com.flansmod.client.render.animation.elements.SequenceEntryDefinition;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.nodes.AnimationAction;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class TurboRenderUtility
{
	public static boolean USE_MODELVIEW_MATRIX_RENDER_MODE = false;
	public static boolean USE_BAKED_TURBO_MODELS = false;

	protected static final RenderStateShard.ShaderStateShard GUN_SOLID_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunSolidShader);
	protected static final RenderStateShard.ShaderStateShard GUN_CUTOUT_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunCutoutShader);
	protected static final RenderStateShard.ShaderStateShard GUN_EMISSIVE_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunEmissiveShader);
	protected static final RenderStateShard.ShaderStateShard GUN_TRANSPARENT_SHADER = new RenderStateShard.ShaderStateShard(FlansModClient::GetGunTransparentShader);

	private static class RenderTypeFlanItem extends RenderType
	{

		protected static RenderType.CompositeState.CompositeStateBuilder BaseState(ResourceLocation texture)
		{
			return RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
				.setCullState(CULL)
				.setOverlayState(OVERLAY)
				.setLightmapState(LIGHTMAP)
				.setDepthTestState(LEQUAL_DEPTH_TEST);
		}

		protected static final Function<ResourceLocation, RenderType> GUN_CUTOUT = Util.memoize((texture) -> {
			RenderType.CompositeState compositeState =
				RenderType.CompositeState.builder()
					.setShaderState(GUN_CUTOUT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
					.setTransparencyState(NO_TRANSPARENCY)
					.setCullState(CULL)
					.setOverlayState(OVERLAY)
					.setLightmapState(LIGHTMAP)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.createCompositeState(true);
			return create("flan_gun_item",
				DefaultVertexFormat.BLOCK,
				VertexFormat.Mode.QUADS,
				256,
				true,
				false,
				compositeState);
		});


		protected static final Function<ResourceLocation, RenderType> GUN_EMISSIVE =
			Util.memoize((texture) ->
				create("flan_gun_emissive",
					DefaultVertexFormat.BLOCK,
					VertexFormat.Mode.QUADS,
					256,
					true,
					false,
					BaseState(texture)
						.setShaderState(GUN_EMISSIVE_SHADER)
						.setTransparencyState(ADDITIVE_TRANSPARENCY)
						.setWriteMaskState(COLOR_WRITE)
						.createCompositeState(false)));
		protected static final Function<ResourceLocation, RenderType> GUN_TRANSPARENT =
			Util.memoize((texture) ->
				create("flan_gun_transparent",
					DefaultVertexFormat.BLOCK,
					VertexFormat.Mode.QUADS,
					256,
					true,
					false,
					BaseState(texture)
						.setShaderState(GUN_TRANSPARENT_SHADER)
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.createCompositeState(true)));
		protected static final Function<ResourceLocation, RenderType> GUN_SOLID =
			Util.memoize((texture) ->
				create("flan_gun_solid",
					DefaultVertexFormat.BLOCK,
					VertexFormat.Mode.QUADS,
					256,
					true,
					false,
					BaseState(texture)
						.setShaderState(GUN_SOLID_SHADER)
						.setTransparencyState(NO_TRANSPARENCY)
						.createCompositeState(true)));

		public RenderTypeFlanItem(String name, VertexFormat vf, VertexFormat.Mode vfm, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupFunc, Runnable cleanupFunc)
		{
			super(name, vf, vfm, bufferSize, affectsCrumbling, sortOnUpload, setupFunc, cleanupFunc);
		}
	}
	@Nonnull
	public static RenderType flanItemRenderType(@Nullable ResourceLocation texture, @Nonnull ETurboRenderMaterial material)
	{
		if(texture == null)
			texture = MissingTextureAtlasSprite.getLocation();
		return switch (material)
			{
				case Solid -> RenderType.entitySolid(texture); //RenderTypeFlanItem.GUN_SOLID.apply(texture);
				case Cutout -> RenderType.entityCutout(texture); //RenderTypeFlanItem.GUN_CUTOUT.apply(texture);
				case Emissive -> RenderType.eyes(texture);
				case Transparent -> RenderType.entityTranslucent(texture);
			};
	}

	@Nullable
	public final TurboRig Unbaked;
	@Nullable
	public final TurboRig.Baked Baked;

	private TurboRenderUtility(@Nullable TurboRig unbaked, @Nullable TurboRig.Baked baked)
	{
		Unbaked = unbaked;
		Baked = baked;
	}
	@Nonnull
	public static TurboRenderUtility of(@Nullable TurboRig unbaked, @Nullable TurboRig.Baked baked) { return new TurboRenderUtility(unbaked, baked); }
	@Nonnull
	public static TurboRenderUtility of() { return new TurboRenderUtility(null, null); }
	@Nonnull
	public TurboRenderUtility with(@Nonnull TurboRig setUnbaked) { return new TurboRenderUtility(setUnbaked, Baked); }
	@Nonnull
	public TurboRenderUtility with(@Nonnull TurboRig.Baked setBaked) { return new TurboRenderUtility(Unbaked, setBaked); }

	@Nonnull
	public Map<String, Float> GetParameters()
	{
		return Unbaked != null ? Unbaked.GetFloatParams() : Map.of();
	}
	@Nullable
	public BakedModel GetIconModel(@Nonnull String skinName)
	{
		return Baked != null ? Baked.GetIconModel(skinName) : null;
	}
	@Nonnull
	public ResourceLocation GetSkinLocation(@Nonnull String skinName)
	{
		return Baked != null ? Baked.GetTexture(skinName) : new ResourceLocation(FlansMod.MODID, "skins/unknown");
	}
	public boolean HasPart(@Nonnull String partName)
	{
		if(Unbaked != null && Unbaked.GetPart(partName) != null)
			return true;
		if(Baked != null && Baked.GetPart(partName) != null)
			return true;
		return false;
	}
	public void ForEachChild(@Nonnull String partName, @Nonnull Consumer<String> childFunc)
	{
		if (Baked != null)
			for (var kvp : Baked.GetAttachPoints())
				if (kvp.getValue().Parent != null && kvp.getValue().Parent.PartName.equals(partName))
					childFunc.accept(kvp.getKey());
	}
	public void ForEachChild(@Nonnull String partName, @Nonnull BiConsumer<String, TurboRig.AttachPoint.Baked> childFunc)
	{
		if(Baked != null)
			for (var kvp : Baked.GetAttachPoints())
				if (kvp.getValue().Parent != null && kvp.getValue().Parent.PartName.equals(partName))
					childFunc.accept(kvp.getKey(), kvp.getValue());
	}
	@Nullable
	public TurboRig.AttachPoint.Baked GetAP(@Nonnull String partName)
	{
		return Baked != null ? Baked.GetAttachPoint(partName) : null;
	}
	@Nonnull
	public String GetAPKey(@Nonnull EAttachmentType attachmentType, int attachmentIndex)
	{
		if(Baked != null)
		{
			String apKey = attachmentType.toString().toLowerCase() + "_" + attachmentIndex;
			if (Baked.GetAttachPoint(apKey) != null)
				return apKey;

			// Backup, try without the index i.e. "barrel" instead of "barrel_0"
			if (attachmentIndex == 0)
			{
				apKey = attachmentType.toString().toLowerCase();
				if (Baked.GetAttachPoint(apKey) != null)
					return apKey;
			}
		}
		return "";
	}
	@Nonnull
	public Transform GetTransform(@Nonnull ItemDisplayContext transformType)
	{
		return Baked != null ? Baked.GetTransform(transformType) : Transform.IDENTITY;
	}
	@Nonnull
	public Transform GetPose(@Nonnull String partName,
							 @Nonnull ResourceLocation modelLocation,
							 @Nonnull FlanimationDefinition animationSet,
							 @Nullable ActionStack actionStack)
	{
		if(Unbaked == null)
			return Transform.error("Unbaked Rig Missing");

		if(actionStack != null)
		{
			if(!animationSet.IsValid())
				return Transform.error("Missing animation set");

			List<AnimationAction> animActions = new ArrayList<>();
			for(ActionGroupInstance group : actionStack.GetActiveActionGroups())
				for(ActionInstance action : group.GetActions())
				{
					if (action instanceof AnimationAction animAction)
						animActions.add(animAction);
				}

			List<Transform> poses = new ArrayList<>();
			for (AnimationAction animAction : animActions)
			{
				SequenceDefinition sequence = animationSet.GetSequence(animAction.Def.anim);
				if (sequence == null)
				{
					FlansMod.LOGGER.warn("Could not find animation sequence " + animAction.Def.anim + " in anim set " + animationSet.Location);
					continue;
				}

				// Make sure we scale the sequence (which can be played at any speed) with the target duration of this specific animation action
				float progress = animAction.AnimFrame + Minecraft.getInstance().getPartialTick();
				float animMultiplier = sequence.Duration() / (animAction.Def.duration * 20f);
				progress *= animMultiplier;

				// Find the segment of this animation that we need
				SequenceEntryDefinition[] segment = sequence.GetSegment(progress);
				float segmentDuration = segment[1].tick - segment[0].tick;

				// If it is valid, let's animate it
				if (segmentDuration >= 0.0f)
				{
					KeyframeDefinition from = animationSet.GetKeyframe(segment[0]);
					KeyframeDefinition to = animationSet.GetKeyframe(segment[1]);
					if (from != null && to != null)
					{
						float linearParameter = (progress - segment[0].tick) / segmentDuration;
						linearParameter = Maths.clamp(linearParameter, 0f, 1f);
						float outputParameter = linearParameter;

						// Instant transitions take priority first
						if (segment[0].exit == ESmoothSetting.instant)
							outputParameter = 1.0f;
						if (segment[1].entry == ESmoothSetting.instant)
							outputParameter = 0.0f;

						// Then apply smoothing?
						if (segment[0].exit == ESmoothSetting.smooth)
						{
							// Smoothstep function
							if (linearParameter < 0.5f)
								outputParameter = linearParameter * linearParameter * (3f - 2f * linearParameter);
						}
						if (segment[1].entry == ESmoothSetting.smooth)
						{
							// Smoothstep function
							if (linearParameter > 0.5f)
								outputParameter = linearParameter * linearParameter * (3f - 2f * linearParameter);
						}

						//PoseDefinition fromPose = animationSet.GetPoseForPart(from, partName);
						//PoseDefinition toPose = animationSet.GetPoseForPart(to, partName);

						poses.add(PoseCache.Lerp(modelLocation,
							animationSet.Location,
							from.name,
							to.name,
							partName,
							outputParameter));
					}
				}
			}

			Transform resultPose = poses.size() > 0 ? Transform.interpolate(poses) : Transform.identity();
			TurboModel model = Unbaked.GetPart(partName);
			if (model != null)
			{
				//return resultPose.Translate(model.offset.x, model.offset.y, model.offset.z);
			}
			// else
			return resultPose;
		}

		return Transform.identity();
	}
	public void RenderPartIteratively(@Nonnull RenderContext renderContext,
										 @Nonnull String partName,
										 @Nonnull Function<String, ResourceLocation> textureFunc,
										 @Nonnull BiFunction<String, RenderContext, Boolean> preRenderFunc,
										 @Nonnull BiConsumer<String, RenderContext> postRenderFunc)
	{
		renderContext.Transforms.push();
		{
			boolean shouldRender = preRenderFunc.apply(partName, renderContext);
			if(shouldRender)
			{
				RenderPart(partName, textureFunc, renderContext);
				ForEachChild(partName, (childName, childAP) -> {
					renderContext.Transforms.push();
					renderContext.Transforms.add(childAP.Offset);
					RenderPartIteratively(renderContext, childName, textureFunc, preRenderFunc, postRenderFunc);
					renderContext.Transforms.pop();
				});
			}
			postRenderFunc.accept(partName, renderContext);
		}
		renderContext.Transforms.pop();
	}
	public void RenderPart(@Nonnull String partName,
						   @Nonnull Function<String, ResourceLocation> textureFunc,
						   @Nonnull RenderContext renderContext)
	{
		TurboModel unbakedModel = Unbaked != null ? Unbaked.GetPart(partName) : null;
		ETurboRenderMaterial material = unbakedModel != null ? unbakedModel.material : ETurboRenderMaterial.Cutout;
		RenderPart(partName, material, textureFunc.apply(partName), renderContext);
	}
	public void RenderPart(@Nonnull String partName,
						   @Nonnull ETurboRenderMaterial material,
						   @Nullable ResourceLocation withTexture,
						   @Nonnull RenderContext renderContext)
	{
		VertexConsumer vc = renderContext.Buffers.getBuffer(flanItemRenderType(withTexture, material));
		RenderPart(partName, renderContext.Transforms, vc, renderContext.Light, renderContext.Overlay);
	}
	public void RenderPart(@Nonnull String partName,
						   @Nonnull TransformStack transformStack,
						   @Nonnull VertexConsumer vc,
						   int light, int overlay)
	{
		if(USE_BAKED_TURBO_MODELS && Baked != null)
		{
			TurboModel.Baked bakedSection = Baked.GetPart(partName);
			if(bakedSection != null)
				RenderPart(bakedSection, vc, transformStack, light, overlay);
		}
		else if(Unbaked != null)
		{
			TurboModel unbakedSection = Unbaked.GetPart(partName);
			if(unbakedSection != null)
				RenderPart(unbakedSection, vc, transformStack, light, overlay, 1f/16f);
		}
		else
		{
			// Render a missing model?
		}
	}

	public void RenderPart(@Nonnull TurboModel.Baked bakedModel, @Nonnull VertexConsumer vc, @Nonnull TransformStack transformStack, int light, int overlay)
	{
		Minecraft.getInstance().getItemRenderer().renderQuadList(
			transformStack.top().toNewPoseStack(),
			vc,
			bakedModel.getQuads(
				null,
				null,
				Minecraft.getInstance().font.random),
			ItemStack.EMPTY,
			light,
			overlay
		);
	}

	public void RenderPart(@Nonnull TurboModel model, @Nonnull VertexConsumer vc, @Nonnull TransformStack transformStack, int light, int overlay, float scale)
	{
		Transform topPose = transformStack.top();
		if(USE_MODELVIEW_MATRIX_RENDER_MODE)
		{
			// Copy our pose into the model view matrix and upload it
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			modelViewStack.setIdentity();
			modelViewStack.mulPoseMatrix(transformStack.top().toNewPoseStack().last().pose());
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
					Vec3 nPosed = topPose.localToGlobalDirection(new Vec3(normal.x, normal.y, normal.z));

					for (int i = 0; i < 4; i++)
					{
						Vec3 vPosed = topPose.localToGlobalPosition(new Vec3(
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
