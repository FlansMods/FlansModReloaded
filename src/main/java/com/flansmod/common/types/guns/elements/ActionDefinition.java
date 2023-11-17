package com.flansmod.common.types.guns.elements;

import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.gunshots.ModifierStack;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.SoundDefinition;
import com.flansmod.util.Maths;

public class ActionDefinition
{
	public static final ActionDefinition Invalid = new ActionDefinition();
	public boolean IsValid() { return actionType != EActionType.Invalid; }

	// General fields
	@JsonField
	public EActionType actionType = EActionType.Invalid;

	@JsonField(Docs = "In seconds", Min = 0.0f)
	public float duration = 0.0f;

	@JsonField
	public SoundDefinition[] sounds = new SoundDefinition[0];
	@JsonField
	public String itemStack = "";

	@JsonField(Docs = "These will be applied to this action if applicable")
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];

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

	public float ModifyFloat(String key, String groupPath, float baseValue)
	{
		ModifierStack stack = new ModifierStack(key, groupPath);
		for(ModifierDefinition mod : modifiers)
			stack.Apply(mod);
		return stack.ApplyTo(baseValue);
	}
	public int ToolLevel(String groupPath)
	{
		return Maths.Ceil(ModifyFloat(ModifierDefinition.STAT_TOOL_HARVEST_LEVEL, groupPath, toolLevel));
	}
}