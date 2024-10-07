package com.flansmod.client.particle;

import com.flansmod.physics.common.util.Maths;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GunshotHitBlockParticle extends TerrainParticle
{
	public GunshotHitBlockParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, BlockState blockState, BlockPos pos)
	{
		super(level, x, y, z, dx, dy, dz, blockState, pos);
		gravity = 0.1f;
		friction = 0.8f;
		hasPhysics = false;

		xd = dx;
		yd = dy;
		zd = dz;

		lifetime = (int)(Maths.SqrtF((float)(dx*dx+dy*dy+dz*dz)) * 10.0f);
	}

	public void tick()
	{
		xo = x;
		yo = y;
		zo = z;
		if (age++ >= lifetime)
		{
			remove();
		}
		else
		{
			yd -= 0.04D * (double)gravity;
			//quadSize = 0.1f * (1.0f - (float)age / lifetime);
			move(xd, yd, zd);

			xd *= (double)friction;
			yd *= (double)friction;
			zd *= (double)friction;
		}
	}
}
