package com.flansmod.packs.vendersgame;

import com.flansmod.common.FlansMod;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.item.AttachmentItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.packs.vendersgame.client.VenderModel;
import com.flansmod.packs.vendersgame.client.VenderRenderer;
import com.flansmod.packs.vendersgame.common.VenderEntity;
import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Mod(VendersGameMod.MODID)
public class VendersGameMod
{
	public static final String MODID = "flansvendersgame";
	private static final Logger LOGGER = LogUtils.getLogger();

	// Item registration
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

	// Flint & Cobblesons Premium Guns
	public static final RegistryObject<Item> GUN_FC_700 = 						FlansMod.Gun(ITEMS, MODID, "fc_700");
	public static final RegistryObject<Item> GUN_FC_870 = 						FlansMod.Gun(ITEMS, MODID, "fc_870");
	public static final RegistryObject<Item> GUN_FC_HAWK = 						FlansMod.Gun(ITEMS, MODID, "fc_hawk");
	public static final RegistryObject<Item> GUN_FC_PUNCHER = 					FlansMod.Gun(ITEMS, MODID, "fc_puncher");
	public static final RegistryObject<Item> GUN_FC_FAR = 						FlansMod.Gun(ITEMS, MODID, "fc_far");
	public static final RegistryObject<Item> GUN_FC_49 = 						FlansMod.Gun(ITEMS, MODID, "fc_49");
	public static final RegistryObject<Item> GUN_FC_56 = 						FlansMod.Gun(ITEMS, MODID, "fc_56");

