package com.flansmod.common.types.blocks;

import com.flansmod.common.types.Definitions;

public class TurretBlockDefinitions extends Definitions<TurretBlockDefinition>
{
    public TurretBlockDefinitions()
    {
        super(TurretBlockDefinition.FOLDER,
              TurretBlockDefinition.class,
              TurretBlockDefinition.INVALID,
              TurretBlockDefinition::new);
    }
}
