package com.flansmod.common.types.blocks.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.LocationFilterDefinition;
import com.flansmod.common.types.elements.TransformDefinition;

public class TurretMotionSettingsDefinition
{
    public static final TurretMotionSettingsDefinition Default = new TurretMotionSettingsDefinition();

    @JsonField
    public ETurretMotionType motionType = ETurretMotionType.Static;
    @JsonField
    public float minYaw = 0.0f;
    @JsonField
    public float maxYaw = 0.0f;
    @JsonField
    public float minPitch = 0.0f;
    @JsonField
    public float maxPitch = 0.0f;
    @JsonField
    public float minRoll = 0.0f;
    @JsonField
    public float maxRoll = 0.0f;


    @JsonField(Docs = "For use with LookAtEntity or LookAtBlock")
    public LocationFilterDefinition[] idFilters = new LocationFilterDefinition[0];
    @JsonField(Docs = "For use with CycleStates or CycleContinuous")
    public TransformDefinition[] poses = new TransformDefinition[0];

    @JsonField(Docs = "How long to wait in each state in a Cycle")
    public float cycleStatesWaitSeconds = 0.0f;
    @JsonField
    public float cycleTransitionTimeSeconds = 0.0f;


}
