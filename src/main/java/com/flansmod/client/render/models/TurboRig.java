package com.flansmod.client.render.models;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.textures.UnitTextureAtlasSprite;
import org.jetbrains.annotations.Nullable;

import javax.json.Json;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public class TurboRig implements IUnbakedGeometry<TurboRig>, UnbakedModel
{
	public static final Loader LOADER = new Loader();

	private final Map<String, TurboModel> Parts;
	private final Map<String, ResourceLocation> Textures;
	private final ItemTransforms Transforms;
	private final Map<String, Float> FloatParams;

	public TurboRig(Map<String, TurboModel> parts, Map<String, ResourceLocation> textures, ItemTransforms transforms, Map<String, Float> floatParams)
	{
		Parts = parts;
		Textures = textures;
		Transforms = transforms;
		FloatParams = floatParams;
	}

	public Map<String, Float> GetFloatParams() { return FloatParams; }

	public TurboModel GetPart(String partName) { return Parts.get(partName); }

	public ItemTransform GetTransforms(ItemTransforms.TransformType transformType) { return Transforms.getTransform(transformType); }
	@Override
	public Collection<ResourceLocation> getDependencies() { return Collections.emptyList(); }
	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelLookup) {}
	@Nullable
	@Override
	public BakedModel bake(ModelBaker p_250133_, Function<Material, TextureAtlasSprite> p_119535_, ModelState p_119536_, ResourceLocation p_119537_)
	{
		return new BakedModelProxy();
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context,
						   ModelBaker baker,
						   Function<Material, TextureAtlasSprite> spriteGetter,
						   ModelState modelState,
						   ItemOverrides overrides,
						   ResourceLocation modelLocation)
	{
		Map<String, TurboModel.Baked> bakedModels = new HashMap<>();
		for(var kvp : Parts.entrySet())
		{
			bakedModels.put(kvp.getKey(), (TurboModel.Baked)kvp.getValue().bake(context, baker, spriteGetter, modelState, overrides, modelLocation));
		}

		return new Baked(bakedModels, Textures, Transforms);
	}

	public static class Baked implements BakedModel
	{
		private final Map<String, TurboModel.Baked> BakedModels;
		private final Map<String, ResourceLocation> Textures;
		private final ItemTransforms Transforms;

		public Baked(Map<String, TurboModel.Baked> bakedParts,
					 Map<String, ResourceLocation> textures,
					 ItemTransforms transforms)
		{
			BakedModels = bakedParts;
			Textures = textures;
			Transforms = transforms;
		}

		public void ApplyTransform(ItemTransforms.TransformType transformType, PoseStack ms, boolean b)
		{
			Transforms.getTransform(transformType).apply(b, ms);
		}

		public TurboModel.Baked GetPart(String part)
		{
			return BakedModels.get(part);
		}

		public ResourceLocation GetTexture(String skin)
		{
			return Textures.get(skin);
		}

		@Override
		public List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState p_235039_, @org.jetbrains.annotations.Nullable Direction p_235040_, RandomSource p_235041_)
		{
			return Collections.emptyList();
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
		public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
		@Override
		public ItemTransforms getTransforms() { return Transforms; }
	}

	public static class Loader implements IGeometryLoader<TurboRig>
	{
		private static final Gson GSON = (new GsonBuilder())
			.registerTypeAdapter(TurboRig.class, new TurboRig.Deserializer())
			.registerTypeAdapter(TurboModel.class, new TurboModel.Deserializer())
			.registerTypeAdapter(TurboElement.class, new TurboElement.Deserializer())
			.registerTypeAdapter(TurboFace.class, new TurboFace.Deserializer())
			.registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer())
			.registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
			.registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
			.create();

		@Override
		public TurboRig read(JsonObject jsonObject,
							   JsonDeserializationContext deserializationContext) throws JsonParseException
		{
			return GSON.fromJson(jsonObject, TurboRig.class);
		}
	}

	public static class Deserializer implements JsonDeserializer<TurboRig>
	{
		@Override
		public TurboRig deserialize(JsonElement jElement,
									   Type typeOfT,
									   JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jObject = jElement.getAsJsonObject();

			Map<String, TurboModel> parts = new HashMap<>();
			if(jObject.has("parts"))
			{
				JsonObject jParts = jObject.getAsJsonObject("parts");
				for(var kvp : jParts.entrySet())
				{
					String key = kvp.getKey();
					TurboModel model = context.deserialize(kvp.getValue(), TurboModel.class);
					if(model != null)
					{
						parts.put(key, model);
					}
				}
			}

			Map<String, Float> floatParams = new HashMap<>();
			if(jObject.has("animations"))
			{
				JsonObject jAnimObject = GsonHelper.getAsJsonObject(jObject, "animations");
				for(var kvp : jAnimObject.entrySet())
				{
					try
					{
						floatParams.put(kvp.getKey(), kvp.getValue().getAsFloat());
					}
					catch(JsonParseException jsonParseException)
					{
						try
						{
							floatParams.put(kvp.getKey(), kvp.getValue().getAsBoolean() ? 1.0f : 0.0f);
						}
						catch (JsonParseException jsonParseException1)
						{
							// oh well
						}
					}
				}
			}

			Map<String, ResourceLocation> textures = new HashMap<>();
			if (jObject.has("textures"))
			{
				JsonObject jTextureObject = GsonHelper.getAsJsonObject(jObject, "textures");
				for(var kvp : jTextureObject.entrySet())
				{
					ResourceLocation texLoc = ResourceLocation.tryParse(kvp.getValue().getAsString());
					texLoc = texLoc.withPath("textures/" + texLoc.getPath() + ".png");
					textures.put(kvp.getKey(), texLoc);
				}
			}


			ItemTransforms itemTransforms = ItemTransforms.NO_TRANSFORMS;
			if (jObject.has("display"))
			{
				JsonObject jDisplayObject = GsonHelper.getAsJsonObject(jObject, "display");
				itemTransforms = context.deserialize(jDisplayObject, ItemTransforms.class);

				for(ItemTransforms.TransformType transformType : ItemTransforms.TransformType.values())
				{
					itemTransforms.getTransform(transformType).translation.mul(16f);
				}
			}

			return new TurboRig(parts, textures, itemTransforms, floatParams);
		}
	}
}
