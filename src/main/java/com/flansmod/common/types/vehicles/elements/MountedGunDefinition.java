package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.world.phys.Vec3;

public class MountedGunDefinition
{
	@JsonField
	public ActionDefinition[] primaryActions = new ActionDefinition[0];

	@JsonField
	public Vec3 recoil = Vec3.ZERO;
	@JsonField
	public String attachedTo = "body";
}
