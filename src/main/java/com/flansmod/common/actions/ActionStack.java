package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ReloadDefinition;
import com.flansmod.common.types.guns.EReloadStage;
import com.flansmod.common.types.guns.GunContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ActionStack
{
	private List<Action> ActiveActions = new ArrayList<Action>();
	private float ShotCooldown = 0.0f;
	private List<ReloadProgress> ActiveReloads = new ArrayList<>();
	private boolean cancelActionRequested = false;

	public int TryShootMultiple(float timeBetweenShotsInTicks)
	{
		int shotCount = 0;
		while(ShotCooldown < 1.0f)
		{
			shotCount++;
			ShotCooldown += timeBetweenShotsInTicks;
		}
		return shotCount;
	}
	public float GetShotCooldown() { return ShotCooldown; }
	public List<Action> GetActions() { return ActiveActions; }
	public boolean IsReloading() { return ActiveReloads.size() > 0; }
	public void RequestCancel() { cancelActionRequested = true; }

	public Action AddAction(Level level, GunContext context, Action action)
	{
		ActiveActions.add(action);
		if (level.isClientSide)
			action.OnStartClient(context);
		else
			action.OnStartServer(context);
		return action;
	}

	public void AddReload(Level level, GunContext context, EActionSet actionSet, InteractionHand hand, ReloadDefinition reloadDef)
	{
		ReloadProgress reload = new ReloadProgress(reloadDef, actionSet, hand);
		EnterReloadState(context, reload, hand, EReloadStage.Start);
		ActiveReloads.add(reload);
		cancelActionRequested = false;
	}

	private void EnterReloadState(GunContext context, ReloadProgress reload, InteractionHand hand, EReloadStage stage)
	{
		if(context.IsValidForUse())
		{
			ActionDefinition[] reloadActionDefs = reload.Def.GetReloadActions(stage);
			for (int i = 0; i < reloadActionDefs.length; i++)
			{
				Action action = Actions.CreateAction(this, reloadActionDefs[i], hand);
				AddAction(context.shootFrom.level, context, action);
			}
			reload.CurrentStage = stage;
			reload.TicksInCurrentStage = 0;

			switch (stage)
			{
				case LoadOne:
				{
					int slotToLoad = context.GetNextBulletSlotToLoad(reload.ActionSet);
					context.LoadOne(reload.ActionSet, slotToLoad);
					break;
				}
			}
		}
	}


	public void OnTick(Level level, GunContext mainHand, GunContext offHand)
	{
		ShotCooldown--;
		if(ShotCooldown < 0.0f)
			ShotCooldown = 0.0f;

		for(int i = ActiveReloads.size() - 1; i >= 0; i--)
		{
			ReloadProgress reload = ActiveReloads.get(i);
			reload.TicksInCurrentStage++;
			GunContext context = reload.Hand == InteractionHand.MAIN_HAND ? mainHand : offHand;

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
						if(context.CanPerformReload() && !cancelActionRequested)
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
					EnterReloadState(context, reload, reload.Hand, nextStage);
				}
			}
		}

		// Reverse iterate to delete when done
		for(int i = ActiveActions.size() - 1; i >= 0; i--)
		{
			Action action = ActiveActions.get(i);
			GunContext context = action.hand == InteractionHand.MAIN_HAND ? mainHand : offHand;
			if(level.isClientSide)
				action.OnTickClient(context);
			else
				action.OnTickServer(context);

			if(action.Finished())
			{
				if(level.isClientSide)
					action.OnFinishClient(context);
				else
					action.OnFinishServer(context);
				ActiveActions.remove(i);
			}
		}
	}
}
