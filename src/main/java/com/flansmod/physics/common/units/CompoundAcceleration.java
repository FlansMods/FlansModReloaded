package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public record CompoundAcceleration(@Nonnull LinearAcceleration linear, @Nonnull AngularAcceleration angular)
        implements IAcceleration
{
    public static final CompoundAcceleration Zero = new CompoundAcceleration(LinearAcceleration.Zero, AngularAcceleration.Zero);

    @Nonnull
    public static CompoundAcceleration of(@Nonnull LinearAcceleration linear, @Nonnull AngularAcceleration angular)
    {
        return new CompoundAcceleration(linear, angular);
    }

    @Nonnull
    public CompoundVelocity applyOneTick() { return applyOneTick(Transform.IDENTITY); }
    @Nonnull
    public CompoundVelocity applyOneTick(@Nonnull Transform actingOn)
    {
        return CompoundVelocity.of(getLinearComponent(actingOn).applyOneTick(), getAngularComponent(actingOn).applyOneTick());
    }
    @Nonnull
    public CompoundForce asForceForPointMass(double mass) { return CompoundForce.of(linear.multiplyBy(mass), angular.asTorqueForPointMass(mass)); }
    @Nonnull
    public CompoundForce asForceForMass(double mass, @Nonnull Vec3 momentOfInertia) { return CompoundForce.of(linear.multiplyBy(mass), angular.asTorqueForSpinMass(momentOfInertia)); }

    @Override @Nonnull
    public CompoundAcceleration inverse() { return new CompoundAcceleration(linear.inverse(), angular.inverse()); }
    @Override
    public boolean isApproxZero() { return linear.isApproxZero() && angular.isApproxZero(); }
    @Override
    public boolean hasLinearComponent(@Nonnull Transform actingOn) { return true; }
    @Override @Nonnull
    public LinearAcceleration getLinearComponent(@Nonnull Transform actingOn) { return linear; }
    @Override
    public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
    @Override @Nonnull
    public AngularAcceleration getAngularComponent(@Nonnull Transform actingOn) { return angular; }
    @Override
    public String toString() { return "CompoundAcceleration ["+linear+"] at ["+angular+"]"; }
    @Override @Nonnull
    public Component toFancyString() { return Component.translatable("flansphysicsmod.compound_acceleration",
            linear.Acceleration().x, linear.Acceleration().y, linear.Acceleration().z, angular.Magnitude(), angular.Axis().x, angular.Axis().y, angular.Axis().z); }
    @Override
    public boolean equals(Object other)
    {
        if(other instanceof CompoundAcceleration otherCompound)
            return otherCompound.linear.equals(linear) && otherCompound.angular.equals(angular);
        return false;
    }
    public boolean isApprox(@Nonnull CompoundAcceleration other) { return linear.isApprox(other.linear) && angular.isApprox(other.angular); }
    public boolean isApprox(@Nonnull CompoundAcceleration other, double epsilon) { return linear.isApprox(other.linear, epsilon) && angular.isApprox(other.angular, epsilon); }


}