	public static final RegistryObject<Block> WORKBENCH_BLOCK_FC = 				FlansMod.Workbench_Block(BLOCKS, MODID, "fc_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_FC = 				FlansMod.Workbench_Item(ITEMS, MODID, "fc_workbench", WORKBENCH_BLOCK_FC);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_FC =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "fc_workbench");

	public static final RegistryObject<Item> ATTACHMENT_FC_DELUXE_COMPENSATOR = FlansMod.Attachment(ITEMS, MODID, "fc_deluxe_compensator");
	public static final RegistryObject<Item> ATTACHMENT_FC_DELUXE_FOREGRIP =	FlansMod.Attachment(ITEMS, MODID, "fc_deluxe_foregrip");
	public static final RegistryObject<Item> ATTACHMENT_FC_PREMIUM_FOREGRIP = 	FlansMod.Attachment(ITEMS, MODID, "fc_premium_foregrip");
	public static final RegistryObject<Item> ATTACHMENT_FC_DIAMOND_SIGHTS = 	FlansMod.Attachment(ITEMS, MODID, "fc_diamond_sights");
	public static final RegistryObject<Item> ATTACHMENT_FC_AGED_OAK_STOCK = 	FlansMod.Attachment(ITEMS, MODID, "fc_aged_oak_stock");

	// HitMarket Budget Weaponry
	public static final RegistryObject<Item> GUN_HM_9 = 						FlansMod.Gun(ITEMS, MODID, "hm_9");
	public static final RegistryObject<Item> GUN_HM_14 = 						FlansMod.Gun(ITEMS, MODID, "hm_14");
	public static final RegistryObject<Item> GUN_HM_16 = 						FlansMod.Gun(ITEMS, MODID, "hm_16");
	public static final RegistryObject<Item> GUN_HM_1200 = 						FlansMod.Gun(ITEMS, MODID, "hm_1200");
	public static final RegistryObject<Item> GUN_HM_COMBINE = 					FlansMod.Gun(ITEMS, MODID, "hm_combine");
	public static final RegistryObject<Item> GUN_HM_POCKET = 					FlansMod.Gun(ITEMS, MODID, "hm_pocket");
	public static final RegistryObject<Item> GUN_HM_KRANK = 					FlansMod.Gun(ITEMS, MODID, "hm_krank");

	public static final RegistryObject<Block> WORKBENCH_BLOCK_HM = 				FlansMod.Workbench_Block(BLOCKS, MODID, "hm_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_HM = 				FlansMod.Workbench_Item(ITEMS, MODID, "hm_workbench", WORKBENCH_BLOCK_HM);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_HM =
																				FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "hm_workbench");

	public static final RegistryObject<Item> ATTACHMENT_HM_FLASHLIGHT = 		FlansMod.Attachment(ITEMS, MODID, "hm_flashlight");
	public static final RegistryObject<Item> ATTACHMENT_HM_FLASHLIGHT_MINI =	FlansMod.Attachment(ITEMS, MODID, "hm_flashlight_mini");
	public static final RegistryObject<Item> ATTACHMENT_HM_PINPOINT_SIGHT = 	FlansMod.Attachment(ITEMS, MODID, "hm_pinpoint_sight");
	public static final RegistryObject<Item> ATTACHMENT_HM_POCKET_SILENCER = 	FlansMod.Attachment(ITEMS, MODID, "hm_pocket_silencer");
	public static final RegistryObject<Item> ATTACHMENT_HM_PRECISION_BARREL = 	FlansMod.Attachment(ITEMS, MODID, "hm_precision_barrel");
	public static final RegistryObject<Item> ATTACHMENT_HM_RED_DOT =		 	FlansMod.Attachment(ITEMS, MODID, "hm_red_dot");
	public static final RegistryObject<Item> ATTACHMENT_HM_STEADY_FOREGRIP = 	FlansMod.Attachment(ITEMS, MODID, "hm_steady_foregrip");
	public static final RegistryObject<Item> ATTACHMENT_HM_SNAP_STOCK = 		FlansMod.Attachment(ITEMS, MODID, "hm_snap_stock");

	// PhanTek Advanced Guns
	public static final RegistryObject<Item> GUN_PHANTEK_90 = 					FlansMod.Gun(ITEMS, MODID, "phantek_90");
	public static final RegistryObject<Item> GUN_PHANTEK_AUTO_12 = 				FlansMod.Gun(ITEMS, MODID, "phantek_auto_12");
	public static final RegistryObject<Item> GUN_PHANTEK_EYE = 					FlansMod.Gun(ITEMS, MODID, "phantek_eye");
	public static final RegistryObject<Item> GUN_PHANTEK_FURY = 				FlansMod.Gun(ITEMS, MODID, "phantek_fury");
	public static final RegistryObject<Item> GUN_PHANTEK_LOCK = 				FlansMod.Gun(ITEMS, MODID, "phantek_lock");
	public static final RegistryObject<Item> GUN_PHANTEK_SEEKER = 				FlansMod.Gun(ITEMS, MODID, "phantek_seeker");
	public static final RegistryObject<Item> GUN_PHANTEK_BATTLEMAGE =			FlansMod.Gun(ITEMS, MODID, "phantek_battlemage");

	public static final RegistryObject<Block> WORKBENCH_BLOCK_PHANTEK = 		FlansMod.Workbench_Block(BLOCKS, MODID, "phantek_workbench");
	public static final RegistryObject<Item> WORKBENCH_ITEM_PHANTEK = 			FlansMod.Workbench_Item(ITEMS, MODID, "phantek_workbench", WORKBENCH_BLOCK_PHANTEK);
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_TILE_ENTITY_PHANTEK =
		FlansMod.Workbench_TileEntityType(TILE_ENTITIES, MODID, "phantek_workbench");

	public static final RegistryObject<Item> ATTACHMENT_PHANTEK_4X_OPTIC = 		FlansMod.Attachment(ITEMS, MODID, "phantek_4x_optic");
	public static final RegistryObject<Item> ATTACHMENT_PHANTEK_SHOTGUN_SILENCER = FlansMod.Attachment(ITEMS, MODID, "phantek_shotgun_silencer");
	public static final RegistryObject<Item> ATTACHMENT_PHANTEK_SILENCER = 		FlansMod.Attachment(ITEMS, MODID, "phantek_silencer");
	public static final RegistryObject<Item> ATTACHMENT_PHANTEK_IMPACT_STOCK = 	FlansMod.Attachment(ITEMS, MODID, "phantek_impact_stock");

	// Vender Customs and Rare Items
	public static final RegistryObject<Item> GUN_VENDERS_CUSTOMS_VIPER =		FlansMod.Gun(ITEMS, MODID, "venders_customs_viper");
	public static final RegistryObject<Item> GUN_VENDERS_CUSTOMS_MARTINI =		FlansMod.Gun(ITEMS, MODID, "venders_martini");
	public static final RegistryObject<Item> ATTACHMENT_DRAGONS_BREATH_INFUSER =FlansMod.Attachment(ITEMS, MODID, "dragons_breath_infuser");
	public static final RegistryObject<Item> ATTACHMENT_TAKE_YOUR_LIFE_IN_YOUR_HANDS = FlansMod.Attachment(ITEMS, MODID, "take_your_life_in_your_hands");
	public static final RegistryObject<Item> TOOL_VENDERS_RADIO =				FlansMod.Tool(ITEMS, MODID, "venders_radio");

	// Chest Loot
	public static final RegistryObject<Codec<ChestLootModifier>> DUNGEON_LOOT = LOOT_MODIFIERS.register("chest_loot", ChestLootModifier.CODEC);

	// Vender NPC
	public static final RegistryObject<EntityType<VenderEntity>> ENTITY_TYPE_VENDER = ENTITY_TYPES.register(
		"vender",
		() -> EntityType.Builder.of(
				VenderEntity::new,
				MobCategory.CREATURE)
			.sized(0.8f, 1.65f)
			.build("vender"));


	public VendersGameMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		BLOCKS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
		ENTITY_TYPES.register(modEventBus);
		LOOT_MODIFIERS.register(modEventBus);
		modEventBus.register(this);
	}

