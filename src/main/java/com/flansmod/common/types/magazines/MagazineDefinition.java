package com.flansmod.common.types.magazines;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemCollectionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
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

	@JsonField
	public String[] tags = new String[0];
	@JsonField
	public ModifierDefinition[] modifiers = new ModifierDefinition[0];
	@JsonField
	public EAmmoLoadMode ammoLoadMode = EAmmoLoadMode.FullMag;
	@JsonField
	public EAmmoConsumeMode ammoConsumeMode = EAmmoConsumeMode.RoundRobin;
	@JsonField(Docs = "The number of Magazine Upgrade items needed to swap to this mag")
	public int upgradeCost = 0;

	// Bullet matching settings
	@JsonField(Min = 0, Max = 32000)
	public int numRounds = 0;
	@JsonField(Docs = "A performance optimisation, recommended if the mag size is 100 or more")
	public boolean allRoundsMustBeIdentical = true;

	@JsonField
	public ItemCollectionDefinition matchingBullets = new ItemCollectionDefinition();

	@Nonnull
	public List<JsonDefinition> GetMatchingBullets()
	{
		return matchingBullets.GetDefinitionMatches();
	}
	public boolean HasTag(String tag)
	{
		for (String s : tags)
			if (s.equals(tag))
				return true;
		return false;
	}
	@JsonField(Docs = "Override for the casing model specified in the gun definition")
	public String casingModelOverride = "";
	@JsonField(Docs = "What item does the ejected bullet casing drop")
	public String spawnBulletCasing = "";
	@JsonField(Docs = "Whether to allow the gun to spawn bullet casings for this ammo type")
	public boolean ejectCasings = true;
}
