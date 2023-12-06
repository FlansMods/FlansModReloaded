package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.gunshots.EPlayerHitArea;
import com.flansmod.common.gunshots.PlayerHitResult;
import com.flansmod.common.types.abilities.AbilityDefinition;
import com.flansmod.common.types.abilities.EAbilityTrigger;
import com.flansmod.common.types.guns.elements.EReloadStage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbilityInstance
{
	public final AbilityDefinition Def;
	public final int Level;



	public AbilityInstance(AbilityDefinition def, int level)
	{
		Def = def;
		Level = level;
	}

	public abstract void Trigger(@Nonnull GunContext gun, @Nullable HitResult hit);
	public abstract void End(@Nonnull GunContext gun);
	public abstract void Tick();

	public void OnEquip(@Nonnull GunContext gun, boolean equip)
	{
		switch(Def.startTrigger)
		{
			case Equip -> {
				if(equip)
					Trigger(gun, null);
			}
			case Unequip -> {
				if(!equip)
					Trigger(gun, null);
			}
		}
		switch(Def.endTrigger)
		{
			case Equip -> {
				if(equip)
					End(gun);
			}
			case Unequip -> {
				if(!equip)
					End(gun);
			}
		}
	}

	public void OnHit(@Nonnull GunContext gun, @Nonnull HitResult hit)
	{
		switch(Def.startTrigger)
		{
			case ShotHeadshot -> {
				if (hit instanceof PlayerHitResult playerHit)
				{
					if(playerHit.GetHitbox().area == EPlayerHitArea.HEAD)
						Trigger(gun, playerHit);
				}
			}
			case ShotEntity -> {
				if(hit instanceof EntityHitResult entityHit)
					Trigger(gun, entityHit);
			}
			case ShotBlock -> {
				if(hit instanceof BlockHitResult blockHit)
					Trigger(gun, blockHit);
			}
			case ShotAndBrokeBlock -> {
				// Hmm TODO:
			}
		}
		switch(Def.endTrigger)
		{
			case ShotHeadshot -> {
				if (hit instanceof PlayerHitResult playerHit)
				{
					if(playerHit.GetHitbox().area == EPlayerHitArea.HEAD)
						End(gun);
				}
			}
			case ShotEntity -> {
				if(hit instanceof EntityHitResult entityHit)
					End(gun);
			}
			case ShotBlock -> {
				if(hit instanceof BlockHitResult blockHit)
					End(gun);
			}
			case ShotAndBrokeBlock -> {
				// Hmm TODO:
			}
		}
	}

	public void OnReloadStage(@Nonnull ActionGroupContext actionGroup, @Nonnull EReloadStage stage)
	{
		switch(Def.startTrigger)
		{
			case ReloadStart -> {
				if(stage == EReloadStage.Start)
					Trigger(actionGroup.Gun, null);
			}
			case ReloadEject -> {
				if(stage == EReloadStage.Eject)
					Trigger(actionGroup.Gun, null);
			}
			case ReloadLoadOne -> {
				if(stage == EReloadStage.LoadOne)
					Trigger(actionGroup.Gun, null);
			}
			case ReloadEnd -> {
				if(stage == EReloadStage.End)
					Trigger(actionGroup.Gun, null);
			}
		}
		switch(Def.endTrigger)
		{
			case ReloadStart -> {
				if(stage == EReloadStage.Start)
					End(actionGroup.Gun);
			}
			case ReloadEject -> {
				if(stage == EReloadStage.Eject)
					End(actionGroup.Gun);
			}
			case ReloadLoadOne -> {
				if(stage == EReloadStage.LoadOne)
					End(actionGroup.Gun);
			}
			case ReloadEnd -> {
				if(stage == EReloadStage.End)
					End(actionGroup.Gun);
			}
		}
	}

	private boolean CheckConditions(@Nonnull String input)
	{
		if(Def.triggerConditions.length == 0)
			return true;
		for (String condition : Def.triggerConditions)
			if (condition.equals(input))
				return true;
		return false;
	}


	public void OnActionGroupStarted(@Nonnull ActionGroupContext groupContext)
	{
		if(Def.startTrigger == EAbilityTrigger.StartActionGroup)
			if(CheckConditions(groupContext.GroupPath))
				Trigger(groupContext.Gun, null);
		if(Def.endTrigger == EAbilityTrigger.StartActionGroup)
			if(CheckConditions(groupContext.GroupPath))
				End(groupContext.Gun);
	}

	public void OnActionGroupTriggered(@Nonnull ActionGroupContext groupContext)
	{
		if(Def.startTrigger == EAbilityTrigger.TriggerActionGroup)
			if(CheckConditions(groupContext.GroupPath))
				Trigger(groupContext.Gun, null);
		if(Def.endTrigger == EAbilityTrigger.TriggerActionGroup)
			if(CheckConditions(groupContext.GroupPath))
				End(groupContext.Gun);
	}

	public void OnActionGroupEnded(@Nonnull ActionGroupContext groupContext)
	{
		if(Def.startTrigger == EAbilityTrigger.EndActionGroup)
			if(CheckConditions(groupContext.GroupPath))
				Trigger(groupContext.Gun, null);
		if(Def.endTrigger == EAbilityTrigger.EndActionGroup)
			if(CheckConditions(groupContext.GroupPath))
				End(groupContext.Gun);
	}

	public void OnModeSwitch(@Nonnull GunContext gunContext, @Nonnull String modeSelected)
	{
		if(Def.startTrigger == EAbilityTrigger.SwitchMode)
			if(CheckConditions(modeSelected))
				Trigger(gunContext, null);
		if(Def.endTrigger == EAbilityTrigger.SwitchMode)
			if(CheckConditions(modeSelected))
				End(gunContext);
	}

	@Nonnull
	protected List<Entity> GetEntityTargets(@Nonnull GunContext gun, @Nullable HitResult hit)
	{
		List<Entity> targets = new ArrayList<>();
		switch(Def.targetType)
		{
			case Shooter -> { targets.add(gun.GetShooter().Entity()); }
			case Owner -> { targets.add(gun.GetShooter().Owner()); }
			case ShotEntity -> {
				if(hit instanceof EntityHitResult entityHit)
					targets.add(entityHit.getEntity());
			}
			case SplashedEntities -> {
				// TODO:
			}
		}
		return targets;
	}
}
