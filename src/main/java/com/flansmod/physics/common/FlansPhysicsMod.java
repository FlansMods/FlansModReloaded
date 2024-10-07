package com.flansmod.physics.common;

import com.flansmod.client.render.vehicles.VehicleDebugRenderer;
import com.flansmod.physics.client.PhysicsDebugRenderer;
import com.flansmod.physics.client.TestCubeEntityRenderer;
import com.flansmod.physics.common.collision.OBBCollisionSystem;
import com.flansmod.physics.common.tests.TestCubeEntity;
import com.flansmod.physics.common.util.Transform;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
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
        Transform.RunTests();
        MinecraftForge.EVENT_BUS.register(this);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modEventBus);
    }
    @SubscribeEvent
    public void OnLevelTick(@Nonnull TickEvent.LevelTickEvent levelTick)
    {
        OBBCollisionSystem physics = OBBCollisionSystem.ForLevel(levelTick.level);

        if(levelTick.phase == TickEvent.Phase.START)
            physics.PreTick();
        if(levelTick.phase == TickEvent.Phase.END)
            physics.PhysicsTick();
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
    public static class ClientMod
    {
        public static final PhysicsDebugRenderer PHYSICS_DEBUG_RENDERER = new PhysicsDebugRenderer();

        @SubscribeEvent
        public static void ClientInit(final FMLClientSetupEvent event)
        {
            EntityRenderers.register(ENT_TYPE_TEST_CUBE.get(), TestCubeEntityRenderer::new);
        }
    }

}
