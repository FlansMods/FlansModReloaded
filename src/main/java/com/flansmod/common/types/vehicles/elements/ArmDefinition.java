package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;

public class ArmDefinition
{
	@JsonField
	public String name = "default";
	@JsonField
	public String attachedTo = "body";
	@JsonField
	public boolean right = false;
	@JsonField
	public Vec3 origin = Vec3.ZERO;
	@JsonField
	public float armLength = 1.0f;
	@JsonField
	public boolean hasHoldingSlot = false;
	@JsonField
	public int numUpgradeSlots = 0;
	@JsonField
	public boolean canFireGuns = false;
	@JsonField
	public boolean canUseMechaTools = false;
	@JsonField
	public float heldItemScale = 1.0f; // Should be in the model?
	@JsonField(Docs = "How far this hand can reach when using tools")
	public float reach = 10.0f;
}
