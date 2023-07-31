package com.flansmod.client.render.models;

import com.flansmod.common.FlansMod;
import com.flansmod.common.item.AttachmentItem;
import com.flansmod.util.Maths;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.json.Json;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public class TurboRig implements IUnbakedGeometry<TurboRig>, UnbakedModel
{
	public static final Loader LOADER = new Loader();

	public static final class AttachPoint
	{
		public final String AttachTo;
		public final Vector3f Offset;
		public AttachPoint(String attachTo, Vector3f offset)
		{
			AttachTo = attachTo;
			Offset = offset;
		}
		public static final AttachPoint Invalid = new AttachPoint("", new Vector3f());
	}

	private final Map<String, TurboModel> Parts;
	private final Map<String, ResourceLocation> Textures;
	private final ItemTransforms Transforms;
	private final Map<String, Float> FloatParams;
	private final Map<String, AttachPoint> AttachPoints;

	public TurboRig(Map<String, TurboModel> parts,
					Map<String, ResourceLocation> textures,
					ItemTransforms transforms,
					Map<String, Float> floatParams,
					Map<String, AttachPoint> attachPoints)
	{
		Parts = parts;
		Textures = textures;
		Transforms = transforms;
		FloatParams = floatParams;
		AttachPoints = attachPoints;
	}

	public AttachPoint GetAttachPoint(String attachmentName) { return AttachPoints.getOrDefault(attachmentName, AttachPoint.Invalid); }

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
			//if(transformType == ItemTransforms.TransformType.GROUND)
			//{
				//Vector3f copyOfTranslation = new Vector3f(Transforms.getTransform(transformType).translation);
				//Vector3f eulers = new Vector3f(90f, 0f, 0f);

				//Maths.QuaternionFromEuler(eulers).transform(copyOfTranslation);

				//new ItemTransform(eulers,
				//	copyOfTranslation, //.mul(-1f).add(0f, 0.5f, 0.5f),
				//	new Vector3f(1f / 16f, 1f / 16f, 1f / 16f)).apply(b, ms);

				//new ItemTransform(new Vector3f(-90f, 0f, 0f),
				//	new Vector3f(0f, 0f, 3f),
				//	new Vector3f(1f/16f, 1f/16f, 1f/16f)).apply(b, ms);
			//}
			//else
			ItemTransform transform = Transforms.getTransform(transformType);
			transform.apply(b, ms);
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
		public ItemTransforms getTransforms()
		{
			return Transforms;
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
					attachPoints.put(name, new AttachPoint(attachTo, offset));
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

			return new TurboRig(parts, textures, itemTransforms, floatParams, attachPoints);
		}

		private Vector3f getVector3f(JsonElement jObject)
		{
			JsonArray jArray = jObject.getAsJsonArray();
			return new Vector3f(
				jArray.get(0).getAsFloat(),
				jArray.get(1).getAsFloat(),
				jArray.get(2).getAsFloat());
		}
	}
}
