package com.flansmod.common.network.bidirectional;

import com.flansmod.common.actions.*;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.ContextCache;
import com.flansmod.common.gunshots.EPressType;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.network.FlansModMessage;
import com.flansmod.common.network.elements.ActionGroupNetID;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.*;

public class ActionUpdateMessage extends FlansModMessage
{
	public static class ToServer extends FlansModMessage
	{
		public ActionUpdateMessage Data;
		public ToServer() { Data = new ActionUpdateMessage(); }
		public ToServer(ActionUpdateMessage data) { Data = data; }
		@Override
		public void Encode(FriendlyByteBuf buf) { Data.Encode(buf); }
		@Override
		public void Decode(FriendlyByteBuf buf) { Data.Decode(buf); }
	}

	public static class ToClient extends FlansModMessage
	{
		public ActionUpdateMessage Data;
		public ToClient() { Data = new ActionUpdateMessage(); }
		public ToClient(ActionUpdateMessage data) { Data = data; }
		@Override
		public void Encode(FriendlyByteBuf buf) { Data.Encode(buf); }
		@Override
		public void Decode(FriendlyByteBuf buf) { Data.Decode(buf); }
	}

	public static class ActionTriggerInfo
	{
		public int DeltaTick;
		public List<ActionInstance.NetData> NetData = new ArrayList<>();
	}

	private long StartTick;
	private final ActionGroupNetID Context;
	private EPressType PressType;
	private final HashMap<Integer, ActionTriggerInfo> Triggers = new HashMap<>();

	public EPressType GetPressType() { return PressType; }
	public Set<Map.Entry<Integer, ActionTriggerInfo>> GetTriggers() { return Triggers.entrySet(); }
	public long GetStartTick() { return StartTick; }
	public int GetFirstTriggerIndex() {
		int firstIndex = Integer.MAX_VALUE;
		for(int triggerIndex : Triggers.keySet())
			if(triggerIndex < firstIndex)
				firstIndex = triggerIndex;
		return firstIndex == Integer.MAX_VALUE ? -1 : firstIndex;
	}
	public int GetLastTriggerIndex() {
		int lastIndex = -1;
		for(int triggerIndex : Triggers.keySet())
			if(triggerIndex > lastIndex)
				lastIndex = triggerIndex;
		return lastIndex;
	}
	public long GetFirstTriggerTick() {
		long firstTick = Long.MAX_VALUE;
		for(ActionTriggerInfo trigger : Triggers.values())
			if((trigger.DeltaTick + StartTick) < firstTick)
				firstTick = (trigger.DeltaTick + StartTick);
		return firstTick == Long.MAX_VALUE ? -1 : firstTick;
	}
	public long GetLastTriggerTick() {
		long lastTick = -1;
		for(ActionTriggerInfo trigger : Triggers.values())
			if((trigger.DeltaTick + StartTick) > lastTick)
				lastTick = (trigger.DeltaTick + StartTick);
		return lastTick;
	}

	@Nonnull
	public ActionGroupContext GetActionGroupContext(boolean client)
	{
		// Construct the shooter context first
		ShooterContext shooterContext = ShooterContext.of(Context.EntityUUID, Context.EntityUUID, client);
		if(!shooterContext.IsValid())
			return ActionGroupContext.INVALID;

		// Specifically construct the GunContext using the hash, so we can look up historical data
		GunContext gunContext = GunContext.of(shooterContext, Context.GunID);
		if(!gunContext.IsValid())
			return ActionGroupContext.INVALID;

		return gunContext.GetActionGroupContextByHash(Context.GroupPathHash);
	}

	public ActionUpdateMessage()
	{
		Context = new ActionGroupNetID();
	}

	public ActionUpdateMessage(ActionGroupContext actionGroupContext, EPressType pressType, long startTick)
	{
		StartTick = startTick;
		Context = new ActionGroupNetID(
			actionGroupContext.Gun.GetShooter().EntityUUID(),
			actionGroupContext.GroupPath.hashCode(),
			actionGroupContext.Gun.GetInventorySlotIndex(),
			actionGroupContext.Gun.GetUUID()
		);
		PressType = pressType;
	}

	public ActionInstance.NetData GetNetData(int triggerIndex, int actionIndex)
	{
		ActionTriggerInfo triggerInfo = Triggers.get(triggerIndex);
		if(triggerInfo != null && actionIndex < triggerInfo.NetData.size())
		{
			return triggerInfo.NetData.get(actionIndex);
		}
		return ActionInstance.NetData.Invalid;
	}

	public void AddTriggers(ActionGroupInstance group, int triggerIndexMin, int triggerIndexMax)
	{
		for(int i = triggerIndexMin; i <= triggerIndexMax; i++)
		{
			ActionTriggerInfo accumulatedNetData = new ActionTriggerInfo();
			accumulatedNetData.DeltaTick = (int)(MinecraftHelpers.GetTick() - group.GetStartedTick());
			for(ActionInstance action : group.GetActions())
			{
				accumulatedNetData.NetData.add(action.GetNetDataForTrigger(i));
			}
			Triggers.put(i, accumulatedNetData);
		}
	}

	@Override
	public void Encode(FriendlyByteBuf buf)
	{
		buf.writeByte(PressType.ordinal());
		buf.writeLong(StartTick);

		Context.Encode(buf);

		buf.writeInt(Triggers.size());

		for(var kvp : Triggers.entrySet())
		{
			buf.writeInt(kvp.getKey());
			buf.writeInt(kvp.getValue().DeltaTick);
			buf.writeInt(kvp.getValue().NetData.size());

			for(ActionInstance.NetData netData : kvp.getValue().NetData)
			{
				buf.writeInt(netData.GetID());
				netData.Encode(buf);
			}
		}
	}

	@Override
	public void Decode(FriendlyByteBuf buf)
	{
		PressType = EPressType.values()[buf.readByte()];
		StartTick = buf.readLong();

		Context.Decode(buf);

		// So now we know which action group on which gun we are triggering, so let's process it
		int numTriggers = buf.readInt();
		for (int i = 0; i < numTriggers; i++)
		{
			int triggerIndex = buf.readInt();
			int deltaTick = buf.readInt();
			int count = buf.readInt();

			ActionTriggerInfo triggerInfo = new ActionTriggerInfo();
			triggerInfo.DeltaTick = deltaTick;
			for (int j = 0; j < count; j++)
			{
				int netDataType = buf.readInt();
				ActionInstance.NetData netData = Actions.CreateEmptyNetData(netDataType);
				netData.Decode(buf);
				triggerInfo.NetData.add(netData);
			}

			Triggers.put(triggerIndex, triggerInfo);
		}
	}

	@Override
	public String toString()
	{
		return "ActionUpdate (" +
			PressType +
			") @" +
			StartTick +
			"L - GunID:'" +
			Context.GunID +
			"', Slot: " +
			Context.InventorySlotIndex +
			", #Triggers=" +
			Triggers.size();
	}
}
