package com.flansmod.packs.clockwork;

import com.flansmod.common.FlansMod;
import com.flansmod.packs.clockwork.client.ChipModel;
import com.flansmod.packs.clockwork.client.ChipRenderer;
import com.flansmod.packs.clockwork.client.SadieModel;
import com.flansmod.packs.clockwork.client.SadieRenderer;
import com.flansmod.packs.clockwork.common.ChipEntity;
import com.flansmod.packs.clockwork.common.SadieEntity;
import com.flansmod.packs.vendersgame.client.VenderModel;
import com.flansmod.packs.vendersgame.client.VenderRenderer;
import com.flansmod.packs.vendersgame.common.VenderEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(ClockworkCalamityMod.MODID)
public class ClockworkCalamityMod
{
	public static final String MODID = "flansclockworkcalamity";
	private static final Logger LOGGER = LogUtils.getLogger();

	// Item registration
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);


	// Clockwork Guns
	public static final RegistryObject<Item> GUN_CLOCKWORK_OVERKILL = 						FlansMod.Gun(ITEMS, MODID, "clockwork_overkill");




	// Sadie NPC
	public static final RegistryObject<EntityType<SadieEntity>> ENTITY_TYPE_SADIE = ENTITY_TYPES.register(
		"sadie",
		() -> EntityType.Builder.of(
				SadieEntity::new,
				MobCategory.CREATURE)
			.sized(0.8f, 2.0f)
			.build("sadie"));
	// Chip NPC
	public static final RegistryObject<EntityType<ChipEntity>> ENTITY_TYPE_CHIP = ENTITY_TYPES.register(
		"chip",
		() -> EntityType.Builder.of(
				ChipEntity::new,
				MobCategory.CREATURE)
			.sized(0.85f, 1.8f)
			.build("chip"));


	public ClockworkCalamityMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		BLOCKS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
		ENTITY_TYPES.register(modEventBus);
		modEventBus.register(this);
	}

	@SubscribeEvent
	public void SupplyAttributes(EntityAttributeCreationEvent event)
	{
		event.put(ENTITY_TYPE_SADIE.get(), SadieEntity.createAttributes().build());
		event.put(ENTITY_TYPE_CHIP.get(), ChipEntity.createAttributes().build());
	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
	public static class ClientMod
	{
		@SubscribeEvent
		public static void RegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
		{
			event.registerLayerDefinition(SadieModel.MODEL_LAYER_LOCATION, SadieModel::createBodyLayer);
			event.registerLayerDefinition(ChipModel.MODEL_LAYER_LOCATION, ChipModel::createBodyLayer);
		}

		@SubscribeEvent
		public static void ModelRegistryEvent(ModelEvent.RegisterAdditional event)
		{
			ItemModelShaper shaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();

			for (var entry : ITEMS.getEntries())
			{
				event.register(new ModelResourceLocation(MODID, entry.getId().getPath() + "_inventory", "inventory"));
				shaper.register(entry.get(), new ModelResourceLocation(MODID, entry.getId().getPath() + "_inventory", "inventory"));
			}
		}

		@SubscribeEvent
		public static void EntityRenderEvent(EntityRenderersEvent.RegisterRenderers event)
		{
			event.registerEntityRenderer(ENTITY_TYPE_CHIP.get(), ChipRenderer::new);
			event.registerEntityRenderer(ENTITY_TYPE_SADIE.get(), SadieRenderer::new);
		}
	}
}
