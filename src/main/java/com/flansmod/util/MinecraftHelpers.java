package com.flansmod.util;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.elements.ItemStackDefinition;
import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MinecraftHelpers
{
	@Nullable
	public static Level GetLevel(ResourceKey<Level> dimension)
	{
		// Try getting from the running server
		MinecraftServer server = GetServer();
		if(server != null && server.isRunning())
		{
			return server.getLevel(dimension);
		}

		// Failing that, there is a chance this is our current loaded client level
		Level clientLevel = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> MinecraftHelpers::Client_GetCurrentLevel);
		if(clientLevel.dimension().equals(dimension))
			return clientLevel;

		return null;
	}
	@Nonnull
	public static Iterable<? extends Level> GetLoadedLevels()
	{
		// Try getting from the running server
		MinecraftServer server = GetServer();
		if(server != null && server.isRunning())
		{
			return server.getAllLevels();
		}

		// If not on a server, return the one loaded level
		Level clientLevel = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> MinecraftHelpers::Client_GetCurrentLevel);
		List<Level> list = new ArrayList<>(1);
		list.add(clientLevel);
		return list;
	}

	public static boolean TagEqual(Tag tag, String stringValue)
	{
		if(tag instanceof IntTag intTag)
		{
			return intTag.getAsInt() == Integer.parseInt(stringValue);
		}
		else if(tag instanceof FloatTag floatTag)
		{
			return floatTag.getAsFloat() == Float.parseFloat(stringValue);
		}
		else if(tag instanceof StringTag stringTag)
		{
			return stringTag.getAsString().equals(stringValue);
		}
		else if(tag instanceof DoubleTag doubleTag)
		{
			return doubleTag.getAsDouble() == Double.parseDouble(stringValue);
		}
		else
		{
			FlansMod.LOGGER.warn("Unknown tag type in ingredient");
		}
		return true;
	}

	public static long GetTick()
	{
		if(IsClient())
		{
			Level level = Client_GetCurrentLevel();
			if(level != null)
				return level.getGameTime();
		}
		else
		{
			MinecraftServer server = GetServer();
			if(server != null && server.isRunning())
			{
				Level level = server.getLevel(Level.OVERWORLD);
				if(level != null)
					return level.getGameTime();
			}
		}
		return 0L;
	}

	public static String GetFEString(int fe)
	{
		if(fe >= 1000000000)
			return String.format("%.2f GFE", fe / 1000000000f);
		if(fe >= 1000000)
			return String.format("%.2f MFE", fe / 1000000f);
		if(fe >= 1000)
			return String.format("%.2f KFE", fe / 1000f);
		return fe + " FE";
	}

	public static boolean IsClient()
	{
		return FMLEnvironment.dist == Dist.CLIENT;
	}

	@Nullable
	public static MinecraftServer GetServer()
	{
		return ServerLifecycleHooks.getCurrentServer();
	}

	@OnlyIn(Dist.CLIENT)
	public static HumanoidArm GetArm(InteractionHand hand)
	{
		return hand == InteractionHand.MAIN_HAND ?
			Minecraft.getInstance().options.mainHand().get() :
			Minecraft.getInstance().options.mainHand().get().getOpposite();
	}

	@OnlyIn(Dist.CLIENT)
	public static InteractionHand GetHand(ItemTransforms.TransformType transformType)
	{
		boolean rightHanded = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
		return switch (transformType)
		{
			case FIRST_PERSON_LEFT_HAND -> rightHanded ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
			case FIRST_PERSON_RIGHT_HAND -> rightHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			case THIRD_PERSON_LEFT_HAND -> InteractionHand.OFF_HAND;
			default -> InteractionHand.MAIN_HAND;
		};
	}

	@OnlyIn(Dist.CLIENT)
	public static Entity GetCamera() { return Minecraft.getInstance().cameraEntity; }

	@OnlyIn(Dist.CLIENT)
	public static Minecraft GetClient()
	{
		return Minecraft.getInstance();
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	public static Level Client_GetCurrentLevel()
	{
		return GetClient().level;
	}


	public static Material FindMaterial(String name)
	{
		switch (name.toUpperCase(Locale.ROOT))
		{
			case "AIR" -> 								{ return Material.AIR; }
			case "STRUCTURALAIR" -> 					{ return Material.STRUCTURAL_AIR; }
			case "PORTAL" -> 							{ return Material.PORTAL; }
			case "CLOTHDECORATION" -> 					{ return Material.CLOTH_DECORATION; }
			case "PLANT" -> 							{ return Material.PLANT; }
			case "WATERPLANT" -> 						{ return Material.WATER_PLANT; }
			case "REPLACEABLEPLANT" -> 					{ return Material.REPLACEABLE_PLANT; }
			case "REPLACEABLEFIREPROOFPLANT" -> 		{ return Material.REPLACEABLE_FIREPROOF_PLANT; }
			case "REPLACEABLEWATERPLANT" -> 			{ return Material.REPLACEABLE_WATER_PLANT; }
			case "WATER" -> 							{ return Material.WATER; }
			case "BUBBLECOLUMN" -> 						{ return Material.BUBBLE_COLUMN; }
			case "LAVA" -> 								{ return Material.LAVA; }
			case "TOPSNOW" -> 							{ return Material.TOP_SNOW; }
			case "FIRE" -> 								{ return Material.FIRE; }
			case "DECORATION" -> 						{ return Material.DECORATION; }
			case "WEB" -> 								{ return Material.WEB; }
			case "SCULK" -> 							{ return Material.SCULK; }
			case "BUILDABLEGLASS" -> 					{ return Material.BUILDABLE_GLASS; }
			case "CLAY" -> 								{ return Material.CLAY; }
			case "DIRT" -> 								{ return Material.DIRT; }
			case "GRASS" -> 							{ return Material.GRASS; }
			case "ICESOLID" -> 							{ return Material.ICE_SOLID; }
			case "SAND" -> 								{ return Material.SAND; }
			case "SPONGE" -> 							{ return Material.SPONGE; }
			case "SHULKERSHELL" -> 						{ return Material.SHULKER_SHELL; }
			case "WOOD" -> 								{ return Material.WOOD; }
			case "NETHERWOOD" -> 						{ return Material.NETHER_WOOD; }
			case "BAMBOOSAPLING" -> 					{ return Material.BAMBOO_SAPLING; }
			case "BAMBOO" -> 							{ return Material.BAMBOO; }
			case "WOOL" -> 								{ return Material.WOOL; }
			case "EXPLOSIVE" -> 						{ return Material.EXPLOSIVE; }
			case "LEAVES" -> 							{ return Material.LEAVES; }
			case "GLASS" -> 							{ return Material.GLASS; }
			case "ICE" -> 								{ return Material.ICE; }
			case "CACTUS" -> 							{ return Material.CACTUS; }
			case "STONE" -> 							{ return Material.STONE; }
			case "METAL" -> 							{ return Material.METAL; }
			case "SNOW" -> 								{ return Material.SNOW; }
			case "HEAVYMETAL" -> 						{ return Material.HEAVY_METAL; }
			case "BARRIER" -> 							{ return Material.BARRIER; }
			case "PISTON" -> 							{ return Material.PISTON; }
			case "MOSS" -> 								{ return Material.MOSS; }
			case "VEGETABLE" -> 						{ return Material.VEGETABLE; }
			case "EGG" -> 								{ return Material.EGG; }
			case "CAKE" -> 								{ return Material.CAKE; }
			case "AMETHYST" -> 							{ return Material.AMETHYST; }
			case "POWDERSNOW" -> 						{ return Material.POWDER_SNOW; }
			case "FROGSPAWN" -> 						{ return Material.FROGSPAWN; }
			case "FROGLIGHT" -> 						{ return Material.FROGLIGHT; }

			default -> 									{ return Material.AIR; }
		}
	}

	public static ResourceLocation CreateLocation(String locString)
	{
		return ResourceLocation.tryParse(locString);
	}

	public static ItemStack CreateStack(ItemStackDefinition def)
	{
		if(def.tags != null && def.tags.length() > 0)
		{
			try
			{
				CompoundTag tags = new TagParser(new StringReader(def.tags)).readStruct();
				return new ItemStack(ForgeRegistries.ITEMS.getValue(CreateLocation(def.item)), def.count, tags);
			}
			catch(Exception e)
			{
				FlansMod.LOGGER.error("Could not parse " + def.tags);
			}
		}
		return new ItemStack(ForgeRegistries.ITEMS.getValue(CreateLocation(def.item)), def.count);
	}
}
