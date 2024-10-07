package com.flansmod.common.types.blocks.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public class BlockDefinition
{
    private static final VoxelShape DEFAULT_SHAPE = Shapes.block();

    @JsonField
    public boolean full3D = false;
    @JsonField(Docs = "This is only used if full3D is false")
    public VoxelShapeDefinition[] hitboxes = new VoxelShapeDefinition[0];
    @JsonField(Docs = "If empty, all facings are allowed")
    public Direction[] allowedFacings = new Direction[0];

    private VoxelShape Shape = null;
    @Nonnull
    public VoxelShape CreateVoxelShape()
    {
        if(full3D || hitboxes.length == 0)
            return DEFAULT_SHAPE;

        if(Shape == null)
        {
            Shape = hitboxes[0].Create();
            for(int i = 1; i < hitboxes.length; i++)
            {
                Shape = Shapes.join(Shape, hitboxes[i].Create(), BooleanOp.AND);
            }
        }

        return Shape;
    }

    @Nonnull
    public Direction GetPlacementFacing(@Nonnull Direction lookDirection)
    {
        if(allowedFacings.length > 0)
        {
            if (allowedFacings.length == 1)
                return allowedFacings[0];

            Direction best = null;
            for(Direction facing : allowedFacings)
            {
                if(lookDirection == facing)
                    return facing;

                if(lookDirection == facing.getOpposite())
                    best = facing;

                if(best == null)
                    best = facing;
            }

            return best;
        }
        return lookDirection;
    }
}
