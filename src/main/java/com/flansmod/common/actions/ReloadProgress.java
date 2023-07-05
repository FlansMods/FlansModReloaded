package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.elements.ReloadDefinition;
import com.flansmod.common.types.guns.EReloadStage;
import net.minecraft.world.InteractionHand;

public class ReloadProgress
{
	public final ReloadDefinition Def;
	public final EActionInput ReloadType;
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

	public ReloadProgress(ReloadDefinition def, EActionInput inputType)
	{
		Def = def;
		CurrentStage = EReloadStage.Start;
		TicksInCurrentStage = 0;
		switch(inputType)
		{
			case PRIMARY, RELOAD_PRIMARY -> { ReloadType = EActionInput.RELOAD_PRIMARY; }
			case SECONDARY, RELOAD_SECONDARY -> { ReloadType = EActionInput.RELOAD_SECONDARY; }
			default -> {
				FlansMod.LOGGER.warn("Invalid reload type for reload progress " + inputType);
				ReloadType = EActionInput.RELOAD_PRIMARY;
			}
		}
	}
}
