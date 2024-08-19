package com.flansmod.client;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.gunshots.EPressType;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.network.bidirectional.ActionUpdateMessage;
import com.flansmod.common.types.elements.EPlayerInput;
import com.flansmod.util.Maths;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientActionManager extends ActionManager
{
	public ClientActionManager()
	{
		super(true);
	}

	// ----------------------------------------------------------------------------------------------------------------
	// CLIENT
	// ----------------------------------------------------------------------------------------------------------------

	public void HookClient(IEventBus modEventBus)
	{
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyPressed(Player player, EPlayerInput inputType)
	{
		// See if pressing this button should trigger any actions
		// First on our main hand, then on our off hand
		ShooterContext shooter = ShooterContext.of(player);
		if (!shooter.IsValid())
			return;

		// Ask the ShooterContext which Guns to use
		for (GunContext gunContext : shooter.GetAllGunContexts(true))
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
		ShooterContext shooter = ShooterContext.of(player);
		if (!shooter.IsValid())
			return;

		// Ask the ShooterContext which Guns to use
		for (GunContext gunContext : shooter.GetAllGunContexts(true))
		{
			if (!gunContext.IsValid())
				continue;

			ActionStack actionStack = gunContext.GetActionStack();
			GunInputContext inputContext = GunInputContext.CreateFrom(gunContext, inputType);
			IterateActionGroupsThatRespondToHeld(inputContext, actionStack::Client_TryUpdateGroupInstanceHeld);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void ClientKeyReleased(Player player, EPlayerInput inputType, int ticksSinceHeld)
	{
		// See if any of the in-progress actions on this gun should stop on release
		ShooterContext shooter = ShooterContext.of(player);
		if (!shooter.IsValid())
			return;

		// Ask the ShooterContext which Guns to use
		for (GunContext gunContext : shooter.GetAllGunContexts(true))
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
	public void OnClientReceivedActionUpdate(ActionUpdateMessage.ToClient msg)
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
			EPressType pressTypeToProcess = msg.Data.GetPressType();
			if(groupInstance == null)
			{
				if(pressTypeToProcess != EPressType.Press)
				{
					FlansMod.LOGGER.warn("Received ActionUpdateMessage with wrong press type for action that was not already running");
					if(pressTypeToProcess == EPressType.Hold)
						pressTypeToProcess = EPressType.Press;
				}
				groupInstance = actionStack.GetOrCreateGroupInstance(groupContext);
				groupInstance.OnStartClientFromNetwork(msg.Data.GetStartTick());
			}

			// Now run through all the triggers that are bundled in this message and run any client side effects
			switch(pressTypeToProcess)
			{
				case Press ->
				{
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
				}
				case Release ->
				{
					// Check the action stack for this action/gun pairing and see if any of them are waiting for mouse release
					switch(groupContext.RepeatMode())
					{
						case FullAuto, Minigun -> {
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
						}
					}

					groupInstance.UpdateInputHeld(false);
					groupInstance.OnFinishClient();
				}
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
			List<UUID> invalidIDs = new ArrayList<>();
			for(var kvp : ActionStacks.entrySet())
			{
				UUID gunID = kvp.getKey();
				ActionStack actionStack = kvp.getValue();
				GunContext gunContext = GunContext.of(gunID);
				if(gunContext.IsValid())
				{
					if (actionStack.IsValid())
						actionStack.OnTick(Minecraft.getInstance().level, gunContext);
				}
				else
				{
					actionStack.Clear(gunContext);
					invalidIDs.add(gunID);
				}
			}

			for(UUID invalidID : invalidIDs)
				ActionStacks.remove(invalidID);

			Player player = Minecraft.getInstance().player;
			if(player != null)
			{
				ShooterContext shooterContext = ShooterContext.of(player);
				for (int i = 0; i < player.getInventory().getContainerSize(); i++)
				{
					if (player.getInventory().getItem(i).getItem() instanceof GunItem)
					{
						UUID gunID = FlanItem.GetGunID(player.getInventory().getItem(i));
						if(gunID != FlanItem.InvalidGunUUID)
						{
							GunContext gunContext = GunContext.of(shooterContext, gunID);
							ActionStack actionStack = GetActionStack(gunID);
							boolean equipped = (i == player.getInventory().selected)
								|| (i == Inventory.SLOT_OFFHAND);
							actionStack.UpdateEquipped(gunContext, equipped);
						}
					}
				}
			}
		}
	}
}
