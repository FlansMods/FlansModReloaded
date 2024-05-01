package com.flansmod.common.types.vehicles;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.VehicleDefinitionHierarchy;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemDefinition;
import com.flansmod.common.types.parts.elements.EngineDefinition;
import com.flansmod.common.types.vehicles.elements.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;

public class VehicleDefinition extends JsonDefinition
{
	public static final VehicleDefinition INVALID = new VehicleDefinition(new ResourceLocation(FlansMod.MODID, "vehicles/null"));
	public static final String TYPE = "vehicle";
	public static final String FOLDER = "vehicles";
	@Override
	public String GetTypeName() { return TYPE; }

	public VehicleDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();
	@JsonField
	public SeatDefinition[] seats = new SeatDefinition[0];
	@JsonField
	public ArticulatedPartDefinition[] articulatedParts = new ArticulatedPartDefinition[0];
	@JsonField
	public MountedGunDefinition[] guns = new MountedGunDefinition[0];
	@JsonField
	public DamageablePartDefinition[] damageables = new DamageablePartDefinition[0];
	@JsonField
	public VehiclePhysicsDefinition physics = new VehiclePhysicsDefinition();
	@JsonField
	public VehicleControlOptionDefinition[] controllers = new VehicleControlOptionDefinition[0];


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



	@Nonnull
	public Lazy<VehicleDefinitionHierarchy> AsHierarchy = Lazy.of(() -> VehicleDefinitionHierarchy.of(this));
}
