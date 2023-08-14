package com.flansmod.common.types.magazines;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MagazineDefinition extends JsonDefinition
{
	public static final MagazineDefinition INVALID = new MagazineDefinition(new ResourceLocation(FlansMod.MODID, "magazines/null"));
	public static final String TYPE = "magazine";
	public static final String FOLDER = "magazines";
	@Override
	public String GetTypeName() { return TYPE; }

	public MagazineDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	private List<JsonDefinition> MatchingBulletReferences = null;
	public List<JsonDefinition> GetMatchingBullets()
	{
		if(MatchingBulletReferences == null)
		{
			MatchingBulletReferences = new ArrayList<>(matchBulletNames.length);
			FlansMod.BULLETS.RunOnMatches(
				(bullet) -> {
					// First check for exact name matches. These do not also need to pass tag checks
					for (String matchBulletName : matchBulletNames)
					{
						if(bullet.Location.equals(matchBulletName))
							return true;
					}
					// Then check the tags of this bullet
					for (final String tag : requiredBulletTags)
					{
						if(!bullet.HasTag(tag))
							return false;
					}
					for(final String tag : disallowedBulletTags)
					{
						if(bullet.HasTag(tag))
							return false;
					}
					return true;
				},
				(bullet) ->
				{
					if (!MatchingBulletReferences.contains(bullet))
						MatchingBulletReferences.add(bullet);
				});

		}
		return MatchingBulletReferences;
	}

	public boolean HasTag(String tag)
	{
		for (String s : tags)
			if (s.equals(tag))
				return true;
		return false;
	}

	@JsonField
	public String[] tags = new String[0];
	@JsonField
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];
	@JsonField
	public EAmmoLoadMode ammoLoadMode = EAmmoLoadMode.FullMag;
	@JsonField
	public EAmmoConsumeMode ammoConsumeMode = EAmmoConsumeMode.RoundRobin;




	// Bullet matching settings
	@JsonField(Min = 0, Max = 32000)
	public int numRounds = 0;
	@JsonField
	public boolean allRoundsMustBeIdentical = true;
	@JsonField
	public String[] matchBulletNames = new String[0];
	@JsonField
	public String[] requiredBulletTags = new String[0];
	@JsonField
	public String[] disallowedBulletTags = new String[0];
}
