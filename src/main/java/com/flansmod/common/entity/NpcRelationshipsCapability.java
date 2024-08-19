package com.flansmod.common.entity;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class NpcRelationshipsCapability
{
	/// WARNING
	// Loading the class CapabilityToken causes Forge to run the CapabilityTokenSubclass transformer
	// This returns "ComputeFlags.COMPUTE_MAXS", the only place in source that seems to
	// IF YOU DO THIS, HOT-CODE-RELOAD BREAKS!!!
	// My fix for now is going to be to avoid calling this until anyone actually cares about NpcRelationships
	/// WARNING
	public static final Capability<INpcRelationshipsCapability> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
	private NpcRelationshipsCapability() {}
}
