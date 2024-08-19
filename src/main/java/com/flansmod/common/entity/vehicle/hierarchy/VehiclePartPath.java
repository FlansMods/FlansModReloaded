package com.flansmod.common.entity.vehicle.hierarchy;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;

import javax.annotation.Nonnull;
import java.util.Objects;

public record VehiclePartPath(@Nonnull Either<Integer, ImmutableList<String>> Source)
{
	public static final VehiclePartPath Invalid = new VehiclePartPath(Either.left(0));
	public static final VehiclePartPath Core = new VehiclePartPath(Either.right(ImmutableList.of()));

	@Nonnull
	public static VehiclePartPath of(int hash)
	{
		return new VehiclePartPath(Either.left(hash));
	}
	@Nonnull
	public static VehiclePartPath of(@Nonnull ImmutableList<String> names)
	{
		return new VehiclePartPath(Either.right(names));
	}
	@Nonnull
	public static VehiclePartPath of(@Nonnull String string)
	{
		String[] components = string.split("/");
		return new VehiclePartPath(Either.right(ImmutableList.copyOf(components)));
	}
	@Nonnull
	public VehiclePartPath Compact() {
		return IsNamed() ? VehiclePartPath.of(hashCode()) : this;
	}
	public boolean IsRoot() { return Source.map((i) -> i == 0, (list) -> list.size() == 0); }
	public boolean IsNamed() { return Source.right().isPresent(); }

	@Nonnull public VehicleComponentPath Damageable() { return VehicleComponentPath.of(this, EPartDefComponent.Damage, 0); }
	@Nonnull public VehicleComponentPath Articulation() { return VehicleComponentPath.of(this, EPartDefComponent.Articulation, 0); }
	@Nonnull public VehicleComponentPath Arm(int index) { return VehicleComponentPath.of(this, EPartDefComponent.Arm, index); }
	@Nonnull public VehicleComponentPath Leg(int index) { return VehicleComponentPath.of(this, EPartDefComponent.Leg, index); }
	@Nonnull public VehicleComponentPath Seat(int index) { return VehicleComponentPath.of(this, EPartDefComponent.Seat, index); }
	@Nonnull public VehicleComponentPath Gun(int index) { return VehicleComponentPath.of(this, EPartDefComponent.Gun, index); }
	@Nonnull public VehicleComponentPath Wheel(int index) { return VehicleComponentPath.of(this, EPartDefComponent.Wheel, index); }
	@Nonnull public VehicleComponentPath Propeller(int index) { return VehicleComponentPath.of(this, EPartDefComponent.Propeller, index); }


	@Override
	public int hashCode()
	{
		return Source.map(
			Integer::intValue,
			(stringList) ->
			{
				int hash = 0;
				for(String s : stringList)
					hash = Objects.hash(hash, s);
				return hash;
			});
	}
	@Override
	public String toString()
	{
		return Source.map(
				(i) -> Integer.toString(i),
				(stringList) ->
				{
					StringBuilder sb = new StringBuilder();
					for(int i = 0; i < stringList.size(); i++)
					{
						sb.append(stringList.get(i));
						if(i != stringList.size() - 1)
							sb.append("/");
					}
					return sb.toString();
				});
	}
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof VehiclePartPath otherPath)
		{
			return otherPath.hashCode() == hashCode();
		}
		return false;
	}


}
