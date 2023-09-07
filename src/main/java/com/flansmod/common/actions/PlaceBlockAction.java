package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PlaceBlockAction extends Action
{
	public PlaceBlockAction(@Nonnull ActionGroup group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public void OnTriggerClient(ActionGroupContext context, int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(ActionGroupContext context, int triggerIndex)
	{
		String blockID = BlockID(context);
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockID));
		if(block != null)
		{
			Transform ray = context.Shooter().GetShootOrigin();
			Vec3 origin = ray.PositionVec3();
			Vec3 direction = ray.ForwardVec3();
			float reach = Reach(context);

			Level level = context.Level();
			if(level != null)
			{
				Raytracer raytracer = Raytracer.ForLevel(level);
				if(raytracer != null)
				{
					List<HitResult> hits = new ArrayList<>();
					raytracer.CastBullet(context.Entity(), origin, direction.normalize().scale(reach), 0.0f, 0.0f, hits);
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

	public String BlockID(ActionGroupContext context) { return context.ModifyString(ModifierDefinition.STAT_BLOCK_ID, ""); }
}
