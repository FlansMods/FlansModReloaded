package com.flansmod.common.actions.nodes;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.abilities.elements.EAbilityTrigger;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RaycastAction extends ActionInstance
{
	public RaycastAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def)
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
		Level level = Group.Context.Gun.GetLevel();
		if(level != null)
		{
			Raytracer raytracer = Raytracer.ForLevel(level);
			List<HitResult> hits = new ArrayList<>();
			raytracer.CastBullet(Group.Context.Gun.GetShooter().Entity(), origin, direction.normalize().scale(reach), 0.0f, 0.0f, hits);
			if(hits.size() > 0)
			{
				Group.Context.Gun.GetActionStack().EvaluateTrigger(
					EAbilityTrigger.RaycastAction,
					Group.Context,
					TriggerContext.hit(Group.Context.Gun.GetShooter(), hits.get(0)));
			}
		}
		else FlansMod.LOGGER.warn("RaycastAction[" + Def + "]: Could not find level");
	}
}
