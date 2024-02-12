package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.JsonField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nonnull;

public class AbilityTargetDefinition
{
	@JsonField
	public EAbilityTarget targetType = EAbilityTarget.Shooter;

	// TODO: Use ItemCollectionDefinition, but for Entity/Block
	@JsonField
	public ResourceLocation[] matchIDs = new ResourceLocation[0];
	@JsonField
	public ResourceLocation[] matchTags = new ResourceLocation[0];


	public boolean Matches(@Nonnull EAbilityTarget checkTargetType)
	{
		return targetType == checkTargetType;
	}

	public boolean Matches(@Nonnull EAbilityTarget checkTargetType, @Nonnull TriggerContext triggerContext)
	{
		if(targetType != checkTargetType)
			return false;



		return true;
	}

	public static boolean Matches(@Nonnull AbilityTargetDefinition[] targetDefs, @Nonnull EAbilityTarget checkTargetType, @Nonnull TriggerContext triggerContext)
	{
		for(AbilityTargetDefinition targetDef : targetDefs)
			if(targetDef.Matches(checkTargetType, triggerContext))
				return true;
		return false;
	}

	public void ApplyTo(@Nonnull TriggerContext triggerContext, @Nonnull TargetsContext targetsContext)
	{
		switch(targetType)
		{
			case Owner -> { targetsContext.Add(triggerContext.Owner); }
			case Shooter -> { targetsContext.Add(triggerContext.Shooter); }
			case ShotEntity -> {
				if(triggerContext.Hit instanceof EntityHitResult entHitResult)
					targetsContext.Add(entHitResult.getEntity());
			}
			case ShotBlock -> {
				Level level = triggerContext.Shooter != null ? triggerContext.Shooter.level() : null;
				if(level != null && triggerContext.Hit instanceof BlockHitResult blockHitResult)
					targetsContext.Add(blockHitResult.getBlockPos(), level.getBlockState(blockHitResult.getBlockPos()));
			}
			case ShotPosition -> {
				if(triggerContext.Hit != null)
					targetsContext.Add(triggerContext.Hit.getLocation());
			}
			case SplashedEntities -> {
				if(triggerContext.SplashedEntities != null)
					for(Entity entity : triggerContext.SplashedEntities)
						targetsContext.Add(entity);
			}
		}
	}
}
