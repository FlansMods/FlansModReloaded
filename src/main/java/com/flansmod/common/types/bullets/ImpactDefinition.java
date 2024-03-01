package com.flansmod.common.types.bullets;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.abilities.elements.EAbilityTarget;

public class ImpactDefinition
{
	@JsonField
	public EAbilityTarget targetType = EAbilityTarget.ShotEntity;

	//@JsonField(Docs = "The amount of damage to apply to the target")
	//public float damage = 1.0f;
	//@JsonField(Docs = "The amount of armour to ignore")
	//public float ignoreArmour = 0.0f;
	//@JsonField(Docs = "If >0 and valid for that entity/block(only FallingBlocks) it will push them from the impact origin")
	//public float knockback = 0.0f;
	//@JsonField(Docs = "If non-empty, this will be parsed similar to Minecraft's /effect command")
	//public String applyPotionEffect = "";
	//@JsonField(Docs = "If >0, set the hit entity as on fire for that many seconds, or put a fire block at the block hit")
	//public float setFire = 0.0f;
	//@JsonField(Docs = "If >0, create an explosion effect at the impact location")
	//public float explosionRadius = 0.0f;
	//@JsonField(AssetPathHint = "textures/")
	//public ResourceLocation decal = InvalidLocation;
	@JsonField
	public AbilityEffectDefinition[] impactEffects = new AbilityEffectDefinition[0];

	//@JsonField
	//public float multiplierVsPlayers = 1.0f;
	//@JsonField
	//public float multiplierVsVehicles = 1.0f;

	//@JsonField
	//public SoundDefinition[] hitSounds = new SoundDefinition[0];
}
