package com.flansmod.common.types.elements;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.EFireMode;
import com.flansmod.common.types.guns.ESpreadPattern;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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
	private List<JsonDefinition> MatchingBulletReferences = null;
	public List<JsonDefinition> GetMatchingBullets()
	{
		if(MatchingBulletReferences == null)
		{
			MatchingBulletReferences = new ArrayList<>(matchAmmoNames.length + matchAmmoTags.length * 4);
			for (String matchAmmoName : matchAmmoNames)
			{
				FlansMod.BULLETS.RunOnMatch(matchAmmoName, (bullet) ->
				{
					if(!MatchingBulletReferences.contains(bullet))
						MatchingBulletReferences.add(bullet);
				});
			}
			for (final String tag : matchAmmoTags)
			{
				FlansMod.BULLETS.RunOnMatches(
					(bullet) -> bullet.HasTag(tag),
					(bullet) ->
					{
						if (!MatchingBulletReferences.contains(bullet))
							MatchingBulletReferences.add(bullet);
					});
			}
		}
		return MatchingBulletReferences;
	}

	@JsonField
	public float verticalReocil = 3.0f;
	@JsonField
	public float horizontalRecoil = 0.0f;
	@JsonField
	public float spread = 3.0f;
	@JsonField
	public ESpreadPattern spreadPattern = ESpreadPattern.FilledCircle;
	@JsonField
	public boolean hitscan = true;
	@JsonField
	public float speed = 0.0f;
	@JsonField
	public EFireMode fireMode = EFireMode.SemiAuto;
	@JsonField(Docs = "Number of times to repeat the fire action if we are set to burst fire mode")
	public int repeatCount = 1;
	@JsonField(Docs = "Number of raycasts or bullet entities to create", Min = 0, Max = 128)
	public int bulletCount = 1;
	@JsonField(Docs = "If using minigun fire mode, this is the time (in seconds) that it will take to spin up the motor and start shooting")
	public float spinUpDuration = 1.0f;
	@JsonField(Docs = "If using minigun fire mode, this is the max rotational speed (in degrees/second) of the barrels")
	public float spinSpeed = 360.0f;
	@JsonField(Docs = "The delay from this action to being able to perform it again (in seconds). If you have a desired Fire Rate in RPM, enter 60 / RPM")
	public float timeToNextShot = 2.0f;

	@JsonField
	public String[] breaksMaterials = new String[0];

	@JsonField
	public String[] matchAmmoNames = new String[0];
	@JsonField
	public String[] matchAmmoTags = new String[0];

	@JsonField
	public float penetrationPower = 1.0f;
	@JsonField
	public String trailParticles = "";

	@JsonField
	@Nonnull
	public ImpactDefinition impact = new ImpactDefinition();
}
