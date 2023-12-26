package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ImpactDefinition;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
	@JsonField
	public float verticalRecoil = 3.0f;
	@JsonField
	public float horizontalRecoil = 0.0f;
	@JsonField
	public float spread = 0.0f;
	@JsonField
	public ESpreadPattern spreadPattern = ESpreadPattern.FilledCircle;
	@JsonField
	public float speed = 0.0f;
	@JsonField(Docs = "Number of raycasts or bullet entities to create", Min = 0, Max = 128)
	public int bulletCount = 1;
	@JsonField(Docs = "If using minigun fire mode, this is the max rotational speed (in degrees/second) of the barrels")
	public float spinSpeed = 360.0f;
	@JsonField
	public String[] breaksBlockTags = new String[0];
	@JsonField
	public float penetrationPower = 1.0f;
	@JsonField
	@Nonnull
	public ImpactDefinition impact = new ImpactDefinition();

	@JsonField
	public boolean hitscan = true;

	// The following only apply to spawned bullets (non-hitscan)
	@JsonField(Docs = "If set to a non-zero amount, this projectile will have a fuse timer, in seconds")
	public float fuseTime = 0.0f;
	@JsonField
	public float gravityFactor = 1.0f;
	@JsonField
	public boolean sticky = false;
	@JsonField(Docs = "How quickly a projectile rotates to face the direction of travel")
	public float turnRate = 0.5f;
	@JsonField(Docs = "Percent speed loss per tick (1/20s)")
	public float dragInAir = 0.01f;
	@JsonField
	public String trailParticles = "";
	@JsonField
	public float secondsBetweenTrailParticles = 0.25f;
	@JsonField(Docs = "Percent speed loss per tick (1/20s)")
	public float dragInWater = 0.2f;
	@JsonField
	public String waterParticles = "";
}
