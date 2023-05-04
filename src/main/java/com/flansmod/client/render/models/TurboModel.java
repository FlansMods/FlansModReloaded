package com.flansmod.client.render.models;

import com.google.common.collect.*;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TurboModel implements IUnbakedGeometry<TurboModel>, UnbakedModel
{
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final FaceBakery FACE_BAKERY = new FaceBakery();


	private final List<TurboElement> elements;
	private final BlockModel.GuiLight guiLight;
	public final boolean hasAmbientOcclusion;
	private final ItemTransforms transforms;
	private final List<ItemOverride> overrides;
	public final Map<String, Either<Material, String>> textureMap;
	@Nullable
	public TurboModel parent;
	@Nullable
	protected ResourceLocation parentLocation;

	public TurboModel(@Nullable ResourceLocation parentLocation,
					  List<TurboElement> elements,
					  Map<String, Either<Material, String>> textureMap,
					  boolean hasAmbientOcclusion,
					  @Nullable BlockModel.GuiLight guiLight,
					  ItemTransforms transforms,
					  List<ItemOverride> overrides)
	{
		this.elements = elements;
		this.hasAmbientOcclusion = hasAmbientOcclusion;
		this.guiLight = guiLight;
		this.textureMap = textureMap;
		this.parentLocation = parentLocation;
		this.transforms = transforms;
		this.overrides = overrides;
	}

	@Nullable
	public ResourceLocation getParentLocation() { return parentLocation; }
	public boolean hasAmbientOcclusion() { return parent != null ? parent.hasAmbientOcclusion() : hasAmbientOcclusion; }
	public boolean isResolved() { return parentLocation == null || parent != null && parent.isResolved(); }
	public List<ItemOverride> getOverrides() {
		return overrides;
	}
	// getItemOverrides
	// getOverrides
	public Collection<ResourceLocation> getDependencies()
	{
		Set<ResourceLocation> set = Sets.newHashSet();
		for(ItemOverride itemoverride : overrides)
			set.add(itemoverride.getModel());

		if (parentLocation != null)
			set.add(parentLocation);

		return set;
	}
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelFinderFunction)
	{
		Set<UnbakedModel> set = Sets.newLinkedHashSet();

		for(TurboModel turboModel = this;
			turboModel.parentLocation != null && turboModel.parent == null;
			turboModel = turboModel.parent)
		{
			set.add(turboModel);
			UnbakedModel unbakedmodel = modelFinderFunction.apply(turboModel.parentLocation);
			if (unbakedmodel == null) {
				LOGGER.warn("No parent '{}' while loading model '{}'", this.parentLocation, turboModel);
			}

			if (set.contains(unbakedmodel)) {
				LOGGER.warn("Found 'parent' loop while loading model '{}' in chain: {} -> {}", turboModel, set.stream().map(Object::toString).collect(Collectors.joining(" -> ")), this.parentLocation);
				unbakedmodel = null;
			}

			if (unbakedmodel == null) {
				turboModel.parentLocation = ModelBakery.MISSING_MODEL_LOCATION;
				unbakedmodel = modelFinderFunction.apply(turboModel.parentLocation);
			}

			if (!(unbakedmodel instanceof BlockModel)) {
				throw new IllegalStateException("BlockModel parent has to be a block model.");
			}

			turboModel.parent = (TurboModel)unbakedmodel;
		}

		overrides.forEach((override) ->
		{
			UnbakedModel unbakedmodel1 = modelFinderFunction.apply(override.getModel());
			if (!Objects.equals(unbakedmodel1, this))
			{
				unbakedmodel1.resolveParents(modelFinderFunction);
			}
		});
	}

	@org.jetbrains.annotations.Nullable
	@Override
	public BakedModel bake(ModelBaker baker,
						   Function<Material, TextureAtlasSprite> spriteLookupFunction,
						   ModelState p_119536_,
						   ResourceLocation p_119537_)
	{
		return null;
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context,
						   ModelBaker baker,
						   Function<Material, TextureAtlasSprite> spriteGetter,
						   ModelState modelState,
						   ItemOverrides overrides,
						   ResourceLocation modelLocation)
	{
		Material particleLocation = context.getMaterial("particle");
		TextureAtlasSprite particle = spriteGetter.apply(particleLocation);

		// Find the root transform of the context
		var rootTransform = context.getRootTransform();
		if (!rootTransform.isIdentity())
			modelState = new SimpleModelState(modelState.getRotation().compose(rootTransform), modelState.isUvLocked());




		return new Baked(

		)
	}

	public static class Baked implements IDynamicBakedModel
	{
		private static final FaceBakery FACE_BAKERY = new FaceBakery();
		private final TextureAtlasSprite particle;
		private final ItemOverrides overrides;

		public Baked(TextureAtlasSprite particle,
					 ItemOverrides overrides)
		{
			this.particle = particle;
			this.overrides = overrides;
		}

		@Override
		public @NotNull List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState state, @org.jetbrains.annotations.Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @org.jetbrains.annotations.Nullable RenderType renderType)
		{
			return null;
		}

		@Override
		public boolean useAmbientOcclusion() { return false; }
		@Override
		public boolean isGui3d() { return true;}
		@Override
		public boolean usesBlockLight() { return false; }
		@Override
		public boolean isCustomRenderer() { return false; }
		@Override
		public TextureAtlasSprite getParticleIcon() { return particle; }
		@Override
		public ItemOverrides getOverrides() { return overrides; }

		public static class Builder
		{

		}
	}


	public static final class Loader implements IGeometryLoader<TurboModel>
	{
		public static final TurboModel.Loader INSTANCE = new TurboModel.Loader();

		private Loader()
		{
		}

		@Override
		public TurboModel read(JsonObject jsonObject,
							   JsonDeserializationContext deserializationContext)
		{

		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class Deserializer implements JsonDeserializer<TurboModel>
	{
		private static final boolean DEFAULT_AMBIENT_OCCLUSION = true;

		public TurboModel deserialize(JsonElement jElement,
									  Type p_111499_,
									  JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jObject = jElement.getAsJsonObject();
			List<TurboElement> elementList = getElements(context, jObject);
			String parentName = getParentName(jObject);
			Map<String, Either<Material, String>> map = this.getTextureMap(jObject);
			boolean flag = this.getAmbientOcclusion(jObject);

			ItemTransforms itemTransforms = ItemTransforms.NO_TRANSFORMS;
			if (jObject.has("display"))
			{
				JsonObject jDisplayObject = GsonHelper.getAsJsonObject(jObject, "display");
				itemTransforms = context.deserialize(jDisplayObject, ItemTransforms.class);
			}

			List<ItemOverride> list1 = this.getOverrides(context, jObject);
			BlockModel.GuiLight blockmodel$guilight = null;
			if (jObject.has("gui_light")) {
				blockmodel$guilight = BlockModel.GuiLight.getByName(GsonHelper.getAsString(jObject, "gui_light"));
			}

			ResourceLocation parentLocation = parentName.isEmpty() ? null : new ResourceLocation(parentName);
			return new TurboModel(
				parentLocation,
				elementList,
				map,
				flag,
				blockmodel$guilight,
				itemTransforms,
				list1);
		}

		protected List<ItemOverride> getOverrides(JsonDeserializationContext p_111495_, JsonObject p_111496_) {
			List<ItemOverride> list = Lists.newArrayList();
			if (p_111496_.has("overrides")) {
				for(JsonElement jsonelement : GsonHelper.getAsJsonArray(p_111496_, "overrides")) {
					list.add(p_111495_.deserialize(jsonelement, ItemOverride.class));
				}
			}

			return list;
		}

		private Map<String, Either<Material, String>> getTextureMap(JsonObject jObject)
		{
			ResourceLocation resourcelocation = TextureAtlas.LOCATION_BLOCKS;
			Map<String, Either<Material, String>> map = Maps.newHashMap();
			if (jObject.has("textures"))
			{
				JsonObject jTextureObject = GsonHelper.getAsJsonObject(jObject, "textures");
				for(var kvp : jTextureObject.entrySet())
				{
					map.put(kvp.getKey(), parseTextureLocationOrReference(resourcelocation, kvp.getValue().getAsString()));
				}
			}

			return map;
		}

		private static Either<Material, String> parseTextureLocationOrReference(ResourceLocation p_111504_, String p_111505_) {
			if (TurboModel.isTextureReference(p_111505_)) {
				return Either.right(p_111505_.substring(1));
			} else {
				ResourceLocation resourcelocation = ResourceLocation.tryParse(p_111505_);
				if (resourcelocation == null) {
					throw new JsonParseException(p_111505_ + " is not valid resource location");
				} else {
					return Either.left(new Material(p_111504_, resourcelocation));
				}
			}
		}

		private String getParentName(JsonObject jObject)
		{
			return GsonHelper.getAsString(jObject, "parent", "");
		}

		protected boolean getAmbientOcclusion(JsonObject jObject)
		{
			return GsonHelper.getAsBoolean(jObject, "ambientocclusion", true);
		}

		protected List<TurboElement> getElements(JsonDeserializationContext context,
												 JsonObject jObject)
		{
			List<TurboElement> list = Lists.newArrayList();
			if (jObject.has("elements"))
			{
				for(JsonElement jElement : GsonHelper.getAsJsonArray(jObject, "elements"))
				{
					list.add(context.deserialize(jElement, TurboElement.class));
				}
			}

			return list;
		}
	}
}
