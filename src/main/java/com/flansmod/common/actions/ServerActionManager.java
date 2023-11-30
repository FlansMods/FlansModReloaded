package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.INpcRelationshipsCapability;
import com.flansmod.common.entity.NpcRelationshipsCapability;
import com.flansmod.common.network.FlansModPacketHandler;
import com.flansmod.common.network.bidirectional.ActionUpdateMessage;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

		for(var level : MinecraftHelpers.Server_GetLoadedLevels())
		{
			for(var player : level.players())
			{
				LazyOptional<INpcRelationshipsCapability> relationshipCap = player.getCapability(NpcRelationshipsCapability.INSTANCE);
				if(relationshipCap.isPresent() && relationshipCap.resolve().isPresent())
				{
					relationshipCap.resolve().get().TickAllCooldowns(1);
				}
			}
		}
	}
}
