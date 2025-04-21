package com.flansmod.airtraffic.client.gui;

import com.flansmod.airtraffic.common.AirTrafficControlMod;
import com.flansmod.airtraffic.network.AirTrafficControlPacketHandler;
import com.flansmod.airtraffic.network.toserver.ConfigureRunwayMessage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RunwayEditScreen extends Screen
{
	private static final ResourceLocation RUNWAY_EDIT_BG =
		new ResourceLocation(AirTrafficControlMod.MODID, "textures/gui/runway_edit.png");
	private static final int RUNWAY_EDIT_BG_W = 256;
	private static final int RUNWAY_EDIT_BG_H = 256;

	public final BlockPos blockPos;

	protected RunwayEditScreen(@Nonnull Component title, @Nonnull BlockPos pos)
	{
		super(title);

		blockPos = pos;
	}

	@Override
	protected void init()
	{
		addRenderableWidget(
			Button.builder(
				Component.translatable("runway.connect"),
				(t) -> {
					//connectToAirport(airportID);
				})
				.bounds(width, width, 0, 20)
				.build()
		);
	}

	public void connectToAirport(@Nonnull UUID airportID)
	{
		AirTrafficControlPacketHandler.sendToServer(ConfigureRunwayMessage.connect(blockPos, airportID));
	}
}
