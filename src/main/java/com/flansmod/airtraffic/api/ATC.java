package com.flansmod.airtraffic.api;

import com.flansmod.airtraffic.api.client.IAirTrafficClient;
import com.flansmod.airtraffic.api.server.IAirTrafficServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ATC
{
	private static IAirTrafficClient clientInstance = null;
	private static final Map<ResourceKey<Level>, IAirTrafficServer> serverInstances = new HashMap<>();

	@Nullable
	public static IAirTrafficClient getClient() { return clientInstance; }
	public static boolean isActiveClient() { return clientInstance != null; }
	public static void registerClient(@Nonnull IAirTrafficClient client) { clientInstance = client; }
	public static void invalidateClient() { clientInstance = null; }


	@Nullable
	public static IAirTrafficServer getServer(@Nonnull Level level) { return serverInstances.get(level.dimension()); }
	@Nullable
	public static IAirTrafficServer getServer(@Nonnull ResourceKey<Level> key) { return serverInstances.get(key); }
	public static boolean isActiveServer(@Nonnull ResourceKey<Level> key) { return serverInstances.containsKey(key); }
	public static void registerServer(@Nonnull ResourceKey<Level> key, @Nonnull IAirTrafficServer server)
	{
		serverInstances.put(key, server);
	}
	public static void invalidateServer(@Nonnull ResourceKey<Level> key)
	{
		serverInstances.remove(key);
	}
}
