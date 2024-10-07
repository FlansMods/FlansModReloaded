package com.flansmod.common.types.vehicles.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;

public class VehiclePartDefinition
{
	public static VehiclePartDefinition INVALID = new VehiclePartDefinition("body");
	public VehiclePartDefinition() {}
	private VehiclePartDefinition(@Nonnull String name) {
		VehiclePartDefinition partDef = new VehiclePartDefinition();
		partDef.partName = name;
		partDef.attachedTo = "";
	}


	@JsonField
	public String partName = "default";
	@JsonField
	public String attachedTo = "body";
	@JsonField
	public Vec3 localPosition = Vec3.ZERO;
	@JsonField
	public Vec3 localEulerAngles = Vec3.ZERO;




	@JsonField
	public DamageablePartDefinition damage = new DamageablePartDefinition();

	@JsonField
	public SeatDefinition[] seats = new SeatDefinition[0];
	@JsonField
	public MountedGunDefinition[] guns = new MountedGunDefinition[0];
	@JsonField
	public WheelDefinition[] wheels = new WheelDefinition[0];
	@JsonField
	public PropellerDefinition[] propellers = new PropellerDefinition[0];
	@JsonField
	public LegsDefinition[] legs = new LegsDefinition[0];
	@JsonField
	public ArmDefinition[] arms = new ArmDefinition[0];

	@JsonField
	public ArticulatedPartDefinition articulation = new ArticulatedPartDefinition();

	public boolean IsDamageable() { return damage.IsActive(); }
	public boolean IsArticulated() { return articulation.active; }
	@Nonnull
	public Lazy<Transform> LocalTransform = Lazy.of(() -> Transform.FromPosAndEuler(localPosition, localEulerAngles.toVector3f(), () -> "Default Pose:" + partName));
}
