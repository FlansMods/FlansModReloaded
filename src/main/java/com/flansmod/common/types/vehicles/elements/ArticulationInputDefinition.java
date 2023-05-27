package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.vehicles.EArticulationInputType;
import net.minecraft.world.phys.Vec3;

public class ArticulationInputDefinition
{
	@JsonField
	public String partName = "";
	@JsonField
	public EArticulationInputType type = EArticulationInputType.CycleKeyframes;
	@JsonField(Docs = "Used with the SpecificKeyframe type. Index based on the ArticulatedPart definition")
	public int keyframeIndex = 0;
	@JsonField(Docs = "Used with the ApplyMotion type. Pushes in this direction if within the bounds of the part")
	public Vec3 motion = Vec3.ZERO;
	@JsonField(Docs = "Used with the ApplyMotion type. Rotates in this direction (euler angles) if within the bounds of the part")
	public Vec3 rotationalMotion = Vec3.ZERO;
}
