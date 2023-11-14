package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.vehicles.EPlayerInput;

public class HandlerDefinition
{
	public static final HandlerDefinition INVALID = new HandlerDefinition();

	@JsonField
	public EPlayerInput inputType = EPlayerInput.Fire1;

	@JsonField
	public HandlerNodeDefinition[] nodes = new HandlerNodeDefinition[0];
}
