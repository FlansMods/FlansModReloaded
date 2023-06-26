package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.EActionType;
import com.flansmod.common.types.guns.EFireMode;

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
	@JsonField(Docs = "If true, then this action will only work if the other hand is empty")
	public boolean twoHanded = false;

	@JsonField
	public SoundDefinition[] sounds = new SoundDefinition[0];

	@JsonField
	public float duration = 0.0f;
	@JsonField
	public String itemStack = "";

	// Shoot Action Specifics
	@JsonField
	public EFireMode FireMode = EFireMode.FullAuto;
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
