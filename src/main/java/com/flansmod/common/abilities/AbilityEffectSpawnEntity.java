package com.flansmod.common.abilities;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.gunshots.Raytracer;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AbilityEffectSpawnEntity implements IAbilityEffect
{
	@Nullable
	private final EntityType<? extends Entity> EntityType;

	public AbilityEffectSpawnEntity(@Nonnull AbilityEffectDefinition def)
	{
		String entityID = def.ModifyString(ModifierDefinition.KEY_ENTITY_TAG, "");
		EntityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityID));
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		if(EntityType != null)
		{
			Level level = gun.GetLevel();
			if (level != null)
			{
				targets.ForOnePosition((pos) ->
				{
					Entity entity = EntityType.create(level);
					if (entity != null)
					{
						entity.setPos(pos);
						level.addFreshEntity(entity);
					}
				});
			}
		}
	}
	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}
}
