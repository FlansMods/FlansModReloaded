package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.guns.elements.ModeDefinition;

import javax.annotation.Nonnull;

public class SwitchModeAction extends ActionInstance
{
	public SwitchModeAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public void OnTriggerClient(int triggerIndex)
	{
		Trigger();
	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{
		Trigger();
	}

	private void Trigger()
	{
		String key = ModeKey();
		ModeDefinition modeDef = Group.Context.Gun.GetModeDef(key);
		if(modeDef != null)
		{
			if (CycleModes())
			{
				String currentValue = Group.Context.Gun.GetModeValue(key);
				int currentIndex = -1;
				for(int i = 0; i < modeDef.values.length; i++)
					if(modeDef.values[i].equals(currentValue))
						currentIndex = i;

				int newModeIndex = currentIndex + 1;
				if(newModeIndex >= modeDef.values.length)
					newModeIndex = 0;

				Group.Context.Gun.SetModeValue(key, modeDef.values[newModeIndex]);
			}
			else
			{
				String selectMode = SelectValue();
				Group.Context.Gun.SetModeValue(key, selectMode);
			}
		}
	}


	public String ModeKey() { return Group.Context.ModifyString(ModifierDefinition.KEY_MODE, "mode"); }
	public String SelectValue() { return Group.Context.ModifyString(ModifierDefinition.KEY_SET_VALUE, ""); }
	public boolean CycleModes() { return SelectValue().isEmpty(); }
}
