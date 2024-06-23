package com.flansmod.common.entity.vehicle.hierarchy;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;

import javax.annotation.Nonnull;
import java.util.Objects;

public record VehicleComponentPath(@Nonnull VehiclePartPath Part,
								   @Nonnull EPartDefComponent Type,
								   int Index)
{
	public static final VehicleComponentPath Invalid = new VehicleComponentPath(VehiclePartPath.Invalid, EPartDefComponent.Articulation, 0);
	@Nonnull public static VehicleComponentPath coreDamage() { return new VehicleComponentPath(VehiclePartPath.Core, EPartDefComponent.Damage, 0); }

	@Override
	public int hashCode()
	{
		return Objects.hash(Part.hashCode(), Type, Index);
	}
	@Override
	public String toString()
	{
		return Part + ":" + Type + "_" + Index;
	}
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof VehicleComponentPath otherPath)
		{
			return otherPath.Part.equals(Part)
				&& otherPath.Type == Type
				&& otherPath.Index == Index;
		}
		return false;
	}

	@Nonnull
	public static VehicleComponentPath of(@Nonnull String string)
	{
		String[] partComponentSplit = string.split(":");
		if(partComponentSplit.length == 2)
		{
			VehiclePartPath partPath = VehiclePartPath.of(partComponentSplit[0]);
			String[] underscoreSplit = partComponentSplit[1].split("_");
			if(underscoreSplit.length == 2)
			{
				return of(partPath, EPartDefComponent.valueOf(underscoreSplit[0]), Integer.parseInt(underscoreSplit[1]));
			}
		}
		return VehicleComponentPath.Invalid;
	}
	@Nonnull
	public static VehicleComponentPath of(@Nonnull VehiclePartPath part, @Nonnull EPartDefComponent type, int childIndex)
	{
		return new VehicleComponentPath(part, type, childIndex);
	}
	@Nonnull
	public static VehicleComponentPath of(int pathHash, @Nonnull EPartDefComponent type, int childIndex)
	{
		return new VehicleComponentPath(new VehiclePartPath(Either.left(pathHash)), type, childIndex);
	}
	@Nonnull
	public static VehicleComponentPath of(@Nonnull ImmutableList<String> names, @Nonnull EPartDefComponent type, int childIndex)
	{
		return new VehicleComponentPath(new VehiclePartPath(Either.right(names)), type, childIndex);
	}
}
