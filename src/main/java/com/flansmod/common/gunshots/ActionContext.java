package com.flansmod.common.gunshots;

import com.flansmod.common.actions.*;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.EActionType;
import com.flansmod.common.types.guns.ESpreadPattern;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.world.entity.Entity;

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
	public CachedActionStats BuildActionStatCache(ActionDefinition actionDef)
	{
		return new CachedActionStats(actionDef, Gun().GetAllApplicableModifiers(InputType));
	}

	@Nonnull
	public CachedGunStats BuildGunStatCache(ActionDefinition actionDef)
	{
		return new CachedGunStats(actionDef.shootStats[0], Gun().GetAllApplicableModifiers(InputType));
	}
}
