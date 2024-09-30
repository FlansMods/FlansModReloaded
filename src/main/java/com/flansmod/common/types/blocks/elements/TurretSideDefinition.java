package com.flansmod.common.types.blocks.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.core.Direction;

public class TurretSideDefinition
{
    @JsonField
    public Direction side = Direction.NORTH;

    @JsonField
    public RedstoneResponseDefinition[] redstoneResponses = new RedstoneResponseDefinition[0];
    @JsonField
    public RedstoneOutputDefinition[] redstoneOutputs = new RedstoneOutputDefinition[0];
}
