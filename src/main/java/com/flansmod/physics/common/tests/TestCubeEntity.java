package com.flansmod.physics.common.tests;

import com.flansmod.physics.common.entity.PhysicsEntity;
import com.flansmod.physics.common.util.ITransformPair;
import com.flansmod.physics.common.util.ITransformEntity;
import com.flansmod.common.entity.vehicle.hierarchy.VehiclePartPath;
import com.flansmod.physics.common.entity.PhysicsComponent;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.collision.ColliderHandle;
import com.flansmod.physics.common.collision.OBBCollisionSystem;
import com.flansmod.physics.common.units.AngularAcceleration;
import com.flansmod.physics.common.units.LinearAcceleration;
import com.flansmod.physics.common.units.LinearForce;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class TestCubeEntity extends PhysicsEntity // implements ITransformEntity
{
    public float Mass = 1.0f;
    public Vec3 MomentOfInertia = new Vec3(1d, 1d, 1d);

    public TestCubeEntity(@Nonnull EntityType<? extends Entity> type, @Nonnull Level world)
    {
        super(type, world);
    }


    @Override
    protected void tickPhysics()
    {
        PhysicsComponent coreComponent = getRootComponent();

        LinearForce coreGravity = LinearForce.kgBlocksPerSecondSq(new Vec3(0f, -9.81f * Mass, 0f));
        //coreComponent.Forces.AddForce(coreGravity);
        coreComponent.Forces.AddDampener(0.1f);
    }
    @Override
    protected void tickOutsidePhysicsRange()
    {

    }

    @Override
    protected void defineSynchedData() { }
    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) { }
    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) { }
}
