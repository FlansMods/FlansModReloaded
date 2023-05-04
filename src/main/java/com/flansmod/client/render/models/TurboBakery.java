package com.flansmod.client.render.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class TurboBakery
{
	public BakedQuad[] Bake(TurboElement element)
	{

	}

	public BakedQuad Bake(TurboFace face, TextureAtlasSprite sprite)
	{
		BlockFaceUV uvData = face.uv;

		// Copy the uv data into a temp array
		float[] uvs = new float[uvData.uvs.length];
		System.arraycopy(uvData.uvs, 0, uvs, 0, uvs.length);

		//
		float shrink = sprite.uvShrinkRatio();
		float f1 = (uvs[0] + uvs[0] + uvs[2] + uvs[2]) / 4.0F;
		float f2 = (uvs[1] + uvs[1] + uvs[3] + uvs[3]) / 4.0F;
		uvData.uvs[0] = Mth.lerp(f, blockfaceuv.uvs[0], f1);
		uvData.uvs[2] = Mth.lerp(f, blockfaceuv.uvs[2], f1);
		uvData.uvs[1] = Mth.lerp(f, blockfaceuv.uvs[1], f2);
		uvData.uvs[3] = Mth.lerp(f, blockfaceuv.uvs[3], f2);

		Vector3f[] positions = new Vector3f[4];
		


		int[] rawVertexInfo = new int[32];
		Direction direction = calculateFacing(aint);

		return new BakedQuad(
			VertexMagic(positions, sprite, uvData),
			0, // tintIndex
			direction,
			sprite,
			false, // ???
			false // ambient occlusion
		)

	}

	public int[] CreateRawVerts(TurboFace face)
	{
		int[] vertData = new int[32];



		return vertData;
	}

	public int[] VertexMagic(Vector3f[] positions, TextureAtlasSprite sprite, BlockFaceUV uvs)
	{
		// 4 verts * 8 bytes???
		int[] magicVertData = new int[32];

		for(int i = 0; i < positions.length; i++)
		{
			// WHAATT?
			float u = sprite.getU(uvs.getU(i) * 0.999d + uvs.getU((i + 2) % 4) * 0.001d);
			float v = sprite.getV(uvs.getV(i) * 0.999d + uvs.getV((i + 2) % 4) * 0.001d);

			magicVertData[i + 0] = Float.floatToRawIntBits(positions[i].x());
			magicVertData[i + 1] = Float.floatToRawIntBits(positions[i].y());
			magicVertData[i + 2] = Float.floatToRawIntBits(positions[i].z());
			magicVertData[i + 3] = -1;
			magicVertData[i + 4] = Float.floatToRawIntBits(u);
			magicVertData[i + 5] = Float.floatToRawIntBits(v);
		}
	}

	public int CreateVertex(Vector3f v)
	{

	}
}
