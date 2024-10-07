package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class TransformDefinition
{
    @JsonField
    public Vec3 position = new Vec3(0d, 0d, 0d);
    @JsonField
    public Vector3d eulerAngles = new Vector3d(0d, 0d, 0d);


}
