package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.guns.EReloadStage;
import com.flansmod.common.gunshots.GunContext;
import com.flansmod.common.types.guns.ERepeatMode;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * There should be one ActionStack per gun, per shooter
 * So a plane with 4 cannons would have 4 seperate action stacks
 * And a player would have a MAIN_HAND and OFF_HAND action stack
 *
 * Any custom Flan's Mod entities can store their own ActionStacks
 * Any vanilla entity shooters, like players, can store them in ActionManager
 */
public class ActionStack
{
	public static final ActionStack Invalid = new ActionStack(false) {
		@Override
		public void AddActionGroup(ActionGroupContext context, ActionGroup action) {}
		@Override
		public void AddReload(ActionGroupContext context, ReloadProgress reload) {}
		@Override
		public void OnTick(Level level, GunContext gunContext) {}
		@Override
		public boolean IsValid() { return false; }
	};

	private final List<ActionGroup> ActiveActionGroups = new ArrayList<>();
	private final List<ReloadProgress> ActiveReloads = new ArrayList<>();
	private float ShotCooldown = 0.0f;
	private boolean cancelActionRequested = false;
	private final boolean IsClient;

	public ActionStack(boolean client)
	{
		IsClient = client;
	}

	public int TryShootMultiple(float timeBetweenShotsInSeconds)
	{
		int shotCount = 0;
		while(ShotCooldown < 1.0f)
		{
			shotCount++;
			ShotCooldown += timeBetweenShotsInSeconds * 20f;
		}
		return shotCount;
	}
	public float GetShotCooldown() { return ShotCooldown; }
	public List<ActionGroup> GetActiveActionGroups() { return ActiveActionGroups; }
	public boolean IsReloading() { return ActiveReloads.size() > 0; }
	public void RequestCancel() { cancelActionRequested = true; }
	public boolean IsValid() { return true; }

	public void AddActionGroup(ActionGroupContext context, ActionGroup group)
	{
		// Stop any actions that are waiting for a new action to be applied
		for(ActionGroup existingGroup : ActiveActionGroups)
			if(existingGroup.InputType == context.InputType)
				if(existingGroup.RepeatMode(context) == ERepeatMode.WaitUntilNextAction)
					existingGroup.SetFinished();

		ActiveActionGroups.add(group);
		if(IsClient)
			group.OnStartClient(context);
		else
			group.OnStartServer(context);
	}

	@Nullable
	public ActionGroup FindMatchingActiveGroup(ActionGroupDefinition groupDef)
	{
		for(ActionGroup group : ActiveActionGroups)
			if(group.Def == groupDef)
				return group;
		return null;
	}

	public void AddReload(ActionGroupContext context, ReloadProgress reload)
	{
		if(!IsReloading())
		{
			ActiveReloads.add(reload);
			cancelActionRequested = false;
			EnterReloadState(context, reload, EReloadStage.Start);
		}
	}

	private void EnterReloadState(ActionGroupContext actionContext, ReloadProgress reload, EReloadStage stage)
	{
		if(actionContext.IsValid())
		{
			ActionGroupDefinition reloadActionGroup = reload.Def.GetReloadActionGroup(stage);
			ActionGroup actionGroup = Actions.CreateActionGroup(reloadActionGroup, reload.ReloadType);
			AddActionGroup(actionContext, actionGroup);
			reload.CurrentStage = stage;
			reload.TicksInCurrentStage = 0;

			switch (stage)
			{
				case LoadOne:
				{
					actionContext.LoadOne(0, actionContext.Gun.GetAttachedInventory());
					break;
				}
			}
		}
	}

	public void OnTick(Level level, GunContext gunContext)
	{
		if(level == null)
		{
			return;
		}

		ShotCooldown--;
		if(ShotCooldown < 0.0f)
			ShotCooldown = 0.0f;

		for(int i = ActiveReloads.size() - 1; i >= 0; i--)
		{
			ReloadProgress reload = ActiveReloads.get(i);
			ActionGroupContext actionContext = ActionGroupContext.CreateFrom(gunContext, reload.ReloadType);
			reload.TicksInCurrentStage++;
			if(reload.FinishedCurrentStage())
			{
				EReloadStage nextStage = null;
				for(ActionGroup group : actionContext.ActionStack().GetActiveActionGroups())
				{
					if(group.Def == gunContext.GunDef().GetReload(reload.ReloadType).GetReloadActionGroup(reload.CurrentStage))
					{
						group.SetFinished();
					}
				}
				switch(reload.CurrentStage)
				{
					case Start -> {
						if(reload.Def.eject.actions.length > 0)
							nextStage = EReloadStage.Eject;
						else
							nextStage = EReloadStage.LoadOne;
					}
					case Eject, LoadOne -> {
						if(actionContext.CanPerformReloadFromAttachedInventory(0) && !cancelActionRequested)
							nextStage = EReloadStage.LoadOne;
						else
						{
							cancelActionRequested = false;
							nextStage = EReloadStage.End;
						}
					}
				}

				if(nextStage == null)
				{
					ActiveReloads.remove(i);
				}
				else
				{
					EnterReloadState(actionContext, reload, nextStage);
				}
			}
		}

		// Reverse iterate to delete when done
		for(int i = ActiveActionGroups.size() - 1; i >= 0; i--)
		{
			ActionGroup actionGroup = ActiveActionGroups.get(i);
			ActionGroupContext groupContext = ActionGroupContext.CreateFrom(gunContext, actionGroup.InputType);
			if(level.isClientSide)
				actionGroup.OnTickClient(groupContext);
			else
				actionGroup.OnTickServer(groupContext);

			if(actionGroup.Finished(groupContext))
			{
				if(level.isClientSide)
					actionGroup.OnFinishClient(groupContext);
				else
					actionGroup.OnFinishServer(groupContext);
				ActiveActionGroups.remove(i);
			}
		}
	}
}