	@SubscribeEvent
	public void SupplyAttributes(EntityAttributeCreationEvent event)
	{
		event.put(ENTITY_TYPE_VENDER.get(), VenderEntity.createAttributes().build());
	}

	@SubscribeEvent
	public void OnCreativeTabs(CreativeModeTabEvent.BuildContents event)
	{
		if (event.getTab().getDisplayName() == FlansMod.CREATIVE_TAB_NAME_GUNS)
		{
			event.accept(WORKBENCH_ITEM_HM);
			event.accept(WORKBENCH_ITEM_PHANTEK);
			event.accept(WORKBENCH_ITEM_FC);
		}
	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
	public static class ClientMod
	{
		@SubscribeEvent
		public static void RegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
		{
			event.registerLayerDefinition(VenderModel.MODEL_LAYER_LOCATION, VenderModel::createBodyLayer);
		}

		@SubscribeEvent
		public static void ModelRegistryEvent(ModelEvent.RegisterAdditional event)
		{
			ItemModelShaper shaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();

			for (var entry : ITEMS.getEntries())
			{
				if(entry.get() instanceof GunItem || entry.get() instanceof AttachmentItem)
				{
					event.register(new ModelResourceLocation(MODID, entry.getId().getPath() + "", "inventory"));
					shaper.register(entry.get(), new ModelResourceLocation(MODID, entry.getId().getPath() + "", "inventory"));
				}
				else
				{
					shaper.register(entry.get(), new ModelResourceLocation(entry.getId(), "inventory"));
				}
			}
		}

		@SubscribeEvent
		public static void EntityRenderEvent(EntityRenderersEvent.RegisterRenderers event)
		{
			event.registerEntityRenderer(ENTITY_TYPE_VENDER.get(), VenderRenderer::new);
		}
	}

	private static class ChestLootModifier extends LootModifier
	{
		public static final Supplier<Codec<ChestLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst)
			.and(ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("radio_chance", 0.25f).forGetter(m -> m.radioChance))
			.apply(inst, ChestLootModifier::new)
		));

		private final float radioChance;

		public ChestLootModifier(final LootItemCondition[] conditionsIn, final float radioChance) {
			super(conditionsIn);
			this.radioChance = radioChance;
		}

		@Override
		@Nonnull
		protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
		{
			if(context.getRandom().nextFloat() < radioChance)
				generatedLoot.add(new ItemStack(TOOL_VENDERS_RADIO.get()));

			return generatedLoot;
		}

		@Override
		public Codec<? extends IGlobalLootModifier> codec() {
			return CODEC.get();
		}
	}
}
