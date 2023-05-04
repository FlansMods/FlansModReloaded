package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunContext;
public abstract class Action
{
	public ActionDefinition actionDef;
	public float progress = 0.0f;
	public float duration = 0.0f;

	public boolean Finished() { return progress >= duration; }

	public Action(ActionDefinition def)
	{
		actionDef = def;
	}

	public boolean CanStart(GunContext context) { return true; }

	protected void OnStartServer(GunContext context) { progress = 0.0f; }
	protected void OnTickServer(GunContext context) { progress++; }
	protected void OnFinishServer(GunContext context) {}

	protected void OnStartClient(GunContext context) { progress = 0.0f; }
	protected void OnTickClient(GunContext context)	{ progress++; }
	protected void OnFinishClient(GunContext context) {}

}
