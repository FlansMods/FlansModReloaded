package com.flansmod.physics.common.tests;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.collision.TransformedBB;
import com.flansmod.physics.common.collision.threading.CollisionTasks;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.ProjectedRange;
import com.flansmod.physics.common.util.ProjectionUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class CollisionTests
{
    public static void runTests()
    {
        Vec3 xAxis = new Vec3(1, 0, 0);
        Vec3 yAxis = new Vec3(0, 1, 0);
        Vec3 zAxis = new Vec3(0, 0, 1);
        AABB unitCube = new AABB(0d, 0d, 0d, 1d, 1d, 1d);
        AABB collidingCube = new AABB(0.25d, 0.25d, 0.25d, 1.25d, 1.25d, 1.25d);
        AABB collidingOnlyZCube = new AABB(-2d, -2d, 0.5d, -1d, -1d, 1.5d);
        AABB nonCollidingCube = new AABB(2.0d, 2.0d, 2.0d, 3.0d, 3.0d, 3.0d);
        assertEqual(ProjectionUtil.ProjectAABBMinMax(xAxis, unitCube), 0d, 1d, "UnitCube projection x");
        assertEqual(ProjectionUtil.ProjectAABBMinMax(yAxis, unitCube), 0d, 1d, "UnitCube projection y");
        assertEqual(ProjectionUtil.ProjectAABBMinMax(zAxis, unitCube), 0d, 1d, "UnitCube projection z");
        assertEqual(ProjectionUtil.ProjectAABBMinMax(xAxis.scale(-1d), unitCube), -1d, 0d, "UnitCube projection -x");
        assertEqual(ProjectionUtil.ProjectAABBMinMax(yAxis.scale(-1d), unitCube), -1d, 0d, "UnitCube projection -y");
        assertEqual(ProjectionUtil.ProjectAABBMinMax(zAxis.scale(-1d), unitCube), -1d, 0d, "UnitCube projection -z");

        assertCollidesOnAxis(xAxis, unitCube, collidingCube, "Colliding cube did not collide in x");
        assertCollidesOnAxis(yAxis, unitCube, collidingCube, "Colliding cube did not collide in y");
        assertCollidesOnAxis(zAxis, unitCube, collidingCube, "Colliding cube did not collide in z");

        assertSeparatesOnAxis(xAxis, unitCube, collidingOnlyZCube, "Colliding only in Z cube did collide in x");
        assertSeparatesOnAxis(yAxis, unitCube, collidingOnlyZCube, "Colliding only in Z cube did collide in y");
        assertCollidesOnAxis(zAxis, unitCube, collidingOnlyZCube, "Colliding only in Z cube did not collide in z");

        assertCollides(unitCube, collidingCube, "Colliding cube did not collide");
        assertSeparated(unitCube, collidingOnlyZCube, "Z-overlap non-colliding cube collided");
        assertSeparated(unitCube, nonCollidingCube, "Non colliding cube collided");


    }



    private static void assertCollidesOnAxis(@Nonnull Vec3 axis, @Nonnull AABB a, @Nonnull AABB b, @Nonnull String error)
    {
        assertCollides(ProjectionUtil.ProjectAABBMinMax(axis, a), ProjectionUtil.ProjectAABBMinMax(axis, b), error);
    }
    private static void assertSeparatesOnAxis(@Nonnull Vec3 axis, @Nonnull AABB a, @Nonnull AABB b, @Nonnull String error)
    {
        assertNotCollides(ProjectionUtil.ProjectAABBMinMax(axis, a), ProjectionUtil.ProjectAABBMinMax(axis, b), error);
    }
    private static void assertCollides(@Nonnull ProjectedRange a, @Nonnull ProjectedRange b, @Nonnull String error)
    {
        if(ProjectionUtil.Separated(a, b))
            FlansPhysicsMod.LOGGER.error(error);
    }
    private static void assertNotCollides(@Nonnull ProjectedRange a, @Nonnull ProjectedRange b, @Nonnull String error)
    {
        if(!ProjectionUtil.Separated(a, b))
            FlansPhysicsMod.LOGGER.error(error);
    }
    private static void assertEqual(@Nonnull ProjectedRange range, double min, double max, @Nonnull String error)
    {
        if(!Maths.approx(range.min(), min) || !Maths.approx(range.max(), max))
            FlansPhysicsMod.LOGGER.error(error);
    }
    private static void assertCollides(@Nonnull AABB a, @Nonnull AABB b, @Nonnull String error)
    {
        if(CollisionTasks.separate(a, b).success())
            FlansPhysicsMod.LOGGER.error(error);
    }
    private static void assertCollides(@Nonnull TransformedBB a, @Nonnull AABB b, @Nonnull String error)
    {
        if(CollisionTasks.separate(a, b).success())
            FlansPhysicsMod.LOGGER.error(error);
    }
    private static void assertCollides(@Nonnull TransformedBB a, @Nonnull TransformedBB b, @Nonnull String error)
    {
        if(CollisionTasks.separate(a, b).success())
            FlansPhysicsMod.LOGGER.error(error);
    }
    private static void assertSeparated(@Nonnull AABB a, @Nonnull AABB b, @Nonnull String error)
    {
        if(!CollisionTasks.separate(a, b).success())
            FlansPhysicsMod.LOGGER.error(error);
    }
    private static void assertSeparated(@Nonnull TransformedBB a, @Nonnull AABB b, @Nonnull String error)
    {
        if(!CollisionTasks.separate(a, b).success())
            FlansPhysicsMod.LOGGER.error(error);
    }
    private static void assertSeparated(@Nonnull TransformedBB a, @Nonnull TransformedBB b, @Nonnull String error)
    {
        if(!CollisionTasks.separate(a, b).success())
            FlansPhysicsMod.LOGGER.error(error);
    }
}
