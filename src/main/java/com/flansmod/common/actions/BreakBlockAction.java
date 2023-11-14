package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BreakBlockAction extends ActionInstance
{
	public BreakBlockAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public void OnTriggerClient(int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{
		Transform ray = Group.Context.Gun.GetShooter().GetShootOrigin();
		Vec3 origin = ray.PositionVec3();
		Vec3 direction = ray.ForwardVec3();
		float reach = Reach();
		float harvestLevel = ToolLevel();

		Level level = Group.Context.Gun.Level;
		if(level != null)
		{
			Raytracer raytracer = Raytracer.ForLevel(level);
			if(raytracer != null)
			{
				List<HitResult> hits = new ArrayList<>();
				Entity shooterEntity = Group.Context.Gun.GetShooter().Entity();
				if(shooterEntity != null)
				{
					raytracer.CastBullet(shooterEntity, origin, direction.normalize().scale(reach), 0.0f, 0.0f, hits);
					if (hits.size() > 0)
					{
						if (hits.get(0).getType() == HitResult.Type.BLOCK)
						{
							BlockHitResult blockHit = (BlockHitResult) hits.get(0);
							BlockState hitBlockState = level.getBlockState(blockHit.getBlockPos());
							if (hitBlockState.canEntityDestroy(level, blockHit.getBlockPos(), shooterEntity))
							{
								if (hitBlockState.getBlock().defaultDestroyTime() <= harvestLevel)
									level.destroyBlock(blockHit.getBlockPos(), true, shooterEntity);
							}
						}
					}
				}
			}
			else FlansMod.LOGGER.warn("BreakBlockAction[" + Def + "]: Could not find raytracer for level " + level);
		}
		else FlansMod.LOGGER.warn("BreakBlockAction[" + Def + "]: Could not find level when attempting to break a block");
	}

	public String BlockID() { return Group.Context.ModifyString(ModifierDefinition.STAT_BLOCK_ID, ""); }
}
