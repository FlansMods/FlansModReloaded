package com.flansmod.client.render.models.unbaked;

import com.flansmod.client.render.models.ITurboDeserializer;
import com.flansmod.client.render.models.baked.BakedAttachPoint;
import com.flansmod.client.render.models.baked.BakedModelProxy;
import com.flansmod.client.render.models.baked.BakedTurboRig;
import com.flansmod.client.render.models.baked.BakedTurboSection;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.physics.common.util.Transform;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public class TurboRig implements IUnbakedGeometry<TurboRig>, UnbakedModel
{
	public static final Loader LOADER = new Loader();
	public static final ResourceLocation ICON_KEY_3D = new ResourceLocation("flansmod", "3d_icon");
	static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

	private final Map<String, TurboSection> Parts;
	private final Map<String, ResourceLocation> Textures;
	private final Map<String, ResourceLocation> Icons;
	private final ItemTransforms Transforms;
	private final Map<String, Float> FloatParams;
	private final Map<String, AttachPoint> AttachPoints;
	private final Vector2i TextureSize;

	public TurboRig(@Nonnull Map<String, TurboSection> parts,
					@Nonnull Map<String, ResourceLocation> textures,
					@Nonnull Map<String, ResourceLocation> icons,
					@Nonnull ItemTransforms transforms,
					@Nonnull Map<String, Float> floatParams,
					@Nonnull Map<String, AttachPoint> attachPoints,
					@Nonnull Vector2i textureSize)
	{
		Parts = parts;
		Textures = textures;
		Icons = icons;
		Transforms = transforms;
		FloatParams = floatParams;
		AttachPoints = attachPoints;
		TextureSize = textureSize;
	}

	public Set<Map.Entry<String, AttachPoint>> GetAttachmentPoints() { return AttachPoints.entrySet(); }

	public AttachPoint GetAttachPoint(String attachmentName) { return AttachPoints.getOrDefault(attachmentName, AttachPoint.Invalid); }

	public AttachPoint GetAttachPoint(EAttachmentType attachmentType, int index)
	{
		if(index == 0 && AttachPoints.containsKey(attachmentType.name().toLowerCase()))
			return AttachPoints.get(attachmentType.name().toLowerCase());
		if(AttachPoints.containsKey(attachmentType.name().toLowerCase() + "_" + index))
			return AttachPoints.get(attachmentType.name().toLowerCase() + "_" + index);
		return AttachPoint.Invalid;
	}

	public Map<String, Float> GetFloatParams() { return FloatParams; }

	public TurboSection GetPart(String partName) { return Parts.get(partName); }

	public ItemTransform GetTransforms(ItemDisplayContext transformType) { return Transforms.getTransform(transformType); }
	@Override
	@Nonnull
	public Collection<ResourceLocation> getDependencies() { return Collections.emptyList(); }
	@Override
	public void resolveParents(@Nonnull Function<ResourceLocation, UnbakedModel> modelLookup) {}
	@Nullable
	@Override
	public BakedModel bake(@Nonnull ModelBaker p_250133_, @Nonnull Function<Material, TextureAtlasSprite> p_119535_, @Nonnull ModelState p_119536_, @Nonnull ResourceLocation p_119537_)
	{
		return new BakedModelProxy();
	}

	@Override @Nonnull
	public BakedTurboRig bake(@Nullable IGeometryBakingContext context,
							  @Nonnull ModelBaker baker,
							  @Nonnull Function<Material, TextureAtlasSprite> spriteGetter,
							  @Nonnull ModelState modelState,
							  @Nullable ItemOverrides overrides,
							  @Nonnull ResourceLocation modelLocation)
	{
		ImmutableMap.Builder<String, BakedModel> iconModels = new ImmutableMap.Builder<>();
		ImmutableMap.Builder<String, BakedTurboSection> sections = new ImmutableMap.Builder<>();
		ImmutableMap.Builder<String, BakedAttachPoint> attachPoints = new ImmutableMap.Builder<>();

		for(var kvp : Icons.entrySet())
		{
			Map<String, Either<Material, String>> textureMap = Maps.newHashMap();
			Material material = new Material(TextureAtlas.LOCATION_BLOCKS, kvp.getValue());
			TextureAtlasSprite sprite = spriteGetter.apply(material);
			List<BlockElement> elements = ITEM_MODEL_GENERATOR.processFrames(0, "default", sprite.contents());
			textureMap.put("default", Either.left(material));
			textureMap.put("particle", Either.left(material));
			BlockModel itemModel = new BlockModel((ResourceLocation)null, elements, textureMap, false, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, List.of());
			iconModels.put(kvp.getKey(), itemModel.bake(baker, spriteGetter, modelState, modelLocation));
		}

		for(var kvp : Parts.entrySet())
		{
			sections.put(kvp.getKey(), kvp.getValue().bake(context, baker, spriteGetter, modelState, overrides, modelLocation, TextureSize));
		}

		attachPoints.put("body", new BakedAttachPoint(null, Transform.identity(), false));
		for(var kvp : AttachPoints.entrySet())
		{
			attachPoints.put(kvp.getKey(), kvp.getValue().bake());
		}

		return new BakedTurboRig(
				iconModels.build(),
				sections.build(),
				attachPoints.build(),
				Textures,
				FloatParams,
				Transforms);
	}

	public static class Loader implements IGeometryLoader<TurboRig>
	{
		private static final Gson GSON = (new GsonBuilder())
			.registerTypeAdapter(TurboRig.class, new TurboRig.Deserializer())
			.registerTypeAdapter(TurboSection.class, new TurboSection.Deserializer())
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

	public static class Deserializer implements JsonDeserializer<TurboRig>, ITurboDeserializer
	{
		@Override
		public TurboRig deserialize(JsonElement jElement,
									   Type typeOfT,
									   JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jObject = jElement.getAsJsonObject();

			Map<String, TurboSection> parts = new HashMap<>();
			if(jObject.has("parts"))
			{
				JsonObject jParts = jObject.getAsJsonObject("parts");
				for(var kvp : jParts.entrySet())
				{
					String key = kvp.getKey();
					TurboSection model = context.deserialize(kvp.getValue(), TurboSection.class);
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
					catch(Exception exception)
					{
						try
						{
							floatParams.put(kvp.getKey(), kvp.getValue().getAsBoolean() ? 1.0f : 0.0f);
						}
						catch (Exception exception1)
						{
							try
							{
								JsonArray jArray = kvp.getValue().getAsJsonArray();

								float x = jArray.size() > 0 ? jArray.get(0).getAsFloat() : 0.0f;
								float y = jArray.size() > 1 ? jArray.get(1).getAsFloat() : 0.0f;
								float z = jArray.size() > 2 ? jArray.get(2).getAsFloat() : 0.0f;

								floatParams.put(kvp.getKey() + "_x", x);
								floatParams.put(kvp.getKey() + "_y", y);
								floatParams.put(kvp.getKey() + "_z", z);
							}
							catch (Exception exception2)
							{
								// Well I'm out of ideas
								FlansMod.LOGGER.warn("Could not parse " + kvp.getValue() + " into float anim params");
							}
						}
					}
				}
			}

			Map<String, AttachPoint> attachPoints = new HashMap<>();
			if(jObject.has("attachPoints"))
			{
				JsonArray jAPArray = GsonHelper.getAsJsonArray(jObject, "attachPoints");
				for(var jAPElement : jAPArray.asList())
				{
					JsonObject jAPObject = jAPElement.getAsJsonObject();
					String name = jAPObject.get("name").getAsString();
					String attachTo = jAPObject.get("attachTo").getAsString();
					Vector3f offset = getOrDefaultVector3f(jAPObject, "offset", new Vector3f());
					Vector3f euler = getOrDefaultVector3f(jAPObject, "euler", new Vector3f());
					boolean physics = jAPObject.has("physics") && jAPObject.get("physics").getAsBoolean();
					attachPoints.put(name, new AttachPoint(attachTo, offset, euler, physics));
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

			Vector2i textureSize = new Vector2i(16, 16);
			if(jObject.get("textureSize") instanceof JsonArray jTextureSize)
			{
				textureSize = getVector2i(jTextureSize);
			}

			ItemTransforms itemTransforms = ItemTransforms.NO_TRANSFORMS;
			if (jObject.has("display"))
			{
				JsonObject jDisplayObject = GsonHelper.getAsJsonObject(jObject, "display");
				itemTransforms = context.deserialize(jDisplayObject, ItemTransforms.class);
			}

			Map<String, ResourceLocation> iconMap = new HashMap<>();
			if(jObject.has("icons"))
			{
				JsonObject jIcons = jObject.get("icons").getAsJsonObject();
				for(var kvp : jIcons.entrySet())
				{
					iconMap.put(kvp.getKey(), new ResourceLocation(kvp.getValue().getAsString()));
				}
			}

			return new TurboRig(parts, textures, iconMap, itemTransforms, floatParams, attachPoints, textureSize);
		}
	}
}
