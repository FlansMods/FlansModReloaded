package com.flansmod.common.entity;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.npc.NpcDefinition;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NpcRelationshipCapabilityAttacher
{
	private static class NpcRelationshipCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag>
	{
		public static final ResourceLocation NPC_RELATIONSHIPS_CAPABILITY = new ResourceLocation(FlansMod.MODID, "npc_relationships");

		private final INpcRelationshipsCapability Storage = new NpcRelationshipCapabilityImpl();
		private final LazyOptional<INpcRelationshipsCapability> Wrapper = LazyOptional.of(() -> Storage);

		@NotNull
		@Override
		public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
		{
			return NpcRelationshipsCapability.INSTANCE.orEmpty(cap, Wrapper);
		}
		void Invalidate()
		{
			Wrapper.invalidate();
		}
		@Override
		public CompoundTag serializeNBT()
		{
			return Storage.serializeNBT();
		}
		@Override
		public void deserializeNBT(CompoundTag nbt)
		{
			Storage.deserializeNBT(nbt);
		}
	}



	public NpcRelationshipCapabilityAttacher()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void OnRegisterCaps(final RegisterCapabilitiesEvent event)
	{
		event.register(INpcRelationshipsCapability.class);
	}

	@SubscribeEvent
	public void OnAttachCaps(final AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player player)
		{
			event.addCapability(
				NpcRelationshipCapabilityProvider.NPC_RELATIONSHIPS_CAPABILITY,
				new NpcRelationshipCapabilityProvider());
		}
	}
}
