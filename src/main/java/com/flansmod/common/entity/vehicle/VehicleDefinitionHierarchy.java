package com.flansmod.common.entity.vehicle;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class VehicleDefinitionHierarchy
{
	public static class Node
	{
		@Nonnull
		public final VehiclePartDefinition Def;
		public Node(@Nonnull VehiclePartDefinition part)
		{
			Def = part;
		}
		public Node Parent = null;
		public Map<String, Node> Children = new HashMap<>();

		@Nonnull
		public String Path()
		{
			if(Parent != null)
			{
				String parentPath = Parent.Path();
				if(!parentPath.isEmpty())
					return Parent.Path() + "/" + Def.partName;
			}
			return Def.partName;
		}

		@Nullable
		public Node FindNode(@Nonnull String path)
		{
			return RunOnNode(path, (node, subpath) -> node).orElse(null);
		}
		@Nonnull
		public <T> Optional<T> RunOnNode(@Nonnull String path, @Nonnull BiFunction<Node, String, T> func)
		{
			String[] subpath = path.split("/", 1);
			if(subpath.length > 1)
			{
				if (Children.containsKey(subpath[0]))
					return Children.get(subpath[0]).RunOnNode(subpath[1], func);
				else
				{
					FlansMod.LOGGER.warn("Failed to find child node for subpath " + subpath[0]);
					return Optional.empty();
				}
			}
			else if(subpath.length == 1)
			{
				return Optional.ofNullable(func.apply(this, subpath[0]));
			}
			else
			{
				FlansMod.LOGGER.warn("Failed to find path, but hit 0-length");
				return Optional.empty();
			}
		}

		public void ForEachNode(@Nonnull Consumer<Node> func)
		{
			func.accept(this);
			for(var child : Children.values())
				child.ForEachNode(func);
		}
	}
	public final Node RootNode;
	public VehicleDefinitionHierarchy(@Nonnull Node rootNode)
	{
		RootNode = rootNode;
	}

	@Nonnull
	public static VehicleDefinitionHierarchy of(@Nonnull VehicleDefinition def)
	{
		Map<String, Node> nodes = new HashMap<>();
		for(VehiclePartDefinition partDef : def.parts)
		{
			if (nodes.containsKey(partDef.partName))
			{
				FlansMod.LOGGER.warn("More than one VehiclePartDefinition named " + partDef.partName);
			}
			nodes.put(partDef.partName, new Node(partDef));
		}

		// Make sure we have a root node
		if(!nodes.containsKey("body"))
		{
			nodes.put("body", new Node(VehiclePartDefinition.INVALID));
		}

		for(var kvp : nodes.entrySet())
		{
			if(kvp.getKey().equals("body"))
				continue;

			String attachedTo = kvp.getValue().Def.attachedTo;
			if(nodes.containsKey(attachedTo))
			{
				Node parentNode = nodes.get(attachedTo);
				parentNode.Children.put(kvp.getKey(), kvp.getValue());
				kvp.getValue().Parent = parentNode;
			}
			else
			{
				FlansMod.LOGGER.warn("Could not find parent node " + attachedTo + " for " + kvp.getKey());
			}
		}
		return new VehicleDefinitionHierarchy(nodes.get("body"));
	}

	private static int FindIndex(@Nonnull String id, @Nonnull String prefix)
	{
		if (id.startsWith(prefix))
		{
			return Integer.parseInt(id.substring(prefix.length() + 1));
		}
		return Integer.parseInt(id);
	}
	public void Traverse(@Nonnull String partPath, @Nonnull Consumer<Node> func)
	{
		Traverse(partPath.split("/"), func);
	}
	public void Traverse(@Nonnull String[] partPathElements, @Nonnull Consumer<Node> func)
	{
		Node node = RootNode;
		func.accept(node);
		for(int i = 0; i < partPathElements.length; i++)
		{

			// You can start with "body", but it isn't really needed
			if(partPathElements[i].equals("body"))
				continue;

			Node childNode = node.Children.get(partPathElements[i]);
			if(childNode != null)
			{
				func.accept(childNode);
				node = childNode;
			}
			else
			{
				// This is the end of the line. We may have one more identifier like "wheel_0" (an entity)
				// or "articulation_2", a component
			}
		}
	}
	@Nullable
	public Node Find(@Nonnull String partName) { return RootNode.FindNode(partName); }
	public void ForEachNode(@Nonnull Consumer<Node> func) { RootNode.ForEachNode(func); }

	public static final String SEAT_PREFIX = "seat";
	public void ForEachSeatPath(@Nonnull Consumer<String> func)
	{
		ForEachNode((node) -> {
			for(int i = 0; i < node.Def.seats.length; i++)
				func.accept(node.Path() + "/" + SEAT_PREFIX + "_" + i);
		});
	}
	public void ForEachSeat(@Nonnull Consumer<SeatDefinition> func)
	{
		ForEachNode((node) -> {
			for(SeatDefinition seat : node.Def.seats)
				func.accept(seat);
		});
	}
	public void ForEachSeat(@Nonnull BiConsumer<String, SeatDefinition> func)
	{
		ForEachNode((node) -> {
			for(int i = 0; i < node.Def.seats.length; i++)
				func.accept(node.Path()+ "/" + SEAT_PREFIX + "_" + i, node.Def.seats[i]);
		});
	}
	@Nonnull
	public SeatDefinition FindSeat(@Nonnull String path)
	{
		return RootNode.RunOnNode(path, (node, endOfPath) -> node.Def.seats[FindIndex(endOfPath, SEAT_PREFIX)]).orElse(SeatDefinition.INVALID);
	}

	public static final String ARTICULATION_PREFIX = "articulation";
	public void ForEachArticulationPath(@Nonnull Consumer<String> func)
	{
		ForEachNode((node) -> {
			if(node.Def.IsArticulated())
				func.accept(node.Path() + "/" + ARTICULATION_PREFIX);
		});
	}
	public void ForEachArticulation(@Nonnull BiConsumer<String, ArticulatedPartDefinition> func)
	{
		ForEachNode((node) -> {
			if(node.Def.IsArticulated())
				func.accept(node.Path(), node.Def.articulation);
		});
	}
	@Nullable
	public ArticulatedPartDefinition FindArticulation(@Nonnull String path)
	{
		return RootNode.RunOnNode(path, (node, endOfPath) -> node.Def.articulation).orElse(null);
	}

	public static final String MOUNTED_GUN_PREFIX = "gun";
	public void ForEachMountedGunPath(@Nonnull Consumer<String> func)
	{
		ForEachNode((node) -> {
			for(int i = 0; i < node.Def.guns.length; i++)
				func.accept(node.Path() + "/" + MOUNTED_GUN_PREFIX + "_" + i);
		});
	}
	public void ForEachMountedGun(@Nonnull Consumer<MountedGunDefinition> func)
	{
		ForEachNode((node) -> {
			for(MountedGunDefinition gun : node.Def.guns)
				func.accept(gun);
		});
	}
	public void ForEachMountedGun(@Nonnull BiConsumer<String, MountedGunDefinition> func)
	{
		ForEachNode((node) -> {
			for(int i = 0; i < node.Def.guns.length; i++)
				func.accept(node.Path()+ "/" + MOUNTED_GUN_PREFIX + "_" + i, node.Def.guns[i]);
		});
	}
	@Nonnull
	public MountedGunDefinition FindMountedGun(@Nonnull String path)
	{
		return RootNode.RunOnNode(path, (node, endOfPath) -> node.Def.guns[FindIndex(endOfPath, MOUNTED_GUN_PREFIX)]).orElse(MountedGunDefinition.INVALID);
	}

	public static final String DAMAGEABLE_PREFIX = "damageable";
	public void ForEachDamageablePath(@Nonnull Consumer<String> func)
	{
		ForEachNode((node) -> {
			if(node.Def.IsDamageable())
				func.accept(node.Path() + "/" + DAMAGEABLE_PREFIX);
		});
	}
	public void ForEachDamageable(@Nonnull Consumer<DamageablePartDefinition> func)
	{
		ForEachNode((node) -> {
			if(node.Def.IsDamageable())
				func.accept(node.Def.damage);
		});
	}
	public void ForEachDamageable(@Nonnull BiConsumer<String, DamageablePartDefinition> func)
	{
		ForEachNode((node) -> {
			if(node.Def.IsDamageable())
				func.accept(node.Path()+ "/" + DAMAGEABLE_PREFIX, node.Def.damage);
		});
	}
	@Nonnull
	public DamageablePartDefinition FindDamageable(@Nonnull String path)
	{
		return RootNode.RunOnNode(path, (node, endOfPath) -> node.Def.damage).orElse(DamageablePartDefinition.INVALID);
	}


	public static final String WHEEL_PREFIX = "wheel";
	public void ForEachWheelPath(@Nonnull Consumer<String> func)
	{
		ForEachNode((node) -> {
			for(int i = 0; i < node.Def.wheels.length; i++)
				func.accept(node.Path() + "/" + WHEEL_PREFIX + "_" + i);
		});
	}
	public void ForEachWheel(@Nonnull Consumer<WheelDefinition> func)
	{
		ForEachNode((node) -> {
			for(int i = 0; i < node.Def.wheels.length; i++)
				func.accept(node.Def.wheels[i]);
		});
	}
	public void ForEachWheel(@Nonnull BiConsumer<String, WheelDefinition> func)
	{
		ForEachNode((node) -> {
			for(int i = 0; i < node.Def.wheels.length; i++)
				func.accept(node.Path() + "/" + WHEEL_PREFIX + "_" + i, node.Def.wheels[i]);
		});
	}
	@Nonnull
	public List<WheelDefinition> GetAllWheels()
	{
		List<WheelDefinition> wheels = new ArrayList<>();
		ForEachWheel((wheel) -> wheels.add(wheel));
		return wheels;
	}
	@Nonnull
	public WheelDefinition FindWheel(@Nonnull String path)
	{
		return RootNode.RunOnNode(path, (node, endOfPath) -> node.Def.wheels[FindIndex(endOfPath, WHEEL_PREFIX)]).orElse(WheelDefinition.INVALID);
	}
}
