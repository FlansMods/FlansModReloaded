package com.flansmod.common.gunshots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

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


}
