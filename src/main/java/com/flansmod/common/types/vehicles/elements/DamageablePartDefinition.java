package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.util.Maths;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;


public class DamageablePartDefinition
{
	public static final DamageablePartDefinition INVALID = new DamageablePartDefinition();
	public static final DamageablePartDefinition DEFAULT_BODY = new DamageablePartDefinition();

	@JsonField
	public float maxHealth = 100;
	@JsonField
	public float armourToughness = 1;

	@JsonField
	public Vec3 hitboxCenter = Vec3.ZERO;
	@JsonField
	public Vec3 hitboxHalfExtents = new Vec3(1f, 1f, 1f);

	public boolean IsActive()
	{
		return maxHealth > 0 && hitboxHalfExtents.lengthSqr() > Maths.Epsilon;
	}
	@Nonnull
	public Lazy<AABB> Hitbox = Lazy.of(()-> new AABB(hitboxCenter, hitboxHalfExtents));
}
