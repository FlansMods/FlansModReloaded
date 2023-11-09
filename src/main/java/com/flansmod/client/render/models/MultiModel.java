package com.flansmod.client.render.models;

import com.flansmod.util.Maths;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.json.Json;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;
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

	public final ResourceLocation[] Dependencies;

	public MultiModel(ResourceLocation first,
					  ResourceLocation third,
					  ResourceLocation head,
					  ResourceLocation gui,
					  ResourceLocation ground,
					  ResourceLocation fixed,
					  ResourceLocation[] otherDependencies)
	{
		Dependencies = otherDependencies;
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
		Set<ResourceLocation> set = Sets.newHashSet();

		set.add(FirstPersonLocation);
		set.add(ThirdPersonLocation);
		set.add(HeadLocation);
		set.add(GUILocation);
		set.add(GroundLocation);
		set.add(FixedLocation);
		set.addAll(Arrays.asList(Dependencies));

		return set;
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
		// Step 1. Bake each skinned model (it switches between the 3D model and the specific icon model)
		List<BakedModel> first = BakePerSkin(FirstPersonLocation, baker, state, spriteGetter);
		List<BakedModel> third = BakePerSkin(ThirdPersonLocation, baker, state, spriteGetter);
		List<BakedModel> head = BakePerSkin(HeadLocation, baker, state, spriteGetter);
		List<BakedModel> gui = BakePerSkin(GUILocation, baker, state, spriteGetter);
		List<BakedModel> ground = BakePerSkin(GroundLocation, baker, state, spriteGetter);
		List<BakedModel> fixed = BakePerSkin(FixedLocation, baker, state, spriteGetter);

		int maxSkinCount =
			Maths.Max(first.size(),
				Maths.Max(third.size(),
					Maths.Max(head.size(),
						Maths.Max(gui.size(),
							Maths.Max(ground.size(), fixed.size())))));
		BakedTransformSwitcher[] transformSwitchers = new BakedTransformSwitcher[maxSkinCount];
		for (int i = 0; i < maxSkinCount; i++)
		{
			BakedTransformSwitcher switcherForSkin = new BakedTransformSwitcher(
				Minecraft.getInstance().getModelManager().getMissingModel(),
				GetIndexOrLastOrNull(first, i),
				GetIndexOrLastOrNull(third, i),
				GetIndexOrLastOrNull(head, i),
				GetIndexOrLastOrNull(gui, i),
				GetIndexOrLastOrNull(ground, i),
				GetIndexOrLastOrNull(fixed, i)
			);
			transformSwitchers[i] = switcherForSkin;
		}

		return new BakedSkinSwitcher(
			GetIndexOrMissing(first, 0),
			transformSwitchers);

		//return new Baked(
		//	,
		//	baker.bake(FirstPersonLocation, state, spriteGetter),
		//	baker.bake(ThirdPersonLocation, state, spriteGetter),
		//	baker.bake(HeadLocation, state, spriteGetter),
		//	baker.bake(GUILocation, state, spriteGetter),
		//	baker.bake(GroundLocation, state, spriteGetter),
		//	baker.bake(FixedLocation, state, spriteGetter));
	}

	private BakedModel GetIndexOrMissing(List<BakedModel> list, int index)
	{
		if(index < list.size())
			return list.get(index);
		return Minecraft.getInstance().getModelManager().getMissingModel();
	}

	private BakedModel GetIndexOrLastOrNull(List<BakedModel> list, int index)
	{
		if(list.size() == 0)
			return null;
		if(index < list.size())
			return list.get(index);
		return list.get(list.size() - 1);
	}

	private List<BakedModel> BakePerSkin(ResourceLocation modelLocation, ModelBaker baker, ModelState state, Function<Material, TextureAtlasSprite> spriteGetter)
	{
		List<BakedModel> skinnedModels = new ArrayList<>();
		BakedModel rootModel = baker.bake(modelLocation, state, spriteGetter);
		if(rootModel != null)
		{
			int testIndex = 0;
			skinnedModels.add(rootModel);
			while(true)
			{
				ItemStack testStack = new ItemStack(Items.APPLE);
				testStack.getOrCreateTag().putInt("CustomModelData", testIndex+1);
				BakedModel skinnedModel = rootModel.getOverrides().resolve(rootModel, testStack, null, null, 0);
				if (skinnedModel == skinnedModels.get(skinnedModels.size() - 1))
				{
					break;
				}
				else
				{
					skinnedModels.add(skinnedModel);
				}
				testIndex++;
			}
		}
		return skinnedModels;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Baked
	// -----------------------------------------------------------------------------------------------------------------

	public static class BakedSkinSwitcher implements BakedModel
	{
		@Nonnull
		public final BakedModel DefaultModel;
		@Nonnull
		public final SkinSwitchingItemOverrides SkinSwitchingOverrides;

		public BakedSkinSwitcher(@Nonnull BakedModel wrapped, @Nonnull BakedModel[] skinSwitchedModels)
		{
			DefaultModel = wrapped;
			SkinSwitchingOverrides = new SkinSwitchingItemOverrides(skinSwitchedModels);
		}
		@Override
		@Nonnull
		public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction dir, @Nonnull RandomSource rand)
		{
			return DefaultModel.getQuads(blockState, dir, rand);
		}
		@Override
		public boolean useAmbientOcclusion() { return DefaultModel.useAmbientOcclusion(); }
		@Override
		public boolean isGui3d() { return DefaultModel.isGui3d(); }
		@Override
		public boolean usesBlockLight() { return DefaultModel.usesBlockLight(); }
		@Override
		public boolean isCustomRenderer() { return DefaultModel.isCustomRenderer(); }
		@Override
		@Nonnull
		public TextureAtlasSprite getParticleIcon() { return DefaultModel.getParticleIcon(); }
		@Override
		@Nonnull
		public ItemOverrides getOverrides() { return SkinSwitchingOverrides; }
	}

	public static class SkinSwitchingItemOverrides extends ItemOverrides
	{
		public final BakedModel[] PaintjobWrappers;

		public SkinSwitchingItemOverrides(BakedModel[] paintjobWrappers)
		{
			PaintjobWrappers = paintjobWrappers;
		}

		@Override
		public BakedModel resolve(@Nonnull BakedModel bakedModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity heldByEntity, int light)
		{
			int skinIndex = stack.hasTag() ? stack.getTag().getInt("CustomModelData") : 0;
			if(skinIndex < PaintjobWrappers.length)
				return PaintjobWrappers[(int)skinIndex];
			return bakedModel;
		}
	}

	public static class BakedTransformSwitcher implements BakedModel
	{
		public final BakedModel FirstPersonModel;
		public final BakedModel ThirdPersonModel;
		public final BakedModel HeadModel;
		public final BakedModel GUIModel;
		public final BakedModel GroundModel;
		public final BakedModel FixedModel;

		public BakedTransformSwitcher(@Nonnull BakedModel missingModel,
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
		public ItemOverrides getOverrides() { return FirstPersonModel.getOverrides(); }
		@Override
		@Nonnull
		public ItemTransforms getTransforms() { return FirstPersonModel.getTransforms(); }
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

			ResourceLocation[] dependencies = new ResourceLocation[0];
			if(jObject.has("dependencies"))
			{
				JsonArray depArray = jObject.get("dependencies").getAsJsonArray();
				dependencies = new ResourceLocation[depArray.size()];
				for(int i = 0; i < depArray.size(); i++)
				{
					dependencies[i] = new ResourceLocation(depArray.get(i).getAsString());
				}
			}

			return new MultiModel(
				firstPersonLocation,
				thirdPersonLocation,
				headLocation,
				guiLocation,
				groundLocation,
				fixedLocation,
				dependencies);
		}
	}
}
