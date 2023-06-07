package com.flansmod.common.types.vehicles;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import com.flansmod.common.types.vehicles.elements.ArticulatedPartDefinition;
import com.flansmod.common.types.vehicles.elements.MountedGunDefinition;
import com.flansmod.common.types.vehicles.elements.SeatDefinition;
import com.flansmod.common.types.vehicles.elements.VehicleMovementDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class VehicleDefinition extends JsonDefinition
{
	public static final VehicleDefinition INVALID = new VehicleDefinition(new ResourceLocation(FlansMod.MODID, "vehicles/null"));
	public static final String TYPE = "vehicle";
	@Override
	public String GetTypeName() { return TYPE; }

	public VehicleDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public SeatDefinition[] seats = new SeatDefinition[0];
	@JsonField
	public ArticulatedPartDefinition[] articulatedParts = new ArticulatedPartDefinition[0];
	@JsonField
	public MountedGunDefinition[] guns = new MountedGunDefinition[0];
	@JsonField
	public VehicleMovementDefinition[] movementModes = new VehicleMovementDefinition[0];

	// Rest Pose
	@JsonField
	public Vec3 restingEulerAngles = Vec3.ZERO;

	// Power / Fuel
	@JsonField
	public EngineDefinition defaultEngine = new EngineDefinition();

	// Harvest volumes
	// TODO:

	// Collision
	// TODO:
	@JsonField
	public boolean useAABBCollisionOnly = false;
	@JsonField
	public Vec3 aabbSize = Vec3.ZERO;


	// Storage
	@JsonField
	public int CargoSlots = 0;
	@JsonField
	public boolean CanAccessMenusWhileMoving = true;
}
