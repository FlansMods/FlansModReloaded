package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;

public class ArmInputDefinition
{
	@JsonField
	public String armName = "default";
	@JsonField
	public EArmInputType type = EArmInputType.SetPosition;
	@JsonField
	public Vec3 position = Vec3.ZERO;
	@JsonField
	public Vec3 euler = Vec3.ZERO;
	@JsonField
	public float speed = 1.0f;
}
