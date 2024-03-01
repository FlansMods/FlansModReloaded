package com.flansmod.common.types.bullets;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.bullets.ImpactDefinition;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class ShotDefinition
{
	private TagKey<Block>[] BreakTagRefs = null;
	public TagKey<Block>[] GetBreakMaterials()
	{
		if(BreakTagRefs == null)
		{
			BreakTagRefs = new TagKey[breaksBlockTags.length];
			for(int i = 0; i < breaksBlockTags.length; i++)
			{
				BreakTagRefs[i] = MinecraftHelpers.FindBlockTag(breaksBlockTags[i]);
			}
		}
		return BreakTagRefs;
	}
	public boolean BreaksBlock(BlockState blockState)
	{
		TagKey<Block>[] breaks = GetBreakMaterials();
		for (int i = 0; i < breaks.length; i++)
		{
			if (blockState.is(breaks[i]))
				return true;
		}
		return false;
	}

	// These apply to all bullet types
	@JsonField(Docs = "Number of raycasts or bullet entities to create", Min = 0, Max = 128)
	public int bulletCount = 1;
	@JsonField
	public String[] breaksBlockTags = new String[0];
	@JsonField
	public float penetrationPower = 1.0f;
	@JsonField
	@Nonnull
	public ImpactDefinition impact = new ImpactDefinition();
}
