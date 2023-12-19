package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunInputContext;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.guns.elements.HandlerDefinition;
import com.flansmod.common.types.guns.elements.HandlerNodeDefinition;
import com.flansmod.common.types.guns.elements.ModeDefinition;
import com.flansmod.common.types.vehicles.EPlayerInput;
import com.mojang.datafixers.util.Pair;

import javax.annotation.Nonnull;
import java.util.List;

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

				SelectMode(key, modeDef.values[newModeIndex]);
			}
			else
			{
				String selectMode = SelectValue();
				SelectMode(key, selectMode);
			}
		}
	}

	private void SelectMode(String modeKey, String modeValue)
	{
		ActionStack actionStack = Group.Context.Gun.GetActionStack();
		for(HandlerDefinition handler : Group.Context.Gun.Def.inputHandlers)
		{
			for(HandlerNodeDefinition node : handler.nodes)
			{
				// We are modifying this mode setting
				if(node.modalCheck.startsWith(modeKey))
				{
					// And we are switching away from the on-mode
					String modalCheckValue = node.modalCheck.contains(":") ? node.modalCheck.split(":")[1] : "on";
					if(!modeValue.equals(modalCheckValue))
					{
						String actionGroupPath = node.actionGroupToTrigger;
						if(node.deferToAttachment)
						{
							AttachmentDefinition attachmentDef = Group.Context.Gun.GetAttachmentDefinition(node.attachmentType, node.attachmentIndex);
							if(attachmentDef.IsValid())
							{
								HandlerDefinition attachmentInputHandler = attachmentDef.GetInputHandler(handler.inputType);
								for(HandlerNodeDefinition attachmentInputNode : attachmentInputHandler.nodes)
								{
									actionGroupPath = ActionGroupContext.CreateGroupPath(
										node.attachmentType,
										node.attachmentIndex,
										attachmentInputNode.actionGroupToTrigger);
								}
							}
						}

						ActionGroupContext actionGroupContext = Group.Context.Gun.GetActionGroupContext(actionGroupPath);
						ActionGroupInstance actionGroupInstance = actionStack.TryGetGroupInstance(actionGroupContext);
						if (actionGroupInstance != null)
						{
							actionGroupInstance.SetFinished();
						}
					}
				}
			}
		}


		Group.Context.Gun.SetModeValue(modeKey, modeValue);
	}


	public String ModeKey() { return Group.Context.ModifyString(ModifierDefinition.KEY_MODE, "mode"); }
	public String SelectValue() { return Group.Context.ModifyString(ModifierDefinition.KEY_SET_VALUE, ""); }
	public boolean CycleModes() { return SelectValue().isEmpty(); }
}
