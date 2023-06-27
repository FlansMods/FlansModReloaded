package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

import java.util.ArrayList;

public class ReloadStageDefinition
{
	@JsonField(Docs = "The full duration of this reload stage, in seconds")
	public float duration = 10.0f;
	@JsonField(Docs = "All actions to run when entering this reload stage")
	public ActionDefinition[] actions = new ActionDefinition[0];

}
