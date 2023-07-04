package com.flansmod.common.actions;

import com.flansmod.common.gunshots.ActionContext;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ReloadDefinition;
import com.flansmod.common.types.guns.EReloadStage;
import com.flansmod.common.gunshots.GunContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

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
	private List<Action> ActiveActions = new ArrayList<Action>();
	private List<ReloadProgress> ActiveReloads = new ArrayList<>();
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
	public List<Action> GetActions() { return ActiveActions; }
	public boolean IsReloading() { return ActiveReloads.size() > 0; }
	public void RequestCancel() { cancelActionRequested = true; }

	public void AddAction(ActionContext context, Action action)
	{
		ActiveActions.add(action);
		if(IsClient)
			action.OnStartClient(context);
		else
			action.OnStartServer(context);
	}

	public void AddReload(ActionContext context, ReloadProgress reload)
	{
		if(!IsReloading())
		{
			ActiveReloads.add(reload);
			cancelActionRequested = false;
			EnterReloadState(context, reload, EReloadStage.Start);
		}
	}

	private void EnterReloadState(ActionContext actionContext, ReloadProgress reload, EReloadStage stage)
	{
		if(actionContext.IsValid())
		{
			ActionDefinition[] reloadActionDefs = reload.Def.GetReloadActions(stage);
			for (int i = 0; i < reloadActionDefs.length; i++)
			{
				Action action = Actions.CreateAction(reloadActionDefs[i], EActionInput.RELOAD);
				AddAction(actionContext, action);
			}
			reload.CurrentStage = stage;
			reload.TicksInCurrentStage = 0;

			switch (stage)
			{
				case LoadOne:
				{
					int slotToLoad = actionContext.Gun().GetNextBulletSlotToLoad();
					actionContext.Gun().LoadOne(slotToLoad);
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
			reload.TicksInCurrentStage++;
			if(reload.FinishedCurrentStage())
			{
				EReloadStage nextStage = null;
				switch(reload.CurrentStage)
				{
					case Start -> {
						nextStage = EReloadStage.Eject;
					}
					case Eject -> {
						nextStage = EReloadStage.LoadOne;
					}
					case LoadOne -> {
						if(gunContext.CanPerformReload() && !cancelActionRequested)
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
					ActionContext actionContext = ActionContext.CreateFrom(gunContext, EActionInput.RELOAD);
					EnterReloadState(actionContext, reload, nextStage);
				}
			}
		}

		// Reverse iterate to delete when done
		for(int i = ActiveActions.size() - 1; i >= 0; i--)
		{
			Action action = ActiveActions.get(i);
			ActionContext actionContext = ActionContext.CreateFrom(gunContext, action.inputType);
			if(level.isClientSide)
				action.OnTickClient(actionContext);
			else
				action.OnTickServer(actionContext);

			if(action.Finished(actionContext))
			{
				if(level.isClientSide)
					action.OnFinishClient(actionContext);
				else
					action.OnFinishServer(actionContext);
				ActiveActions.remove(i);
			}
		}
	}
}
