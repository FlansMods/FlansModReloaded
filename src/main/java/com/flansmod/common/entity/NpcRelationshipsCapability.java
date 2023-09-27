package com.flansmod.common.entity;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class NpcRelationshipsCapability
{
	public static final Capability<INpcRelationshipsCapability> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
	private NpcRelationshipsCapability() {}
}
