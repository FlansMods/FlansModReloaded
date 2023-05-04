package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
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
			BreakMaterialReferences = new Material[BreaksMaterials.length];
			for(int i = 0; i < BreaksMaterials.length; i++)
			{
				BreakMaterialReferences[i] = MinecraftHelpers.FindMaterial(BreaksMaterials[i]);
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
	public float VerticalRecoil = 3.0f;
	@JsonField
	public float HorizontalRecoil = 0.0f;
	@JsonField
	public float Spread = 3.0f;
	@JsonField
	public boolean Hitscan = true;
	@JsonField
	public float Speed = 0.0f;
	@JsonField
	public int Count = 1;
	@JsonField
	public float TimeToNextShot = 2.0f;
	@JsonField
	public ESpreadPattern SpreadPattern = ESpreadPattern.FilledCircle;
	@JsonField
	public String[] BreaksMaterials = new String[0];

	@JsonField
	public float PenetrationPower = 1.0f;
	@JsonField
	public String TrailParticles = "";

	@JsonField
	@Nonnull
	public ImpactDefinition Impact = new ImpactDefinition();
}
