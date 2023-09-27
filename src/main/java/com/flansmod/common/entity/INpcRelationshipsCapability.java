package com.flansmod.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public interface INpcRelationshipsCapability extends INBTSerializable<CompoundTag>
{
	ENpcRelationship GetRelationship(ResourceLocation npcID);
	void SetRelationship(ResourceLocation npcID, ENpcRelationship relationship);
	int GetCooldownTicks(ResourceLocation npcID);
	void SetCooldownTicks(ResourceLocation npcID, int ticks);
	void TickAllCooldowns(int ticks);
}
