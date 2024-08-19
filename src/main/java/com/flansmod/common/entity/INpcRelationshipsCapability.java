package com.flansmod.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public interface INpcRelationshipsCapability extends INBTSerializable<CompoundTag>
{
	ENpcRelationship GetRelationship(ResourceLocation npcID);
	void SetRelationship(ResourceLocation npcID, ENpcRelationship relationship);
	long GetEndCooldownTick(ResourceLocation npcID);
	void SetEndCooldownTick(ResourceLocation npcID, long tick);
	int GetLevel(ResourceLocation npcID);
	void SetLevel(ResourceLocation npcID, int level);
}
