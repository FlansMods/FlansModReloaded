package com.flansmod.client.render.models;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class TurboElement
{
	public final Vector3f[] vertices;
	public final Map<Direction, TurboFace> faces;
	public final boolean shade;
	public final Vector3f eulerRotations;
	public final Vector3f rotationOrigin;

	public TurboElement(Vector3f[] vertices,
						Map<Direction, TurboFace> faces,
						Vector3f eulerRotations,
						Vector3f rotationOrigin,
						boolean shade) {
		this.vertices = vertices;
		this.faces = faces;
		this.eulerRotations = eulerRotations;
		this.rotationOrigin = rotationOrigin;
		//this.fillUvs();
		this.shade = shade;
		this.faces.values().forEach(face -> face.parent = this);
	}

	public TurboFace GetFace(Direction direction) { return faces.get(direction); }

	private static final int[] UP_VERTS 	= new int[] { 7,3,2,6 };
	private static final int[] DOWN_VERTS 	= new int[] { 5,4,0,1 };
	private static final int[] NORTH_VERTS 	= new int[] { 0,2,3,1 };
	private static final int[] EAST_VERTS 	= new int[] { 1,3,7,5 };
	private static final int[] SOUTH_VERTS 	= new int[] { 5,7,6,4 };
	private static final int[] WEST_VERTS 	= new int[] { 4,6,2,0 };

	public Vector3f[] GetFaceVertices(Direction direction, boolean applyRotation)
	{
		switch(direction)
		{
			case UP -> 		{ return GetQuadByVertIndexes(UP_VERTS, applyRotation); }
			case DOWN -> 	{ return GetQuadByVertIndexes(DOWN_VERTS, applyRotation); }
			case NORTH -> 	{ return GetQuadByVertIndexes(NORTH_VERTS, applyRotation); }
			case EAST -> 	{ return GetQuadByVertIndexes(EAST_VERTS, applyRotation); }
			case SOUTH -> 	{ return GetQuadByVertIndexes(SOUTH_VERTS, applyRotation); }
			case WEST -> 	{ return GetQuadByVertIndexes(WEST_VERTS, applyRotation); }
			default -> 		{ return new Vector3f[4]; }
		}
	}

	private Vector3f[] GetQuadByVertIndexes(int[] indices, boolean applyRotation)
	{
		if(applyRotation)
		{
			// TODO: Check order
			Quaternionf rotation = new Quaternionf().rotateZYX(eulerRotations.z, eulerRotations.y, eulerRotations.x);
			return new Vector3f[] {
				new Vector3f(vertices[indices[0]]).rotate(rotation).add(rotationOrigin),
				new Vector3f(vertices[indices[1]]).rotate(rotation).add(rotationOrigin),
				new Vector3f(vertices[indices[2]]).rotate(rotation).add(rotationOrigin),
				new Vector3f(vertices[indices[3]]).rotate(rotation).add(rotationOrigin)
			};
		}
		else return new Vector3f[] {
			new Vector3f(vertices[indices[0]]),
			new Vector3f(vertices[indices[1]]),
			new Vector3f(vertices[indices[2]]),
			new Vector3f(vertices[indices[3]])
		};
	}

	/*
	private void fillUvs()
	{
		for(var kvp : faces.entrySet())
		{
			float[] afloat = this.uvsByFace(kvp.getKey());
			(kvp.getValue()).uv.setMissingUv(afloat);
		}
	}


	public float[] uvsByFace(Direction dir)
	{
		return switch (dir)
		{
			case DOWN -> new float[]{from.x(), 16.0F - to.z(), to.x(), 16.0F - from.z()};
			case UP -> new float[]{from.x(), from.z(), to.x(), to.z()};
			default -> new float[]{16.0F - to.x(), 16.0F - to.y(), 16.0F - from.x(), 16.0F - from.y()};
			case SOUTH -> new float[]{from.x(), 16.0F - to.y(), to.x(), 16.0F - from.y()};
			case WEST -> new float[]{from.z(), 16.0F - to.y(), to.z(), 16.0F - from.y()};
			case EAST -> new float[]{16.0F - to.z(), 16.0F - to.y(), 16.0F - from.z(), 16.0F - from.y()};
		};
	}*/

	@OnlyIn(Dist.CLIENT)
	public static class Deserializer implements JsonDeserializer<TurboElement>
	{
		private static final boolean DEFAULT_SHADE = true;

		public TurboElement deserialize(JsonElement jElement,
										Type p_111330_,
										JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jObject = jElement.getAsJsonObject();

			JsonArray jVertArray = jObject.get("verts").getAsJsonArray();
			Vector3f[] vertices = new Vector3f[8];
			if(jVertArray.size() == 8)
			{
				for(int i = 0; i < 8; i++)
				{
					vertices[i] = getVector3f(jVertArray.get(i));
				}
			}
			Vector3f eulerRotations = getVector3f(jObject, "eulerRotations");
			Vector3f rotationOrigin = getVector3f(jObject, "rotationOrigin");
			Map<Direction, TurboFace> map = getFaces(context, jObject);

			if (jObject.has("shade") && !GsonHelper.isBooleanValue(jObject, "shade"))
				throw new JsonParseException("Expected shade to be a Boolean");

			boolean shade = GsonHelper.getAsBoolean(jObject, "shade", true);
			return new TurboElement(
				vertices,
				map,
				eulerRotations,
				rotationOrigin,
				shade);
		}

		private Map<Direction, TurboFace> getFaces(JsonDeserializationContext context,
														  JsonObject jObject)
		{
			Map<Direction, TurboFace> map = filterNullFromFaces(context, jObject);
			if (map.isEmpty())
			{
				throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
			}
			else
			{
				return map;
			}
		}

		private Map<Direction, TurboFace> filterNullFromFaces(JsonDeserializationContext context,
															  JsonObject jObject) {
			Map<Direction, TurboFace> map = Maps.newEnumMap(Direction.class);
			JsonObject jsonobject = GsonHelper.getAsJsonObject(jObject, "faces");

			for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet())
			{
				Direction direction = this.getFacing(entry.getKey());
				map.put(direction, context.deserialize(entry.getValue(), TurboFace.class));
			}

			return map;
		}

		private Direction getFacing(String p_111338_)
		{
			Direction direction = Direction.byName(p_111338_);
			if (direction == null) {
				throw new JsonParseException("Unknown facing: " + p_111338_);
			} else {
				return direction;
			}
		}

		private Vector3f getVector3f(JsonElement jObject)
		{
			JsonArray jArray = jObject.getAsJsonArray();
			return new Vector3f(
				jArray.get(0).getAsFloat(),
				jArray.get(1).getAsFloat(),
				jArray.get(2).getAsFloat());
		}

		private Vector3f getVector3f(JsonObject jObject, String key)
		{
			JsonArray jArray = GsonHelper.getAsJsonArray(jObject, key);
			if (jArray.size() != 3)
			{
				throw new JsonParseException("Expected 3 " + key + " values, found: " + jArray.size());
			}
			else
			{
				float[] afloat = new float[3];

				for(int i = 0; i < afloat.length; ++i)
				{
					afloat[i] = GsonHelper.convertToFloat(jArray.get(i), key + "[" + i + "]");
				}

				return new Vector3f(afloat[0], afloat[1], afloat[2]);
			}
		}
	}

}
