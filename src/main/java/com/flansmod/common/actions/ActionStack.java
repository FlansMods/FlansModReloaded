package com.flansmod.common.actions;

import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.common.FlansMod;
import com.flansmod.common.abilities.AbilityStack;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.nodes.EjectCasingAction;
import com.flansmod.common.gunshots.EPressType;
import com.flansmod.common.network.FlansModPacketHandler;
import com.flansmod.common.network.bidirectional.ActionUpdateMessage;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.abilities.elements.AbilityStackingDefinition;
import com.flansmod.common.types.abilities.elements.EAbilityTrigger;
import com.flansmod.common.types.guns.elements.*;
import com.flansmod.common.types.magazines.EAmmoLoadMode;
import com.flansmod.physics.common.util.Maths;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
		public void EvaluateTrigger(@Nonnull EAbilityTrigger triggerType,
									@Nonnull GunContext gunContext,
									@Nullable ActionGroupContext actionGroupContext,
									@Nonnull TriggerContext triggerContext)
		{}
		@Override
		public boolean IsValid() { return false; }
	};

	private final List<ActionGroupInstance> ActiveActionGroups = new ArrayList<>();
	private final Map<String, AbilityStack> AbilityStacks = new HashMap<>();

	//private final List<AbilityInstance> InstancedAbilities = new ArrayList<>();
	private float ShotCooldown = 0.0f;
	private boolean cancelActionRequested = false;
	public final boolean IsClient;
	public boolean IsEquipped = false;

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
	private EActionResult TryStartGroupInstance(ActionGroupContext groupContext, boolean doInitialTrigger)
	{
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance == null)
			return EActionResult.TryNextAction;

		EActionResult result = groupInstance.CanStart();
		ReloadDefinition reload = groupContext.Gun.GetReloadDefinitionContaining(groupContext);
		if(reload != null)
		{
			EReloadStage stage = reload.GetStage(groupContext.GroupPath);
			if(stage == EReloadStage.Start && !groupContext.CanPerformReloadFromAttachedInventory(0))
			{
				result = EActionResult.TryNextAction;
			}
		}
		if(result == EActionResult.CanProcess)
		{
			// Check for toggle actions
			if(groupContext.RepeatMode() == ERepeatMode.Toggle)
			{
				if (groupInstance.HasStarted())
				{
					groupInstance.SetFinished();
					return result;
				}
			}

			// And see if any actions are waiting for the next action before finishing
			for(ActionGroupInstance existingGroup : ActiveActionGroups)
				if (existingGroup.RepeatMode() == ERepeatMode.WaitUntilNextAction)
					existingGroup.SetFinished();

			if (IsClient)
				groupInstance.OnStartClient();
			else
				groupInstance.OnStartServer(doInitialTrigger);

			OnActionGroupStarted(groupContext);
			EvaluateTrigger(EAbilityTrigger.StartActionGroup, groupContext, TriggerContext.self(groupContext));
			if(reload != null)
			{
				EReloadStage stage = reload.GetStage(groupContext.GroupPath);
				EvaluateTrigger(EAbilityTrigger.FromReloadStage(stage), groupContext, TriggerContext.self(groupContext));
			}
		}
		return result;
	}
	@Nonnull
	private EActionResult TryUpdateInputHeld(@Nonnull ActionGroupContext groupContext, boolean held)
	{
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance == null)
			return EActionResult.TryNextAction;

		groupInstance.UpdateInputHeld(held);

		return EActionResult.CanProcess;
	}

	public void CancelGroupInstance(@Nonnull ActionGroupContext context)
	{
		StopActionGroup(context);
	}
	public void UpdateEquipped(@Nonnull GunContext gunContext, boolean isEquipped)
	{
		if(isEquipped && !IsEquipped)
		{
			EvaluateTrigger(EAbilityTrigger.Equip, gunContext, TriggerContext.self(gunContext));
		}
		else if(!isEquipped && IsEquipped)
		{
			EvaluateTrigger(EAbilityTrigger.Unequip, gunContext, TriggerContext.self(gunContext));
		}
		IsEquipped = isEquipped;
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
				{
					actionGroup.OnTickClient();
					if(!actionGroup.Context.Gun.GetShooter().IsLocalPlayerOwner())
						actionGroup.CheckTimeout();
				}
				else
					actionGroup.OnTickServer();


			}
			else
			{
				FlansMod.LOGGER.error("Action " + ActiveActionGroups.get(i).Def.key + " was left in the system without being started");
				StopActionGroup(actionGroup.Context);
			}

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
			boolean stillFinished = true;
			if(groupInstance.HasStarted())
			{
				OnActionGroupFinished(groupContext);
				stillFinished = groupInstance.Finished();
			}

			if(stillFinished)
			{
				EvaluateTrigger(EAbilityTrigger.EndActionGroup, groupContext, TriggerContext.self(groupContext));

				if (IsClient)
					groupInstance.OnFinishClient();
				else
					groupInstance.OnFinishServer();

				for (int i = ActiveActionGroups.size() - 1; i >= 0; i--)
					if (ActiveActionGroups.get(i).Def.key.equals(groupContext.Def.key))
						ActiveActionGroups.remove(i);

				DebugLog("StopActionGroup called on " + groupContext.GroupPath);
			}
			else
			{
				DebugLog("StopActionGroup retriggered itself, not removing " + groupContext.GroupPath);
			}
		}
	}

	// -------------------------------------------------------------------------------------------------
	// Reload state machines
	// -------------------------------------------------------------------------------------------------
	private void OnActionGroupStarted(ActionGroupContext groupContext)
	{
		// When an action group starts, if it was part of a reload, move to the next step
		ReloadDefinition reload = groupContext.Gun.GetReloadDefinitionContaining(groupContext);
		if(reload != null)
		{
			EReloadStage stage = reload.GetStage(groupContext.GroupPath);
			if(stage == EReloadStage.Start)
			{
				OnStartReload(groupContext, 0);
			}
		}
	}

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
						if (CanReloadOne(groupContext, 0)
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
		DebugLog("EnterReloadState - " + reloadStage + " in " + reload.key);
		ActionGroupContext newGroupContext = triggeringActionGroup.Gun
			.GetActionGroupContextSibling(triggeringActionGroup, reload.GetReloadActionKey(reloadStage));
		ActionGroupInstance groupInstance = GetOrCreateGroupInstance(newGroupContext);
		EActionResult result = TryStartGroupInstance(newGroupContext, true);
		if (reloadStage == EReloadStage.LoadOne)// && !IsClient)
		{
			newGroupContext.LoadOne(0, newGroupContext.Gun.GetAttachedInventory(),IsClient);
		}
		//EActionResult result = TryStartGroupInstance(newGroupContext, true);
		// Send a message to the server about these actions if required
		
		if(IsClient) {
			if (result == EActionResult.CanProcess && (groupInstance.PropogateToServer() || groupInstance.NeedsNetSync())) {
				ActionUpdateMessage updateMsg = new ActionUpdateMessage(newGroupContext, EPressType.Press, groupInstance.GetStartedTick());
				updateMsg.AddTriggers(groupInstance, groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
				FlansModPacketHandler.SendToServer(new ActionUpdateMessage.ToServer(updateMsg));
				groupInstance.OnPerformedNetSync(groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
			}
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

	public void OnStartReload(ActionGroupContext groupContext, int magIndex)
	{
		if(IsClient && groupContext.Gun.GetShooter().IsLocalPlayerOwner())
			Client_LocalPlayerStartReload(groupContext, magIndex);
	}

	private void Client_LocalPlayerStartReload(ActionGroupContext groupContext, int magIndex)
	{
		if(groupContext.CanPerformReloadFromAttachedInventory(magIndex))
		{
			int bulletsInMag = groupContext.GetNumBulletsInMag(magIndex);
			int magSize = groupContext.GetMagazineSize(magIndex);

			if(groupContext.GetMagazineType(magIndex).ammoLoadMode == EAmmoLoadMode.FullMag)
				FirstPersonManager.LocalPlayerStartReload(1);
			else
				FirstPersonManager.LocalPlayerStartReload(magSize - bulletsInMag);
		}
	}

	public boolean CanReloadOne(ActionGroupContext groupContext, int magIndex)
	{
		if(IsClient && groupContext.Gun.GetShooter().IsLocalPlayerOwner())
		{
			return Client_LocalPlayerCanReloadOne(groupContext);
		}
		return groupContext.CanPerformReloadFromAttachedInventory(0);
	}

	@OnlyIn(Dist.CLIENT)
	private boolean Client_LocalPlayerCanReloadOne(ActionGroupContext groupContext)
	{
		return FirstPersonManager.ConsumeLoadOne();
	}

	// -------------------------------------------------------------------------------------------------
	// Tick
	// -------------------------------------------------------------------------------------------------
	public void OnTick(@Nullable Level level, @Nonnull GunContext gunContext)
	{
		if(level == null)
		{
			return;
		}

		ShotCooldown--;
		if(ShotCooldown < 0.0f)
			ShotCooldown = 0.0f;

		TickActions();
		TickAbilities(gunContext);
	}

	protected void DebugLog(@Nonnull String string)
	{
		//FlansMod.LOGGER.info("[" + MinecraftHelpers.GetTick() +
		//	(IsClient ? "|Client]" : "|Server]")
		//	+ string);
	}

	public void Clear(@Nonnull GunContext gunContext)
	{
		for(ActionGroupInstance actionGroup : ActiveActionGroups)
			actionGroup.SetFinished();
		ActiveActionGroups.clear();

		for(AbilityStack stacks : AbilityStacks.values())
			stacks.DecayAll();
		AbilityStacks.clear();
	}

	// -------------------------------------------------------------------------------------------------
	// CLIENT
	// -------------------------------------------------------------------------------------------------
	@OnlyIn(Dist.CLIENT)
	public EActionResult Client_TryStartGroupInstance(ActionGroupContext groupContext)
	{
		if(!IsClient)
		{
			FlansMod.LOGGER.error("Called Client function on server in ActionStack!");
			return EActionResult.TryNextAction;
		}
		// Start the instance
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);

		if(groupInstance == null)
			return EActionResult.TryNextAction;

		EActionResult result = TryStartGroupInstance(groupContext, true);
		// Send a message to the server about these actions if required
		if (result == EActionResult.CanProcess && (groupInstance.PropogateToServer() || groupInstance.NeedsNetSync()))
		{
			ActionUpdateMessage updateMsg = new ActionUpdateMessage(groupContext, EPressType.Press, groupInstance.GetStartedTick());
			updateMsg.AddTriggers(groupInstance, groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
			FlansModPacketHandler.SendToServer(new ActionUpdateMessage.ToServer(updateMsg));
			groupInstance.OnPerformedNetSync(groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
		}
		return result;
	}
	@OnlyIn(Dist.CLIENT) @Nonnull
	public EActionResult Client_TryUpdateGroupInstanceHeld(@Nonnull ActionGroupContext groupContext)
	{
		return Client_TryUpdateGroupInstance(groupContext, true);
	}
	@OnlyIn(Dist.CLIENT) @Nonnull
	public EActionResult Client_TryUpdateGroupInstanceNotHeld(@Nonnull ActionGroupContext groupContext)
	{
		return Client_TryUpdateGroupInstance(groupContext, false);
	}
	@OnlyIn(Dist.CLIENT) @Nonnull
	public EActionResult Client_TryUpdateGroupInstance(@Nonnull ActionGroupContext groupContext, boolean held)
	{
		if(!IsClient)
		{
			FlansMod.LOGGER.error("Called Client function on server in ActionStack!");
			return EActionResult.TryNextAction;
		}
		// Start the instance
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance == null || (!groupInstance.HasStarted() && groupInstance.RepeatMode() == ERepeatMode.FullAuto))
		{
			if(held)
				return Client_TryStartGroupInstance(groupContext);
			else
				return EActionResult.TryNextAction;
		}

		EActionResult result = TryUpdateInputHeld(groupContext, held);
		// Send a message to the server about these actions if required
		if (result == EActionResult.CanProcess && (groupInstance.NeedsNetSync() || !held))
		{
			ActionUpdateMessage updateMsg = new ActionUpdateMessage(groupContext, held ? EPressType.Hold : EPressType.Release, groupInstance.GetStartedTick());
			updateMsg.AddTriggers(groupInstance, groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
			FlansModPacketHandler.SendToServer(new ActionUpdateMessage.ToServer(updateMsg));
			groupInstance.OnPerformedNetSync(groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
		}
		return result;
	}
	// -------------------------------------------------------------------------------------------------
	// SERVER
	// -------------------------------------------------------------------------------------------------
	@Nonnull
	public EActionResult Server_TryHandleMessage(@Nonnull ActionUpdateMessage.ToServer msg, @Nonnull ServerPlayer from)
	{
		if (IsClient)
		{
			FlansMod.LOGGER.error("Called Server function on client in ActionStack!");
			return EActionResult.TryNextAction;
		}

		// Check that this is a valid context
		ActionGroupContext groupContext = msg.Data.GetActionGroupContext(false);
		if (!groupContext.IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedActionUpdate had invalid action");
			return EActionResult.TryNextAction;
		}

		if(!groupContext.Gun.GetShooter().IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedActionUpdate had invalid shooter");
			return EActionResult.TryNextAction;
		}

		EActionResult startResult = EActionResult.CanProcess;
		ActionGroupInstance groupInstance = GetOrCreateGroupInstance(groupContext);
		if (!groupInstance.HasStarted())
		{
			// Start the instance if we need to, though this should really only happen on Press actions
			if (msg.Data.GetPressType() != EPressType.Press)
				FlansMod.LOGGER.warn("Received ActionUpdateMessage with wrong press type for action that was not already running");
			startResult = TryStartGroupInstance(groupContext, false);
		}
		// Send a message to the server about these actions if required
		if (startResult == EActionResult.CanProcess)
		{
			for (var kvp : msg.Data.GetTriggers())
			{
				int triggerIndex = kvp.getKey();
				// TODO: Verify that this triggerIndex is valid. Rate limit to the gun fire rate for example

				// Quick catch. Don't let players put the server in a big loop
				if(triggerIndex >= groupInstance.TriggerCount + 30)
					continue;

				int actionIndex = 0;
				for (ActionInstance action : groupInstance.GetActions())
				{
					if (!action.VerifyServer(null))
						startResult = EActionResult.Wait;

					ActionInstance.NetData netData = msg.Data.GetNetData(triggerIndex, actionIndex);
					action.UpdateFromNetData(netData, triggerIndex);
					actionIndex++;
				}

				//FlansMod.LOGGER.info("Trying to trigger "+triggerIndex+ " with TriggerCount at "+groupInstance.TriggerCount);
				while(groupInstance.TriggerCount <= triggerIndex)
				{
					for (ActionInstance action : groupInstance.GetActions())
					{
						//action.OnTriggerServer(triggerIndex);
						if(action instanceof EjectCasingAction) {
							ActionInstance.NetData netData = msg.Data.GetNetData(triggerIndex, actionIndex);
							action.UpdateFromNetData(netData, triggerIndex);
						}
					}
					groupInstance.ProxyTriggerServer(triggerIndex);
					groupInstance.TriggerCount++;
				}

				// When we get a release message, we may need to do a bit of catchup in missed triggers
				if (msg.Data.GetPressType() == EPressType.Release)
				{
					// Check the action stack for this action/gun pairing and see if any of them are waiting for mouse release
					long numTicks = msg.Data.GetLastTriggerTick() - groupInstance.GetStartedTick();
					int expectedTriggerCount = Maths.floor(numTicks / groupContext.RepeatDelayTicks()) + 1;
					int serverTriggerCount = groupInstance.GetTriggerCount();

					if (expectedTriggerCount > serverTriggerCount + 1)
					{
						FlansMod.LOGGER.info("Client expected to trigger " + expectedTriggerCount + " repeat(s), but server only triggered " + serverTriggerCount + " repeat(s)");
					} else if (expectedTriggerCount < serverTriggerCount - 1)
					{
						FlansMod.LOGGER.info("Client expected to trigger " + expectedTriggerCount + " repeat(s), but server triggered " + serverTriggerCount + " many repeat(s)");
					}
					groupInstance.UpdateInputHeld(false);
				}
			}

			if (groupInstance.PropogateToServer() || groupInstance.NeedsNetSync())
			{
				Send_ServerToClient(groupContext, groupInstance, msg.Data, from);
			}
		}
		else
		{
			FlansMod.LOGGER.warn("Server believes we cannot start " + groupContext + " that client sent us");
			CancelGroupInstance(groupContext);
			// If we have a desync, lets remind them of the stack contents at this point
			groupContext.Gun.SetItemStack(groupContext.Gun.GetItemStack());
		}

		return startResult;
	}
	@Nonnull
	public EActionResult Server_TryStartGroupInstance(@Nonnull ActionGroupContext groupContext)
	{
		if(IsClient)
		{
			FlansMod.LOGGER.error("Called Server function on client in ActionStack!");
			return EActionResult.TryNextAction;
		}
		// Start the instance
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance == null)
			return EActionResult.TryNextAction;

		EActionResult result = TryStartGroupInstance(groupContext, true);
		// Send a message to the nearby clients about these actions if required
		if (result == EActionResult.CanProcess && (groupInstance.PropogateToServer() || groupInstance.NeedsNetSync()))
		{
			Send_ServerToClient_ActionGroupStart(groupContext, groupInstance, null);
		}
		return result;
	}
	@Nonnull
	public EActionResult Server_TryUpdateGroupInstanceHeld(@Nonnull ActionGroupContext groupContext)
	{
		return Server_TryUpdateGroupInstance(groupContext, true);
	}
	@Nonnull
	public EActionResult Server_TryUpdateGroupInstanceNotHeld(@Nonnull ActionGroupContext groupContext)
	{
		return Server_TryUpdateGroupInstance(groupContext, false);
	}
	@Nonnull
	public EActionResult Server_TryUpdateGroupInstance(@Nonnull ActionGroupContext groupContext, boolean held)
	{
		if(IsClient)
		{
			FlansMod.LOGGER.error("Called Server function on client in ActionStack!");
			return EActionResult.TryNextAction;
		}
		// Start the instance
		ActionGroupInstance groupInstance = TryGetGroupInstance(groupContext);
		if(groupInstance == null || (!groupInstance.HasStarted() && groupInstance.RepeatMode() == ERepeatMode.FullAuto))
		{
			if(held)
				return Server_TryStartGroupInstance(groupContext);
			else
				return EActionResult.TryNextAction;
		}

		EActionResult result = TryUpdateInputHeld(groupContext, held);
		// Send a message to the clients about these actions if required
		if (result == EActionResult.CanProcess && (groupInstance.NeedsNetSync() || !held))
		{
			if(held)
				Send_ServerToClient_ActionGroupHeld(groupContext, groupInstance, null);
			else
				Send_ServerToClient_ActionGroupReleased(groupContext, groupInstance, null);
		}
		return result;
	}

	// -------------------------------------------------------------------------------------------------
	// NET-SYNC
	// -------------------------------------------------------------------------------------------------
	protected void Send_ServerToClient_ActionGroupStart(@Nonnull ActionGroupContext groupContext,
														@Nonnull ActionGroupInstance groupInstance,
														@Nullable ServerPlayer triggeredByPlayer)
	{
		Send_ServerToClient(groupContext, groupInstance, EPressType.Press, triggeredByPlayer);
	}
	protected void Send_ServerToClient_ActionGroupHeld(@Nonnull ActionGroupContext groupContext,
													   @Nonnull ActionGroupInstance groupInstance,
													   @Nullable ServerPlayer triggeredByPlayer)
	{
		Send_ServerToClient(groupContext, groupInstance, EPressType.Hold, triggeredByPlayer);
	}
	protected void Send_ServerToClient_ActionGroupReleased(@Nonnull ActionGroupContext groupContext,
													       @Nonnull ActionGroupInstance groupInstance,
													       @Nullable ServerPlayer triggeredByPlayer)
	{
		Send_ServerToClient(groupContext, groupInstance, EPressType.Release, triggeredByPlayer);
	}
	protected void Send_ServerToClient(@Nonnull ActionGroupContext groupContext,
									   @Nonnull ActionGroupInstance groupInstance,
									   @Nonnull EPressType pressType,
									   @Nullable ServerPlayer triggeredByPlayer)
	{
		ActionUpdateMessage updateMsg = new ActionUpdateMessage(groupContext, pressType, groupInstance.GetStartedTick());
		updateMsg.AddTriggers(groupInstance, groupInstance.GetRequiredNetSyncMin(), groupInstance.GetRequiredNetSyncMax());
		Send_ServerToClient(groupContext, groupInstance, updateMsg, triggeredByPlayer);
	}
	protected void Send_ServerToClient(@Nonnull ActionGroupContext groupContext,
									   @Nonnull ActionGroupInstance groupInstance,
									   @Nonnull ActionUpdateMessage updateMsg,
									   @Nullable ServerPlayer triggeredByPlayer)
	{
		Level level = groupContext.Gun.GetLevel();
		if (level != null) {
			double radius = groupInstance.GetPropogationRadius();

			// Find out which positions we want to map around
			List<Vec3> positions = new ArrayList<>(2);
			for (var kvp : updateMsg.GetTriggers()) {
				groupInstance.AddExtraPositionsForNetSync(kvp.getKey(), positions);
			}
			if (groupInstance.ShouldAddPlayerPosForNetSync())
				positions.add(groupContext.Gun.GetShootOrigin().positionVec3());

			// Then send them some messages about the shot
			FlansModPacketHandler.SendToAllAroundPoints(
					new ActionUpdateMessage.ToClient(updateMsg),
					level.dimension(),
					positions,
					radius,
					triggeredByPlayer);
		}
	}


	// -------------------------------------------------------------------------------------------------
	// ABILTIES
	// -------------------------------------------------------------------------------------------------

	public void ForEachNonZeroStack(@Nonnull Consumer<AbilityStack> func)
	{
		for(AbilityStack stacks : AbilityStacks.values())
			if(stacks.IsActive())
				func.accept(stacks);
	}

	private void TickAbilities(@Nonnull GunContext gunContext)
	{
		for(AbilityStack stacks : AbilityStacks.values())
			stacks.Tick(gunContext);
	}

	@Nullable
	public AbilityStack GetStacks(@Nonnull AbilityStackingDefinition stackingDef)
	{
		if(AbilityStacks.containsKey(stackingDef.stackingKey))
			return AbilityStacks.get(stackingDef.stackingKey);
		return null;
	}
	@Nonnull
	public AbilityStack GetOrCreateStacks(@Nonnull AbilityStackingDefinition stackingDef, int tier)
	{
		if(AbilityStacks.containsKey(stackingDef.stackingKey))
			return AbilityStacks.get(stackingDef.stackingKey);

		AbilityStack stack = new AbilityStack(stackingDef, tier);
		AbilityStacks.put(stackingDef.stackingKey, stack);
		return stack;
	}

	public void EvaluateTrigger(@Nonnull EAbilityTrigger triggerType, @Nonnull ActionGroupContext actionGroupContext, @Nonnull TriggerContext triggerContext)
	{
		EvaluateTrigger(triggerType, actionGroupContext.Gun, actionGroupContext, triggerContext);
	}
	public void EvaluateTrigger(@Nonnull EAbilityTrigger triggerType, @Nonnull GunContext gunContext, @Nonnull TriggerContext triggerContext)
	{
		EvaluateTrigger(triggerType, gunContext, null, triggerContext);
	}

	// So there should be rules about don't trigger yourself... but for now, I'm just gonna cap it.
	private static int LOOP_CHECK = 0;
	private static final int LOOP_MAX = 4;
	public void EvaluateTrigger(@Nonnull EAbilityTrigger triggerType,
								@Nonnull GunContext gunContext,
								@Nullable ActionGroupContext actionGroupContext,
								@Nonnull TriggerContext triggerContext)
	{
		// ------ LOOOOPs are bad --------
		LOOP_CHECK++;
		if(LOOP_CHECK >= LOOP_MAX)
			return;
		// -------------------------------

		// e.g. OnHit -> SpawnProjectile for cluster munitions
		gunContext.ForEachAbility((ability, tier) ->
		{
			// Is this ability valid? Basic checks
			if(ability.IsValid())
			{
				// Is this ability triggered by this event?
				if(ability.MatchTrigger(triggerType, triggerContext))
				{
					// If so, which targets does it want from us?
					TargetsContext targetsContext = ability.MatchTargets(triggerContext);
					if(!targetsContext.IsEmpty())
					{
						// Now, run all the effects!
						// Stackable abilities store a little extra data
						AbilityStack stacks = null;
						if(ability.IsStackable())
						{
							stacks = GetOrCreateStacks(ability.stacking, tier);
							stacks.AddStack();
						}

						for(AbilityEffectDefinition effectDef : ability.effects)
						{
							if(IsClient)
								effectDef.GetEffectProcessor().TriggerClient(gunContext.GetActionGroupContext("base"), triggerContext, targetsContext, stacks);
							else
								effectDef.GetEffectProcessor().TriggerServer(gunContext.GetActionGroupContext("base"), triggerContext, targetsContext, stacks);
						}
					}
				}
			}
		 });

		LOOP_CHECK--;
	}
}


















