package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class PaintjobDefinition
{
	@JsonField
	public String textureName = "";
	@JsonField
	public int paintBucketsRequired = 1;
	@JsonField(Docs = "If non-empty, this will lock cosmetic content based on an entitlement")
	public String entitlementKey = "";
}
