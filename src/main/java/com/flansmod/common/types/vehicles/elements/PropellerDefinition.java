package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;

public class PropellerDefinition
{
	@JsonField
	public String attachedTo = "body";
	@JsonField
	public Vec3 visualOffset = Vec3.ZERO;
	@JsonField
	public Vec3 forceOffset = Vec3.ZERO;
}

