package com.flansmod.common.entity.vehicle;

import com.flansmod.common.entity.vehicle.hierarchy.VehicleComponentPath;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PerPartMap<T>
{
	@Nonnull
	private final Map<Integer, T> Values = new HashMap<>();


	@Nonnull
	public Set<Integer> HashKeys() { return Values.keySet(); }
	@Nonnull
	public Collection<T> Values() { return Values.values(); }
	@Nonnull
	public Set<Map.Entry<Integer, T>> EntrySet() { return Values.entrySet(); }


	@Nonnull
	public <R> R ApplyOrDefault(int hash, @Nonnull Function<T, R> func, @Nonnull R defaultValue)
	{
		if(Values.containsKey(hash))
			return func.apply(Values.get(hash));
		return defaultValue;
	}
	@Nonnull
	public T GetOrDefault(int hash, @Nonnull T defaultValue) { return Values.getOrDefault(hash, defaultValue); }
	@Nullable
	public T ForHash(int hash) { return Values.get(hash); }
	public void Put(int hash, @Nonnull T value) { Values.put(hash, value); }
	public void ApplyTo(int hash, @Nonnull Consumer<T> applyFunc)
	{
		if(Values.containsKey(hash))
			applyFunc.accept(Values.get(hash));
	}
	@Nonnull
	public Optional<T> TryGet(int hash) { return Optional.ofNullable(Values.get(hash)); }

	@Nonnull
	public <R> R ApplyOrDefault(@Nonnull VehicleComponentPath componentPath, @Nonnull Function<T, R> func, @Nonnull R defaultValue) { return ApplyOrDefault(componentPath.hashCode(), func, defaultValue); }
	@Nonnull
	public T GetOrDefault(@Nonnull VehicleComponentPath componentPath, @Nonnull T defaultValue) { return GetOrDefault(componentPath.hashCode(), defaultValue); }
	@Nullable
	public T ForPart(@Nonnull VehicleComponentPath componentPath) { return ForHash(componentPath.hashCode()); }
	@Nonnull
	public Optional<T> TryGet(@Nonnull VehicleComponentPath componentPath) { return TryGet(componentPath.hashCode()); }
	public void Put(@Nonnull VehicleComponentPath componentPath, @Nonnull T value) { Put(componentPath.hashCode(), value); }
	public void ApplyTo(@Nonnull VehicleComponentPath componentPath, @Nonnull Consumer<T> applyFunc) { ApplyTo(componentPath.hashCode(), applyFunc); }
	public void CreateAndApply(@Nonnull VehicleComponentPath componentPath, @Nonnull Supplier<T> createFunc, @Nonnull Consumer<T> applyFunc)
	{
		int hash = componentPath.hashCode();
		if(!Values.containsKey(hash))
			Values.put(hash, createFunc.get());
		applyFunc.accept(Values.get(hash));
	}

	@Override
	public boolean equals(Object other)
	{
		if(other instanceof PerPartMap<?> otherMap)
		{
			return Values.equals(otherMap.Values);
		}
		return false;
	}

	public static <TDataType> EntityDataSerializer<PerPartMap<TDataType>> SERIALIZER(
		@Nonnull EntityDataSerializer<TDataType> elementSerializer)
	{
		return new EntityDataSerializer.ForValueType<>()
		{
			@Override
			public void write(@Nonnull FriendlyByteBuf buf, @Nonnull PerPartMap<TDataType> data)
			{
				buf.writeInt(data.Values.size());
				for(var kvp : data.Values.entrySet())
				{
					buf.writeInt(kvp.getKey());
					elementSerializer.write(buf, kvp.getValue());
				}
			}

			@Override
			@Nonnull
			public PerPartMap<TDataType> read(@Nonnull FriendlyByteBuf buf)
			{
				int numEntries = buf.readInt();
				PerPartMap<TDataType> map = new PerPartMap<TDataType>();
				for(int i = 0; i < numEntries; i++)
				{
					int hash = buf.readInt();
					TDataType element = elementSerializer.read(buf);
					map.Values.put(hash, element);
				}
				return map;
			}
		};
	}
}
