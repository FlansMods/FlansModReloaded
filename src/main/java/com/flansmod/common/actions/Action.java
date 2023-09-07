package com.flansmod.common.actions;

import com.flansmod.common.gunshots.*;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class Action
{
	public static final float TICK_RATE = 1.0f / 20.0f;
	@Nonnull
	public final ActionGroup Group;
	@Nonnull
	public final ActionDefinition Def;

	public static abstract class NetData
	{
		public abstract int GetID();
		public abstract void Encode(FriendlyByteBuf buf);
		public abstract void Decode(FriendlyByteBuf buf);

		public static final int INVALID_ID = -1;
		public static final NetData Invalid = new NetData()
		{
			@Override
			public int GetID() { return INVALID_ID; }
			@Override
			public void Encode(FriendlyByteBuf buf) {}
			@Override
			public void Decode(FriendlyByteBuf buf) {}
		};
	}

	public boolean Finished() { return Group.Finished; }
	public float GetCharge() { return Group.Charge; }
	public boolean IsCharging() { return Group.IsCharging; }
	public int GetProgressTicks() { return Group.Progress; }
	public float GetProgressSeconds() { return Group.Progress * TICK_RATE; }

	public int GetDurationPerTriggerTicks() { return Group.Duration; }
	public float GetDurationPerTriggerSeconds() { return Group.Duration * TICK_RATE; }

	public int GetDurationTotalTicks() { return Group.Duration * Group.TriggerCount; }
	public float GetDurationTotalSeconds() { return Group.Duration * Group.TriggerCount * TICK_RATE; }
	public int GetTriggerCount() { return Group.TriggerCount; }

	public int GetNumBurstsRemaining() { return Group.NumBurstsRemaining; }

	public Action(@Nonnull ActionGroup group, @Nonnull ActionDefinition def)
	{
		Group = group;
		Def = def;
	}

	public boolean ShouldRender(GunContext context) { return true; }
	public boolean PropogateToServer(ActionGroupContext context) { return true; }
	public boolean ShouldFallBackToReload(ActionGroupContext context) { return false; }
	public boolean CanStart(ActionGroupContext context) { return true; }
	public boolean CanRetrigger(ActionGroupContext context) { return true; }
	public abstract void OnTriggerClient(ActionGroupContext context, int triggerIndex);
	public abstract void OnTriggerServer(ActionGroupContext context, int triggerIndex);
	public void OnStartServer(ActionGroupContext context) {}
	public void OnTickServer(ActionGroupContext context) {}
	public void OnFinishServer(ActionGroupContext context) {}
	public void OnStartClient(ActionGroupContext context) {}
	public void OnTickClient(ActionGroupContext context) {}
	public void OnFinishClient(ActionGroupContext context) {}
	public void SkipTicks(ActionGroupContext context, int ticks) {}

	// NetData and sync
	// Careful when changing this. The action group will only propogate to players within range of the furthest action in the group
	public double GetPropogationRadius(ActionGroupContext context) { return 200.0d; }
	public void AddExtraPositionsForNetSync(ActionGroupContext context, int triggerIndex, List<Vec3> positions) {}
	public boolean ShouldNetSyncAroundPlayer(ActionGroupContext context) { return true; }
	@Nonnull
	public NetData GetNetDataForTrigger(int triggerIndex)
	{
		return NetData.Invalid;
	}
	public void UpdateFromNetData(NetData netData, int triggerIndex) {}

	public boolean VerifyServer(ActionGroupContext context, GunshotCollection shots) { return true; }

	// These ones are specific to this action
	public float Duration(ActionGroupContext context) { return context.ModifyFloat(ModifierDefinition.STAT_DURATION, Def.duration); }
	public float ToolLevel(ActionGroupContext context) { return context.ModifyFloat(ModifierDefinition.STAT_TOOL_HARVEST_LEVEL, Def.toolLevel); }
	public float HarvestSpeed(ActionGroupContext context) { return context.ModifyFloat(ModifierDefinition.STAT_TOOL_HARVEST_SPEED, Def.harvestSpeed); }
	public float Reach(ActionGroupContext context) { return context.ModifyFloat(ModifierDefinition.STAT_TOOL_REACH, Def.reach); }
}
