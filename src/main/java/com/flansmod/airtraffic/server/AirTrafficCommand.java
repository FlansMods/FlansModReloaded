package com.flansmod.airtraffic.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class AirTrafficCommand
{
	public static void register(@Nonnull CommandDispatcher<CommandSourceStack> dispatcher,
								@Nonnull CommandBuildContext context)
	{
		dispatcher.register(
			Commands.literal("atc")
				.requires((player) -> player.hasPermission(2))
				.then(Commands.literal("airport")
					.then(Commands.literal("list"))
				)
		);
	}

	private static int airportList(@Nonnull CommandSourceStack source)
	{
		//source.sendSuccess(() -> Component.translatable("flans_air_traffic_control.command.airport_list"), true);
		return -1;
	}
}
