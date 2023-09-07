package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HealAction extends Action
{
	public HealAction(@NotNull ActionGroup group, @NotNull ActionDefinition def)
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
		Transform ray = context.Shooter().GetShootOrigin();
		Vec3 origin = ray.PositionVec3();
		Vec3 direction = ray.ForwardVec3();
		float reach = Reach(context);
		float healAmount = HealAmount(context);
		String healTag = HealEntityTag(context);
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
					if(hits.get(0).getType() == HitResult.Type.ENTITY)
					{
						EntityHitResult entityHit = (EntityHitResult)hits.get(0);
						if(entityHit.getEntity() instanceof LivingEntity living)
						{
							if(healTag.isEmpty() || living.getTags().contains(healTag))
								living.heal(healAmount);
						}
					}
				}
			}
			else FlansMod.LOGGER.warn("HealAction[" + Def + "]: Could not find raytracer for level " + level);
		}
		else FlansMod.LOGGER.warn("HealAction[" + Def + "]: Could not find level when attempting to heal player");
	}

	public float HealAmount(ActionGroupContext context) { return context.ModifyFloat(ModifierDefinition.STAT_HEAL_AMOUNT, 1f); }
	public String HealEntityTag(ActionGroupContext context) { return context.ModifyString(ModifierDefinition.STAT_ENTITY_TAG, ""); }
}
