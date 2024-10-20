package com.flansmod.physics.common.units;

import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public record CompoundVelocity(@Nonnull LinearVelocity linear, @Nonnull AngularVelocity angular)
        implements IVelocity
{
        public static final CompoundVelocity Zero = new CompoundVelocity(LinearVelocity.Zero, AngularVelocity.Zero);

        @Nonnull
        public static CompoundVelocity of(@Nonnull LinearVelocity linear, @Nonnull AngularVelocity angular)
        {
                return new CompoundVelocity(linear, angular);
        }

        @Override @Nonnull
        public CompoundVelocity inverse() { return new CompoundVelocity(linear.inverse(), angular.inverse()); }
        @Override
        public boolean isApproxZero() { return linear.isApproxZero() && angular.isApproxZero(); }
        @Override
        public boolean hasLinearComponent(@Nonnull Transform actingOn) { return true; }
        @Override @Nonnull
        public LinearVelocity getLinearComponent(@Nonnull Transform actingOn) { return linear; }
        @Override
        public boolean hasAngularComponent(@Nonnull Transform actingOn) { return true; }
        @Override @Nonnull
        public AngularVelocity getAngularComponent(@Nonnull Transform actingOn) { return angular; }
        @Override
        public String toString() { return "CompoundVelocity ["+linear+"] at ["+angular+"]"; }
        @Override @Nonnull
        public Component toFancyString() { return Component.translatable("flansphysicsmod.compound_velocity",
                linear.Velocity().x, linear.Velocity().y, linear.Velocity().z, angular.Magnitude(), angular.Axis().x, angular.Axis().y, angular.Axis().z); }
        @Override
        public boolean equals(Object other)
        {
            if(other instanceof CompoundVelocity otherCompound)
                return otherCompound.linear.equals(linear) && otherCompound.angular.equals(angular);
            return false;
        }
        public boolean isApprox(@Nonnull CompoundVelocity other) { return linear.isApprox(other.linear) && angular.isApprox(other.angular); }
        public boolean isApprox(@Nonnull CompoundVelocity other, double epsilon) { return linear.isApprox(other.linear, epsilon) && angular.isApprox(other.angular, epsilon); }
}
