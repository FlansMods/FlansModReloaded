package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.EPressType;
import com.flansmod.common.network.FlansModPacketHandler;
import com.flansmod.common.network.bidirectional.ActionUpdateMessage;
import com.flansmod.common.types.guns.elements.*;
import com.flansmod.common.types.vehicles.EPlayerInput;
import com.flansmod.util.Maths;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
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
		public void OnTick(Level level, GunContext gunContext) {}
		@Override
		public boolean IsValid() { return false; }
	};

	private final List<ActionGroupInstance> ActiveActionGroups = new ArrayList<>();
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
	public List<ActionGroupInstance> GetActiveActionGroups() { return ActiveActionGroups; }

	public void RequestCancel() { cancelActionRequested = true; }
	public boolean IsValid() { return true; }


	// -------------------------------------------------------------------------------------------------
	// Action Group Instances
	// -------------------------------------------------------------------------------------------------
	public boolean IsActionGroupActive(ActionGroupContext groupContext)
	{
		for(ActionGroupInstance instance : ActiveActionGroups)
			if(instance.Def.key.equals(groupContext.Def.key))
				return true;
		return false;
	}
	@Nonnull
	private ActionGroupInstance CreateGroupInstance(ActionGroupContext groupContext)
	{
		ActionGroupInstance groupInstance = new ActionGroupInstance(groupContext);
		for(ActionDefinition actionDef : groupContext.Def.actions)
		{
			ActionInstance actionInstance = Actions.InstanceAction(groupInstance, actionDef);
			if(actionInstance != null)
				groupInstance.AddAction(actionInstance);
		}
		ActiveActionGroups.add(groupInstance);
		return groupInstance;
	}
	@Nullable
	public ActionGroupInstance TryGetGroupInstance(ActionGroupContext groupContext)
	{
		for(ActionGroupInstance instance : ActiveActionGroups)
			if(instance.Def.key.equals(groupContext.Def.key))
				return instance;
		return null;
	}
	@Nonnull
	public ActionGroupInstance GetOrCreateGroupInstance(ActionGroupContext groupContext)
	{
		ActionGroupInstance instance = TryGetGroupInstance(groupContext);
		if(instance == null)
			instance = CreateGroupInstance(groupContext);
		return instance;
	}
	private boolean TryStartGroupInstance(ActionGroupContext groupContext)
	{
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance == null)
			return false;
		if (!groupInstance.CanStart())
			return false;

		boolean shouldStart = true;

		// Check for toggle actions
		if(groupContext.RepeatMode() == ERepeatMode.Toggle)
		{
			if (groupInstance.HasStarted())
			{
				groupInstance.SetFinished();
				shouldStart = false;
			}
		}

		// And see if any actions are waiting for the next action before finishing
		for(ActionGroupInstance existingGroup : ActiveActionGroups)
			if (existingGroup.RepeatMode() == ERepeatMode.WaitUntilNextAction)
				existingGroup.SetFinished();

		if(shouldStart)
		{
			if (IsClient)
				groupInstance.OnStartClient();
			else
				groupInstance.OnStartServer();
		}
		return shouldStart;
	}
	private boolean TryUpdateInputHeld(ActionGroupContext groupContext, boolean held)
	{
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance == null)
			return false;

		groupInstance.UpdateInputHeld(held);

		return true;
	}

	public void CancelGroupInstance(ActionGroupContext context)
	{
		StopActionGroup(context);
	}
	private void TickActions()
	{
		// Reverse iterate to delete when done
		for(int i = ActiveActionGroups.size() - 1; i >= 0; i--)
		{
			ActionGroupInstance actionGroup = ActiveActionGroups.get(i);
			if(actionGroup.HasStarted())
			{
				if (IsClient)
					actionGroup.OnTickClient();
				else
					actionGroup.OnTickServer();
			}
			else
				FlansMod.LOGGER.error("Action was left in the system without being started");

			if(actionGroup.Finished())
			{
				StopActionGroup(actionGroup.Context);
			}
		}
	}
	private void StopActionGroup(ActionGroupContext groupContext)
	{
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance != null)
		{
			if(groupInstance.HasStarted())
			{
				OnActionGroupFinished(groupContext);
				if (IsClient)
					groupInstance.OnFinishClient();
				else
					groupInstance.OnFinishServer();
			}

			for(int i = ActiveActionGroups.size() - 1; i >= 0; i--)
				if(ActiveActionGroups.get(i).Def.key.equals(groupContext.Def.key))
					ActiveActionGroups.remove(i);
		}
	}

	// -------------------------------------------------------------------------------------------------
	// Reload state machines
	// -------------------------------------------------------------------------------------------------
	private void OnActionGroupFinished(ActionGroupContext groupContext)
	{
		// When an action group finishes, if it was part of a reload, move to the next step
		ReloadDefinition reload = groupContext.Gun.GetReloadDefinitionContaining(groupContext);
		if(reload != null)
		{
			EReloadStage endedStage = reload.GetStage(groupContext.GroupPath);
			EReloadStage nextStage = null;
			if(cancelActionRequested && endedStage != EReloadStage.End)
			{
				cancelActionRequested = false;
				if(!reload.endActionKey.isEmpty())
					nextStage = EReloadStage.End;
			}
			else
			{
				switch (endedStage)
				{
					case Start -> {
						if (!reload.ejectActionKey.isEmpty())
							nextStage = EReloadStage.Eject;
						else if (!reload.loadOneActionKey.isEmpty())
							nextStage = EReloadStage.LoadOne;
						else if (!reload.endActionKey.isEmpty())
							nextStage = EReloadStage.End;
					}
					case Eject, LoadOne -> {
						if (groupContext.CanPerformReloadFromAttachedInventory(0)
							&& !reload.loadOneActionKey.isEmpty())
						{
							nextStage = EReloadStage.LoadOne;
						}
						else if(!reload.endActionKey.isEmpty())
						{
							nextStage = EReloadStage.End;
						}
					}
				}
			}

			if(nextStage != null)
			{
				EnterReloadState(reload, nextStage, groupContext);
			}
		}
	}

	private void EnterReloadState(ReloadDefinition reload, EReloadStage reloadStage, ActionGroupContext triggeringActionGroup)
	{
		ActionGroupContext newGroupContext = triggeringActionGroup.Gun
			.GetActionGroupContextSibling(triggeringActionGroup, reload.GetReloadActionKey(reloadStage));
		ActionGroupInstance groupInstance = GetOrCreateGroupInstance(newGroupContext);
		TryStartGroupInstance(newGroupContext);
		if (reloadStage == EReloadStage.LoadOne)
		{
			newGroupContext.LoadOne(0, newGroupContext.Gun.GetAttachedInventory());
		}
	}

	public boolean IsReloading()
	{
		for(ActionGroupInstance activeGroup : ActiveActionGroups)
			if(activeGroup.Context.Gun.GetReloadDefinitionContaining(activeGroup.Context) != null
			&& activeGroup.HasStarted())
				return true;
		return false;
	}

	// -------------------------------------------------------------------------------------------------
	// Tick
	// -------------------------------------------------------------------------------------------------
	public void OnTick(Level level, GunContext gunContext)
	{
		if(level == null)
		{
			return;
		}

		ShotCooldown--;
		if(ShotCooldown < 0.0f)
			ShotCooldown = 0.0f;

		TickActions();
	}

	// -------------------------------------------------------------------------------------------------
	// CLIENT
	// -------------------------------------------------------------------------------------------------
	@OnlyIn(Dist.CLIENT)
	public boolean Client_TryStartGroupInstance(ActionGroupContext groupContext)
	{
		if(!IsClient)
		{
			FlansMod.LOGGER.error("Called Client function on server in ActionStack!");
			return false;
		}
		// Start the instance
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);

		if(IsReloading())
			return false;

		if(groupInstance != null && TryStartGroupInstance(groupContext))
		{
			// Send a message to the server about these actions if required
			if (groupInstance.PropogateToServer() || groupInstance.NeedsNetSync())
			{
				ActionUpdateMessage updateMsg = new ActionUpdateMessage(groupContext, EPressType.Press, groupInstance.GetStartedTick());
				updateMsg.AddTriggers(groupInstance, groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
				FlansModPacketHandler.SendToServer(new ActionUpdateMessage.ToServer(updateMsg));
				groupInstance.OnPerformedNetSync(groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
			}
			return true;
		}
		return false;
	}
	@OnlyIn(Dist.CLIENT)
	public boolean Client_TryUpdateGroupInstanceHeld(ActionGroupContext groupContext)
	{
		return Client_TryUpdateGroupInstance(groupContext, true);
	}
	@OnlyIn(Dist.CLIENT)
	public boolean Client_TryUpdateGroupInstanceNotHeld(ActionGroupContext groupContext)
	{
		return Client_TryUpdateGroupInstance(groupContext, false);
	}
	@OnlyIn(Dist.CLIENT)
	public boolean Client_TryUpdateGroupInstance(ActionGroupContext groupContext, boolean held)
	{
		if(!IsClient)
		{
			FlansMod.LOGGER.error("Called Client function on server in ActionStack!");
			return false;
		}
		// Start the instance
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance != null && TryUpdateInputHeld(groupContext, held))
		{
			// Send a message to the server about these actions if required
			if (groupInstance.NeedsNetSync())
			{
				ActionUpdateMessage updateMsg = new ActionUpdateMessage(groupContext, held ? EPressType.Hold : EPressType.Release, groupInstance.GetStartedTick());
				updateMsg.AddTriggers(groupInstance, groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
				FlansModPacketHandler.SendToServer(new ActionUpdateMessage.ToServer(updateMsg));
				groupInstance.OnPerformedNetSync(groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
			}
			return true;
		}
		return false;
	}
	// -------------------------------------------------------------------------------------------------
	// SERVER
	// -------------------------------------------------------------------------------------------------
	public boolean Server_TryHandleMessage(ActionUpdateMessage.ToServer msg, ServerPlayer from)
	{
		if(IsClient)
		{
			FlansMod.LOGGER.error("Called Server function on client in ActionStack!");
			return false;
		}

		// Check that this is a valid context
		ActionGroupContext groupContext = msg.Data.GetActionGroupContext(false);
		if(!groupContext.IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedActionUpdate had invalid action");
			return false;
		}

		boolean isValid = true;
		ActionGroupInstance groupInstance = GetOrCreateGroupInstance(groupContext);
		if(!groupInstance.HasStarted())
		{
			// Start the instance if we need to, though this should really only happen on Press actions
			if(msg.Data.GetPressType() != EPressType.Press)
				FlansMod.LOGGER.warn("Received ActionUpdateMessage with wrong press type for action that was not already running");
			isValid = TryStartGroupInstance(groupContext);
		}

		for(var kvp : msg.Data.GetTriggers())
		{
			int triggerIndex = kvp.getKey();
			// TODO: Verify that this triggerIndex is valid. Rate limit to the gun fire rate for example
			int actionIndex = 0;
			for(ActionInstance action : groupInstance.GetActions())
			{
				if(!action.VerifyServer(null))
					isValid = false;

				ActionInstance.NetData netData = msg.Data.GetNetData(triggerIndex, actionIndex);
				action.UpdateFromNetData(netData, triggerIndex);
				action.OnTriggerServer(triggerIndex);
				actionIndex++;
			}

			// When we get a release message, we may need to do a bit of catchup in missed triggers
			if(msg.Data.GetPressType() == EPressType.Release)
			{
				// Check the action stack for this action/gun pairing and see if any of them are waiting for mouse release
				long numTicks = msg.Data.GetLastTriggerTick() - groupInstance.GetStartedTick();
				int expectedTriggerCount = Maths.Floor(numTicks / groupContext.RepeatDelayTicks()) + 1;
				int serverTriggerCount = groupInstance.GetTriggerCount();

				if(expectedTriggerCount > serverTriggerCount)
				{
					FlansMod.LOGGER.info("Client expected to trigger " + expectedTriggerCount + " repeat(s), but server only triggered " + serverTriggerCount + " repeat(s)");
				}
				else if(expectedTriggerCount < serverTriggerCount)
				{
					FlansMod.LOGGER.info("Client expected to trigger " + expectedTriggerCount + " repeat(s), but server triggered " + serverTriggerCount + " many repeat(s)");
				}
				groupInstance.UpdateInputHeld(false);
			}
		}

		// Send a message to the server about these actions if required
		if (isValid && groupInstance.PropogateToServer() || groupInstance.NeedsNetSync())
		{
			double radius = groupInstance.GetPropogationRadius();

			// Find out which positions we want to map around
			List<Vec3> positions = new ArrayList<>(2);
			for(var kvp : msg.Data.GetTriggers())
			{
				groupInstance.AddExtraPositionsForNetSync(kvp.getKey(), positions);
			}
			if(groupInstance.ShouldAddPlayerPosForNetSync())
				positions.add(groupContext.Gun.GetShooter().GetShootOrigin().PositionVec3());

			// Then send them some messages about the shot
			FlansModPacketHandler.SendToAllAroundPoints(
				new ActionUpdateMessage.ToClient(msg.Data),
				from.level.dimension(),
				positions,
				radius,
				groupContext.Gun.GetShooter().Owner());
		}

		return isValid;
	}
	public boolean Server_TryStartGroupInstance(ActionGroupContext groupContext)
	{
		if(IsClient)
		{
			FlansMod.LOGGER.error("Called Server function on client in ActionStack!");
			return false;
		}
		// Start the instance
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance != null && TryStartGroupInstance(groupContext))
		{
			// Send a message to the nearby clients about these actions if required
			if (groupInstance.PropogateToServer() || groupInstance.NeedsNetSync())
			{
				Level level = groupContext.Gun.Level;
				if (level != null)
				{
					double radius = groupInstance.GetPropogationRadius();

					ActionUpdateMessage updateMsg = new ActionUpdateMessage(groupContext, EPressType.Press, groupInstance.GetStartedTick());
					updateMsg.AddTriggers(groupInstance, groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());

					// Find out which positions we want to map around
					List<Vec3> positions = new ArrayList<>(2);
					for (var kvp : updateMsg.GetTriggers())
					{
						groupInstance.AddExtraPositionsForNetSync(kvp.getKey(), positions);
					}
					if (groupInstance.ShouldAddPlayerPosForNetSync())
						positions.add(groupContext.Gun.GetShooter().GetShootOrigin().PositionVec3());

					// Then send them some messages about the shot
					FlansModPacketHandler.SendToAllAroundPoints(
						new ActionUpdateMessage.ToClient(updateMsg),
						level.dimension(),
						positions,
						radius,
						groupContext.Gun.GetShooter().Owner());
				}
			}
			return true;
		}
		return false;
	}
}
