package com.flansmod.physics.common;

import com.flansmod.physics.client.PhysicsDebugRenderer;
import com.flansmod.physics.client.PhysicsKeyMappings;
import com.flansmod.physics.client.TestCubeEntityRenderer;
import com.flansmod.physics.common.collision.ColliderHandle;
import com.flansmod.physics.common.collision.OBBCollisionSystem;
import com.flansmod.physics.common.entity.PhysicsEntity;
import com.flansmod.physics.common.tests.CollisionTests;
import com.flansmod.physics.common.tests.TestCubeEntity;
import com.flansmod.physics.common.tests.TransformTests;
import com.flansmod.physics.network.PhysicsPacketHandler;
import com.flansmod.physics.network.PhysicsSyncMessage;
import com.flansmod.physics.server.command.CommandPhysicsDebug;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

@Mod(FlansPhysicsMod.MODID)
public class FlansPhysicsMod
{
    public static final String MODID = "flansphysics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<TestCubeEntity>> ENT_TYPE_TEST_CUBE = ENTITY_TYPES.register(
            "test_cube",
            () -> EntityType.Builder.of(
                            TestCubeEntity::new,
                            MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .build("test_cube"));

    public FlansPhysicsMod()
    {
        TransformTests.runTests();
        CollisionTests.runTests();
        PhysicsPacketHandler.registerMessages();
        MinecraftForge.EVENT_BUS.register(this);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modEventBus);
    }
    @SubscribeEvent
    public void OnLevelTick(@Nonnull TickEvent.LevelTickEvent levelTick)
    {
        OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(levelTick.level);

        if(levelTick.phase == TickEvent.Phase.START)
            physics.preTick();
        if(levelTick.phase == TickEvent.Phase.END)
            physics.physicsTick();
    }
    @SubscribeEvent
    public void OnRegisterCommands(@Nonnull RegisterCommandsEvent event)
    {
        CommandPhysicsDebug.register(event.getDispatcher(), event.getBuildContext());
    }

    public static void HandlePhysicsSync(@Nonnull PhysicsSyncMessage syncMessage, @Nonnull ServerPlayer player)
    {
        Entity entity = player.level().getEntity(syncMessage.EntityID);
        if (entity instanceof PhysicsEntity physicsEntity)
        {
            if(physicsEntity.getControllingPassenger() == player)
            {
                physicsEntity.handleSyncMessage(syncMessage);
            }
            else
                LOGGER.warn("Player "+player+" tried to send physics packet for server-controlled entity "+entity);
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
    public static class ClientMod
    {
        public static final PhysicsDebugRenderer PHYSICS_DEBUG_RENDERER = new PhysicsDebugRenderer();

        @SubscribeEvent
        public static void ClientInit(final FMLClientSetupEvent event)
        {
            EntityRenderers.register(ENT_TYPE_TEST_CUBE.get(), TestCubeEntityRenderer::new);
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            modEventBus.addListener(ClientMod::OnKeyMappings);
            MinecraftForge.EVENT_BUS.addListener(ClientMod::OnClientTick);
        }

        public static void OnClientTick(@Nonnull TickEvent.ClientTickEvent event)
        {
            if(event.phase == TickEvent.Phase.END)
            {
                while(PhysicsKeyMappings.DEBUG_PAUSE_PHYSICS.get().consumeClick())
                {
                    OBBCollisionSystem.PAUSE_PHYSICS = !OBBCollisionSystem.PAUSE_PHYSICS;
                    if(OBBCollisionSystem.PAUSE_PHYSICS)
                        Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("Phys Pause"), false);
                    else
                        Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("Phys Resume"), false);
                }
                while(PhysicsKeyMappings.DEBUG_INCREASE_PHYSICS_INSPECT.get().consumeClick())
                {
                    CyclePhysDebug(+1);
                }
                while(PhysicsKeyMappings.DEBUG_DECREASE_PHYSICS_INSPECT.get().consumeClick())
                {
                    CyclePhysDebug(-1);
                }

            }
        }

        public static void OnKeyMappings(@Nonnull RegisterKeyMappingsEvent event)
        {
            event.register(PhysicsKeyMappings.DEBUG_INCREASE_PHYSICS_INSPECT.get());
            event.register(PhysicsKeyMappings.DEBUG_DECREASE_PHYSICS_INSPECT.get());
        }

        private static void CyclePhysDebug(int delta)
        {
            if(Minecraft.getInstance().level != null)
            {
                ColliderHandle handle = OBBCollisionSystem.Debug_CycleInspectHandle(Minecraft.getInstance().level, delta);
                int numHandles = OBBCollisionSystem.Debug_GetNumHandles(Minecraft.getInstance().level);
                if(handle.Handle() == 0L)
                    Minecraft.getInstance().getChatListener().handleSystemMessage(Component.translatable("No Debug"), false);
                else
                    Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal("Debug handle:"+handle.Handle()+"/"+numHandles), false);
            }
        }

        public static void HandlePhysicsSync(@Nonnull PhysicsSyncMessage syncMessage)
        {
            if(Minecraft.getInstance().level != null)
            {
                Entity entity = Minecraft.getInstance().level.getEntity(syncMessage.EntityID);
                if (entity instanceof PhysicsEntity physicsEntity)
                {
                    physicsEntity.handleSyncMessage(syncMessage);
                }
            }
        }
    }

}
