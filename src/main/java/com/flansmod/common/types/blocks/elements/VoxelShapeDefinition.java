package com.flansmod.common.types.blocks.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public class VoxelShapeDefinition
{
    @JsonField
    public Vec3 min = new Vec3(0d, 0d, 0d);
    @JsonField
    public Vec3 max = new Vec3(1d, 1d, 1d);

    @Nonnull
    public VoxelShape Create()
    {
        return Shapes.box(min.x, min.y, min.z, max.x - min.x, max.y - min.y, max.z - min.z);
    }
}
