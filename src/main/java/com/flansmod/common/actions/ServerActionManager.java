package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.ContextCache;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.entity.INpcRelationshipsCapability;
import com.flansmod.common.entity.NpcRelationshipsCapability;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.network.bidirectional.ActionUpdateMessage;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerActionManager extends ActionManager
{
	public ServerActionManager()
	{
		super(false);
	}

	// ----------------------------------------------------------------------------------------------------------------
	// SERVER
	// ----------------------------------------------------------------------------------------------------------------

	public void HookServer(IEventBus modEventBus)
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	// When a client tells us they want to reload, we need to process their items
	private static final double RELOAD_MSG_RADIUS = 50d;
	public void OnServerReceivedActionUpdate(ActionUpdateMessage.ToServer msg, ServerPlayer from)
	{
		// Check that this is a valid context
		ActionGroupContext groupContext = msg.Data.GetActionGroupContext(false);
		if(!groupContext.IsValid() || !groupContext.Gun.GetShooter().IsValid())
		{
			FlansMod.LOGGER.warn("OnServerReceivedActionUpdate had invalid action");
			return;
		}

		FlansMod.LOGGER.info("Received server: "+msg.Data.GetPressType()+msg.Data.GetActionGroupContext(false));

		ActionStack actionStack = groupContext.Gun.GetActionStack();
		actionStack.Server_TryHandleMessage(msg, from);
	}

	@SubscribeEvent
	public void ServerTick(TickEvent.ServerTickEvent tickEvent)
	{
		if(tickEvent.phase == TickEvent.Phase.START)
		{
			List<UUID> invalidIDs = new ArrayList<>();
			for (var kvp : ActionStacks.entrySet())
			{
				UUID gunID = kvp.getKey();
				ActionStack actionStack = kvp.getValue();
				GunContext gunContext = GunContext.of(gunID);
				if (gunContext.IsValid())
				{
					if (actionStack.IsValid())
						actionStack.OnTick(gunContext.GetLevel(), gunContext);
				} else
				{
					actionStack.Clear(gunContext);
					invalidIDs.add(gunID);
				}
			}

			for (UUID invalidID : invalidIDs)
			{
				ActionStacks.remove(invalidID);
			}

			for (var level : MinecraftHelpers.Server_GetLoadedLevels())
			{
				for (var player : level.players())
				{
					ShooterContext shooterContext = ShooterContext.of(player);
					LazyOptional<INpcRelationshipsCapability> relationshipCap = player.getCapability(NpcRelationshipsCapability.INSTANCE);
					if (relationshipCap.isPresent() && relationshipCap.resolve().isPresent())
					{
						relationshipCap.resolve().get().TickAllCooldowns(1);
					}

					for (int i = 0; i < player.getInventory().getContainerSize(); i++)
					{
						if (player.getInventory().getItem(i).getItem() instanceof GunItem)
						{
							UUID gunID = FlanItem.Server_GetOrSetNewGunID(player.getInventory().getItem(i));
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
