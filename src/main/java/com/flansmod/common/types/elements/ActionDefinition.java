package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class ActionDefinition
{
	// General fields
	@JsonField
	public EActionType actionType = EActionType.Animation;
	@JsonField
	public boolean canActUnderwater = true;
	@JsonField
	public boolean canActUnderOtherLiquid = false;
	@JsonField(Docs = "If true, attachments that add an action in the same place will override this one")
	public boolean canBeOverriden = false;
	@JsonField
	public String sound;
	@JsonField
	public int soundLength;
	@JsonField
	public boolean distortSound;
	@JsonField
	public float duration = 0.0f;

	// Shoot Action Specifics
	@JsonField
	public EFireMode FireMode = EFireMode.FullAuto;
	@JsonField
	public ShotDefinition shootStats = new ShotDefinition();

	// IronSight / Scope Action
	@JsonField
	public float fovFactor = 1.25f;
	@JsonField
	public String scopeOverlay = "";

	// Animation action specifics
	@JsonField
	public String anim = "";

}
