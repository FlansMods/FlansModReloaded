package com.flansmod.common.entity.vehicle.hierarchy;

import com.flansmod.common.entity.ITransformPair;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHierarchy;
import com.flansmod.common.entity.vehicle.VehiclePartPath;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public interface IVehicleTransformHelpers
{
	@Nonnull VehicleDefinitionHierarchy.Node GetHeirarchyRoot();
	void ApplyWorldToRootPrevious(@Nonnull TransformStack stack);
	void ApplyWorldToRootCurrent(@Nonnull TransformStack stack);
	void ApplyPartToPartPrevious(@Nonnull VehicleDefinitionHierarchy.Node childPart, @Nonnull TransformStack stack);
	void ApplyPartToPartCurrent(@Nonnull VehicleDefinitionHierarchy.Node childPart, @Nonnull TransformStack stack);

	@Nonnull default Transform GetWorldToRootPrevious() { return Transform.Flatten(this::ApplyWorldToRootPrevious); }
	@Nonnull default Transform GetWorldToRootCurrent() { return Transform.Flatten(this::ApplyWorldToRootCurrent); }
	@Nonnull default ITransformPair GetWorldToRoot() { return ITransformPair.of(this::GetWorldToRootPrevious, this::GetWorldToRootCurrent); }
	@Nonnull default Transform GetPartToPartCurrent(@Nonnull VehicleDefinitionHierarchy.Node node) { return Transform.Flatten((stack) -> ApplyPartToPartPrevious(node, stack)); }
	@Nonnull default Transform GetPartToPartPrevious(@Nonnull VehicleDefinitionHierarchy.Node node) { return Transform.Flatten((stack) -> ApplyPartToPartCurrent(node, stack)); }
	@Nonnull default ITransformPair GetPartToPart(@Nonnull VehicleDefinitionHierarchy.Node node) { return ITransformPair.of(() -> GetPartToPartPrevious(node), () -> GetPartToPartCurrent(node)); }


	// Part transform compositions
	default void ApplyRootToPartPrevious(@Nonnull String vehiclePart, @Nonnull TransformStack stack)
	{
		Reference.Traverse(vehiclePart, (node) -> { stack.add(GetPartLocalPrevious(node)); });
	}
	default void TransformRootToPartCurrent(@Nonnull String vehiclePart, @Nonnull TransformStack stack) {
		Reference.Traverse(vehiclePart, (node) -> { stack.add(GetPartLocalCurrent(node)); });
	}

	// World to Part
	@Nonnull default ITransformPair GetWorldToPart(@Nonnull VehiclePartPath apPath) { return ITransformPair.compose(GetWorldToRoot(), GetRootToPart(apPath)); }
	@Nonnull default Transform GetWorldToPartPrevious(@Nonnull String apPath) { return GetWorldToPart(apPath).GetPrevious(); }
	@Nonnull default Transform GetWorldToPartCurrent(@Nonnull String apPath) { return GetWorldToPart(apPath).GetCurrent(); }


	default void Traverse(@Nonnull VehiclePartPath path, @Nonnull Consumer<VehicleDefinitionHierarchy.Node> func)
	{
		VehicleDefinitionHierarchy.Node rootNode = GetHeirarchyRoot();
		if(path.IsNamed())
		{
			rootNode.Traverse(path.Names())
		}
		else
		{

		}
	}

	// Root to Part
	@Nonnull default ITransformPair GetRootToPart(@Nonnull String vehiclePart) {
		return ITransformPair.of(() -> GetRootToPartPrevious(vehiclePart), () -> GetRootToPartCurrent(vehiclePart));
	}
	@Nonnull default Transform GetRootToPartPrevious(@Nonnull String vehiclePart) {
		TransformStack stack = new TransformStack();
		TransformRootToPartPrevious(vehiclePart, stack);
		return stack.Top();
	}
	@Nonnull default Transform GetRootToPartCurrent(@Nonnull String vehiclePart) {
		TransformStack stack = new TransformStack();
		TransformRootToPartCurrent(vehiclePart, stack);
		return stack.Top();
	}



}
