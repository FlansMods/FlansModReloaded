package com.flansmod.common.actions;

import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunContext;
import net.minecraft.world.InteractionHand;

public abstract class Action
{
	public ActionDefinition actionDef;
	public float progress = 0.0f;
	public float duration = 0.0f;
	public InteractionHand hand = InteractionHand.MAIN_HAND;

	public boolean Finished() { return progress >= duration; }

	public Action(ActionDefinition def, InteractionHand hand)
	{
		this.actionDef = def;
		this.duration = def.duration * 100f;
		this.hand = hand;
	}

	public boolean CanStart(GunContext context) { return true; }

	public void OnStartServer(GunContext context) { progress = 0.0f; }
	public void OnTickServer(GunContext context) { progress++; }
	public void OnFinishServer(GunContext context) {}

	public void OnStartClient(GunContext context) { progress = 0.0f; }
	public void OnTickClient(GunContext context)	{ progress++; }
	public void OnFinishClient(GunContext context) {}

}
