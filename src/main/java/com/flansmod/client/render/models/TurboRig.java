package com.flansmod.client.render.models;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
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
import org.joml.Quaternionf;
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

	public static final class AttachPoint
	{
		public static class Baked
		{
			@Nonnull
			public final String PartName;
			@Nullable
			public final Baked Parent;
			@Nonnull
			public final Vector3f Offset;
			@Nonnull
			public final Quaternionf Rotation;

			public Baked(@Nonnull String partName)
			{
				PartName = partName;
				Parent = null;
				Offset = new Vector3f();
				Rotation = new Quaternionf();
			}

			public Baked(@Nonnull String partName, @Nonnull Baked parent, @Nonnull Vector3f offset, @Nonnull Vector3f euler)
			{
				PartName = partName;
				Parent = parent;
				Offset = offset;
				Rotation = Transform.FromEuler(euler);
			}
		}

		public final String AttachTo;
		public final Vector3f Offset;
		public final Vector3f Euler;
		public AttachPoint(String attachTo, Vector3f offset, Vector3f euler)
		{
			AttachTo = attachTo;
			Offset = offset;
			Euler = euler;
		}
		public boolean IsValid() { return AttachTo.length() > 0; }
		public static final AttachPoint Invalid = new AttachPoint("", new Vector3f(), new Vector3f());
	}


	private final Map<String, TurboModel> Parts;
	private final Map<String, ResourceLocation> Textures;
	private final Map<String, ResourceLocation> Icons;
	private final ItemTransforms Transforms;
	private final Map<String, Float> FloatParams;
	private final Map<String, AttachPoint> AttachPoints;

	public TurboRig(Map<String, TurboModel> parts,
					Map<String, ResourceLocation> textures,
					Map<String, ResourceLocation> icons,
					ItemTransforms transforms,
					Map<String, Float> floatParams,
					Map<String, AttachPoint> attachPoints)
	{
		Parts = parts;
		Textures = textures;
		Icons = icons;
		Transforms = transforms;
		FloatParams = floatParams;
		AttachPoints = attachPoints;
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

	public TurboModel GetPart(String partName) { return Parts.get(partName); }

	public ItemTransform GetTransforms(ItemTransforms.TransformType transformType) { return Transforms.getTransform(transformType); }
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

	@Override
	public BakedModel bake(IGeometryBakingContext context,
						   ModelBaker baker,
						   Function<Material, TextureAtlasSprite> spriteGetter,
						   ModelState modelState,
						   ItemOverrides overrides,
						   ResourceLocation modelLocation)
	{
		Map<String, BakedModel> iconModels = new HashMap<>();
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

		Map<String, TurboModel.Baked> bakedModels = new HashMap<>();
		for(var kvp : Parts.entrySet())
		{
			bakedModels.put(kvp.getKey(), (TurboModel.Baked)kvp.getValue().bake(context, baker, spriteGetter, modelState, overrides, modelLocation));
		}

		Map<String, AttachPoint.Baked> bakedAPs = new HashMap<>();
		bakedAPs.put("body", new AttachPoint.Baked("body"));
		for(var kvp : AttachPoints.entrySet())
		{
			BakeAP(kvp.getKey(), kvp.getValue(), bakedAPs);
		}

		return new Baked(iconModels, bakedModels, bakedAPs, Textures, Transforms);
	}

	private void BakeAP(@Nonnull String apName, @Nonnull AttachPoint ap, @Nonnull Map<String, AttachPoint.Baked> bakedAPs)
	{
		if(!bakedAPs.containsKey(ap.AttachTo))
		{
			AttachPoint parentAP = AttachPoints.get(ap.AttachTo);
			if(parentAP != null)
			{
				BakeAP(ap.AttachTo, parentAP, bakedAPs);
			}
		}

		if(!bakedAPs.containsKey(apName) && bakedAPs.containsKey(ap.AttachTo))
		{
			bakedAPs.put(apName, new AttachPoint.Baked(
				apName,
				bakedAPs.get(ap.AttachTo),
				ap.Offset.mul(1f/16f, new Vector3f()),
				ap.Euler));
		}
	}


	public static class Baked implements BakedModel
	{
		private final Map<String, BakedModel> IconModels;
		private final Map<String, TurboModel.Baked> BakedModels;
		private final Map<String, AttachPoint.Baked> BakedAPs;
		private final Map<String, ResourceLocation> Textures;

		private final ItemTransforms Transforms;

		public Baked(Map<String, BakedModel> bakedIcons,
					 Map<String, TurboModel.Baked> bakedParts,
					 Map<String, AttachPoint.Baked> bakedAPs,
					 Map<String, ResourceLocation> textures,
					 ItemTransforms transforms)
		{
			IconModels = bakedIcons;
			BakedModels = bakedParts;
			BakedAPs = bakedAPs;
			Textures = textures;
			Transforms = transforms;
		}

		@Nonnull
		public Set<Map.Entry<String, AttachPoint.Baked>> GetAttachPoints() { return BakedAPs.entrySet(); }
		@Nullable
		public AttachPoint.Baked GetAttachPoint(String apName)
		{
			return BakedAPs.get(apName);
		}

		@Nullable
		public BakedModel GetIconModel(String skin)
		{
			if(IconModels.containsKey(skin))
			{
				return IconModels.get(skin);
			}
			return null;
		}

		public Transform GetTransform(ItemTransforms.TransformType transformType)
		{
			return new Transform("BakedItemTransform["+transformType+"]", Transforms.getTransform(transformType));
		}

		public void ApplyTransform(TransformStack transformStack, ItemTransforms.TransformType transformType, boolean bFlip)
		{
			ItemTransform transform = Transforms.getTransform(transformType);
			transformStack.add(new Transform("BakedItemTransform["+transformType+"]", transform));
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
		@Nonnull
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
		@Nonnull
		public TextureAtlasSprite getParticleIcon() { return UnitTextureAtlasSprite.INSTANCE; }
		@Override
		@Nonnull
		public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
		@Override
		@Nonnull
		public ItemTransforms getTransforms()
		{
			return ItemTransforms.NO_TRANSFORMS;
		}
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
					Vector3f offset = getVector3f(jAPObject.get("offset"));
					Vector3f euler = getVector3f(jAPObject.get("euler"));
					attachPoints.put(name, new AttachPoint(attachTo, offset, euler));
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

			return new TurboRig(parts, textures, iconMap, itemTransforms, floatParams, attachPoints);
		}

		private Vector3f getVector3f(JsonElement jObject)
		{
			if(jObject != null)
			{
				JsonArray jArray = jObject.getAsJsonArray();
				return new Vector3f(
					jArray.get(0).getAsFloat(),
					jArray.get(1).getAsFloat(),
					jArray.get(2).getAsFloat());
			}
			return new Vector3f();
		}
	}
}
