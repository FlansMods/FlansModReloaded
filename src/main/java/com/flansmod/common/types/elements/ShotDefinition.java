package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.ESpreadPattern;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nonnull;

public class ShotDefinition
{
	private Material[] BreakMaterialReferences = null;
	public Material[] GetBreakMaterials()
	{
		if(BreakMaterialReferences == null)
		{
			BreakMaterialReferences = new Material[breaksMaterials.length];
			for(int i = 0; i < breaksMaterials.length; i++)
			{
				BreakMaterialReferences[i] = MinecraftHelpers.FindMaterial(breaksMaterials[i]);
			}
		}
		return BreakMaterialReferences;
	}
	public boolean BreaksMaterial(Material material)
	{
		Material[] breaks = GetBreakMaterials();
		for(int i = 0; i < breaks.length; i++)
		{
			if(breaks[i] == material)
				return true;
		}
		return false;
	}

	@JsonField
	public float verticalReocil = 3.0f;
	@JsonField
	public float horizontalRecoil = 0.0f;
	@JsonField
	public float spread = 3.0f;
	@JsonField
	public boolean hitscan = true;
	@JsonField
	public float speed = 0.0f;
	@JsonField
	public int count = 1;
	@JsonField
	public float timeToNextShot = 2.0f;
	@JsonField
	public ESpreadPattern spreadPattern = ESpreadPattern.FilledCircle;
	@JsonField
	public String[] breaksMaterials = new String[0];

	@JsonField
	public float penetrationPower = 1.0f;
	@JsonField
	public String trailParticles = "";

	@JsonField
	@Nonnull
	public ImpactDefinition impact = new ImpactDefinition();
}
