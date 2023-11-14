package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ImpactDefinition;
import com.flansmod.common.types.guns.elements.ESpreadPattern;
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
	public String[] breaksMaterials = new String[0];
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
