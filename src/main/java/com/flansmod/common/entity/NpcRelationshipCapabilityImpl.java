package com.flansmod.common.entity;

import com.flansmod.util.Maths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class NpcRelationshipCapabilityImpl implements INpcRelationshipsCapability
{
	private final HashMap<ResourceLocation, ENpcRelationship> Relationships = new HashMap<>();
	private final HashMap<ResourceLocation, Integer> Cooldowns = new HashMap<>();
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
	public int GetCooldownTicks(ResourceLocation npcID) { return Cooldowns.getOrDefault(npcID, 0); }
	@Override
	public void SetCooldownTicks(ResourceLocation npcID, int ticks) { Cooldowns.put(npcID, ticks); }
	@Override
	public int GetLevel(ResourceLocation npcID) { return Levels.getOrDefault(npcID, 0); }
	@Override
	public void SetLevel(ResourceLocation npcID, int level) { Levels.put(npcID, level); }
	@Override
	public void TickAllCooldowns(int ticks)
	{
		for(ResourceLocation npcID : Relationships.keySet())
			if(Cooldowns.containsKey(npcID))
				Cooldowns.put(npcID, Maths.Max(Cooldowns.get(npcID) - ticks, 0));
	}

	@Override
	public CompoundTag serializeNBT()
	{
		final CompoundTag tags = new CompoundTag();
		for(var kvp : Relationships.entrySet())
		{
			CompoundTag relationshipTags = new CompoundTag();
			relationshipTags.putString("relation", kvp.getValue().toString());
			relationshipTags.putInt("cooldown", Cooldowns.getOrDefault(kvp.getKey(), 0));
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
			Cooldowns.put(npcID, relationshipTags.getInt("cooldown"));
		}
	}

}
