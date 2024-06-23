package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.common.entity.vehicle.hierarchy.VehicleComponentPath;
import com.flansmod.common.entity.vehicle.hierarchy.WheelEntity;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForceModel
{
	public record Force(@Nonnull Vec3 Vector, @Nullable Supplier<String> DebugName)
	{
		@Nonnull
		public String GetDebugString() { return DebugName != null ? DebugName.get() : "Force"; }
	}
	public record OffsetForce(@Nonnull Vec3 Offset, @Nonnull Vec3 Vector, @Nullable Supplier<String> DebugName)
	{
		@Nonnull
		public String GetDebugString() { return DebugName != null ? DebugName.get() : "Force"; }
	}
	public record SpringJoint(@Nonnull String PullTowardsAP, @Nonnull Vec3 Offset, float SpringStrength, @Nullable Supplier<String> DebugName)
	{
		@Nonnull
		public String GetDebugString() { return DebugName != null ? DebugName.get() : "Spring"; }
	}
	public static class ForcesOnPart
	{
		public final List<Force> LocalForces = new ArrayList<>();
		public final List<Force> GlobalForces = new ArrayList<>();
		public final List<OffsetForce> OffsetLocalForces = new ArrayList<>();
		public final List<OffsetForce> OffsetGlobalForces = new ArrayList<>();
		public final List<Force> AngularLocals = new ArrayList<>();
		public float Dampening = 1.0f;
		public final List<SpringJoint> Springs = new ArrayList<>();
	}

	public final Map<String, ForcesOnPart> Forces = new HashMap<>();
	@Nonnull
	private ForcesOnPart GetOrCreate(@Nonnull String partName)
	{
		if(Forces.containsKey(partName))
			return Forces.get(partName);
		ForcesOnPart forces = new ForcesOnPart();
		Forces.put(partName, forces);
		return forces;
	}

	@Nonnull
	public static String Wheel(int wheelIndex) { return "wheel_"+wheelIndex; }

	// Apply to core quick functions
	// Vectors are in m/s^2 - Do not divide by 20 for tickrate
	public void AddLocalForceToCore(@Nonnull Vec3 localForce, @Nullable Supplier<String> debug) { AddLocalForce(VehicleDefinition.CoreName, localForce, debug); }
	public void AddGlobalForceToCore(@Nonnull Vec3 globalForce, @Nullable Supplier<String> debug) { AddGlobalForce(VehicleDefinition.CoreName, globalForce, debug); }
	public void AddLocalAngularImpulseToCore(@Nonnull Vec3 localAngularForce, @Nullable Supplier<String> debug) { AddLocalAngularImpulse(VehicleDefinition.CoreName, localAngularForce, debug); }
	public void AddDampenerToCore(float dampening) { AddDampener(VehicleDefinition.CoreName, dampening); }

	// Apply to wheel quick functions
	public void AddLocalForceToWheel(int wheelIndex, @Nonnull Vec3 localForce, @Nullable Supplier<String> debug) { AddLocalForce(Wheel(wheelIndex), localForce, debug); }
	public void AddGlobalForceToWheel(int wheelIndex, @Nonnull Vec3 globalForce, @Nullable Supplier<String> debug) { AddGlobalForce(Wheel(wheelIndex), globalForce, debug); }
	public void AddLocalAngularImpulseToWheel(int wheelIndex, @Nonnull Vec3 localAngularForce, @Nullable Supplier<String> debug) { AddLocalAngularImpulse(Wheel(wheelIndex), localAngularForce, debug); }
	public void AddDampenerToWheel(int wheelIndex, float dampening) { AddDampener(Wheel(wheelIndex), dampening); }

	public void AddLocalForce(@Nonnull String partName, @Nonnull Vec3 localForce, @Nullable Supplier<String> debug)
	{
		GetOrCreate(partName).LocalForces.add(new Force(localForce, debug));
	}
	public void AddGlobalForce(@Nonnull String partName, @Nonnull Vec3 globalForce, @Nullable Supplier<String> debug)
	{
		GetOrCreate(partName).GlobalForces.add(new Force(globalForce, debug));
	}
	public void AddLocalOffsetForce(@Nonnull String partName, @Nonnull Vec3 offset, @Nonnull Vec3 localForce, @Nullable Supplier<String> debug)
	{
		GetOrCreate(partName).OffsetLocalForces.add(new OffsetForce(offset, localForce, debug));
	}
	public void AddGlobalOffsetForce(@Nonnull String partName, @Nonnull Vec3 offset, @Nonnull Vec3 globalForce, @Nullable Supplier<String> debug)
	{
		GetOrCreate(partName).OffsetGlobalForces.add(new OffsetForce(offset, globalForce, debug));
	}
	public void AddLocalAngularImpulse(@Nonnull String partName, @Nonnull Vec3 localAngularForce, @Nullable Supplier<String> debug)
	{
		GetOrCreate(partName).AngularLocals.add(new Force(localAngularForce, debug));
	}
	public void AddDampener(@Nonnull String partName, float dampening)
	{
		GetOrCreate(partName).Dampening *= (1.0f - Maths.Clamp(dampening, 0f, 1f));
	}
	public void AddSpringOneWay(@Nonnull String pullOnPart, @Nonnull String pullTowards, @Nonnull Vec3 offset, float springStrength, @Nullable Supplier<String> debug)
	{
		GetOrCreate(pullOnPart).Springs.add(new SpringJoint(pullTowards, offset, springStrength, debug));
	}
	public void AddDefaultWheelSpring(@Nonnull VehicleEntity vehicle, @Nonnull WheelEntity wheel)
	{
		Transform wheelAP = vehicle.GetWorldToAP(wheel.GetWheelPath()).GetCurrent();
		Vec3 wheelAPPos = wheelAP.PositionVec3();
		Vec3 wheelEntityPos = wheel.position();
		Vec3 delta = wheelEntityPos.subtract(wheelAPPos);
		Vec3 springForce = delta.scale(wheel.GetWheelDef().springStrength * 20f);

		AddGlobalOffsetForce(VehicleDefinition.CoreName, wheelAPPos.subtract(vehicle.position()), springForce, () -> "Wheel Spring Pull on Core");
		AddGlobalForce(Wheel(wheel.GetWheelIndex()), springForce.scale(-1f), () -> "Core Pull on Wheel Spring");
	}


	@Nonnull
	public Vec3 ApplyLinearForcesToCore(@Nonnull Vec3 motion, @Nonnull Transform coreTransform, float coreMass)
	{
		return ApplyLinearForces(motion, VehicleDefinition.CoreName, coreTransform, coreMass);
	}
	@Nonnull
	public Vec3 ApplyLinearForcesToWheel(@Nonnull Vec3 motion, int wheelIndex, @Nonnull Transform wheelTransform, float wheelMass)
	{
		return ApplyLinearForces(motion, Wheel(wheelIndex), wheelTransform, wheelMass);
	}
	@Nonnull
	public Vec3 ApplyLinearForces(@Nonnull Vec3 motion, @Nonnull String partName, @Nonnull Transform partTransform, float mass)
	{
		// When we say apply, we mean advance one tick, so simulate 1/20s
		if(Forces.containsKey(partName))
		{
			float inertia = 1f/(20f*mass);
			ForcesOnPart forces = Forces.get(partName);
			for(Force local : forces.LocalForces)
				motion = motion.add(partTransform.LocalToGlobalDirection(local.Vector.scale(inertia)));
			for(Force global : forces.GlobalForces)
				motion = motion.add(global.Vector.scale(inertia));

			for(OffsetForce local : forces.OffsetLocalForces)
				motion = motion.add(partTransform.LocalToGlobalDirection(local.Vector.scale(inertia)));
			for(OffsetForce global : forces.OffsetGlobalForces)
				motion = motion.add(global.Vector.scale(inertia));
		}
		return motion;
	}
	@Nonnull public Vec3 ApplyDampeningToCore(@Nonnull Vec3 motion) { return ApplyDampening(VehicleDefinition.CoreName, motion); }
	@Nonnull public Vec3 ApplyDampeningToWheel(int wheelIndex, @Nonnull Vec3 motion) { return ApplyDampening(Wheel(wheelIndex), motion); }
	@Nonnull
	public Vec3 ApplyDampening(@Nonnull String partName, @Nonnull Vec3 motion)
	{
		if(Forces.containsKey(partName))
		 	return motion.scale(Forces.get(partName).Dampening);
		return motion;
	}
	@Nonnull public Vec3 ApplySpringForcesToCore(@Nonnull Vec3 motion, @Nonnull Transform coreTransform, float coreMass, @Nonnull Function<VehicleComponentPath, Transform> lookup)
	{ return ApplySpringForces(motion, VehicleDefinition.CoreName, coreTransform, coreMass, lookup); }
	@Nonnull public Vec3 ApplySpringForcesToWheel(@Nonnull Vec3 motion, int wheelIndex, @Nonnull Transform wheelTransform, float wheelMass, @Nonnull Function<VehicleComponentPath, Transform> lookup)
	{ return ApplySpringForces(motion, Wheel(wheelIndex), wheelTransform, wheelMass, lookup); }
	@Nonnull
	public Vec3 ApplySpringForces(@Nonnull Vec3 motion,
								  @Nonnull String partName,
								  @Nonnull Transform thisTransform,
								  float mass,
								  @Nonnull Function<VehicleComponentPath, Transform> lookup)
	{
		if(Forces.containsKey(partName))
		{
			ForcesOnPart forces = Forces.get(partName);
			for(SpringJoint spring : forces.Springs)
			{
				Transform target = lookup.apply(VehicleComponentPath.of(spring.PullTowardsAP)); // TODO: Not gonna work?
				if(target != null)
				{
					Vec3 currentWorldPos = thisTransform.PositionVec3();
					Vec3 targetWorldPos = target.PositionVec3();
					Vec3 delta = targetWorldPos.subtract(currentWorldPos);
					Vec3 springForce = delta.scale(spring.SpringStrength / (20f * mass));
					motion = motion.add(springForce);
				}
			}
		}
		return motion;
	}

	// --
	@Nullable public ForcesOnPart Debug_GetForcesOnCore() { return Debug_GetForcesOn(VehicleDefinition.CoreName); }
	@Nullable public ForcesOnPart Debug_GetForcesOnWheel(int wheelIndex) { return Debug_GetForcesOn(Wheel(wheelIndex)); }

	@Nullable
	public ForcesOnPart Debug_GetForcesOn(@Nonnull String partName)
	{
		return Forces.get(partName);
	}

}
