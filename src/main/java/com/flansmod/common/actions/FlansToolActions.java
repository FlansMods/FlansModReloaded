package com.flansmod.common.actions;

import com.google.common.collect.Sets;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlansToolActions
{
	public static final ToolAction SHOOT_GUN = ToolAction.get("shoot_gun");

	public static final Set<ToolAction> DEFAULT_GUN_ACTIONS = of(SHOOT_GUN);

	private static Set<ToolAction> of(ToolAction... actions)
	{
		return Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
	}
}
