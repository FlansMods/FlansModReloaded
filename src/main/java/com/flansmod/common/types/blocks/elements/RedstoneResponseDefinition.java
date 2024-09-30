package com.flansmod.common.types.blocks.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.EPlayerInput;

public class RedstoneResponseDefinition {
    @JsonField
    public int minRedstoneLevel = 0;
    @JsonField
    public int maxRedstoneLevel = 15;
    @JsonField
    public boolean allowIndirectPower = true;


    @JsonField
    public EPlayerInput simulateInput = EPlayerInput.Fire1;
    @JsonField
    public boolean sendOnPressEvent = true;
    @JsonField
    public boolean sustainHeldEvent = false;
    @JsonField
    public boolean sendOnReleaseEvent = false;
}
