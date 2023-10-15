package com.flansmod.client.render.models;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class MultiModel implements IUnbakedGeometry<MultiModel>, UnbakedModel
{
	public static final MultiModel.Loader LOADER = new MultiModel.Loader();

	public final ResourceLocation FirstPersonLocation;
	public final ResourceLocation ThirdPersonLocation;
	public final ResourceLocation HeadLocation;
	public final ResourceLocation GUILocation;
	public final ResourceLocation GroundLocation;
	public final ResourceLocation FixedLocation;

	public MultiModel(ResourceLocation first,
					  ResourceLocation third,
					  ResourceLocation head,
					  ResourceLocation gui,
					  ResourceLocation ground,
					  ResourceLocation fixed)
	{
		FirstPersonLocation = first;
		ThirdPersonLocation = third;
		HeadLocation = head;
		GUILocation = gui;
		GroundLocation = ground;
		FixedLocation = fixed;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// UnbakedModel interface
	// -----------------------------------------------------------------------------------------------------------------
	@Override
	@Nonnull
	public Collection<ResourceLocation> getDependencies()
	{
		return Set.of(FirstPersonLocation, ThirdPersonLocation, GroundLocation, GUILocation);
	}
	@Override
	public void resolveParents(@Nonnull Function<ResourceLocation, UnbakedModel> modelLookup)
	{

	}

	@Nullable
	@Override
	public BakedModel bake(@Nonnull ModelBaker baker,
						   @Nonnull Function<Material, TextureAtlasSprite> spriteLookup,
						   @Nonnull ModelState state,
						   @Nonnull ResourceLocation location)
	{
		return bake(null, baker, spriteLookup, state, null, location);
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context,
						   ModelBaker baker,
						   Function<Material, TextureAtlasSprite> spriteGetter,
						   ModelState state,
						   ItemOverrides overrides,
						   ResourceLocation modelLocation)
	{
		return new Baked(
			Minecraft.getInstance().getModelManager().getMissingModel(),
			baker.bake(FirstPersonLocation, state, spriteGetter),
			baker.bake(ThirdPersonLocation, state, spriteGetter),
			baker.bake(HeadLocation, state, spriteGetter),
			baker.bake(GUILocation, state, spriteGetter),
			baker.bake(GroundLocation, state, spriteGetter),
			baker.bake(FixedLocation, state, spriteGetter));
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Baked
	// -----------------------------------------------------------------------------------------------------------------
	public static class Baked implements BakedModel
	{
		public final BakedModel FirstPersonModel;
		public final BakedModel ThirdPersonModel;
		public final BakedModel HeadModel;
		public final BakedModel GUIModel;
		public final BakedModel GroundModel;
		public final BakedModel FixedModel;

		public final ItemOverrides Overrides;
		public final ItemTransforms Transforms;

		public Baked(@Nonnull BakedModel missingModel,
					 @Nullable BakedModel first,
					 @Nullable BakedModel third,
					 @Nullable BakedModel head,
					 @Nullable BakedModel gui,
					 @Nullable BakedModel ground,
					 @Nullable BakedModel fixed)
		{
			FirstPersonModel = first == null ? missingModel : first;
			ThirdPersonModel = third == null ? missingModel : third;
			HeadModel = head == null ? missingModel : head;
			GUIModel = gui == null ? missingModel : gui;
			GroundModel = ground == null ? missingModel : ground;
			FixedModel = fixed == null ? missingModel : fixed;

			Overrides = FirstPersonModel.getOverrides();
			Transforms = new ItemTransforms(
				ThirdPersonModel.getTransforms().thirdPersonLeftHand,
				ThirdPersonModel.getTransforms().thirdPersonRightHand,
				FirstPersonModel.getTransforms().firstPersonLeftHand,
				FirstPersonModel.getTransforms().firstPersonRightHand,

				HeadModel.getTransforms().head,
				GUIModel.getTransforms().gui,
				GroundModel.getTransforms().ground,
				FixedModel.getTransforms().fixed);
		}

		@Override
		@Nonnull
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @Nonnull RandomSource rng)
		{
			return Lists.newArrayList();
		}

		@Nonnull
		@Override
		public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, @Nonnull PoseStack poseStack, boolean applyLeftHandTransform)
		{
			switch(cameraTransformType)
			{
				case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> { return FirstPersonModel; }
				case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> { return ThirdPersonModel; }
				case HEAD -> { return HeadModel; }
				case GUI -> { return GUIModel; }
				case GROUND -> { return GroundModel; }
				case FIXED -> { return FixedModel; }
				default -> { return FixedModel; }
			}
		}

		@Override
		public boolean useAmbientOcclusion() { return false; }
		@Override
		public boolean isGui3d() { return true; }
		@Override
		public boolean usesBlockLight() { return false; }
		@Override
		public boolean isCustomRenderer() { return true; }
		@Override
		@Nonnull
		public TextureAtlasSprite getParticleIcon() { return FixedModel.getParticleIcon(); }
		@Override
		@Nonnull
		public ItemOverrides getOverrides() { return Overrides; }
		@Override
		@Nonnull
		public ItemTransforms getTransforms() { return Transforms; }
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Loader
	// -----------------------------------------------------------------------------------------------------------------
	public static class Loader implements IGeometryLoader<MultiModel>
	{
		private static final Gson GSON = (new GsonBuilder())
			.registerTypeAdapter(MultiModel.class, new MultiModel.Deserializer())
			.create();

		@Override
		public MultiModel read(JsonObject jsonObject,
							 JsonDeserializationContext deserializationContext) throws JsonParseException
		{
			return GSON.fromJson(jsonObject, MultiModel.class);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Deserializer
	// -----------------------------------------------------------------------------------------------------------------
	public static class Deserializer implements JsonDeserializer<MultiModel>
	{
		private static final ResourceLocation MISSING_MODEL_LOCATION = new ResourceLocation("minecraft", "missing");

		@Override
		public MultiModel deserialize(JsonElement jElement,
									  Type typeOfT,
									  JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jObject = jElement.getAsJsonObject();
			ResourceLocation firstPersonLocation =
				jObject.has("first_person_model")
					? new ResourceLocation(jObject.get("first_person_model").getAsString())
					: MISSING_MODEL_LOCATION;
			ResourceLocation thirdPersonLocation =
				jObject.has("third_person_model")
					? new ResourceLocation(jObject.get("third_person_model").getAsString())
					: MISSING_MODEL_LOCATION;
			ResourceLocation headLocation =
				jObject.has("head_model")
					? new ResourceLocation(jObject.get("head_model").getAsString())
					: MISSING_MODEL_LOCATION;
			ResourceLocation guiLocation =
				jObject.has("gui_model")
					? new ResourceLocation(jObject.get("gui_model").getAsString())
					: MISSING_MODEL_LOCATION;
			ResourceLocation groundLocation =
				jObject.has("ground_model")
					? new ResourceLocation(jObject.get("ground_model").getAsString())
					: MISSING_MODEL_LOCATION;
			ResourceLocation fixedLocation =
				jObject.has("fixed_model")
					? new ResourceLocation(jObject.get("fixed_model").getAsString())
					: MISSING_MODEL_LOCATION;

			return new MultiModel(
				firstPersonLocation,
				thirdPersonLocation,
				headLocation,
				guiLocation,
				groundLocation,
				fixedLocation);
		}
	}
}
