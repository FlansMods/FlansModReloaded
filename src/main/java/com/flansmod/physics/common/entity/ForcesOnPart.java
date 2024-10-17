package com.flansmod.physics.common.entity;

import com.flansmod.physics.common.units.*;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

// Force model is in Newtons or kgms^-2
public class ForcesOnPart
{
	private final List<IForce> forces = new ArrayList<>();
	private final List<IForce> reactionForces = new ArrayList<>();
	public float dampening = 1.0f;

	public void endFrame()
	{
		reactionForces.clear();
		forces.clear();
		dampening = 1.0f;
	}

	@Nonnull
	public List<IForce> Debug_GetForces()
	{
		return forces;
	}

	public void addReactionForce(@Nonnull IForce force)
	{
		reactionForces.add(force);
	}
	public void addForce(@Nonnull IForce force)
	{
		forces.add(force);
	}
	public void addDampener(float dampening)
	{
		this.dampening *= (1.0f - Maths.clamp(dampening, 0f, 1f));
	}

	public float getDampeningRatio() { return dampening; }


	@Nonnull
	public LinearVelocity applyLinearAcceleration(@Nonnull LinearVelocity motion, @Nonnull Transform partTransform, float mass, boolean includeReactions)
	{
		return motion.add(sumLinearAcceleration(partTransform, mass, includeReactions).applyOneTick());
	}
	@Nonnull
	public LinearAcceleration sumLinearAcceleration(@Nonnull Transform partTransform, double mass, boolean includeReactions)
	{
		return sumLinearForces(partTransform, includeReactions).actingOn(mass);
	}
	@Nonnull
	public LinearForce sumLinearForces(@Nonnull Transform partTransform, boolean includeReactions)
	{
		LinearForce motion = LinearForce.Zero;

		for(IForce force : forces)
		{
			if(force.hasLinearComponent(partTransform))
			{
				motion = motion.add(force.getLinearComponent(partTransform));
			}
		}
		if(includeReactions)
		{
			for(IForce force : reactionForces)
			{
				if(force.hasLinearComponent(partTransform))
				{
					motion = motion.add(force.getLinearComponent(partTransform));
				}
			}
		}

		return motion;
	}
	@Nonnull
	public AngularAcceleration sumAngularAcceleration(@Nonnull Transform partTransform, @Nonnull Vec3 momentOfInertia, boolean includeReactions)
	{
		return sumTorque(partTransform, includeReactions).actingOn(momentOfInertia);
	}

	@Nonnull
	public Torque sumTorque(@Nonnull Transform partTransform, boolean includeReactions)
	{
		Torque sum = Torque.Zero;

		for (IForce force : forces)
		{
			if (force.hasAngularComponent(partTransform))
			{
				sum = sum.compose(force.getTorqueComponent(partTransform));
			}
		}
		if (includeReactions)
		{
			for (IForce force : reactionForces)
			{
				if (force.hasAngularComponent(partTransform))
				{
					sum = sum.compose(force.getTorqueComponent(partTransform));
				}
			}
		}
		return sum;
	}

	@Override
	public boolean equals(Object other)
	{
		if(other instanceof ForcesOnPart otherForces)
		{
			return otherForces.forces.equals(forces)
					&& otherForces.reactionForces.equals(reactionForces)
					&& Maths.approx(dampening, otherForces.dampening);
		}
		return false;
	}
}
