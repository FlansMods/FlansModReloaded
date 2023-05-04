package com.flansmod.common.gunshots;

import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class UnresolvedEntityHitResult extends HitResult
{
	private final int entityId;
	private final EPlayerHitArea hitboxType;

	public int EntityID() { return entityId; }
	public EPlayerHitArea HitboxArea() { return hitboxType; }

	protected UnresolvedEntityHitResult(Vec3 hit, int entId)
	{
		super(hit);
		entityId = entId;
		hitboxType = EPlayerHitArea.BODY;
	}

	protected UnresolvedEntityHitResult(Vec3 hit, int entId, EPlayerHitArea hitArea)
	{
		super(hit);
		entityId = entId;
		hitboxType = hitArea;
	}

	@Override
	public Type getType()
	{
		return Type.ENTITY;
	}
}
