package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ReloadDefinition;
import com.flansmod.common.types.guns.EReloadStage;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ReloadProgress
{
	public ReloadDefinition Def;
	public InteractionHand Hand;
	public EActionSet ActionSet;
	public EReloadStage CurrentStage;
	public int TicksInCurrentStage;
	public float SecondsInCurrentStage() { return TicksInCurrentStage / 20f; }
	public boolean FinishedCurrentStage() { return SecondsInCurrentStage() >= LengthOfCurrentStageSeconds(); }
	public float LengthOfCurrentStageSeconds()
	{
		switch(CurrentStage)
		{
			case Start -> { return Def.start.duration; }
			case Eject -> { return Def.eject.duration; }
			case LoadOne -> { return Def.loadOne.duration; }
			case End -> { return Def.end.duration; }
			default -> { return 0f; }
		}
	}

	public ReloadProgress(ReloadDefinition def, EActionSet actionSet, InteractionHand hand)
	{
		Def = def;
		ActionSet = actionSet;
		Hand = hand;
		CurrentStage = EReloadStage.Start;
		TicksInCurrentStage = 0;
	}
}
