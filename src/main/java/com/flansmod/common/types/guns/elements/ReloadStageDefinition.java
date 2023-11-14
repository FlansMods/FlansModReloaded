package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonField;

public class ReloadStageDefinition
{
	@JsonField(Docs = "The full duration of this reload stage, in seconds")
	public float duration = 1.0f;
	@JsonField(Docs = "All actions to run when entering this reload stage")
	public ActionGroupDefinition actions = new ActionGroupDefinition();

}
