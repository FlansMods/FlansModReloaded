package com.flansmod.common.gunshots;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.network.FlansModPacketHandler;
import com.flansmod.common.network.toclient.ShotFiredMessage;
import com.flansmod.common.network.toserver.SimpleActionMessage;
import com.flansmod.common.network.toserver.ShotRequestMessage;
import com.flansmod.common.types.guns.ERepeatMode;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionManager
{
	private final boolean IsClient;
	private HashMap<GunContext, ActionStack> ActionStacks = new HashMap<GunContext, ActionStack>();

	@Nullable
	public ActionStack GetActionStack(GunContext context)
	{
		if(!context.IsValid())
		{
			FlansMod.LOGGER.warn("Tried to get action stack for invalid context");
			return null;
		}
		if(!context.GetShooter().IsValid())
		{
			FlansMod.LOGGER.warn("Tried to get action stack for a valid context, but without an entity. This may be supported later");
			return null;
		}

		ActionStack entitysActionStack = ActionStacks.get(context);
		if(entitysActionStack == null)
		{
			entitysActionStack = new ActionStack(IsClient);
			ActionStacks.put(context, entitysActionStack);
		}
		return entitysActionStack;
	}

	@Nullable
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
			return null;
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
		FlansModPacketHandler.RegisterClientHandler(ShotFiredMessage.class, ShotFiredMessage::new, this::OnClientShotsFired);
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);

	}

	// You fired a shot, tell the server where you shot
	@OnlyIn(Dist.CLIENT)
	private void ClientSendToServerAction(InteractionHand hand, EActionInput inputType)
	{
		FlansModPacketHandler.SendToServer(new SimpleActionMessage(hand, inputType));
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyPressed(Player player, EActionInput inputType)
	{
		// See if pressing this button should trigger any actions
		// First on our main hand, then on our off hand

		ShooterContext shooter = ShooterContext.CreateFrom(player);
		if(!shooter.IsValid())
			return;

		// Ask the ShooterContext which actions on which guns we should perform
		ActionContext[] actionContexts = shooter.GetPrioritisedActions(inputType);
		// Then, in order, perform those actions (with actions able to block subsequent execution)
		for (ActionContext actionContext : actionContexts)
		{
			// First, accumulate our action set and check if they all allow us to start the requested action
			Action[] actions = actionContext.CreateActions();
			ReloadProgress[] reloads = actionContext.CreateReloads();
			boolean needsNetMsg = false;
			boolean canStart = true;
			boolean shouldFallbackToReload = false;
			for (Action action : actions)
			{
				if (!action.CanStart(actionContext))
				{
					canStart = false;
					if(action.ShouldFallBackToReload(actionContext))
					{
						shouldFallbackToReload = true;
					}
				}
				if (action.PropogateToServer(actionContext))
					needsNetMsg = true;
			}

			// Now we know that they can start, let's trigger them
			if (canStart)
			{
				GunshotCollection shots = null;
				for (Action action : actions)
				{
					actionContext.ActionStack().AddAction(actionContext, action);
					if(action instanceof ShootAction shootAction)
					{
						shots = shootAction.GetResults();
					}
				}
				for (ReloadProgress reload : reloads)
				{
					actionContext.ActionStack().AddReload(actionContext, reload);
				}

				// Send a message to the server about these actions if required
				if (needsNetMsg)
				{
					if(shots != null)
						ClientSendToServer(shots);
					else
						ClientSendToServerAction(actionContext.Gun().GetHand(), inputType);
				}
			}
			else if(shouldFallbackToReload && inputType != EActionInput.RELOAD && !actionContext.ActionStack().IsReloading())
			{
				ClientKeyPressed(player, EActionInput.RELOAD);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyHeld(Player player, EActionInput inputType)
	{
		// See if any of the in-progress actions on this gun should stop on release
		ShooterContext shooter = ShooterContext.CreateFrom(player);
		if(!shooter.IsValid())
			return;

		// Ask the ShooterContext which actions on which guns we should perform
		ActionContext[] actionContexts = shooter.GetPrioritisedActions(inputType);
		for(ActionContext actionContext : actionContexts)
		{
			// Check the action stack for this action/gun pairing and see if any of them are waiting for mouse release
			for(Action action : actionContext.ActionStack().GetActions())
			{
				action.UpdateInputHeld(actionContext, true);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyReleased(Player player, EActionInput inputType)
	{
		// See if any of the in-progress actions on this gun should stop on release
		ShooterContext shooter = ShooterContext.CreateFrom(player);
		if(!shooter.IsValid())
			return;

		// Ask the ShooterContext which actions on which guns we should perform
		ActionContext[] actionContexts = shooter.GetPrioritisedActions(inputType);
		for(ActionContext actionContext : actionContexts)
		{
			// Check the action stack for this action/gun pairing and see if any of them are waiting for mouse release
			for(Action action : actionContext.ActionStack().GetActions())
			{
				action.UpdateInputHeld(actionContext, false);
			}
		}
	}

	// You fired a shot, tell the server where you shot
	@OnlyIn(Dist.CLIENT)
	private void ClientSendToServer(GunshotCollection shots)
	{
		FlansModPacketHandler.SendToServer(new ShotRequestMessage(shots));
	}

	// This will only be sent to you when someone else fires a shot. This is where you play various actions in response
	@OnlyIn(Dist.CLIENT)
	private void OnClientShotsFired(ShotFiredMessage msg)
	{
		// Reconstruct the shot details and context
		GunshotCollection shotCollection = msg.Get();
		InteractionHand hand = shotCollection.seatID == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

		ShooterContext shooterContext = ShooterContext.CreateFrom(shotCollection.Shooter());
		if(!shooterContext.IsValid())
		{
			FlansMod.LOGGER.warn("OnClientShotsFired received with invalid shooter");
			return;
		}

		GunContext gunContext = GunContext.CreateFrom(shooterContext, hand);
		if(!gunContext.IsValid())
		{
			FlansMod.LOGGER.warn("OnClientShotsFired received with invalid gun");
			return;
		}

		ActionContext actionContext = ActionContext.CreateFrom(gunContext, shotCollection.actionUsed);
		Action[] actions = actionContext.CreateActions();
		for(Action action : actions)
		{
			if(action instanceof ShootAction shootAction)
			{
				shootAction.SetResults(shotCollection);
			}
			actionContext.ActionStack().AddAction(actionContext, action);
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
		if(tickEvent.phase == TickEvent.Phase.END)
		{
			for(var kvp : ActionStacks.entrySet())
			{
				GunContext gunContext = kvp.getKey();
				ActionStack stack = kvp.getValue();
				stack.OnTick(Minecraft.getInstance().level, gunContext);
			}
		}
	}

	// ----------------------------------------------------------------------------------------------------------------
	// SERVER
	// ----------------------------------------------------------------------------------------------------------------

	public void HookServer(IEventBus modEventBus)
	{
		FlansModPacketHandler.RegisterServerHandler(ShotRequestMessage.class, ShotRequestMessage::new, this::OnServerReceivedShotData);
		FlansModPacketHandler.RegisterServerHandler(SimpleActionMessage.class, SimpleActionMessage::new, this::OnServerReceivedSimpleAction);
		MinecraftForge.EVENT_BUS.addListener(this::ServerTick);
	}

	// When a client tells us they want to reload, we need to process their items
	private static final double RELOAD_MSG_RADIUS = 50d;
	private void OnServerReceivedSimpleAction(SimpleActionMessage msg, ServerPlayer from)
	{
		ShooterContext shooter = ShooterContext.CreateFrom(from);
		if(!shooter.IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedSimpleAction had invalid shooter");
			return;
		}

		GunContext gunContext = GunContext.CreateFrom(shooter, msg.hand);
		if(!gunContext.IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedSimpleAction had invalid gun");
			return;
		}

		ActionContext actionContext = ActionContext.CreateFrom(gunContext, msg.inputType);
		if(!actionContext.IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedSimpleAction had invalid action");
			return;
		}

		boolean isValid = true;
		switch(msg.inputType)
		{
			case RELOAD -> { actionContext.ActionStack().AddReload(actionContext, new ReloadProgress(gunContext.GunDef().reload)); }
			case LOOK_AT, PRIMARY, SECONDARY -> {
				Action[] actions = actionContext.CreateActions();
				for(Action action : actions)
				{
					if(action instanceof ShootAction)
					{
						FlansMod.LOGGER.warn("OnServerReceivedSimpleAction was sent a shoot action. These should use the specific shoot messages");
						isValid = false;
						break;
					}
					if(!action.VerifyServer(actionContext, null))
					{
						isValid = false;
						break;
					}
				}

				if(isValid)
				{
					for(Action action : actions)
					{
						actionContext.ActionStack().AddAction(actionContext, action);
					}
				}
			}
		}

		// And propogate to players for third person anims
		if(isValid)
		{
			FlansModPacketHandler.SendToAllAroundPoint(
				new SimpleActionMessage(msg.hand, msg.inputType),
				from.level.dimension(),
				from.position(),
				RELOAD_MSG_RADIUS,
				actionContext.Owner());
		}
	}

	// When a client tells us what they shot, we need to verify it
	private void OnServerReceivedShotData(ShotRequestMessage msg, ServerPlayer from)
	{
		// Reconstruct the shot details and context
		GunshotCollection shotCollection = msg.Get();
		InteractionHand hand = shotCollection.seatID == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

		ShooterContext shooterContext = ShooterContext.CreateFrom(from);
		if(!shooterContext.IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedShotData recieved message with invalid shooter from " + from.getDisplayName().getString());
			return;
		}

		GunContext gunContext = GunContext.CreateFrom(shooterContext, hand);
		if(!gunContext.IsValid()) // TODO: Check gun hash
		{
			FlansMod.LOGGER.warn("OnServerReceivedShotData recieved message with invalid gun from " + from.getDisplayName().getString());
			return;
		}

		// TODO: We should hash-check the action set we use, as there could be a race condition
		// between switching what actions are active for the weapon and triggering this code

		ActionContext actionContext = ActionContext.CreateFrom(gunContext, shotCollection.actionUsed);
		Action[] actions = actionContext.CreateActions();
		boolean verified = true;
		for(Action action : actions)
		{
			if(!action.CanStart(actionContext))
				verified = false;
			if(!action.VerifyServer(actionContext, shotCollection))
				verified = false;
		}

		if(!verified)
		{
			FlansMod.LOGGER.warn("Failed verification of ShotFiredMessage from " + from.getDisplayName().getString());
			return;
		}

		// Verify that this shot makes sense by itself
		// TODO: Check if we can shoot based on our local data about our
		// a) Inventory, ammo levels
		// b) Shoot cooldown
		// c) Handedness


		// TODO: Random spot check later - run a little statistical analysis on this player's shots over some time period

		// If we are happy, run all the server actions
		for(Action action : actions)
		{
			actionContext.ActionStack().AddAction(actionContext, action);
		}

		// Then propogate the shot to all other interested parties
		ServerPropogateShot(actionContext, shotCollection);

		// TODO: And let the shooter know whether we verified their shots or not
	}

	//
	private void ServerPropogateShot(ActionContext actionContext, GunshotCollection shotCollection)
	{
		float noiseLevel = 100.0f; // gunContext.GetNoiseLevel();

		// Calculate everyone near the origin or near an endpoint
		List<Vec3> positions = new ArrayList<>(shotCollection.Count() * 2);
		for(int i = 0; i < shotCollection.Count(); i++)
		{
			positions.add(shotCollection.Get(i).origin);
			positions.add(shotCollection.Get(i).Endpoint());
		}

		// Then send them some messages about the shot
		FlansModPacketHandler.SendToAllAroundPoints(
			new ShotFiredMessage(shotCollection),
			shotCollection.dimension,
			positions,
			noiseLevel,
			actionContext.Owner());
	}

	public void ServerTick(TickEvent.ServerTickEvent tickEvent)
	{
		if(tickEvent.phase == TickEvent.Phase.END)
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
