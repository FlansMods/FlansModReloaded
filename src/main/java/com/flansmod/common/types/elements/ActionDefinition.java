package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.EActionType;
import com.flansmod.common.types.guns.ERepeatMode;

public class ActionDefinition
{
	public static final ActionDefinition Invalid = new ActionDefinition();
	public boolean IsValid() { return actionType != EActionType.Invalid; }

	// General fields
	@JsonField
	public EActionType actionType = EActionType.Invalid;
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

	@JsonField(Docs = "")
	public float duration = 0.0f;

	@JsonField
	public SoundDefinition[] sounds = new SoundDefinition[0];
	@JsonField
	public String itemStack = "";

	// Shoot Action Specifics
	@JsonField
	public ShotDefinition[] shootStats = new ShotDefinition[0];

	// IronSight / Scope Action
	@JsonField
	public float fovFactor = 1.25f;
	@JsonField
	public String scopeOverlay = "";

	// Animation action specifics
	@JsonField
	public String anim = "";

	// Tool specifics
	@JsonField
	public float toolLevel = 1.0f;
	@JsonField
	public float harvestSpeed = 1.0f;
	@JsonField
	public float reach = 1.0f;
}
