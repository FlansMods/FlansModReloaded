package com.flansmod.common.actions;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.EPressType;
import com.flansmod.common.gunshots.ShooterContext;
import com.flansmod.common.network.FlansModPacketHandler;
import com.flansmod.common.network.bidirectional.ActionUpdateMessage;
import com.flansmod.common.types.vehicles.EPlayerInput;
import com.flansmod.util.Maths;
import com.mojang.datafixers.util.Pair;
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
import java.util.function.Consumer;
import java.util.function.Function;

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

	private void IteratePossibleActionGroups(GunInputContext inputContext, Function<ActionGroupContext, Boolean> func)
	{
		ActionStack actionStack = inputContext.Gun.GetActionStack();
		List<Pair<ActionGroupContext, Boolean>> actionGroups = inputContext.Gun.EvaluateInputHandler(inputContext);
		for (var kvp : actionGroups)
		{
			ActionGroupInstance groupInstance = actionStack.GetOrCreateGroupInstance(kvp.getFirst());
			boolean funcSuccess = func.apply(kvp.getFirst());
			if(funcSuccess)
			{
				if(!kvp.getSecond()) // andContinueEvaluating
					break;
			}
			else if(!groupInstance.HasStarted())
			{
				actionStack.CancelGroupInstance(kvp.getFirst());
			}
		}
	}

	private void IterateActiveActionGroups(GunInputContext inputContext, Function<ActionGroupContext, Boolean> func)
	{
		ActionStack actionStack = inputContext.Gun.GetActionStack();
		List<Pair<ActionGroupContext, Boolean>> actionGroups = inputContext.Gun.EvaluateInputHandler(inputContext);
		for (var kvp : actionGroups)
		{
			ActionGroupInstance groupInstance = actionStack.TryGetGroupInstance(kvp.getFirst());
			if(groupInstance != null)
				func.apply(kvp.getFirst());
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyPressed(Player player, EPlayerInput inputType)
	{
		// See if pressing this button should trigger any actions
		// First on our main hand, then on our off hand
		ShooterContext shooter = ShooterContext.GetOrCreate(player);
		if (!shooter.IsValid())
			return;

		// Ask the ShooterContext which Guns to use
		for (GunContext gunContext : shooter.GetAllActiveGunContexts())
		{
			if (!gunContext.IsValid())
				continue;

			ActionStack actionStack = gunContext.GetActionStack();
			GunInputContext inputContext = GunInputContext.CreateFrom(gunContext, inputType);
			IteratePossibleActionGroups(inputContext, actionStack::Client_TryStartGroupInstance);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyHeld(Player player, EPlayerInput inputType)
	{
		// See if any of the in-progress actions on this gun should stop on release
		ShooterContext shooter = ShooterContext.GetOrCreate(player);
		if (!shooter.IsValid())
			return;

		// Ask the ShooterContext which Guns to use
		for (GunContext gunContext : shooter.GetAllActiveGunContexts())
		{
			if (!gunContext.IsValid())
				continue;

			ActionStack actionStack = gunContext.GetActionStack();
			GunInputContext inputContext = GunInputContext.CreateFrom(gunContext, inputType);
			IterateActiveActionGroups(inputContext, actionStack::Client_TryUpdateGroupInstanceHeld);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyReleased(Player player, EPlayerInput inputType, int ticksSinceHeld)
	{
		// See if any of the in-progress actions on this gun should stop on release
		ShooterContext shooter = ShooterContext.GetOrCreate(player);
		if (!shooter.IsValid())
			return;

		// Ask the ShooterContext which Guns to use
		for (GunContext gunContext : shooter.GetAllActiveGunContexts())
		{
			if (!gunContext.IsValid())
				continue;

			ActionStack actionStack = gunContext.GetActionStack();
			GunInputContext inputContext = GunInputContext.CreateFrom(gunContext, inputType);
			IterateActiveActionGroups(inputContext, actionStack::Client_TryUpdateGroupInstanceNotHeld);
		}
	}

	// This will only be sent to you when someone else fires a shot. This is where you play various actions in response
	@OnlyIn(Dist.CLIENT)
	private void OnClientReceivedActionUpdate(ActionUpdateMessage.ToClient msg)
	{
		ActionGroupContext groupContext = msg.Data.GetActionGroupContext(true);
		if(groupContext.IsValid())
		{
			if(groupContext.Gun.GetShooter().Entity() == Minecraft.getInstance().player)
			{
				FlansMod.LOGGER.warn("OnClientReceivedActionUpdate received with data for myself");
				return;
			}
			ActionStack actionStack = groupContext.Gun.GetActionStack();

			// See if we are updating an existing action, or if we need to start fresh
			ActionGroupInstance groupInstance = actionStack.TryGetGroupInstance(groupContext);
			if(groupInstance == null)
			{
				if(msg.Data.GetPressType() != EPressType.Press)
					FlansMod.LOGGER.warn("Received ActionUpdateMessage with wrong press type for action that was not already running");
				groupInstance = actionStack.GetOrCreateGroupInstance(groupContext);
			}

			if(msg.Data.GetPressType() == EPressType.Press)
				groupInstance.OnStartClientFromNetwork(msg.Data.GetStartTick());

			// Now run through all the triggers that are bundled in this message and run any client side effects
			for(var kvp : msg.Data.GetTriggers())
			{
				int triggerIndex = kvp.getKey();
				int actionIndex = 0;
				for(ActionInstance action : groupInstance.GetActions())
				{
					ActionInstance.NetData netData = msg.Data.GetNetData(triggerIndex, actionIndex);
					action.UpdateFromNetData(netData, triggerIndex);
					action.OnTriggerClient(triggerIndex);
					actionIndex++;
				}
			}

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

				groupInstance.OnFinishClient();
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
		ActionGroupContext groupContext = msg.Data.GetActionGroupContext(false);
		if(!groupContext.IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedActionUpdate had invalid action");
			return;
		}

		ActionStack actionStack = groupContext.Gun.GetActionStack();
		actionStack.Server_TryHandleMessage(msg, from);
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
