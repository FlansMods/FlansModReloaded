package com.flansmod.airtraffic.common;

import com.flansmod.airtraffic.api.ATC;
import com.flansmod.airtraffic.common.airport.AirportBlockEntity;
import com.flansmod.airtraffic.common.runway.RunwayBlockEntity;
import com.flansmod.airtraffic.common.runway.RunwayBlock;
import com.flansmod.airtraffic.network.AirTrafficControlPacketHandler;
import com.flansmod.airtraffic.network.toclient.UpdateRunwayMessage;
import com.flansmod.airtraffic.network.toserver.ConfigureAirportMessage;
import com.flansmod.airtraffic.network.toserver.ConfigureRunwayMessage;
import com.flansmod.airtraffic.server.AirTrafficServer;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

@Mod(AirTrafficControlMod.MODID)
public class AirTrafficControlMod
{
	public static final String MODID = "flans_air_traffic_control";
	public static final Logger LOGGER = LogUtils.getLogger();

	// Registers
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

	public static final RegistryObject<Block> RUNWAY_MARKER_BLOCK = BLOCKS.register("runway", () -> new RunwayBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK)));
	public static final RegistryObject<Item> RUNWAY_MARKER_ITEM = ITEMS.register("runway", () -> new BlockItem(RUNWAY_MARKER_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<BlockEntityType<RunwayBlockEntity>> RUNWAY_MARKER_TILE_ENTITY = TILE_ENTITIES.register("runway",
		() -> BlockEntityType.Builder.of(RunwayBlockEntity::new, RUNWAY_MARKER_BLOCK.get()).build(null));

	public static final RegistryObject<Block> AIRPORT_BLOCK = BLOCKS.register("airport", () -> new RunwayBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK)));
	public static final RegistryObject<Item> AIRPORT_ITEM = ITEMS.register("airport", () -> new BlockItem(AIRPORT_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<BlockEntityType<AirportBlockEntity>> AIRPORT_TILE_ENTITY = TILE_ENTITIES.register("airport",
		() -> BlockEntityType.Builder.of(AirportBlockEntity::new, AIRPORT_BLOCK.get()).build(null));

	public AirTrafficControlMod()
	{
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
		MENUS.register(modEventBus);

		AirTrafficControlPacketHandler.registerMessages();
	}

	@SubscribeEvent
	public void onLevelLoad(@Nonnull LevelEvent.Load event)
	{
		if(!event.getLevel().isClientSide())
			ATC.registerServer(((ServerLevel)event.getLevel()).dimension(), new AirTrafficServer(event.getLevel()));
	}

	@SubscribeEvent
	public void onLevelUnload(@Nonnull LevelEvent.Unload event)
	{
		if(!event.getLevel().isClientSide())
			ATC.invalidateServer(((ServerLevel)event.getLevel()).dimension());
	}


	public static void handleSetAirportName(@Nonnull ConfigureAirportMessage.RenameAirportMessage msg, @Nonnull ServerPlayer from)
	{

	}
	public static void handleSetRunwayName(@Nonnull ConfigureRunwayMessage.RenameRunwayMessage msg, @Nonnull ServerPlayer from)
	{

	}
	public static void handleSetRunwayParameters(@Nonnull ConfigureRunwayMessage.SetRunwayParametersMessage msg, @Nonnull ServerPlayer from)
	{

	}
	public static void handleRequestRunwayValidation(@Nonnull ConfigureRunwayMessage.RequestRunwayValidationMessage msg, @Nonnull ServerPlayer from)
	{

	}
	public static void handleCancelRunwayValidation(@Nonnull ConfigureRunwayMessage.CancelRunwayValidationMessage msg, @Nonnull ServerPlayer from)
	{

	}
}
