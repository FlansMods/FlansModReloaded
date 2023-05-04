package com.flansmod.common.gunshots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class PlayerHitResult extends EntityHitResult
{
	private final Player player;
	private final PlayerHitbox hitbox;

	public PlayerHitbox GetHitbox() { return hitbox; }

	public PlayerHitResult(Player p, PlayerHitbox hit)
	{
		super(p);
		player = p;
		hitbox = hit;
	}


}
