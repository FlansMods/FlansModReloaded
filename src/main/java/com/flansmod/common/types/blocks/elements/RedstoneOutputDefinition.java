package com.flansmod.common.types.blocks.elements;

import com.flansmod.common.types.JsonField;

public class RedstoneOutputDefinition
{
    @JsonField(Docs = "Leave as -1 to not use this field")
    public int outputConstant = -1;
    @JsonField
    public boolean outputStackFullness = false;

}
