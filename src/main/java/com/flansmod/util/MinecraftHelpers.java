package com.flansmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
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

	public static boolean IsClient()
	{
		Boolean isClientOrNull = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> MinecraftHelpers::True );
		return isClientOrNull != null;
	}

	public static Boolean True() { return true; }

	@Nullable
	private static MinecraftServer GetServer()
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
}
