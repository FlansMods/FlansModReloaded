package com.flansmod.physics.common.util.shapes;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public enum VertexIndex
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

    VertexIndex(Direction[] possibleFaces)
    {
        PossibleFaces = possibleFaces;
    }

    @Nonnull
    public Vec3 getVertex(@Nonnull AABB aabb)
    {
        return switch(this){
            case NegX_NegY_NegZ -> new Vec3(aabb.minX, aabb.minY, aabb.minZ);
            case NegX_NegY_PosZ -> new Vec3(aabb.minX, aabb.minY, aabb.maxZ);
            case NegX_PosY_NegZ -> new Vec3(aabb.minX, aabb.maxY, aabb.minZ);
            case NegX_PosY_PosZ -> new Vec3(aabb.minX, aabb.maxY, aabb.maxZ);
            case PosX_NegY_NegZ -> new Vec3(aabb.maxX, aabb.minY, aabb.minZ);
            case PosX_NegY_PosZ -> new Vec3(aabb.maxX, aabb.minY, aabb.maxZ);
            case PosX_PosY_NegZ -> new Vec3(aabb.maxX, aabb.maxY, aabb.minZ);
            case PosX_PosY_PosZ -> new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ);
        };
    }


    public static final VertexIndex[] UP = new VertexIndex[] { NegX_PosY_NegZ, PosX_PosY_NegZ, PosX_PosY_PosZ, NegX_PosY_PosZ };
    public static final VertexIndex[] DOWN = new VertexIndex[] { NegX_NegY_NegZ, PosX_NegY_NegZ, PosX_NegY_PosZ, NegX_NegY_PosZ };
    public static final VertexIndex[] NORTH = new VertexIndex[] { NegX_PosY_NegZ, PosX_PosY_NegZ, PosX_NegY_NegZ, NegX_NegY_NegZ };
    public static final VertexIndex[] EAST = new VertexIndex[] { PosX_PosY_NegZ, PosX_PosY_PosZ, PosX_NegY_PosZ, PosX_NegY_NegZ };
    public static final VertexIndex[] SOUTH = new VertexIndex[] { NegX_PosY_PosZ, PosX_PosY_PosZ, PosX_NegY_PosZ, NegX_NegY_PosZ };
    public static final VertexIndex[] WEST = new VertexIndex[] { NegX_PosY_NegZ, NegX_PosY_PosZ, NegX_NegY_PosZ, NegX_NegY_NegZ };
    @Nonnull
    public static VertexIndex[] getVertexOrder(@Nonnull Direction dir)
    {
        return switch(dir) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
    @Nonnull
    public static ImmutableList<Vec3> getAABBFace(@Nonnull AABB aabb, @Nonnull Direction dir)
    {
        ImmutableList.Builder<Vec3> builder = new ImmutableList.Builder<>();
        VertexIndex[] vertexOrder = getVertexOrder(dir);
        for(VertexIndex vertex : vertexOrder)
            builder.add(vertex.getVertex(aabb));
        return builder.build();
    }

    @Nonnull
    public static VertexIndex of(boolean positiveX, boolean positiveY, boolean positiveZ)
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
    public VertexIndex opposite()
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
