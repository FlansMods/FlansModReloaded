package com.flansmod.client.render.models;

import com.google.gson.*;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

@OnlyIn(Dist.CLIENT)
public class TurboFace
{
	public final String texture;
	public final BlockFaceUV uv;
	@Nullable
	TurboElement parent; // Parent canot be set by the constructor due to instantiation ordering. This shouldn't really ever be null, but it could theoretically be.

	public TurboFace(String texture,
					 BlockFaceUV uv)
	{
		this.texture = texture;
		this.uv = uv;
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
