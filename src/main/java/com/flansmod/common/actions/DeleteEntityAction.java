package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.Raytracer;
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

public class DeleteEntityAction extends ActionInstance
{
	public DeleteEntityAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def)
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
		String checkTag = EntityTag();
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
					if(hits.get(0).getType() == HitResult.Type.ENTITY)
					{
						EntityHitResult entityHit = (EntityHitResult)hits.get(0);
						if(checkTag.isEmpty() || entityHit.getEntity().getTags().contains(checkTag))
							entityHit.getEntity().kill();
					}
				}
			}
			else FlansMod.LOGGER.warn("DeleteEntityAction[" + Def + "]: Could not find raytracer for level " + level);
		}
		else FlansMod.LOGGER.warn("DeleteEntityAction[" + Def + "]: Could not find level");
	}

	public String EntityTag() { return Group.Context.ModifyString(ModifierDefinition.KEY_ENTITY_ID, ""); }

}
