package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;


public class DamageablePartDefinition
{
	public static final DamageablePartDefinition INVALID = new DamageablePartDefinition();
	public static final DamageablePartDefinition DEFAULT_BODY = new DamageablePartDefinition();

	@JsonField
	public String partName = "body";
	@JsonField
	public float maxHealth = 100;
	@JsonField
	public float armourToughness = 1;

	@JsonField
	public Vec3 hitboxCenter = Vec3.ZERO;
	@JsonField
	public Vec3 hitboxHalfExtents = new Vec3(1f, 1f, 1f);
}
