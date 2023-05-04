package com.flansmod.common.gunshots;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class BulletHitPlayer extends BulletHit
{
    private final Player hitPlayer;
    private final PlayerHitbox hitbox;

    public BulletHitPlayer(Player player, PlayerHitbox box, double d)
    {
        super(d);
        hitPlayer = player;
        hitbox = box;
    }

    @Override
    public Entity GetEntity() { return hitPlayer; }
    public EPlayerHitArea GetHitArea() { return hitbox.area; }
}
