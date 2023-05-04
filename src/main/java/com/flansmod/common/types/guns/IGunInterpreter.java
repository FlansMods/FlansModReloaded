package com.flansmod.common.types.guns;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface IGunInterpreter<TCheckType>
{
	boolean IsGun(TCheckType context);
	Vec3 GetShootOrigin(TCheckType context);
	Vec3 GetShootDirection(TCheckType context);
	Entity GetShooter(TCheckType context);
}
