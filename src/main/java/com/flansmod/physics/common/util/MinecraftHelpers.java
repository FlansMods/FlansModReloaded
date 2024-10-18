package com.flansmod.physics.common.util;

import com.flansmod.physics.common.FlansPhysicsMod;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class MinecraftHelpers
{
	@Nullable
	public static Level getLevel(@Nonnull ResourceKey<Level> dimension)
	{
		if(isClientThread())
		{
			// Failing that, there is a chance this is our current loaded client level
			if(isClientDist())
			{
				Level clientLevel = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> MinecraftHelpers::clientGetCurrentLevel);
				if (clientLevel.dimension().equals(dimension))
					return clientLevel;
				else
				{
                    FlansPhysicsMod.LOGGER.warn("Tried to access non-loaded client dimension: {}", dimension);
					return null;
				}
			}
			else
			{
				FlansPhysicsMod.LOGGER.error("How are we on a client thread outside of a client dist???");
				return null;
			}
		}

		// Try getting from the running server
		if(!isServerThread())
		{
			FlansPhysicsMod.LOGGER.error("What thread are you trying to get the level on?");
		}

		MinecraftServer server = getServer();
		if (server != null && server.isRunning())
		{
			return server.getLevel(dimension);
		}

		return null;
	}
	@Nonnull
	public static Iterable<? extends Level> serverGetLoadedLevels()
	{
		// Try getting from the running server
		MinecraftServer server = getServer();
		if (server != null && server.isRunning())
		{
			return server.getAllLevels();
		}
		return new ArrayList<>();
	}

	public static boolean tagEqual(@Nonnull Tag tag, @Nonnull String stringValue)
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
			FlansPhysicsMod.LOGGER.warn("Unknown tag type in ingredient");
		}
		return true;
	}

	public static long getTick()
	{
		if(isClientDist())
		{
			Level level = clientGetCurrentLevel();
			if(level != null)
				return level.getGameTime();
		}
		else
		{
			MinecraftServer server = getServer();
			if(server != null && server.isRunning())
			{
				Level level = server.getLevel(Level.OVERWORLD);
				if(level != null)
					return level.getGameTime();
			}
		}
		return 0L;
	}

	@Nonnull
	public static String getFEString(int fe)
	{
		if(fe >= 1000000000)
			return String.format("%.2f GFE", fe / 1000000000f);
		if(fe >= 1000000)
			return String.format("%.2f MFE", fe / 1000000f);
		if(fe >= 1000)
			return String.format("%.2f KFE", fe / 1000f);
		return fe + " FE";
	}

	public static boolean isClientDist()
	{
		return FMLEnvironment.dist == Dist.CLIENT;
	}

	public static boolean isClientThread() { return getLogicalSide() == EContextSide.Client; }
	public static boolean isServerThread() { return getLogicalSide() == EContextSide.Server; }

	@Nonnull
	public static EContextSide getLogicalSide(@Nonnull Entity entity)
	{
		return getLogicalSide(entity.level());
	}
	@Nonnull
	public static EContextSide getLogicalSide(@Nonnull Level level)
	{
		return level.isClientSide ? EContextSide.Client : EContextSide.Server;
	}
	@Nonnull
	public static EContextSide getLogicalSide()
	{
		MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
		if(currentServer != null)
			if(Thread.currentThread() == currentServer.getRunningThread())
				return EContextSide.Server;
		if(isClientDist())
			return EContextSide.Client;
		return EContextSide.Unknown;
	}

	@Nullable
	public static MinecraftServer getServer()
	{
		return ServerLifecycleHooks.getCurrentServer();
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static HumanoidArm getArm(@Nonnull InteractionHand hand)
	{
		return hand == InteractionHand.MAIN_HAND ?
			Minecraft.getInstance().options.mainHand().get() :
			Minecraft.getInstance().options.mainHand().get().getOpposite();
	}
	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static HumanoidArm getArm(@Nonnull ItemDisplayContext hand)
	{
		return switch(hand) {
			case FIRST_PERSON_LEFT_HAND, THIRD_PERSON_LEFT_HAND -> HumanoidArm.LEFT;
			default -> HumanoidArm.RIGHT;
		};
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static InteractionHand getHand(@Nullable ItemDisplayContext transformType)
	{
		boolean rightHanded = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
		if(transformType == null)
			return InteractionHand.MAIN_HAND;
		return switch (transformType)
		{
			case FIRST_PERSON_LEFT_HAND -> rightHanded ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
			case FIRST_PERSON_RIGHT_HAND -> rightHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			case THIRD_PERSON_LEFT_HAND -> InteractionHand.OFF_HAND;
			default -> InteractionHand.MAIN_HAND;
		};
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ItemDisplayContext getFirstPersonTransformType(@Nonnull InteractionHand hand)
	{
		boolean rightHanded = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
		return switch (hand)
		{
			case MAIN_HAND -> rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
			case OFF_HAND -> rightHanded ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
		};
	}
	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static ItemDisplayContext getThirdPersonTransformType(boolean isLocalPlayer, @Nonnull InteractionHand hand)
	{
		boolean rightHanded = !isLocalPlayer || Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
		return switch (hand)
			{
				case MAIN_HAND -> rightHanded ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
				case OFF_HAND -> rightHanded ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
			};
	}

	@OnlyIn(Dist.CLIENT)
	public static Entity getCamera() { return Minecraft.getInstance().cameraEntity; }

	@OnlyIn(Dist.CLIENT)
	public static Minecraft getClient()
	{
		return Minecraft.getInstance();
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	public static Level clientGetCurrentLevel()
	{
		return getClient().level;
	}

	@Nullable
	public static ResourceLocation createLocation(@Nonnull String locString)
	{
		return ResourceLocation.tryParse(locString);
	}
	@Nonnull
	public static TagKey<Block> findBlockTag(@Nonnull String location)
	{
		ResourceLocation resLoc = new ResourceLocation(location);
		return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), resLoc);
	}
}
