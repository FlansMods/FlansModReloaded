package com.flansmod.common.types.elements;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.JsonField;

public class ActionGroupOverrideDefinition
{
	@JsonField
	public ActionGroupDefinition actionGroup = new ActionGroupDefinition();
	@JsonField(Docs = "If true, adding this attachment will swap the actions array. Otherwise, it will be additive")
	public boolean override = false;
	@JsonField
	public EActionInput inputType = EActionInput.PRIMARY;
}
