package com.flansmod.common.gunshots;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.network.FlansModPacketHandler;
import com.flansmod.common.network.bidirectional.ActionUpdateMessage;
import com.flansmod.util.Maths;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionManager
{
	private final boolean IsClient;
	private HashMap<GunContext, ActionStack> ActionStacks = new HashMap<GunContext, ActionStack>();

	@Nonnull
	public ActionStack GetActionStack(GunContext context)
	{
		if(!context.IsValid())
		{
			FlansMod.LOGGER.warn("Tried to get action stack for invalid context");
			return ActionStack.Invalid;
		}
		if(!context.GetShooter().IsValid())
		{
			FlansMod.LOGGER.warn("Tried to get action stack for a valid context, but without an entity. This may be supported later");
			return ActionStack.Invalid;
		}

		ActionStack entitysActionStack = ActionStacks.get(context);
		if(entitysActionStack == null)
		{
			entitysActionStack = new ActionStack(IsClient);
			ActionStacks.put(context, entitysActionStack);
		}
		return entitysActionStack;
	}

	@Nonnull
	public static ActionStack SafeGetActionStack(GunContext context)
	{
		Level level = context.Level();
		if(level != null)
		{
			if (level.isClientSide)
			{
				return FlansModClient.ACTIONS_CLIENT.GetActionStack(context);
			}
			else
			{
				return FlansMod.ACTIONS_SERVER.GetActionStack(context);
			}
		}
		else
		{
			FlansMod.LOGGER.warn("Could not get ActionStack for GunContext " + context.toString());
			return ActionStack.Invalid;
		}
	}

	public ActionManager(boolean client)
	{
		IsClient = client;
	}

	// ----------------------------------------------------------------------------------------------------------------
	// CLIENT
	// ----------------------------------------------------------------------------------------------------------------

	public void HookClient(IEventBus modEventBus)
	{
		FlansModPacketHandler.RegisterClientHandler(ActionUpdateMessage.ToClient.class, ActionUpdateMessage.ToClient::new, this::OnClientReceivedActionUpdate);
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyPressed(Player player, EActionInput inputType)
	{
		// See if pressing this button should trigger any actions
		// First on our main hand, then on our off hand

		ShooterContext shooter = ShooterContext.GetOrCreate(player);
		if(!shooter.IsValid())
			return;

		// Ask the ShooterContext which actions on which guns we should perform
		ActionGroupContext[] actionContexts = shooter.GetPrioritisedActions(inputType);
		// Then, in order, perform those actions (with actions able to block subsequent execution)
		for (ActionGroupContext groupContext : actionContexts)
		{
			// First, accumulate our action set and check if they all allow us to start the requested action
			ActionGroup actionGroup = groupContext.CreateActionGroup();
			ReloadProgress[] reloads = groupContext.CreateReloads();
			boolean needsNetMsg = false;
			boolean canStart = true;
			boolean shouldFallbackToReload = false;
			if(!actionGroup.CanStart(groupContext))
			{
				canStart = false;
				if(actionGroup.ShouldFallBackToReload(groupContext))
					shouldFallbackToReload = true;
			}
			if(actionGroup.PropogateToServer(groupContext))
				needsNetMsg = true;

			// Now we know that they can start, let's trigger them
			if (canStart)
			{
				//GunshotCollection shots = null;
				//int triggerCount = 0;
				groupContext.ActionStack().AddActionGroup(groupContext, actionGroup);

				//ShootAction shootAction = actionGroup.GetShootAction();
				//if(shootAction != null)
				//{
				//	shots = shootAction.GetResults(0);
				//	triggerCount = shootAction.GetTriggerCount();
				//}
				for (ReloadProgress reload : reloads)
				{
					groupContext.ActionStack().AddReload(groupContext, reload);
					needsNetMsg = true;
				}

				// Send a message to the server about these actions if required
				if(needsNetMsg || actionGroup.NeedsNetSync())
				{
					ActionUpdateMessage updateMsg = new ActionUpdateMessage(groupContext, EPressType.Press, actionGroup.GetStartedTick());
					updateMsg.AddTriggers(actionGroup, actionGroup.GetRequiredNetSyncMin(), actionGroup.GetRequiredNetSyncMax());
					FlansModPacketHandler.SendToServer(new ActionUpdateMessage.ToServer(updateMsg));
					actionGroup.OnPerformedNetSync(actionGroup.GetRequiredNetSyncMin(), actionGroup.GetRequiredNetSyncMax());
				}
			}
			else if(shouldFallbackToReload
				&& !inputType.IsReload()
				&& inputType.GetReloadType() != null
				&& !groupContext.ActionStack().IsReloading())
			{
				ClientKeyPressed(player, inputType.GetReloadType());
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyHeld(Player player, EActionInput inputType)
	{
		// See if any of the in-progress actions on this gun should stop on release
		ShooterContext shooter = ShooterContext.GetOrCreate(player);
		if(!shooter.IsValid())
			return;

		// Ask the ShooterContext which actions on which guns we should perform
		ActionGroupContext[] groupContexts = shooter.GetPrioritisedActions(inputType);
		for(ActionGroupContext groupContext : groupContexts)
		{
			// Check the action stack for this action/gun pairing and see if any of them are waiting for mouse release
			ActionGroup activeGroup = groupContext.GetExistingActionGroup();
			if(activeGroup != null)
			{
				activeGroup.UpdateInputHeld(groupContext, true);

				// Then if this input hold has triggered repeat actions, we may need to send those to the server
				if(activeGroup.NeedsNetSync())
				{
					ActionUpdateMessage updateMsg = new ActionUpdateMessage(groupContext, EPressType.Hold, activeGroup.GetStartedTick());
					updateMsg.AddTriggers(activeGroup, activeGroup.GetRequiredNetSyncMin(), activeGroup.GetRequiredNetSyncMax());
					FlansModPacketHandler.SendToServer(new ActionUpdateMessage.ToServer(updateMsg));
					activeGroup.OnPerformedNetSync(activeGroup.GetRequiredNetSyncMin(), activeGroup.GetRequiredNetSyncMax());
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyReleased(Player player, EActionInput inputType, int ticksSinceHeld)
	{
		// See if any of the in-progress actions on this gun should stop on release
		ShooterContext shooter = ShooterContext.GetOrCreate(player);
		if(!shooter.IsValid())
			return;

		// Ask the ShooterContext which actions on which guns we should perform
		ActionGroupContext[] actionContexts = shooter.GetPrioritisedActions(inputType);
		for(ActionGroupContext actionContext : actionContexts)
		{
			//
			ActionGroup actionGroup = actionContext.GetExistingActionGroup();

			if(actionGroup != null)
			{
				// Check the action stack for this action/gun pairing and see if any of them are waiting for mouse release
				actionGroup.UpdateInputHeld(actionContext, false);

				ActionUpdateMessage releaseMsg = new ActionUpdateMessage(actionContext, EPressType.Release, actionGroup.GetStartedTick());
				releaseMsg.AddTriggers(actionGroup, actionGroup.GetRequiredNetSyncMin(), actionGroup.GetRequiredNetSyncMax());
				FlansModPacketHandler.SendToServer(new ActionUpdateMessage.ToServer(releaseMsg));
				actionGroup.OnPerformedNetSync(actionGroup.GetRequiredNetSyncMin(), actionGroup.GetRequiredNetSyncMax());
			}
		}
	}

	// This will only be sent to you when someone else fires a shot. This is where you play various actions in response
	@OnlyIn(Dist.CLIENT)
	private void OnClientReceivedActionUpdate(ActionUpdateMessage.ToClient msg)
	{
		ActionGroupContext actionContext = msg.Data.GetActionGroupContext(true);
		if(actionContext.IsValid())
		{
			if(actionContext.Shooter().Entity() == Minecraft.getInstance().player)
			{
				FlansMod.LOGGER.warn("OnClientReceivedActionUpdate received with data for myself");
				return;
			}

			// See if we are updating an existing action, or if we need to start fresh
			ActionGroup actionGroup = actionContext.GetExistingActionGroup();
			if(actionGroup == null)
			{
				if(msg.Data.GetPressType() != EPressType.Press)
					FlansMod.LOGGER.warn("Received ActionUpdateMessage with wrong press type for action that was not already running");
				actionGroup = actionContext.CreateActionGroup();
				actionContext.ActionStack().AddActionGroup(actionContext, actionGroup);
			}


			if(msg.Data.GetPressType() == EPressType.Press)
				actionGroup.OnStartClientFromNetwork(actionContext, msg.Data.GetStartTick());

			// Now run through all the triggers that are bundled in this message and run any client side effects
			for(var kvp : msg.Data.GetTriggers())
			{
				int triggerIndex = kvp.getKey();
				int actionIndex = 0;
				for(Action action : actionGroup.GetActions())
				{
					Action.NetData netData = msg.Data.GetNetData(triggerIndex, actionIndex);
					action.UpdateFromNetData(netData, triggerIndex);
					action.OnTriggerClient(actionContext, triggerIndex);
					actionIndex++;
				}
			}

			if(msg.Data.GetPressType() == EPressType.Release)
			{
				// Check the action stack for this action/gun pairing and see if any of them are waiting for mouse release
				long numTicks = msg.Data.GetLastTriggerTick() - actionGroup.GetStartedTick();
				int expectedTriggerCount = Maths.Floor(numTicks / actionContext.RepeatDelayTicks()) + 1;
				int serverTriggerCount = actionGroup.GetTriggerCount();

				if(expectedTriggerCount > serverTriggerCount)
				{
					FlansMod.LOGGER.info("Client expected to trigger " + expectedTriggerCount + " repeat(s), but server only triggered " + serverTriggerCount + " repeat(s)");
				}
				else if(expectedTriggerCount < serverTriggerCount)
				{
					FlansMod.LOGGER.info("Client expected to trigger " + expectedTriggerCount + " repeat(s), but server triggered " + serverTriggerCount + " many repeat(s)");
				}
				actionGroup.UpdateInputHeld(actionContext, false);

				actionGroup.OnFinishClient(actionContext);
			}

			for(var reload : msg.Data.GetReloads())
			{
				actionContext.ActionStack().AddReload(actionContext, new ReloadProgress(
					actionContext.Gun.GunDef().GetReload(actionContext.InputType),
					actionContext.InputType
				));
			}
		}
	}

	// If you fired some shots and told the server where they went, you will receive a verification message back
	// When you do, any rejected shots might cause your client to rollback some visuals, sounds or other actions
	@OnlyIn(Dist.CLIENT)
	private void OnClientVerifyShots()
	{

	}

	public void ClientTick(TickEvent.ClientTickEvent tickEvent)
	{
		if(tickEvent.phase == TickEvent.Phase.START)
		{
			for(var kvp : ActionStacks.entrySet())
			{
				GunContext gunContext = kvp.getKey();
				ActionStack stack = kvp.getValue();
				if(stack.IsValid() && gunContext.IsValid())
				{
					stack.OnTick(Minecraft.getInstance().level, gunContext);
				}
			}
		}
	}

	// ----------------------------------------------------------------------------------------------------------------
	// SERVER
	// ----------------------------------------------------------------------------------------------------------------

	public void HookServer(IEventBus modEventBus)
	{
		FlansModPacketHandler.RegisterServerHandler(
			ActionUpdateMessage.ToServer.class,
			ActionUpdateMessage.ToServer::new,
			this::OnServerReceivedActionUpdate);
		MinecraftForge.EVENT_BUS.register(this);
	}

	// When a client tells us they want to reload, we need to process their items
	private static final double RELOAD_MSG_RADIUS = 50d;
	private void OnServerReceivedActionUpdate(ActionUpdateMessage.ToServer msg, ServerPlayer from)
	{
		// Check that this is a valid context
		ActionGroupContext actionContext = msg.Data.GetActionGroupContext(false);
		if(!actionContext.IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedActionUpdate had invalid action");
			return;
		}

		// TODO: We should hash-check the action set we use, as there could be a race condition
		// between switching what actions are active for the weapon and triggering this code
		ActionGroup actionGroup = null;
		if(msg.Data.GetPressType() == EPressType.Press)
		{
			actionGroup = actionContext.CreateActionGroup();
			if(actionGroup.GetActions().size() > 0)
				actionContext.ActionStack().AddActionGroup(actionContext, actionGroup);

			ReloadProgress[] reloads = actionContext.CreateReloads();
			for(ReloadProgress reload : reloads)
				actionContext.ActionStack().AddReload(actionContext, reload);
		}
		else
		{
			actionGroup = actionContext.GetExistingActionGroup();
		}

		if(actionGroup == null)
		{
			// Most likely just out of order packets?
			//FlansMod.LOGGER.warn("Received ActionUpdateMessage with wrong press type or incorrect initial triggerIndex");

			actionGroup = actionContext.CreateActionGroup();
			actionContext.ActionStack().AddActionGroup(actionContext, actionGroup);
		}

		// Now run through all the triggers that are bundled in this message and check whether they should be run
		boolean isValid = true;
		for(var kvp : msg.Data.GetTriggers())
		{
			int triggerIndex = kvp.getKey();
			// TODO: Verify that this triggerIndex is valid. Rate limit to the gun fire rate for example
			int actionIndex = 0;
			for(Action action : actionGroup.GetActions())
			{
				if(!action.VerifyServer(actionContext, null))
					isValid = false;

				Action.NetData netData = msg.Data.GetNetData(triggerIndex, actionIndex);
				action.UpdateFromNetData(netData, triggerIndex);
				action.OnTriggerServer(actionContext, triggerIndex);
				actionIndex++;
			}

			// When we get a release message, we may need to do a bit of catchup in missed triggers
			if(msg.Data.GetPressType() == EPressType.Release)
			{
				// Check the action stack for this action/gun pairing and see if any of them are waiting for mouse release
				long numTicks = msg.Data.GetLastTriggerTick() - actionGroup.GetStartedTick();
				int expectedTriggerCount = Maths.Floor(numTicks / actionContext.RepeatDelayTicks()) + 1;
				int serverTriggerCount = actionGroup.GetTriggerCount();

				if(expectedTriggerCount > serverTriggerCount)
				{
					FlansMod.LOGGER.info("Client expected to trigger " + expectedTriggerCount + " repeat(s), but server only triggered " + serverTriggerCount + " repeat(s)");
				}
				else if(expectedTriggerCount < serverTriggerCount)
				{
					FlansMod.LOGGER.info("Client expected to trigger " + expectedTriggerCount + " repeat(s), but server triggered " + serverTriggerCount + " many repeat(s)");
				}
				actionGroup.UpdateInputHeld(actionContext, false);
			}
		}

		for(var reload : msg.Data.GetReloads())
		{
			actionContext.ActionStack().AddReload(actionContext, new ReloadProgress(
				actionContext.Gun.GunDef().GetReload(actionContext.InputType),
				 actionContext.InputType
			));
		}

		// And propogate to players for third person anims
		if(isValid)
		{
			double radius = actionGroup.GetPropogationRadius(actionContext);

			// Find out which positions we want to map around
			List<Vec3> positions = new ArrayList<>(2);
			for(var kvp : msg.Data.GetTriggers())
			{
				actionGroup.AddExtraPositionsForNetSync(actionContext, kvp.getKey(), positions);
			}
			if(actionGroup.ShouldAddPlayerPosForNetSync(actionContext))
				positions.add(actionContext.Shooter().GetShootOrigin().PositionVec3());

			// Then send them some messages about the shot
			FlansModPacketHandler.SendToAllAroundPoints(
				new ActionUpdateMessage.ToClient(msg.Data),
				from.level.dimension(),
				positions,
				radius,
				actionContext.Owner());
		}
	}

	@SubscribeEvent
	public void ServerTick(TickEvent.ServerTickEvent tickEvent)
	{
		if(tickEvent.phase == TickEvent.Phase.START)
		{
			for(var kvp : ActionStacks.entrySet())
			{
				GunContext gunContext = kvp.getKey();
				if(gunContext.IsValid())
				{
					ActionStack stack = kvp.getValue();
					stack.OnTick(gunContext.Level(), gunContext);
				}
			}
		}
	}
	// ----------------------------------------------------------------------------------------------------------------
	// COMMON
	// ----------------------------------------------------------------------------------------------------------------

}
