package com.flansmod.common.actions;

import com.flansmod.common.types.guns.GunContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ActionStack
{
	private List<Action> ActiveActions = new ArrayList<Action>();
	private float ShotCooldown = 0.0f;

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

	public void AddAction(Level level, GunContext context, Action action)
	{
		ActiveActions.add(action);
		if(level.isClientSide)
			action.OnStartClient(context);
		else
			action.OnStartServer(context);
	}


	public void OnTick(Level level, GunContext mainHand, GunContext offHand)
	{
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

		ShotCooldown--;
		if(ShotCooldown < 0.0f)
			ShotCooldown = 0.0f;
	}
}
