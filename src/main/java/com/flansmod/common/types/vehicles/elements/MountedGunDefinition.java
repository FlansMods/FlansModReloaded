package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.SoundDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import static com.flansmod.common.types.JsonDefinition.InvalidLocation;

public class MountedGunDefinition
{
	public static final MountedGunDefinition INVALID = new MountedGunDefinition();

	@JsonField
	public Vec3 shootPointOffset = Vec3.ZERO;
	@JsonField
	public ResourceLocation gun = InvalidLocation;

}
