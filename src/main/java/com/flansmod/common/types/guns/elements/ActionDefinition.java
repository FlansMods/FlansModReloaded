package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.SoundDefinition;
import net.minecraft.resources.ResourceLocation;

import static com.flansmod.common.types.JsonDefinition.InvalidLocation;

public class ActionDefinition
{
	public static final ActionDefinition Invalid = new ActionDefinition();
	public boolean IsValid() { return actionType.equals(InvalidLocation); }

	// General fields
	@JsonField(DefaultModID = "flansmod")
	public ResourceLocation actionType = InvalidLocation;

	// Animation action specifics
	@JsonField
	public String id = "";

	@JsonField(Docs = "In seconds", Min = 0.0f)
	public float duration = 0.0f;

	@JsonField(Docs = "In ticks", Min = 0.0f)
	public float delay = 0.0f;

	@JsonField
	public SoundDefinition[] sounds = new SoundDefinition[0];
	@JsonField
	public String itemStack = "";

	// IronSight / Scope Action
	@JsonField
	public String scopeOverlay = "";

	// Animation action specifics
	@JsonField
	public String anim = "";
}
