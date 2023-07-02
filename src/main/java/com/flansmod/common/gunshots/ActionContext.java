package com.flansmod.common.gunshots;

import com.flansmod.common.actions.*;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.CachedGunStats;
import com.flansmod.common.types.guns.EActionType;
import com.flansmod.common.types.guns.ESpreadPattern;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionContext
{
	public static final ActionContext INVALID = new ActionContext(GunContext.INVALID, EActionInput.PRIMARY);

	private final GunContext Gun;
	private final EActionInput InputType;
	public EActionInput InputType() { return InputType; }

	// Shooter references
	public ShooterContext Shooter() { return Gun.GetShooter(); }
	public Entity Owner() { return Gun.GetShooter().IsValid() ? Gun.GetShooter().Owner() : null; }
	public Entity Entity() { return Gun.GetShooter().IsValid() ? Gun.GetShooter().Entity() : null; }

	// Gun references
	public GunContext Gun() { return Gun; }
	public GunDefinition GunDef() { return Gun.GunDef(); }
	public ActionStack ActionStack() { return Gun.GetActionStack(); }

	public boolean IsValid() { return Gun().IsValid(); }

	// Caches
	@Nullable
	public CachedGunStats StatCache = null;

	public static ActionContext CreateFrom(GunContext gunContext, EActionInput inputType)
	{
		if(gunContext.IsValid())
			return new ActionContext(gunContext, inputType);
		return INVALID;
	}

	protected ActionContext(GunContext gun, EActionInput inputType)
	{
		Gun = gun;
		InputType = inputType;
	}

	public Action[] CreateActions()
	{
		ActionDefinition[] actionDefs = GunDef().GetActions(InputType);
		Action[] actions = new Action[actionDefs.length];
		for (int i = 0; i < actionDefs.length; i++)
		{
			actions[i] = Actions.CreateAction(actionDefs[i], InputType);
		}
		return actions;
	}

	public ReloadProgress[] CreateReloads()
	{
		if(InputType == EActionInput.RELOAD)
		{
			return new ReloadProgress[] {
				new ReloadProgress(GunDef().reload),
			};
		}
		return new ReloadProgress[0];
	}

	public ActionDefinition GetShootActionDefinition()
	{
		// TODO: Check attachments for changes in action
		// e.g. An underslung grenade launcher attachment

		for(ActionDefinition def : GunDef().GetActions(InputType))
			if(def.actionType == EActionType.Shoot)
				return def;
		return ActionDefinition.Invalid;
	}

	@Nonnull
	private CachedGunStats GetStatCache()
	{
		if(StatCache == null)
		{
			StatCache = new CachedGunStats();
			ActionDefinition shootAction = GetShootActionDefinition();
			if (shootAction != null)
			{
				StatCache.InitializeFrom(shootAction.shootStats[0]);
			}

			var modifierMap = Gun.GetAllApplicableModifiers(InputType);
			for (var kvp : modifierMap.entrySet())
			{
				StatCache.ApplyModifiers(kvp.getKey(), kvp.getValue());
			}
		}

		return StatCache;
	}

	public CachedGunStats GetStatBlock() { return GetStatCache(); }
	public float VerticalRecoil() { return GetStatCache().VerticalRecoil();}
	public float HorizontalRecoil() { return GetStatCache().HorizontalRecoil();}
	public float Spread() { return GetStatCache().Spread();}
	public float Speed() { return GetStatCache().Speed();}
	public int Count() { return GetStatCache().Count();}
	public float TimeToNextShot() { return GetStatCache().TimeToNextShot();}
	public float PenetrationPower() { return GetStatCache().PenetrationPower();}
	public float BaseDamage() { return GetStatCache().BaseDamage();}
	public float Knockback() { return GetStatCache().Knockback();}
	public float MultiplierVsPlayers() { return GetStatCache().MultiplierVsPlayers();}
	public float MultiplierVsVehicles() { return GetStatCache().MultiplierVsVehicles();}
	public float SplashDamageRadius() { return GetStatCache().SplashDamageRadius();}
	public float SplashDamageFalloff() { return GetStatCache().SplashDamageFalloff();}
	public float SetFireToTarget() { return GetStatCache().SetFireToTarget();}
	public float FireSpreadRadius() { return GetStatCache().FireSpreadRadius();}
	public float FireSpreadAmount() { return GetStatCache().FireSpreadAmount();}
	public ESpreadPattern SpreadPattern() { return GetStatCache().SpreadPattern();}
}
