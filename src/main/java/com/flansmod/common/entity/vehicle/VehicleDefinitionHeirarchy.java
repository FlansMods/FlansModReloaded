package com.flansmod.common.entity.vehicle;

import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.ArticulatedPartDefinition;
import com.flansmod.common.types.vehicles.elements.DamageablePartDefinition;
import com.flansmod.common.types.vehicles.elements.MountedGunDefinition;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class VehicleDefinitionHeirarchy
{
	public static class Node
	{
		@Nonnull
		public final String Key;
		public Node(@Nonnull String key)
		{
			Key = key;
		}

		public ArticulatedPartDefinition Articulation = null;
		public SeatDefinition Seat = null;
		public MountedGunDefinition MountedGun = null;
		public DamageablePartDefinition Damageable = null;
		public Node Parent = null;

		@Nonnull
		public String Path()
		{
			if(Parent != null)
			{
				String parentPath = Parent.Path();
				if(!parentPath.isEmpty())
					return Parent.Path() + "/" + Key;
			}
			return Key;
		}
	}
	public final Map<String, Node> Nodes = new HashMap<>();

	private void AddRoot()
	{
		if(!Nodes.containsKey("body"))
			Nodes.put("body", new Node(""));
	}
	private void Bind(@Nonnull String key, @Nonnull ArticulatedPartDefinition artic)
	{
		if(!Nodes.containsKey(key))
			Nodes.put(key, new Node(key));
		Nodes.get(key).Articulation = artic;
	}
	private void Bind(@Nonnull String key, @Nonnull SeatDefinition seat)
	{
		if(!Nodes.containsKey(key))
			Nodes.put(key, new Node(key));
		Nodes.get(key).Seat = seat;
	}
	private void Bind(@Nonnull String key, @Nonnull MountedGunDefinition mountedGun)
	{
		if(!Nodes.containsKey(key))
			Nodes.put(key, new Node(key));
		Nodes.get(key).MountedGun = mountedGun;
	}
	private void Bind(@Nonnull String key, @Nonnull DamageablePartDefinition damageable)
	{
		if(!Nodes.containsKey(key))
			Nodes.put(key, new Node(key));
		Nodes.get(key).Damageable = damageable;
	}
	private void Heirarchise()
	{
		for(var kvp : Nodes.entrySet())
		{
			if(kvp.getKey().equals("body"))
				continue;

			if(kvp.getValue().Articulation != null)
			{
				String parentKey = kvp.getValue().Articulation.attachedToPart;
				if(Nodes.containsKey(parentKey))
					kvp.getValue().Parent = Nodes.get(parentKey);
				else
					kvp.getValue().Parent = Nodes.get("body");
			}
		}
	}
	@Nonnull
	public static VehicleDefinitionHeirarchy of(@Nonnull VehicleDefinition def)
	{
		VehicleDefinitionHeirarchy t = new VehicleDefinitionHeirarchy();
		t.AddRoot();

		for(ArticulatedPartDefinition part : def.articulatedParts)
			t.Bind(part.partName, part);
		for(SeatDefinition seat : def.seats)
			t.Bind(seat.attachedTo, seat);
		for(MountedGunDefinition mountedGun : def.guns)
			t.Bind(mountedGun.attachedTo, mountedGun);
		for(DamageablePartDefinition damageable : def.damageables)
			t.Bind(damageable.partName, damageable);

		t.Heirarchise();

		return t;
	}




	public void ForEachSeatPath(@Nonnull Consumer<String> func)
	{
		for(var kvp : Nodes.entrySet())
			if(kvp.getValue().Seat != null)
				func.accept(kvp.getValue().Path());
	}
	public void ForEachSeat(@Nonnull Consumer<SeatDefinition> func)
	{
		for(var kvp : Nodes.entrySet())
			if(kvp.getValue().Seat != null)
				func.accept(kvp.getValue().Seat);
	}
	@Nullable
	public SeatDefinition GetSeat(@Nonnull String path)
	{
		String key = path.substring(path.lastIndexOf('/')+1);
		if(Nodes.containsKey(key))
		{
			Node node = Nodes.get(key);
			if(node.Path().equals(path) && node.Seat != null)
				return node.Seat;
		}
		return null;
	}
	public void ForEachArticulationPath(@Nonnull Consumer<String> func)
	{
		for(var kvp : Nodes.entrySet())
			if(kvp.getValue().Articulation != null)
				func.accept(kvp.getValue().Path());
	}
	public void ForEachArticulation(@Nonnull Consumer<ArticulatedPartDefinition> func)
	{
		for(var kvp : Nodes.entrySet())
			if(kvp.getValue().Articulation != null)
				func.accept(kvp.getValue().Articulation);
	}
	@Nullable
	public ArticulatedPartDefinition GetArticulation(@Nonnull String path)
	{
		String key = path.substring(path.lastIndexOf('/')+1);
		if(Nodes.containsKey(key))
		{
			Node node = Nodes.get(key);
			if(node.Path().equals(path) && node.Articulation != null)
				return node.Articulation;
		}
		return null;
	}
	public void ForEachMountedGunPath(@Nonnull Consumer<String> func)
	{
		for(var kvp : Nodes.entrySet())
			if(kvp.getValue().MountedGun != null)
				func.accept(kvp.getValue().Path());
	}
	public void ForEachMountedGun(@Nonnull Consumer<MountedGunDefinition> func)
	{
		for(var kvp : Nodes.entrySet())
			if(kvp.getValue().MountedGun != null)
				func.accept(kvp.getValue().MountedGun);
	}
	@Nullable
	public MountedGunDefinition GetMountedGun(@Nonnull String path)
	{
		String key = path.substring(path.lastIndexOf('/')+1);
		if(Nodes.containsKey(key))
		{
			Node node = Nodes.get(key);
			if(node.Path().equals(path) && node.MountedGun != null)
				return node.MountedGun;
		}
		return null;
	}
	public void ForEachDamageablePath(@Nonnull Consumer<String> func)
	{
		for(var kvp : Nodes.entrySet())
			if(kvp.getValue().Damageable != null)
				func.accept(kvp.getValue().Path());
	}
	public void ForEachDamageable(@Nonnull Consumer<DamageablePartDefinition> func)
	{
		for(var kvp : Nodes.entrySet())
			if(kvp.getValue().Damageable != null)
				func.accept(kvp.getValue().Damageable);
	}
	@Nullable
	public DamageablePartDefinition GetDamageable(@Nonnull String path)
	{
		String key = path.substring(path.lastIndexOf('/')+1);
		if(Nodes.containsKey(key))
		{
			Node node = Nodes.get(key);
			if(node.Path().equals(path) && node.Damageable != null)
				return node.Damageable;
		}
		return null;
	}
}
