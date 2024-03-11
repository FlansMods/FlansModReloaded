package com.flansmod.util;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.EContextSide;
import com.flansmod.common.types.elements.ItemStackDefinition;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.types.templates.Check;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.MixinEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

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
		if(IsClient())
		{
			Level clientLevel = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> MinecraftHelpers::Client_GetCurrentLevel);
			if (clientLevel.dimension().equals(dimension))
				return clientLevel;
		}

		return null;
	}
	@Nonnull
	public static Iterable<? extends Level> Server_GetLoadedLevels()
	{
		// Try getting from the running server
		MinecraftServer server = GetServer();
		if (server != null && server.isRunning())
		{
			return server.getAllLevels();
		}
		return new ArrayList<>();
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

	@Nonnull
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

	@Nonnull
	public static EContextSide GetLogicalSide(@Nonnull Entity entity)
	{
		return GetLogicalSide(entity.level());
	}
	@Nonnull
	public static EContextSide GetLogicalSide(@Nonnull Level level)
	{
		return level.isClientSide ? EContextSide.Client : EContextSide.Server;
	}
	@Nonnull
	public static EContextSide GetLogicalSide()
	{
		MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
		if(currentServer != null)
			if(Thread.currentThread() == currentServer.getRunningThread())
				return EContextSide.Server;
		if(IsClient())
			return EContextSide.Client;
		return EContextSide.Unknown;
	}

	@Nullable
	public static MinecraftServer GetServer()
	{
		return ServerLifecycleHooks.getCurrentServer();
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static HumanoidArm GetArm(@Nonnull InteractionHand hand)
	{
		return hand == InteractionHand.MAIN_HAND ?
			Minecraft.getInstance().options.mainHand().get() :
			Minecraft.getInstance().options.mainHand().get().getOpposite();
	}
	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static HumanoidArm GetArm(@Nonnull ItemDisplayContext hand)
	{
		return switch(hand) {
			case FIRST_PERSON_LEFT_HAND, THIRD_PERSON_LEFT_HAND -> HumanoidArm.LEFT;
			default -> HumanoidArm.RIGHT;
		};
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	public static InteractionHand GetHand(@Nullable ItemDisplayContext transformType)
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
	public static ItemDisplayContext GetFirstPersonTransformType(@Nonnull InteractionHand hand)
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
	public static ItemDisplayContext GetThirdPersonTransformType(boolean isLocalPlayer, @Nonnull InteractionHand hand)
	{
		boolean rightHanded = !isLocalPlayer || Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
		return switch (hand)
			{
				case MAIN_HAND -> rightHanded ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
				case OFF_HAND -> rightHanded ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
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

	@Nonnull
	public static TagKey<Block> FindBlockTag(String location)
	{
		ResourceLocation resLoc = new ResourceLocation(location);
		return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), resLoc);
	}
}
