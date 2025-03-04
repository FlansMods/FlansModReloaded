package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.physics.common.util.Maths;

public class ActionGroupDefinition
{
	@JsonField
	public String key = "default";

	@JsonField
	public boolean canActUnderwater = true;
	@JsonField
	public boolean canActUnderOtherLiquid = false;
	@JsonField(Docs = "If true, attachments that add an action in the same place will override this one")
	public boolean canBeOverriden = false;
	@JsonField(Docs = "If true, then this action will only work if the other hand is empty")
	public boolean twoHanded = false;
	@JsonField(Docs = "Refers to gun modes like Full Auto, but applies to all actions")
	public ERepeatMode repeatMode = ERepeatMode.SemiAuto;
	@JsonField(Min = 0f, Docs =  "The delay from this action to being able to perform it again (in seconds). If you have a desired Fire Rate in RPM, enter 60 / RPM")
	public float repeatDelay = 0.0f;
	@JsonField(Min = 1, Docs = "Number of times to repeat the fire action if we are set to burst fire mode")
	public int repeatCount = 0;
	@JsonField(Docs = "If using minigun fire mode, this is the time (in seconds) that it will take to spin up the motor and start shooting")
	public float spinUpDuration = 1.0f;
	@JsonField(Docs = "The distance this action should be 'heard' from, in block radius. Modify this for silenced actions to not even show up in the net msgs of other players")
	public float loudness = 150f;
	@JsonField(Docs = "If this is set, this action group will untrigger when NOT in this mode")
	public String autoCancelIfNotInMode = "";

	@JsonField
	public ActionDefinition[] actions = new ActionDefinition[0];
	@JsonField(Docs = "These modifiers will be applied to the above actions if applicable")
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];

	public static final ActionGroupDefinition INVALID = new ActionGroupDefinition();
	public boolean IsValid() {return !key.isEmpty() && actions.length > 0; }

	public float GetMaxDurationSeconds()
	{
		float duration = repeatDelay;
		for(ActionDefinition actionDef : actions)
		{
			if(actionDef.duration+(actionDef.delay/20.0F) > duration)
				duration = actionDef.duration+(actionDef.delay/20.0F);
		}

		// Round up ticks so we know the duration covers everything
		int ticks = Maths.floor(duration * 20.0f);
		return ticks / 20.0f;
	}
}
