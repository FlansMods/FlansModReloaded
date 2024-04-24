package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;


public class DamageablePartDefinition
{
	@JsonField
	public String partName = "body";
	@JsonField
	public float maxHealth = 100;
	@JsonField
	public float armourToughness = 1;


}
