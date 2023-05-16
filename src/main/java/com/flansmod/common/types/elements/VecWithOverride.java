package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;

public class VecWithOverride
{
	public Vector3f ResolveF(Map<String, Float> parameters)
	{
		float xVal = x.map(Double::floatValue, parameters::get);
		float yVal = y.map(Double::floatValue, parameters::get);
		float zVal = z.map(Double::floatValue, parameters::get);
		return new Vector3f(xVal, yVal, zVal);
	}

	public Vec3 Resolve(Map<String, Double> parameters)
	{
		double xVal = x.map((d) -> { return d; }, parameters::get);
		double yVal = y.map((d) -> { return d; }, parameters::get);
		double zVal = z.map((d) -> { return d; }, parameters::get);
		return new Vec3(xVal, yVal, zVal);
	}

	public Either<Double, String> x;
	public Either<Double, String> y;
	public Either<Double, String> z;

	public VecWithOverride()
	{
		x = Either.left(0d);
		y = Either.left(0d);
		z = Either.left(0d);
	}


	public static VecWithOverride ParseFunc(Object ref, JsonElement jNode, JsonField annot)
	{
		VecWithOverride vec = ref == null ? new VecWithOverride() : (VecWithOverride)ref;

		JsonArray jVec = jNode.getAsJsonArray();
		try { vec.x = Either.left(jVec.get(0).getAsDouble()); }
		catch(Exception e) { vec.x = Either.right(jVec.get(0).getAsString()); }

		try { vec.y = Either.left(jVec.get(1).getAsDouble()); }
		catch(Exception e) { vec.y = Either.right(jVec.get(1).getAsString()); }

		try { vec.z = Either.left(jVec.get(2).getAsDouble()); }
		catch(Exception e) { vec.z = Either.right(jVec.get(2).getAsString()); }

		return vec;
	}
}
