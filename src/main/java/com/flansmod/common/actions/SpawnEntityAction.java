package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpawnEntityAction extends ActionInstance
{

	public SpawnEntityAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def)
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

		String entityID = EntityID();
		EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityID));
		if(entityType != null)
		{
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
							Entity entity = entityType.create(level);
							if(entity != null)
							{
								level.addFreshEntity(entity);
							}
						}
					}
				}
				else FlansMod.LOGGER.warn("SpawnEntityAction[" + Def + "]: Could not find raytracer for level " + level);
			}
			else FlansMod.LOGGER.warn("SpawnEntityAction[" + Def + "]: Could not find level");
		}
	}

	public String EntityID() { return Group.Context.ModifyString(ModifierDefinition.KEY_ENTITY_ID, ""); }
}
