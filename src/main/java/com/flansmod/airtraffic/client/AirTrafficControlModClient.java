package com.flansmod.airtraffic.client;

import com.flansmod.airtraffic.api.ATC;
import com.flansmod.airtraffic.common.AirTrafficControlMod;
import com.flansmod.airtraffic.network.toclient.UpdateRunwayMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = AirTrafficControlMod.MODID)
public class AirTrafficControlModClient
{
	@SubscribeEvent
	public static void clientInit(final FMLClientSetupEvent event)
	{
		ATC.registerClient(new AirTrafficClient());
	}

	public static void handleRunwayNameAndID(@Nonnull UpdateRunwayMessage.UpdateRunwayNameAndID msg)
	{

	}
	public static void handleRunwayValidationProgress(@Nonnull UpdateRunwayMessage.UpdateRunwayValidationProgress msg)
	{

	}
	public static void handleCompletedRunwayValidation(@Nonnull UpdateRunwayMessage.CompletedRunwayValidation msg)
	{

	}
}
