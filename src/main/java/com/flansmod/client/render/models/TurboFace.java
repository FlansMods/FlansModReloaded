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
		Vector3f normal = EstimateNormal(positions);
		direction = EstimateFacing(normal);

		return new BakedQuad(
			VertexMagic(positions, normal, uvData),
			-1, // tintIndex
			direction,
			new TurboTextureAtlasSprite(skin),
			false, // ???
			false // ambient occlusion
		);
	}

	private static Vector3f EstimateNormal(Vector3f[] positions)
	{
		Vector3f axis1 = Maths.Sub(positions[0], positions[1]);
		Vector3f axis2 = Maths.Sub(positions[2], positions[1]);
		Vector3f normal = Maths.Cross(axis1, axis2).normalize();
		return normal;
	}

	private static Direction EstimateFacing(Vector3f normal)
	{
		return normal.isFinite() ? Direction.getNearest(normal.x, normal.y, normal.z) : Direction.UP;
	}

	public static int[] VertexMagic(Vector3f[] positions, Vector3f normal, BlockFaceUV uvs)
	{
		// 4 verts * 8 bytes???
		int[] magicVertData = new int[4 * 9];



		int compressedNormal = ((((byte) (normal.x() * 127.0f)) & 0xFF) << 24) |
			((((byte) (normal.y() * 127.0f)) & 0xFF) << 16) |
			((((byte) (normal.z() * 127.0f)) & 0xFF) << 8);

		compressedNormal = 0xffa08040;

		for(int i = 0; i < positions.length; i++)
		{
			// WHAATT?
			int offset = i * 8;
			float u = uvs.getU(i); //sprite.getU(uvs.getU(i) * 0.999d + uvs.getU((i + 2) % 4) * 0.001d);
			float v = uvs.getV(i); //sprite.getV(uvs.getV(i) * 0.999d + uvs.getV((i + 2) % 4) * 0.001d);

			magicVertData[offset + 0] = Float.floatToRawIntBits(positions[i].x());	// 0
			magicVertData[offset + 1] = Float.floatToRawIntBits(positions[i].y());	// 4
			magicVertData[offset + 2] = Float.floatToRawIntBits(positions[i].z());	// 8
			magicVertData[offset + 3] = -1; // Colour = 0xffffffff					// 12
			magicVertData[offset + 4] = Float.floatToRawIntBits(u);					// 16
			magicVertData[offset + 5] = Float.floatToRawIntBits(v);					// 20
			magicVertData[offset + 6] = (0xf << 16) | (0xf); // UV1 = lighting map				// 24
			magicVertData[offset + 7] = compressedNormal; // UV2 = ???								// 28
			//magicVertData[offset + 8] = compressedNormal;
		}

		return magicVertData;
	}

	// public static final VertexFormat NEW_ENTITY =
	// new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
	// .put("Position", ELEMENT_POSITION)
	// .put("Color", ELEMENT_COLOR)
	// .put("UV0", ELEMENT_UV0)
	// .put("UV1", ELEMENT_UV1)
	// .put("UV2", ELEMENT_UV2)
	// .put("Normal", ELEMENT_NORMAL)
	// .put("Padding", ELEMENT_PADDING)
	// .build());
	//

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
