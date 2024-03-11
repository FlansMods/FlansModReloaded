package com.flansmod.common.gunshots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class PlayerHitResult extends EntityHitResult
{
	@Nonnull
	private final Player player;
	@Nonnull
	private final PlayerHitbox hitbox;

	@Nonnull
	public PlayerHitbox GetHitbox() { return hitbox; }

	public PlayerHitResult(@Nonnull Player p, @Nonnull PlayerHitbox hit)
	{
		super(p);
		player = p;
		hitbox = hit;
	}

	public PlayerHitResult(@Nonnull Player p, @Nonnull PlayerHitbox hit, @Nonnull Vec3 hitLocation)
	{
		super(p, hitLocation);
		player = p;
		hitbox = hit;
	}
}
