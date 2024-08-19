package com.flansmod.common.entity.vehicle.physics;

import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.physics.*;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

// Force model is in Newtons or kgms^-2
public class ForcesOnPart
{
	//public final List<LinearForce> LocalForces = new ArrayList<>();
	//public final List<LinearForce> GlobalForces = new ArrayList<>();
	//public final List<OffsetForce> OffsetLocalForces = new ArrayList<>();
	//public final List<OffsetForce> OffsetGlobalForces = new ArrayList<>();
	//public final List<AngularForce> Angulars = new ArrayList<>();
	private final List<IForce> Forces = new ArrayList<>();
	private final List<IForce> ReactionForces = new ArrayList<>();
	private final List<IForce> ForcesLastFrame = new ArrayList<>();
	public float Dampening = 1.0f;


	public void EndFrame()
	{
		//LocalForces.clear();
		//GlobalForces.clear();
		//OffsetLocalForces.clear();
		//OffsetGlobalForces.clear();
		//Angulars.clear();
		ReactionForces.clear();
		ForcesLastFrame.clear();
		ForcesLastFrame.addAll(Forces);
		Forces.clear();
		Dampening = 1.0f;
	}

	@Nonnull
	public List<IForce> Debug_GetForces()
	{
		return ForcesLastFrame;
	}

	public void AddReactionForce(@Nonnull IForce force)
	{
		ReactionForces.add(force);
	}
	public void AddForce(@Nonnull IForce force)
	{
		Forces.add(force);
	}
	public void AddDampener(float dampening)
	{
		Dampening *= (1.0f - Maths.Clamp(dampening, 0f, 1f));
	}

	public float GetDampeningRatio() { return Dampening; }


	@Nonnull
	public LinearVelocity ApplyLinearAcceleration(@Nonnull LinearVelocity motion, @Nonnull Transform partTransform, float mass, boolean includeReactions)
	{
		return motion.add(SumLinearAcceleration(partTransform, mass, includeReactions).ApplyOneTick());
	}
	@Nonnull
	public LinearAcceleration SumLinearAcceleration(@Nonnull Transform partTransform, float mass, boolean includeReactions)
	{
		return SumLinearForces(partTransform, includeReactions).ActingOn(mass);
	}
	@Nonnull
	public LinearForce SumLinearForces(@Nonnull Transform partTransform, boolean includeReactions)
	{
		LinearForce motion = LinearForce.Zero;

		for(IForce force : Forces)
		{
			if(force.HasLinearComponent(partTransform))
			{
				motion = motion.add(force.GetLinearComponent(partTransform));
			}
		}
		if(includeReactions)
		{
			for(IForce force : ReactionForces)
			{
				if(force.HasLinearComponent(partTransform))
				{
					motion = motion.add(force.GetLinearComponent(partTransform));
				}
			}
		}

		//for(LinearForce local : LocalForces)
		//	motion = motion.add(partTransform.LocalToGlobalDirection(local.Vector()));
		//for(LinearForce global : GlobalForces)
		//	motion = motion.add(global.Vector());
		//
		//for(OffsetForce local : OffsetLocalForces)
		//	motion = motion.add(partTransform.LocalToGlobalDirection(local.Vector()));
		//for(OffsetForce global : OffsetGlobalForces)
		//	motion = motion.add(global.Vector());

		return motion;
	}
	@Nonnull
	public AngularAcceleration SumAngularAcceleration(@Nonnull Transform partTransform, @Nonnull Vec3 momentOfInertia, boolean includeReactions)
	{
		return SumTorque(partTransform, includeReactions).ActingOn(momentOfInertia);
	}

	@Nonnull
	public Torque SumTorque(@Nonnull Transform partTransform, boolean includeReactions)
	{
		Torque sum = Torque.Zero;

		for (IForce force : Forces)
		{
			if (force.HasAngularComponent(partTransform))
			{
				sum = sum.compose(force.GetTorqueComponent(partTransform));
			}
		}
		if (includeReactions)
		{
			for (IForce force : ReactionForces)
			{
				if (force.HasAngularComponent(partTransform))
				{
					sum = sum.compose(force.GetTorqueComponent(partTransform));
				}
			}
		}
		return sum;
	}
}
