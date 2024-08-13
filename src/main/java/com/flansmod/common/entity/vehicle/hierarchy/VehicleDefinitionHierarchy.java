package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.common.types.vehicles.elements.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class VehicleDefinitionHierarchy
{
	public static class VehicleNode
	{
		public final Map<String, VehicleNode> ChildNodes = new HashMap<>();
		public VehicleNode ParentNode = null;
		public final VehiclePartDefinition Def;
		public VehicleNode(@Nonnull VehiclePartDefinition part)
		{
			Def = part;
		}
		@Nonnull
		public VehiclePartPath GetPath() { return Path != null ? Path : VehiclePartPath.Invalid; }
		protected VehiclePartPath Path;

		@Nonnull
		public String PartName() { return Def.partName; }
		public void IterateRootToThis(@Nonnull Consumer<VehicleNode> func)
		{
			if(ParentNode != null)
				ParentNode.IterateRootToThis(func);
			func.accept(this);
		}
		public void IterateThisToRoot(@Nonnull Consumer<VehicleNode> func)
		{
			func.accept(this);
			if(ParentNode != null)
				ParentNode.IterateRootToThis(func);
		}
	}

	public final VehicleNode RootNode;
	public final Map<VehiclePartPath, VehicleNode> AllNodes;
	public final Map<VehiclePartPath, VehicleNode> TransformableNodes = new HashMap<>();

	public VehicleDefinitionHierarchy(@Nonnull VehicleNode rootNode)
	{
		RootNode = rootNode;
		ImmutableMap.Builder<VehiclePartPath, VehicleNode> builder = new ImmutableMap.Builder<>();
		Hierarchise(rootNode, new Stack<>(), builder);
		AllNodes = builder.build();
	}
	private void Hierarchise(@Nonnull VehicleNode node, @Nonnull Stack<String> pathBuilder, @Nonnull ImmutableMap.Builder<VehiclePartPath, VehicleNode> mapBuilder)
	{
		node.Path = VehiclePartPath.of(ImmutableList.copyOf(pathBuilder));
		mapBuilder.put(node.Path, node);
		for(var kvp : node.ChildNodes.entrySet())
		{
			pathBuilder.push(kvp.getKey());
			Hierarchise(kvp.getValue(), pathBuilder, mapBuilder);
			pathBuilder.pop();
		}
	}


	public void FromRootToNode(@Nonnull VehiclePartPath path, @Nonnull Consumer<VehicleNode> func)
	{
		if(AllNodes.containsKey(path))
			AllNodes.get(path).IterateRootToThis(func);
	}
	public void FromNodeToRoot(@Nonnull VehiclePartPath path, @Nonnull Consumer<VehicleNode> func)
	{
		if(AllNodes.containsKey(path))
			AllNodes.get(path).IterateThisToRoot(func);
	}
	@Nullable
	public VehicleNode FindNode(@Nonnull VehiclePartPath partPath)
	{
		return AllNodes.get(partPath);
	}
	@Nullable
	public VehicleNode FindNode(@Nonnull VehicleComponentPath componentPath)
	{
		return FindNode(componentPath.Part());
	}
	public void ForEachNode(@Nonnull Consumer<VehicleNode> func)
	{
		for(VehicleNode node : AllNodes.values())
			func.accept(node);
	}
	public void ForEachNode(@Nonnull BiConsumer<VehiclePartPath, VehicleNode> func)
	{
		for(var kvp : AllNodes.entrySet())
			func.accept(kvp.getKey(), kvp.getValue());
	}
	public void ForEachArticulatedPart(@Nonnull BiConsumer<VehicleComponentPath, ArticulatedPartDefinition> func)
	{
		for(var kvp : AllNodes.entrySet())
			if(kvp.getValue().Def.IsArticulated())
				func.accept(kvp.getKey().Articulation(), kvp.getValue().Def.articulation);
	}
	public void IfArticulated(@Nonnull VehicleComponentPath partPath, @Nonnull Consumer<ArticulatedPartDefinition> func)
	{
		VehicleNode node = FindNode(partPath);
		if (node != null && node.Def.IsArticulated())
		{
			func.accept(node.Def.articulation);
		}
	}
	@Nonnull
	public <T> Optional<T> IfArticulated(@Nonnull VehicleComponentPath partPath, @Nonnull Function<ArticulatedPartDefinition, T> func)
	{
		VehicleNode node = FindNode(partPath);
		if (node != null && node.Def.IsArticulated())
		{
			return Optional.of(func.apply(node.Def.articulation));
		}
		return Optional.empty();
	}
	public void ForEachDamageable(@Nonnull BiConsumer<VehicleComponentPath, DamageablePartDefinition> func)
	{
		for(var kvp : AllNodes.entrySet())
			if(kvp.getValue().Def.IsArticulated())
				func.accept(kvp.getKey().Damageable(), kvp.getValue().Def.damage);
	}
	@Nonnull
	public Optional<DamageablePartDefinition> FindDamageable(@Nonnull VehicleComponentPath damagePath)
	{
		VehicleNode node = FindNode(damagePath);
		if(damagePath.Type() == EPartDefComponent.Damage)
			if (node != null && node.Def.IsDamageable())
				return Optional.of(node.Def.damage);
		return Optional.empty();
	}
	public void IfDamageableExists(@Nonnull VehicleComponentPath damagePath, @Nonnull Consumer<DamageablePartDefinition> func)
	{
		if(damagePath.Type() == EPartDefComponent.Damage)
		{
			VehicleNode node = FindNode(damagePath);
			if (node != null && node.Def.IsDamageable())
			{
				func.accept(node.Def.damage);
			}
		}
	}



	public void ForEachSeat(@Nonnull BiConsumer<VehicleComponentPath, SeatDefinition> func)
	{
		for(var kvp : AllNodes.entrySet())
			for(int i = 0; i < kvp.getValue().Def.seats.length; i++)
				func.accept(kvp.getKey().Seat(i), kvp.getValue().Def.seats[i]);
	}
	@Nonnull
	public Optional<SeatDefinition> FindSeat(@Nonnull VehicleComponentPath path)
	{
		VehicleNode node = FindNode(path.Part());
		if(node != null)
			if(path.Index() < node.Def.seats.length)
				return Optional.of(node.Def.seats[path.Index()]);
		return Optional.empty();
	}
	public void IfSeatExists(@Nonnull VehicleComponentPath seatPath, @Nonnull Consumer<SeatDefinition> func)
	{
		if(seatPath.Type() == EPartDefComponent.Seat)
		{
			VehicleNode node = FindNode(seatPath);
			if (node != null && seatPath.Index() < node.Def.seats.length)
			{
				func.accept(node.Def.seats[seatPath.Index()]);
			}
		}
	}
	@Nonnull
	public <T> Optional<T> IfSeatExists(@Nonnull VehicleComponentPath seatPath, @Nonnull Function<SeatDefinition, T> func)
	{
		if(seatPath.Type() == EPartDefComponent.Seat)
		{
			VehicleNode node = FindNode(seatPath);
			if (node != null && seatPath.Index() < node.Def.seats.length)
			{
				return Optional.of(func.apply(node.Def.seats[seatPath.Index()]));
			}
		}
		return Optional.empty();
	}


	public int NumWheels()
	{
		int numWheels = 0;
		for(var kvp : AllNodes.entrySet())
			numWheels += kvp.getValue().Def.wheels.length;
		return numWheels;
	}
	@Nonnull
	public Optional<WheelDefinition> FindWheel(@Nonnull VehicleComponentPath path)
	{
		VehicleNode node = FindNode(path.Part());
		if(node != null)
			if(path.Index() < node.Def.wheels.length)
				return Optional.of(node.Def.wheels[path.Index()]);
		return Optional.empty();
	}
	@Nonnull
	public List<WheelDefinition> AllWheels()
	{
		List<WheelDefinition> wheels = new ArrayList<>();
		for(var kvp : AllNodes.entrySet())
			wheels.addAll(List.of(kvp.getValue().Def.wheels));
		return wheels;
	}
	public void ForEachWheel(@Nonnull BiConsumer<VehicleComponentPath, WheelDefinition> func)
	{
		for(var kvp : AllNodes.entrySet())
			for(int i = 0; i < kvp.getValue().Def.wheels.length; i++)
				func.accept(kvp.getKey().Wheel(i), kvp.getValue().Def.wheels[i]);
	}
	public void IfWheelExists(@Nonnull VehicleComponentPath wheelPath, @Nonnull Consumer<WheelDefinition> func)
	{
		if(wheelPath.Type() == EPartDefComponent.Wheel)
		{
			VehicleNode node = FindNode(wheelPath);
			if (node != null && wheelPath.Index() < node.Def.wheels.length)
			{
				func.accept(node.Def.wheels[wheelPath.Index()]);
			}
		}
	}
	public void ForEachGun(@Nonnull BiConsumer<VehicleComponentPath, MountedGunDefinition> func)
	{
		for(var kvp : AllNodes.entrySet())
			for(int i = 0; i < kvp.getValue().Def.guns.length; i++)
				func.accept(kvp.getKey().Gun(i), kvp.getValue().Def.guns[i]);
	}
	public void IfGunExists(@Nonnull VehicleComponentPath gunPath, @Nonnull Consumer<MountedGunDefinition> func)
	{
		if(gunPath.Type() == EPartDefComponent.Gun)
		{
			VehicleNode node = FindNode(gunPath);
			if (node != null && gunPath.Index() < node.Def.guns.length)
			{
				func.accept(node.Def.guns[gunPath.Index()]);
			}
		}
	}
	public void ForEachPropeller(@Nonnull BiConsumer<VehicleComponentPath, PropellerDefinition> func)
	{
		for(var kvp : AllNodes.entrySet())
			for(int i = 0; i < kvp.getValue().Def.propellers.length; i++)
				func.accept(kvp.getKey().Propeller(i), kvp.getValue().Def.propellers[i]);
	}
	public void IfPropellerExists(@Nonnull VehicleComponentPath propPath, @Nonnull Consumer<PropellerDefinition> func)
	{
		if(propPath.Type() == EPartDefComponent.Propeller)
		{
			VehicleNode node = FindNode(propPath);
			if (node != null && propPath.Index() < node.Def.propellers.length)
			{
				func.accept(node.Def.propellers[propPath.Index()]);
			}
		}
	}



	@Nonnull
	public static VehicleDefinitionHierarchy of(@Nonnull VehicleDefinition def)
	{
		Map<String, VehicleNode> nodes = new HashMap<>();
		for(VehiclePartDefinition partDef : def.parts)
		{
			if (nodes.containsKey(partDef.partName))
			{
				FlansMod.LOGGER.warn("More than one VehiclePartDefinition named " + partDef.partName);
			}
			nodes.put(partDef.partName, new VehicleNode(partDef));
		}

		// Make sure we have a root node
		if(!nodes.containsKey("body"))
		{
			nodes.put("body", new VehicleNode(VehiclePartDefinition.INVALID));
		}

		for(var kvp : nodes.entrySet())
		{
			if(kvp.getKey().equals("body"))
				continue;

			String attachedTo = kvp.getValue().Def.attachedTo;
			if(nodes.containsKey(attachedTo))
			{
				VehicleNode parentNode = nodes.get(attachedTo);
				parentNode.ChildNodes.put(kvp.getKey(), kvp.getValue());
				kvp.getValue().ParentNode = parentNode;
			}
			else
			{
				FlansMod.LOGGER.warn("Could not find parent node " + attachedTo + " for " + kvp.getKey());
			}
		}
		return new VehicleDefinitionHierarchy(nodes.get("body"));
	}
}
