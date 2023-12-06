package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.ActionDefinition;

import javax.annotation.Nonnull;

public class AttachEffectAction extends ActionInstance
{
	public AttachEffectAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public boolean PropogateToServer() { return false; }
	@Override
	public void OnTriggerClient(int triggerIndex) { }
	@Override
	public void OnTriggerServer(int triggerIndex) { }

	public String EffectModelLocation() { return Group.Context.ModifyString(ModifierDefinition.KEY_MODEL_ID, "flansmod:muzzle_flash"); }
}
