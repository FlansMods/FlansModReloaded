package com.flansmod.client.render.models;

import com.flansmod.common.FlansMod;
import com.google.common.collect.*;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.ElementsModel;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.textures.UnitTextureAtlasSprite;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
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

	private final List<TurboElement> elements;
	private final BlockModel.GuiLight guiLight;
	public final boolean hasAmbientOcclusion;
	private final ItemTransforms transforms;
	private final List<ItemOverride> overrides;
	public final Vector3f offset;
	//public final Map<String, Either<Material, String>> textureMap;
	public final Map<String, ResourceLocation> textures;


	@Nullable
	public TurboModel parent;
	@Nullable
	protected ResourceLocation parentLocation;

	public TurboModel(@Nullable ResourceLocation parentLocation,
					  List<TurboElement> elements,
					  //Map<String, Either<Material, String>> textureMap,
					  Map<String, ResourceLocation> textures,
					  boolean hasAmbientOcclusion,
					  @Nullable BlockModel.GuiLight guiLight,
					  ItemTransforms transforms,
					  List<ItemOverride> overrides,
					  Vector3f offset)
	{
		this.elements = elements;
		this.hasAmbientOcclusion = hasAmbientOcclusion;
		this.guiLight = guiLight;
		//this.textureMap = textureMap;
		this.textures = textures;
		this.parentLocation = parentLocation;
		this.transforms = transforms;
		this.overrides = overrides;
		this.offset = offset;
	}

	public List<TurboElement> GetElements() { return elements; }

	// -----------------------------------------------------------------------------------------------------------------
	// UnbakedModel interface
	// -----------------------------------------------------------------------------------------------------------------
	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		Set<ResourceLocation> set = Sets.newHashSet();
		return set;
	}
	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelLookup)
	{
		// No parenting
	}
	@Nullable
	@Override
	public BakedModel bake(ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ResourceLocation location)
	{
		return bake(null, bakery, spriteGetter, state, null, location);
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

		List<BakedQuad> quad = new ArrayList<>(elements.size() * 6);
		for(TurboElement element : elements)
		{
			for(Direction direction : element.faces.keySet())
			{
				TurboFace face = element.faces.get(direction);
				ResourceLocation skin = ResolveSkin(face.texture);
				quad.add(face.Bake(skin, element, direction));
			}
		}

		return new Baked(quad, overrides, transforms);
	}

	public static class Baked implements BakedModel
	{
		private final List<BakedQuad> quads;
		private final ItemOverrides overrides;
		private final ItemTransforms transforms;

		public Baked(List<BakedQuad> quads, ItemOverrides overrides, ItemTransforms transforms)
		{
			this.transforms = transforms;
			this.quads = quads;
			this.overrides = overrides;
		}

		// -----------------------------------------------------------------------------------------------------------------
		// BakedModel interface
		// -----------------------------------------------------------------------------------------------------------------
		public List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState state,
										@org.jetbrains.annotations.Nullable Direction direction, RandomSource rng)
		{
			return quads;
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
		public TextureAtlasSprite getParticleIcon() { return UnitTextureAtlasSprite.INSTANCE; }
		@Override
		public ItemOverrides getOverrides() { return overrides; }
		@Override
		public ItemTransforms getTransforms() { return transforms; }
	}

	private ResourceLocation ResolveSkin(String path)
	{
		if (isTextureReference(path))
			path = path.substring(1);

		for(TurboModel modelLineage = this; modelLineage != null; modelLineage = modelLineage.parent)
		{
			ResourceLocation skin = modelLineage.textures.get(path);
			if (skin != null)
				return skin;
		}

		return MissingTextureAtlasSprite.getLocation();
	}

	private static boolean isTextureReference(String key) { return key.charAt(0) == '#'; }



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
			Map<String, ResourceLocation> map = this.getTextureMap(jObject);
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

			Vector3f offset = new Vector3f();
			if(jObject.has("origin"))
			{
				offset = getVector3f(jObject.get("origin"));
			}

			ResourceLocation parentLocation = parentName.isEmpty() ? null : new ResourceLocation(parentName);
			return new TurboModel(
				parentLocation,
				elementList,
				map,
				flag,
				blockmodel$guilight,
				itemTransforms,
				list1,
				offset);
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

		private Map<String, ResourceLocation> getTextureMap(JsonObject jObject)
		{
			Map<String, ResourceLocation> map = Maps.newHashMap();
			if (jObject.has("textures"))
			{
				JsonObject jTextureObject = GsonHelper.getAsJsonObject(jObject, "textures");
				for(var kvp : jTextureObject.entrySet())
				{
					map.put(kvp.getKey(), parseTextureLocationOrReference(kvp.getValue().getAsString()));
				}
			}

			return map;
		}

		private Vector3f getVector3f(JsonElement jObject)
		{
			JsonArray jArray = jObject.getAsJsonArray();
			return new Vector3f(
				jArray.get(0).getAsFloat(),
				jArray.get(1).getAsFloat(),
				jArray.get(2).getAsFloat());
		}

		private static ResourceLocation parseTextureLocationOrReference(String textureName)
		{
			return ResourceLocation.tryParse(textureName);
			/*
			if (TurboModel.isTextureReference(textureName))
			{
				return Either.right(textureName.substring(1));
			}
			else
			{
				ResourceLocation resourcelocation = ResourceLocation.tryParse(textureName);
				if (resourcelocation == null)
				{
					throw new JsonParseException(textureName + " is not valid resource location");
				}
				else
				{
					return Either.left(new Material(atlas, resourcelocation));
				}
			}
			*/
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
			if (jObject.has("turboelements"))
			{
				for(JsonElement jElement : GsonHelper.getAsJsonArray(jObject, "turboelements"))
				{
					list.add(context.deserialize(jElement, TurboElement.class));
				}
			}

			return list;
		}
	}
}
