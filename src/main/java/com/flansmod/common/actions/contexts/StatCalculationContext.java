package com.flansmod.common.actions.contexts;

import com.flansmod.common.abilities.AbilityStack;
import com.flansmod.common.actions.Actions;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StatCalculationContext
{
	public final int Level;
	public final int StackCount;
	// Lazy these, because they do a lot of lookup
	public final Lazy<Integer> NumAttachments;
	public final Lazy<Float> MagFullnessRatio;

	private StatCalculationContext(int level, int stackCount, @Nonnull Lazy<Integer> numAttachments, @Nonnull Lazy<Float> magFullnessRatio)
	{
		Level = level;
		StackCount = stackCount;
		NumAttachments = numAttachments;
		MagFullnessRatio = magFullnessRatio;
	}

	public static StatCalculationContext of(@Nonnull GunContext gun)
	{
		return of(0, 0, gun);
	}
	public static StatCalculationContext of(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		if(stacks != null)
			return of(stacks.Level, stacks.GetStackCount(), gun);
		else return of(0, 0, gun);
	}
	public static StatCalculationContext of(int level, int stackCount, @Nonnull GunContext gun)
	{
		return new StatCalculationContext(
			level,
			stackCount,
			Lazy.of(gun::GetNumAttachments),
			Lazy.of(() -> {
				ActionGroupContext groupContext = gun.GetActionGroupContext(Actions.DefaultPrimaryActionKey);
				if(groupContext.IsValid())
				{
					return groupContext.GetMagFullnessRatio(0);
				}
				return 0.0f;
			}));
	}
}