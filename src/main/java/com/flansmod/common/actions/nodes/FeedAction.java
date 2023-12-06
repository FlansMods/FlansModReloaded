package com.flansmod.common.actions.nodes;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FeedAction extends ActionInstance
{
	public FeedAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def)
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
		Transform ray = Group.Context.Gun.GetShootOrigin();
		Vec3 origin = ray.PositionVec3();
		Vec3 direction = ray.ForwardVec3();
		float reach = Reach();
		int feedAmount = FeedAmount();
		float feedSaturation = FeedSaturation();
		String healTag = FeedEntityTag();
		Level level = Group.Context.Gun.GetLevel();
		if(level != null)
		{
			Raytracer raytracer = Raytracer.ForLevel(level);
			if(raytracer != null)
			{
				List<HitResult> hits = new ArrayList<>();
				raytracer.CastBullet(Group.Context.Gun.GetShooter().Entity(), origin, direction.normalize().scale(reach), 0.0f, 0.0f, hits);
				if(hits.size() > 0)
				{
					if(hits.get(0).getType() == HitResult.Type.ENTITY)
					{
						EntityHitResult entityHit = (EntityHitResult)hits.get(0);
						if(entityHit.getEntity() instanceof Player player)
						{
							if(healTag.isEmpty() || player.getTags().contains(healTag))
								player.getFoodData().eat(feedAmount, feedSaturation);
						}
					}
				}
			}
			else FlansMod.LOGGER.warn("FeedAction[" + Def + "]: Could not find raytracer for level " + level);
		}
		else FlansMod.LOGGER.warn("FeedAction[" + Def + "]: Could not find level when attempting to heal player");
	}

	public int FeedAmount() { return Maths.Ceil(Group.Context.ModifyFloat(ModifierDefinition.STAT_FEED_AMOUNT, 1f)); }
	public float FeedSaturation() { return Group.Context.ModifyFloat(ModifierDefinition.STAT_FEED_SATURATION, 1f); }
	public String FeedEntityTag() { return Group.Context.ModifyString(ModifierDefinition.KEY_ENTITY_TAG, ""); }
}
