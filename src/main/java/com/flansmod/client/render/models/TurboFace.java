package com.flansmod.client.render.models;

import com.flansmod.util.Maths;
import com.google.gson.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.textures.UnitTextureAtlasSprite;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

@OnlyIn(Dist.CLIENT)
public class TurboFace
{
	public final String texture;
	public final BlockFaceUV uvData;
	public Direction cullDirection;

	@Nullable
	TurboElement parent; // Parent canot be set by the constructor due to instantiation ordering. This shouldn't really ever be null, but it could theoretically be.

	public TurboFace(String texture,
					 BlockFaceUV uv)
	{
		this.texture = texture;
		this.uvData = uv;
	}

	public BakedQuad Bake(ResourceLocation skin,
						  TurboElement element,
						  Direction direction)
	{
		Vector3f[] positions = element.GetFaceVertices(direction, true);
		// If this element is rotated, it might have a different facing, let's check
		direction = EstimateFacing(positions);

		return new BakedQuad(
			VertexMagic(positions, uvData),
			0, // tintIndex
			direction,
			new TurboTextureAtlasSprite(skin),
			false, // ???
			false // ambient occlusion
		);
	}

	private static Direction EstimateFacing(Vector3f[] positions)
	{
		Vector3f axis1 = Maths.Sub(positions[0], positions[1]);
		Vector3f axis2 = Maths.Sub(positions[2], positions[1]);
		Vector3f normal = Maths.Cross(axis1, axis2).normalize();

		return normal.isFinite() ? Direction.getNearest(normal.x, normal.y, normal.z) : Direction.UP;
	}

	public static int[] VertexMagic(Vector3f[] positions, BlockFaceUV uvs)
	{
		// 4 verts * 8 bytes???
		int[] magicVertData = new int[32];

		for(int i = 0; i < positions.length; i++)
		{
			// WHAATT?
			int offset = i * 8;
			float u = uvs.getU(i); //sprite.getU(uvs.getU(i) * 0.999d + uvs.getU((i + 2) % 4) * 0.001d);
			float v = uvs.getV(i); //sprite.getV(uvs.getV(i) * 0.999d + uvs.getV((i + 2) % 4) * 0.001d);

			magicVertData[offset + 0] = Float.floatToRawIntBits(positions[i].x());
			magicVertData[offset + 1] = Float.floatToRawIntBits(positions[i].y());
			magicVertData[offset + 2] = Float.floatToRawIntBits(positions[i].z());
			magicVertData[offset + 3] = -1;
			magicVertData[offset + 4] = Float.floatToRawIntBits(u);
			magicVertData[offset + 5] = Float.floatToRawIntBits(v);
		}

		return magicVertData;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Deserializer implements JsonDeserializer<TurboFace>
	{
		private static final int DEFAULT_TINT_INDEX = -1;

		public TurboFace deserialize(JsonElement jElement,
									 Type p_111366_,
									 JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jObject = jElement.getAsJsonObject();
			String texture = getTexture(jObject);
			BlockFaceUV uv = context.deserialize(jObject, BlockFaceUV.class);
			return new TurboFace(texture, uv);
		}

		private String getTexture(JsonObject jObject)
		{
			return GsonHelper.getAsString(jObject, "texture");
		}
	}
}
