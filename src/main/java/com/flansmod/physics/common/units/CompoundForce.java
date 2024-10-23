package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record CompoundForce(@Nonnull LinearForce linear, @Nonnull Torque angular)
        implements IForce
{
    public static final CompoundForce Zero = new CompoundForce(LinearForce.Zero, Torque.Zero);

    @Nonnull
    public static CompoundForce of(@Nonnull LinearForce linear, @Nonnull Torque angular)
    {
        return new CompoundForce(linear, angular);
    }

    @Nonnull
    public CompoundAcceleration applyTo(double mass, @Nonnull Vec3 momentOfInertia) { return applyTo(mass, momentOfInertia, Transform.IDENTITY); }
    @Nonnull
    public CompoundAcceleration applyTo(double mass, @Nonnull Vec3 momentOfInertia, @Nonnull Transform actingOn)
    {
        return CompoundAcceleration.of(getLinearComponent(actingOn).actingOn(mass), getTorqueComponent(actingOn).actingOn(momentOfInertia));
    }

    @Override @Nonnull
    public CompoundForce inverse() { return new CompoundForce(linear.inverse(), angular.inverse()); }
    @Override
    public boolean isApproxZero() { return linear.isApproxZero() && angular.isApproxZero(); }
    @Override
    public boolean hasLinearComponent(@Nonnull Transform actingOn) { return true; }
    @Override @Nonnull
    public LinearForce getLinearComponent(@Nonnull Transform actingOn) { return linear; }
    @Override
    public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
    @Override @Nonnull
    public Torque getTorqueComponent(@Nonnull Transform actingOn) { return angular; }
    @Override
    public String toString() { return "CompoundForce ["+linear+"] at ["+angular+"]"; }
    @Override @Nonnull
    public Component toFancyString() { return Component.translatable("flansphysicsmod.compound_force",
            linear.Force().x, linear.Force().y, linear.Force().z, angular.Magnitude(), angular.Axis().x, angular.Axis().y, angular.Axis().z); }
    @Override
    public boolean equals(Object other)
    {
        if(other instanceof CompoundForce otherCompound)
            return otherCompound.linear.equals(linear) && otherCompound.angular.equals(angular);
        return false;
    }
    public boolean isApprox(@Nonnull CompoundForce other) { return linear.isApprox(other.linear) && angular.isApprox(other.angular); }
    public boolean isApprox(@Nonnull CompoundForce other, double epsilon) { return linear.isApprox(other.linear, epsilon) && angular.isApprox(other.angular, epsilon); }


}
