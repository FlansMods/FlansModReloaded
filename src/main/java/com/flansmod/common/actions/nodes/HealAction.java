package com.flansmod.common.actions.nodes;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HealAction extends ActionInstance
{
	public HealAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def)
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
		float healAmount = HealAmount();
		String healTag = HealEntityTag();
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

	public float HealAmount() { return Group.Context.ModifyFloat(ModifierDefinition.STAT_HEAL_AMOUNT, 1f); }
	public String HealEntityTag() { return Group.Context.ModifyString(ModifierDefinition.KEY_ENTITY_TAG, ""); }
}
