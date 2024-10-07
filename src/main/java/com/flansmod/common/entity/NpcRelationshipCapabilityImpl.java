package com.flansmod.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class NpcRelationshipCapabilityImpl implements INpcRelationshipsCapability
{
	private final HashMap<ResourceLocation, ENpcRelationship> Relationships = new HashMap<>();
	private final HashMap<ResourceLocation, Long> Cooldowns = new HashMap<>();
	private final HashMap<ResourceLocation, Integer> Levels = new HashMap<>();

	@Override
	public ENpcRelationship GetRelationship(ResourceLocation npcID)
	{
		return Relationships.getOrDefault(npcID, ENpcRelationship.NotMet);
	}
	@Override
	public void SetRelationship(ResourceLocation npcID, ENpcRelationship relationship)
	{
		Relationships.put(npcID, relationship);
	}
	@Override
	public long GetEndCooldownTick(ResourceLocation npcID) { return Cooldowns.getOrDefault(npcID, 0L); }
	@Override
	public void SetEndCooldownTick(ResourceLocation npcID, long tick) { Cooldowns.put(npcID, tick); }
	@Override
	public int GetLevel(ResourceLocation npcID) { return Levels.getOrDefault(npcID, 0); }
	@Override
	public void SetLevel(ResourceLocation npcID, int level) { Levels.put(npcID, level); }

	@Override
	public CompoundTag serializeNBT()
	{
		final CompoundTag tags = new CompoundTag();
		for(var kvp : Relationships.entrySet())
		{
			CompoundTag relationshipTags = new CompoundTag();
			relationshipTags.putString("relation", kvp.getValue().toString());
			relationshipTags.putLong("cooldown", Cooldowns.getOrDefault(kvp.getKey(), 0L));
			tags.put(kvp.getKey().toString(), relationshipTags);
		}
		return tags;
	}
	@Override
	public void deserializeNBT(CompoundTag tags)
	{
		for(String key : tags.getAllKeys())
		{
			ResourceLocation npcID = new ResourceLocation(key);
			CompoundTag relationshipTags = tags.getCompound(key);
			Relationships.put(npcID, ENpcRelationship.valueOf(relationshipTags.getString("relation")));
			Cooldowns.put(npcID, relationshipTags.getLong("cooldown"));
		}
	}

}
