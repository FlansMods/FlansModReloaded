package com.flansmod.common.types.bullets;

import com.flansmod.common.FlansMod;
import com.flansmod.common.abilities.Abilities;
import com.flansmod.common.item.BulletItem;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.abilities.elements.EAbilityEffect;
import com.flansmod.common.types.bullets.elements.HitscanDefinition;
import com.flansmod.common.types.bullets.elements.ImpactDefinition;
import com.flansmod.common.types.bullets.elements.ProjectileDefinition;
import com.flansmod.common.types.elements.ItemDefinition;
import com.flansmod.common.types.guns.elements.ActionGroupDefinition;
import com.flansmod.common.types.guns.elements.AbilityDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;

public class BulletDefinition extends JsonDefinition
{
	public static final BulletDefinition INVALID = new BulletDefinition(new ResourceLocation(FlansMod.MODID, "bullets/null"));
	public static final Lazy<BulletDefinition> STANDARD_TEST_BULLET = Lazy.of(() -> {
		BulletDefinition bulletDef = new BulletDefinition(new ResourceLocation(FlansMod.MODID, "bullets/test"));
		bulletDef.hitscans = new HitscanDefinition[] { new HitscanDefinition() };
		bulletDef.hitscans[0].impacts = new ImpactDefinition[] { new ImpactDefinition() };
		bulletDef.hitscans[0].impacts[0].impactEffects = new AbilityEffectDefinition[] { new AbilityEffectDefinition() };
		bulletDef.hitscans[0].impacts[0].impactEffects[0].effectType = Abilities.ABILITY_EFFECT_TYPE_APPLY_DAMAGE.getId();
		return bulletDef;
	});

	public static final String TYPE = "bullet";
	public static final String FOLDER = "bullets";

	//Trying to create way to reference bullet item/data from definition. This is stupid.
	public BulletItem item;

	@Override
	public String GetTypeName() { return TYPE; }

	public boolean HasTag(@Nonnull ResourceLocation tag)
	{
		for (ResourceLocation s : itemSettings.tags)
			if (s.equals(tag))
				return true;
		return false;
	}

	public int GetItemDurability() { return roundsPerItem > 1 ? roundsPerItem : 0; }
	public int GetMaxStackSize() { return itemSettings.maxStackSize; }

	public BulletDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}


	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();
	@JsonField
	public int roundsPerItem = 1;

	@JsonField(Docs = "Any number of hitscan rays. These shoot the target instantly")
	public HitscanDefinition[] hitscans = new HitscanDefinition[0];
	@JsonField(Docs = "Any number of entity projectiles. These take time to get to their target")
	public ProjectileDefinition[] projectiles = new ProjectileDefinition[0];


	@JsonField
	public AbilityDefinition[] triggers = new AbilityDefinition[0];
	@JsonField(Docs = "These action groups can be triggered at the point of impact")
	public ActionGroupDefinition[] actionGroups = new ActionGroupDefinition[0];



}
