package com.flansmod.airtraffic.network;

import com.flansmod.airtraffic.client.AirTrafficControlModClient;
import com.flansmod.airtraffic.common.AirTrafficControlMod;
import com.flansmod.airtraffic.network.toclient.UpdateRunwayMessage;
import com.flansmod.airtraffic.network.toserver.ConfigureAirportMessage;
import com.flansmod.airtraffic.network.toserver.ConfigureRunwayMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AirTrafficControlPacketHandler
{
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(AirTrafficControlMod.MODID, "main"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals
	);
	private static int NextMessageID = 0;

	public static void registerMessages()
	{
		registerClientHandler(
			UpdateRunwayMessage.UpdateRunwayNameAndID.class,
			UpdateRunwayMessage.UpdateRunwayNameAndID::new,
			() -> AirTrafficControlModClient::handleRunwayNameAndID);
		registerClientHandler(
			UpdateRunwayMessage.UpdateRunwayValidationProgress.class,
			UpdateRunwayMessage.UpdateRunwayValidationProgress::new,
			() -> AirTrafficControlModClient::handleRunwayValidationProgress);
		registerClientHandler(
			UpdateRunwayMessage.CompletedRunwayValidation.class,
			UpdateRunwayMessage.CompletedRunwayValidation::new,
			() -> AirTrafficControlModClient::handleCompletedRunwayValidation);



		registerServerHandler(
			ConfigureAirportMessage.RenameAirportMessage.class,
			ConfigureAirportMessage.RenameAirportMessage::new,
			AirTrafficControlMod::handleSetAirportName);
		registerServerHandler(
			ConfigureRunwayMessage.RenameRunwayMessage.class,
			ConfigureRunwayMessage.RenameRunwayMessage::new,
			AirTrafficControlMod::handleSetRunwayName);
		registerServerHandler(
			ConfigureRunwayMessage.SetRunwayParametersMessage.class,
			ConfigureRunwayMessage.SetRunwayParametersMessage::new,
			AirTrafficControlMod::handleSetRunwayParameters);
		registerServerHandler(
			ConfigureRunwayMessage.RequestRunwayValidationMessage.class,
			ConfigureRunwayMessage.RequestRunwayValidationMessage::new,
			AirTrafficControlMod::handleRequestRunwayValidation);
		registerServerHandler(
			ConfigureRunwayMessage.CancelRunwayValidationMessage.class,
			ConfigureRunwayMessage.CancelRunwayValidationMessage::new,
			AirTrafficControlMod::handleCancelRunwayValidation);
	}
	public interface Factory<TMessage>
	{
		TMessage Create();
	}

	public static <TMessage extends AirTrafficMessage> void registerServerHandler(
		@Nonnull Class<TMessage> clazz,
		@Nonnull Factory<TMessage> factory,
		@Nonnull BiConsumer<TMessage, ServerPlayer> handler)
	{
		INSTANCE.registerMessage(
			NextMessageID,
			clazz,
			AirTrafficMessage::encode,
			(buf) ->
			{
				TMessage msg = factory.Create();
				msg.decode(buf);
				return msg;
			},
			(msg, ctx) ->
			{
				ctx.get().enqueueWork(() ->
				{
					handler.accept(msg, ctx.get().getSender());
					ctx.get().setPacketHandled(true);
				});
			}
		);

		NextMessageID++;
	}

	public static <TMessage extends AirTrafficMessage> void registerClientHandler(
		@Nonnull Class<TMessage> clazz,
		@Nonnull Factory<TMessage> factory,
		@Nonnull Supplier<Consumer<TMessage>> handlerSupplier
	)
	{
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			INSTANCE.registerMessage(
				NextMessageID,
				clazz,
				AirTrafficMessage::encode,
				(buf) ->
				{
					TMessage msg = factory.Create();
					msg.decode(buf);
					return msg;
				},
				(msg, ctx) ->
				{
					ctx.get().enqueueWork(() ->
					{
						handlerSupplier.get().accept(msg);
						ctx.get().setPacketHandled(true);
					});
				}
			);
		});
		DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
			INSTANCE.registerMessage(
				NextMessageID,
				clazz,
				AirTrafficMessage::encode,
				(buf) ->
				{
					TMessage msg = factory.Create();
					msg.decode(buf);
					return msg;
				},
				(msg, ctx) -> {}
			);

		});

		NextMessageID++;
	}

	public static <MSG> void sendToPlayer(@Nonnull ServerPlayer player, @Nonnull MSG message)
	{
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
	}

	public static <MSG> void sendToChunk(@Nonnull LevelChunk levelChunk, @Nonnull MSG message)
	{
		INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> levelChunk), message);
	}

	public static <MSG> void sendToAll(@Nonnull MSG message)
	{
		INSTANCE.send(PacketDistributor.ALL.noArg(), message);
	}

	public static <MSG> void sendToAllAroundPoint(@Nonnull MSG message,
												  @Nonnull ResourceKey<Level> dimension,
												  @Nonnull Vec3 point,
												  double radius,
												  @Nullable Entity excluding)
	{
		for(ServerPlayer player : getServer().getPlayerList().getPlayers())
		{
			if(player.equals(excluding))
				continue;
			if(player.level().dimension().equals(dimension))
			{
				// If the player is within range of ANY point, send it
				if(player.position().distanceTo(point) <= radius)
				{
					INSTANCE.send(PacketDistributor.PLAYER.with(() -> { return player; }), message);
					break;
				}
			}
		}
	}

	public static <MSG> void sendToAllAroundPoints(@Nonnull MSG message,
												   @Nonnull ResourceKey<Level> dimension,
												   @Nonnull Collection<Vec3> points,
												   double radius,
												   @Nullable Entity excluding)
	{
		for(ServerPlayer player : getServer().getPlayerList().getPlayers())
		{
			if(player.equals(excluding))
				continue;
			if(player.level().dimension().equals(dimension))
			{
				for(Vec3 checkPoint : points)
				{
					// If the player is within range of ANY point, send it
					if(player.position().distanceTo(checkPoint) <= radius)
					{
						INSTANCE.send(PacketDistributor.PLAYER.with(() -> { return player; }), message);
						break;
					}
				}
			}
		}
	}

	@Nullable
	private static MinecraftServer getServer()
	{
		return ServerLifecycleHooks.getCurrentServer();
	}


	@OnlyIn(Dist.CLIENT)
	public static <MSG> void sendToServer(MSG message)
	{
		INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
	}

}
