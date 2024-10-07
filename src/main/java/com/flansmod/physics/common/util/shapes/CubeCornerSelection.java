package com.flansmod.physics.common.util.shapes;

import net.minecraft.core.Direction;

import javax.annotation.Nonnull;

public enum CubeCornerSelection
{
    NegX_NegY_NegZ(new Direction[] { Direction.WEST, Direction.DOWN, Direction.NORTH }),
    NegX_NegY_PosZ(new Direction[] { Direction.WEST, Direction.DOWN, Direction.SOUTH }),
    NegX_PosY_NegZ(new Direction[] { Direction.WEST, Direction.UP, Direction.NORTH }),
    NegX_PosY_PosZ(new Direction[] { Direction.WEST, Direction.UP, Direction.SOUTH }),
    PosX_NegY_NegZ(new Direction[] { Direction.EAST, Direction.DOWN, Direction.NORTH }),
    PosX_NegY_PosZ(new Direction[] { Direction.EAST, Direction.DOWN, Direction.SOUTH }),
    PosX_PosY_NegZ(new Direction[] { Direction.EAST, Direction.UP, Direction.NORTH }),
    PosX_PosY_PosZ(new Direction[] { Direction.EAST, Direction.UP, Direction.SOUTH });

    private final Direction[] PossibleFaces;

    CubeCornerSelection(Direction[] possibleFaces)
    {
        PossibleFaces = possibleFaces;
    }

    public static final CubeCornerSelection[] UP = new CubeCornerSelection[] { NegX_PosY_NegZ, PosX_PosY_NegZ, PosX_PosY_PosZ, NegX_PosY_PosZ };
    public static final CubeCornerSelection[] DOWN = new CubeCornerSelection[] { NegX_NegY_NegZ, PosX_NegY_NegZ, PosX_NegY_PosZ, NegX_NegY_PosZ };
    public static final CubeCornerSelection[] NORTH = new CubeCornerSelection[] { NegX_PosY_NegZ, PosX_PosY_NegZ, PosX_NegY_NegZ, NegX_NegY_NegZ };
    public static final CubeCornerSelection[] EAST = new CubeCornerSelection[] { PosX_PosY_NegZ, PosX_PosY_PosZ, PosX_NegY_PosZ, PosX_NegY_NegZ };
    public static final CubeCornerSelection[] SOUTH = new CubeCornerSelection[] { NegX_PosY_PosZ, PosX_PosY_PosZ, PosX_NegY_PosZ, NegX_NegY_PosZ };
    public static final CubeCornerSelection[] WEST = new CubeCornerSelection[] { NegX_PosY_NegZ, NegX_PosY_PosZ, NegX_NegY_PosZ, NegX_NegY_NegZ };

    @Nonnull
    public static CubeCornerSelection of(boolean positiveX, boolean positiveY, boolean positiveZ)
    {
        if(positiveX)
        {
            if(positiveY)
            {
                if(positiveZ)
                    return PosX_PosY_PosZ;
                else
                    return PosX_PosY_NegZ;
            }
            else
            {
                if(positiveZ)
                    return PosX_NegY_PosZ;
                else
                    return PosX_NegY_NegZ;
            }
        }
        else
        {
            if(positiveY)
            {
                if(positiveZ)
                    return NegX_PosY_PosZ;
                else
                    return NegX_PosY_NegZ;
            }
            else
            {
                if(positiveZ)
                    return NegX_NegY_PosZ;
                else
                    return NegX_NegY_NegZ;
            }
        }
    }

    @Nonnull
    public Direction[] getPossibleFaces()
    {
        return PossibleFaces;
    }

    @Nonnull
    public CubeCornerSelection opposite()
    {
        return switch (this) {
            case NegX_NegY_NegZ -> PosX_PosY_PosZ;
            case NegX_NegY_PosZ -> PosX_PosY_NegZ;
            case NegX_PosY_NegZ -> PosX_NegY_PosZ;
            case NegX_PosY_PosZ -> PosX_NegY_NegZ;
            case PosX_NegY_NegZ -> NegX_PosY_PosZ;
            case PosX_NegY_PosZ -> NegX_PosY_NegZ;
            case PosX_PosY_NegZ -> NegX_NegY_PosZ;
            case PosX_PosY_PosZ -> NegX_NegY_NegZ;
        };
    }
}
