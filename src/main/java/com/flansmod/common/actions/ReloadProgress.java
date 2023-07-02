package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ReloadDefinition;
import com.flansmod.common.types.guns.EReloadStage;
import net.minecraft.world.InteractionHand;

public class ReloadProgress
{
	public ReloadDefinition Def;
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

	public ReloadProgress(ReloadDefinition def)
	{
		Def = def;
		CurrentStage = EReloadStage.Start;
		TicksInCurrentStage = 0;
	}
}
