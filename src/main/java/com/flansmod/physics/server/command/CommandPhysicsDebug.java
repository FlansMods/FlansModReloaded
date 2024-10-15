package com.flansmod.physics.server.command;

import com.flansmod.physics.common.collision.ColliderHandle;
import com.flansmod.physics.common.collision.OBBCollisionSystem;
import com.flansmod.physics.common.entity.PhysicsEntity;
import com.flansmod.physics.common.units.AngularAcceleration;
import com.flansmod.physics.common.units.AngularVelocity;
import com.flansmod.physics.common.units.LinearAcceleration;
import com.flansmod.physics.common.units.LinearVelocity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Collection;

public class CommandPhysicsDebug
{
    public static void register(@Nonnull CommandDispatcher<CommandSourceStack> dispatch, @Nonnull CommandBuildContext context)
    {
        dispatch.register(
                Commands.literal("physics")
                        .requires((player) -> player.hasPermission(2))
                        .then(Commands.literal("toggle")
                                .executes((ctx) -> PausePhysicsToggle(ctx.getSource())))
                        .then(Commands.literal("pause")
                                .executes((ctx) -> PausePhysics(ctx.getSource(), true)))
                        .then(Commands.literal("resume")
                                .executes((ctx) -> PausePhysics(ctx.getSource(), false)))
                        .then(Commands.literal("inspect")
                                .executes((ctx) -> DebugInspectCycle(ctx.getSource(), 1))
                                .then(Commands.literal("next")
                                        .executes((ctx) -> DebugInspectCycle(ctx.getSource(), 1)))
                                .then(Commands.literal("prev")
                                        .executes((ctx) -> DebugInspectCycle(ctx.getSource(), -1)))
                                .then(Commands.literal("clear")
                                        .executes((ctx) -> DebugInspectSetIndex(ctx.getSource(), 0)))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                                .executes((ctx) -> DebugInspectSetIndex(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index")))))
                                .then(Commands.literal("closest")
                                        .executes((ctx) -> DebugInspectSetClosest(ctx.getSource())))
                        )
                        .then(Commands.literal("yeet")
                                .executes((ctx) -> DebugYeet(ctx.getSource(), DEFAULT_YEET))
                                .then(Commands.argument("velocity", Vec3Argument.vec3())
                                        .executes((ctx) -> DebugYeet(ctx.getSource(), Vec3Argument.getVec3(ctx, "velocity"))))
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes((ctx) -> DebugYeet(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), DEFAULT_YEET))
                                        .then(Commands.argument("velocity", Vec3Argument.vec3())
                                                .executes((ctx) -> DebugYeet(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), Vec3Argument.getVec3(ctx, "velocity"))))
                                )
                        )
                        .then(Commands.literal("spin")
                                .executes((ctx) -> DebugSpin(ctx.getSource(), DEFAULT_SPIN_AXIS, DEFAULT_SPIN_DEGREES_PER_S))
                                .then(Commands.argument("degrees", DoubleArgumentType.doubleArg())
                                        .executes((ctx) -> DebugSpin(ctx.getSource(), DEFAULT_SPIN_AXIS, DoubleArgumentType.getDouble(ctx, "degrees")))
                                        .then(Commands.argument("axis", Vec3Argument.vec3())
                                                .executes((ctx) -> DebugSpin(ctx.getSource(), Vec3Argument.getVec3(ctx, "axis"), DoubleArgumentType.getDouble(ctx, "degrees"))))
                                )
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes((ctx) -> DebugSpin(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), DEFAULT_SPIN_AXIS, DEFAULT_SPIN_DEGREES_PER_S))
                                        .then(Commands.argument("degrees", DoubleArgumentType.doubleArg())
                                                .executes((ctx) -> DebugSpin(ctx.getSource(), DEFAULT_SPIN_AXIS, DoubleArgumentType.getDouble(ctx, "degrees")))
                                                .then(Commands.argument("axis", Vec3Argument.vec3())
                                                    .executes((ctx) -> DebugSpin(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), Vec3Argument.getVec3(ctx, "axis"), DoubleArgumentType.getDouble(ctx, "degrees"))))
                                        )
                                )
                        )
        );
    }

    private static int PausePhysicsToggle(@Nonnull CommandSourceStack source)
    {
        OBBCollisionSystem.PAUSE_PHYSICS = !OBBCollisionSystem.PAUSE_PHYSICS;
        if(OBBCollisionSystem.PAUSE_PHYSICS)
            source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.physics_pause"), true);
        else
            source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.physics_resume"), true);

        return -1;
    }
    private static int PausePhysics(@Nonnull CommandSourceStack source, boolean pause)
    {
        OBBCollisionSystem.PAUSE_PHYSICS = pause;
        if(pause)
            source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.physics_pause"), true);
        else
            source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.physics_resume"), true);

        return -1;
    }
    private static int DebugInspectCycle(@Nonnull CommandSourceStack source, int delta)
    {
        ColliderHandle newHandle = OBBCollisionSystem.Debug_CycleInspectHandle(source.getLevel(), delta);
        source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.cycle_inspect", newHandle.Handle()), true);
        return -1;
    }
    private static int DebugInspectSetIndex(@Nonnull CommandSourceStack source, int index)
    {
        ColliderHandle newHandle = OBBCollisionSystem.Debug_SetInspectHandleIndex(source.getLevel(), index);
        source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.select_handle", newHandle.Handle(), index, OBBCollisionSystem.Debug_GetNumHandles(source.getLevel())), true);
        return -1;
    }
    private static int DebugInspectSetClosest(@Nonnull CommandSourceStack source)
    {
        ColliderHandle newHandle = OBBCollisionSystem.Debug_SetNearestInspectHandle(source.getLevel(), source.getPosition());
        source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.select_closest", newHandle.Handle()), true);
        return -1;
    }
    private static final Vec3 DEFAULT_YEET = new Vec3(0d, 1d, 0d);
    private static int DebugYeet(@Nonnull CommandSourceStack source, @Nonnull Vec3 yeetVector)
    {
        ColliderHandle handle = OBBCollisionSystem.DEBUG_HANDLE;
        if(handle.IsValid())
        {
            OBBCollisionSystem system = OBBCollisionSystem.ForLevel(source.getLevel());
            LinearAcceleration acc = LinearAcceleration.fromUtoVinTicks(LinearVelocity.Zero, LinearVelocity.blocksPerSecond(yeetVector), 1);
            system.AddLinearAcceleration(handle, acc);
            source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.yeet_single", acc.toFancyString(), handle.Handle()), true);
        }
        return -1;
    }
    private static int DebugYeet(@Nonnull CommandSourceStack source, @Nonnull Collection<? extends Entity> entities, @Nonnull Vec3 yeetVector)
    {
        LinearAcceleration acc = LinearAcceleration.fromUtoVinTicks(LinearVelocity.Zero, LinearVelocity.blocksPerSecond(yeetVector), 1);
        int entityCount = 0;
        for(Entity entity : entities)
        {
            if(entity instanceof PhysicsEntity physicsEntity)
            {
                OBBCollisionSystem system = OBBCollisionSystem.ForLevel(entity.level());
                physicsEntity.forEachPhysicsComponent((component) ->
                {
                    system.AddLinearAcceleration(component.physicsHandle, acc);
                });
                entityCount++;
            }
        }
        final int entCountCopy = entityCount;
        source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.yeet_multiple", acc.toFancyString(), entCountCopy), true);
        return -1;
    }
    private static final Vec3 DEFAULT_SPIN_AXIS = new Vec3(0d, 1d, 0d);
    private static final double DEFAULT_SPIN_DEGREES_PER_S = 30d;
    private static int DebugSpin(@Nonnull CommandSourceStack source, @Nonnull Vec3 spinAxis, double spinSpeed)
    {
        ColliderHandle handle = OBBCollisionSystem.DEBUG_HANDLE;
        if(handle.IsValid())
        {
            OBBCollisionSystem system = OBBCollisionSystem.ForLevel(source.getLevel());
            AngularAcceleration acc = AngularAcceleration.fromUtoVinTicks(AngularVelocity.Zero, AngularVelocity.degreesPerSecond(spinAxis.normalize(), spinSpeed), 1);
            system.AddAngularAcceleration(handle, acc);
            source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.yeet_single", acc.toFancyString(), handle.Handle()), true);
        }
        return -1;
    }
    private static int DebugSpin(@Nonnull CommandSourceStack source, @Nonnull Collection<? extends Entity> entities, @Nonnull Vec3 spinAxis, double spinSpeed)
    {
        AngularAcceleration acc = AngularAcceleration.fromUtoVinTicks(AngularVelocity.Zero, AngularVelocity.degreesPerSecond(spinAxis.normalize(), spinSpeed), 1);
        int entityCount = 0;
        for(Entity entity : entities)
        {
            if(entity instanceof PhysicsEntity physicsEntity)
            {
                OBBCollisionSystem system = OBBCollisionSystem.ForLevel(entity.level());
                physicsEntity.forEachPhysicsComponent((component) ->
                {
                    system.AddAngularAcceleration(component.physicsHandle, acc);
                });
                entityCount++;
            }
        }
        final int entCountCopy = entityCount;
        source.sendSuccess(() -> Component.translatable("flansphysicsmod.command.yeet_multiple", acc.toFancyString(), entCountCopy), true);
        return -1;
    }
}
