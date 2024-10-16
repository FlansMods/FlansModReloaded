package com.flansmod.common.entity.vehicle;

import com.flansmod.common.entity.vehicle.hierarchy.VehicleComponentPath;
import com.flansmod.common.entity.vehicle.hierarchy.VehiclePartPath;
import com.flansmod.physics.common.util.ITransformEntity;
import com.flansmod.physics.common.util.ITransformPair;

import javax.annotation.Nonnull;

public interface ISegmentedEntity extends ITransformEntity
{
    @Nonnull
    ITransformPair getEntityToAP(@Nonnull VehiclePartPath apPath);

    @Nonnull default ITransformPair getWorldToAP(@Nonnull VehiclePartPath apPath) {
        return ITransformPair.compose(getRootTransform(), getEntityToAP(apPath));
    }
    @Nonnull default ITransformPair getWorldToAP(@Nonnull VehicleComponentPath apPath) {
        return ITransformPair.compose(getRootTransform(), getEntityToAP(apPath.Part()));
    }
}
