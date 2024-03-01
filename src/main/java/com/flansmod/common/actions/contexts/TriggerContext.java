package com.flansmod.common.actions.contexts;

import com.flansmod.common.types.abilities.elements.AbilityTargetDefinition;
import com.flansmod.common.types.abilities.elements.EAbilityTarget;
import com.flansmod.common.types.abilities.elements.EAbilityTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TriggerContext
{
	@Nullable
	public final Entity Shooter;
	@Nullable
	public final Entity Owner;
	@Nullable
	public final HitResult Hit;
	@Nullable
	public final String ActionGroupPath;
	@Nullable
	public final Collection<Entity> SplashedEntities;

	private TriggerContext()
	{
		Shooter = null;
		Owner = null;
		Hit = null;
		SplashedEntities = null;
		ActionGroupPath = null;
	}
	private TriggerContext(@Nullable Entity owner, @Nullable Entity shooter, @Nullable HitResult hit, @Nullable Collection<Entity> splashes, @Nullable String actionGroupPath)
	{
		Shooter = shooter;
		Owner = owner;
		Hit = hit;
		SplashedEntities = splashes;
		ActionGroupPath = actionGroupPath;
	}
	public static TriggerContext empty()
	{
		return new TriggerContext(null, null, null, null, null);
	}
	public static TriggerContext self(@Nonnull ActionGroupContext actionGroup)
	{
		return new TriggerContext(actionGroup.Gun.GetShooter().Owner(), actionGroup.Gun.GetShooter().Entity(), null, null, actionGroup.GroupPath);
	}
	public static TriggerContext self(@Nonnull GunContext gun)
	{
		return new TriggerContext(gun.GetShooter().Owner(), gun.GetShooter().Entity(), null, null, null);
	}
	public static TriggerContext self(@Nonnull ShooterContext shooter)
	{
		return new TriggerContext(shooter.Owner(), shooter.Entity(), null, null, null);
	}
	public static TriggerContext self(@Nonnull Entity entity)
	{
		return new TriggerContext(entity, entity, null, null, null);
	}
	public static TriggerContext hit(@Nonnull ShooterContext from, @Nonnull HitResult hit)
	{
		return new TriggerContext(from.Owner(), from.Entity(), hit, null, null);
	}


	public static TriggerContext hitWithSplash(@Nonnull ActionGroupContext from, @Nonnull HitResult hit, @Nonnull Collection<Entity> splashes)
	{
		return new TriggerContext(from.Gun.GetShooter().Owner(), from.Gun.GetShooter().Entity(), hit, splashes, from.GroupPath);
	}
	public static TriggerContext hitWithSplash(@Nonnull GunContext from, @Nonnull HitResult hit, @Nonnull Collection<Entity> splashes)
	{
		return new TriggerContext(from.GetShooter().Owner(), from.GetShooter().Entity(), hit, splashes, null);
	}
	public static TriggerContext hitWithSplash(@Nonnull ShooterContext from, @Nonnull HitResult hit, @Nonnull Collection<Entity> splashes)
	{
		return new TriggerContext(from.Owner(), from.Entity(), hit, splashes, null);
	}

	public boolean CanTriggerFor(@Nonnull EAbilityTarget targetType)
	{
		switch(targetType)
		{
			case Shooter -> { return Shooter != null; }
			case Owner -> { return Owner != null; }
			case ShotEntity -> { return Hit != null && Hit.getType() == HitResult.Type.ENTITY; }
			case ShotBlock -> { return Hit != null && Hit.getType() == HitResult.Type.BLOCK; }
			case ShotPosition -> { return Hit != null; }
			case SplashedEntities -> { return SplashedEntities != null && SplashedEntities.size() > 0; }
			default -> { return true; }
		}
	}
	public boolean CanTriggerFor(@Nonnull AbilityTargetDefinition[] targetTypes)
	{
		for(AbilityTargetDefinition targetDef : targetTypes)
		{
			switch(targetDef.targetType)
			{
				case Shooter -> { if(Shooter != null) return true; }
				case Owner -> { if(Owner != null) return true; }
				case ShotEntity -> { if(Hit != null && Hit.getType() == HitResult.Type.ENTITY) return true; }
				case ShotBlock -> { if(Hit != null && Hit.getType() == HitResult.Type.BLOCK) return true; }
				case ShotPosition -> { if(Hit != null) return true; }
				case SplashedEntities -> { if(SplashedEntities != null && SplashedEntities.size() > 0) return true; }
				default -> { }
			}
		}
		return false;
	}

	public void TriggerOnEntities(@Nonnull AbilityTargetDefinition[] targetTypes, @Nonnull Consumer<Entity> func)
	{
		if(AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.Shooter, this) && Shooter != null)
			func.accept(Shooter);
		if(AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.Owner, this) && Owner != null)
			func.accept(Owner);
		if(AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.ShotEntity, this) && Hit != null && Hit.getType() == HitResult.Type.ENTITY)
			func.accept(((EntityHitResult)Hit).getEntity());
		if(AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.SplashedEntities, this) && SplashedEntities != null)
			for(Entity ent : SplashedEntities)
				func.accept(ent);
	}
	public void TriggerOnBlocks(@Nonnull AbilityTargetDefinition[] targetTypes, @Nonnull BiConsumer<BlockPos, BlockState> func)
	{
		if(AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.ShotBlock, this))
		{
			if(Hit != null && Hit instanceof BlockHitResult blockHitResult)
				if(Shooter != null)
					func.accept(blockHitResult.getBlockPos(), Shooter.level().getBlockState(blockHitResult.getBlockPos()));
		}
	}
	public void TriggerOnPositions(@Nonnull AbilityTargetDefinition[] targetTypes, @Nonnull Consumer<Vec3> func)
	{
		if(AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.Shooter, this) && Shooter != null)
			func.accept(Shooter.position());
		if(AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.Owner, this) && Owner != null)
			func.accept(Owner.position());
		if(Hit != null
			&& (AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.ShotEntity, this)
			|| AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.ShotBlock, this)
			|| AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.ShotPosition, this)))
			func.accept(Hit.getLocation());
		if(AbilityTargetDefinition.Matches(targetTypes, EAbilityTarget.SplashedEntities, this) && SplashedEntities != null)
			for(Entity ent : SplashedEntities)
				func.accept(ent.position());
	}



}
