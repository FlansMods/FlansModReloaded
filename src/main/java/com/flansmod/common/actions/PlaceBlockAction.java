package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PlaceBlockAction extends ActionInstance
{
	public PlaceBlockAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
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
		String blockID = BlockID();
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockID));
		if(block != null)
		{
			Transform ray = Group.Context.Gun.GetShooter().GetShootOrigin();
			Vec3 origin = ray.PositionVec3();
			Vec3 direction = ray.ForwardVec3();
			float reach = Reach();

			Level level = Group.Context.Gun.Level;
			if(level != null)
			{
				Raytracer raytracer = Raytracer.ForLevel(level);
				if(raytracer != null)
				{
					List<HitResult> hits = new ArrayList<>();
					raytracer.CastBullet(Group.Context.Gun.GetShooter().Entity(), origin, direction.normalize().scale(reach), 0.0f, 0.0f, hits);
					if(hits.size() > 0)
					{
						if(hits.get(0).getType() == HitResult.Type.BLOCK)
						{
							BlockHitResult blockHit = (BlockHitResult)hits.get(0);
							level.setBlockAndUpdate(blockHit.getBlockPos().relative(blockHit.getDirection()), block.defaultBlockState());
						}
					}
				}
				else FlansMod.LOGGER.warn("PlaceBlockAction[" + Def + "]: Could not find raytracer for level " + level);
			}
			else FlansMod.LOGGER.warn("PlaceBlockAction[" + Def + "]: Could not find level when attempting to place a block");
		}
		else FlansMod.LOGGER.warn("PlaceBlockAction[" + Def + "]: Could not resolve blockID " + blockID);
	}

	public String BlockID() { return Group.Context.ModifyString(ModifierDefinition.STAT_BLOCK_ID, ""); }
}
