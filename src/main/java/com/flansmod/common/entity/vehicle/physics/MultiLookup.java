package com.flansmod.common.entity.vehicle.physics;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.hierarchy.WheelEntity;
import com.flansmod.common.types.vehicles.elements.EControlLogicHint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class MultiLookup<THint, TValue>
{
	private final Map<String, List<TValue>> ByPartName = new HashMap<>();
	private final List<TValue> ByIndex = new ArrayList<>();
	private final Map<THint, List<TValue>> ByHint = new HashMap<>();

	@Nonnull public Collection<TValue> All() { return ByIndex; }
	@Nullable public TValue ByIndex(int index) { return ByIndex.get(index); }
	@Nonnull public Collection<TValue> ByPart(@Nonnull String partName) { return ByPartName.getOrDefault(partName, List.of()); }
	@Nonnull public Collection<TValue> ByHint(@Nonnull THint hint) { return ByHint.getOrDefault(hint, List.of()); }
	@Nonnull
	public Collection<TValue> ByHints(@Nonnull THint ... hints)
	{
		if(hints.length == 0)
			return List.of();
		Collection<TValue> check0 = ByHint(hints[0]);
		if(hints.length == 1)
			return check0;



		List<TValue> validWheels = new ArrayList<>(check0.size());
		for(THint hint : hints)
		{
			Collection<TValue> checkForHint = ByHint(hint);
			for(TValue t : checkForHint)
			{
				if(!validWheels.contains(t))
					validWheels.add(t);
			}
		}
		return validWheels;
	}
	public void ForEach(@Nonnull Consumer<TValue> func)
	{
		for(TValue t : ByIndex)
			func.accept(t);
	}
	public void ForEachWithRemoval(@Nonnull Function<TValue, Boolean> func)
	{
		for(int i = ByIndex.size() - 1; i >= 0; i--)
		{
			TValue wheel = ByIndex.get(i);
			boolean remove = func.apply(wheel);
			if(remove)
				RemoveAt(i);
		}
	}


	public void Add(@Nonnull TValue t, @Nonnull String partName, @Nonnull THint[] hints)
	{
		// Add to raw indexed list
		ByIndex.add(t);

		// Add lookup by part name
		if(!ByPartName.containsKey(partName))
			ByPartName.put(partName, new ArrayList<>());
		ByPartName.get(partName).add(t);

		// Add lookup by hint (can be multiple hints)
		for(THint hint : hints)
		{
			if (!ByHint.containsKey(hint))
				ByHint.put(hint, new ArrayList<>());
			ByHint.get(hint).add(t);
		}
	}
	public void RemoveAt(int index)
	{
		TValue t = ByIndex.get(index);
		Remove(t);
	}
	public void Remove(@Nonnull TValue t)
	{
		ByIndex.remove(t);
		for(List<TValue> byPart : ByPartName.values())
			byPart.remove(t);
		for(List<TValue> byHint : ByHint.values())
			byHint.remove(t);


	}

}
