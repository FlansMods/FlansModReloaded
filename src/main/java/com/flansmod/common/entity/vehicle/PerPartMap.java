package com.flansmod.common.entity.vehicle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PerPartMap<T>
{
	@Nonnull
	public final Map<Integer, T> Values = new HashMap<>();

	@Nonnull
	public <R> R ApplyOrDefault(@Nonnull String partName, @Nonnull Function<T, R> func, @Nonnull R defaultValue)
	{
		if(Values.containsKey(partName.hashCode()))
			return func.apply(Values.get(partName.hashCode()));
		return defaultValue;
	}
	@Nonnull
	public T GetOrDefault(@Nonnull String partName, @Nonnull T defaultValue) { return Values.getOrDefault(partName.hashCode(), defaultValue); }
	@Nullable
	public T ForHash(int hash) { return Values.get(hash); }
	@Nullable
	public T ForPart(@Nonnull String partName) { return Values.get(partName.hashCode()); }
	public void Put(@Nonnull String partName, @Nonnull T value)
	{
		Values.put(partName.hashCode(), value);
	}
	public void ApplyTo(@Nonnull String partName, @Nonnull Consumer<T> applyFunc)
	{
		if(Values.containsKey(partName.hashCode()))
			applyFunc.accept(Values.get(partName.hashCode()));
	}
	public void CreateAndApply(@Nonnull String partName, @Nonnull Supplier<T> createFunc, @Nonnull Consumer<T> applyFunc)
	{
		if(!Values.containsKey(partName.hashCode()))
			Values.put(partName.hashCode(), createFunc.get());
		applyFunc.accept(Values.get(partName.hashCode()));
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
