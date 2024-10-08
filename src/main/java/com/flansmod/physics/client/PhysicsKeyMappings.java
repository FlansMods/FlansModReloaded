package com.flansmod.physics.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class PhysicsKeyMappings {

    public static final Lazy<KeyMapping> DEBUG_PAUSE_PHYSICS = Lazy.of(() -> new KeyMapping(
            "key.flansphysicsmod.debug.pause_physics",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            "key.categories.flansphysicsmod.debug"
    ));
    public static final Lazy<KeyMapping> DEBUG_INCREASE_PHYSICS_INSPECT = Lazy.of(() -> new KeyMapping(
            "key.flansphysicsmod.debug.cycle_increase",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ADD,
            "key.categories.flansphysicsmod.debug"
    ));
    public static final Lazy<KeyMapping> DEBUG_DECREASE_PHYSICS_INSPECT = Lazy.of(() -> new KeyMapping(
            "key.flansphysicsmod.debug.cycle_decrease",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_SUBTRACT,
            "key.categories.flansphysicsmod.debug"
    ));


}
