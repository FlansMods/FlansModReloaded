package com.flansmod.common.types.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ImpactDefinition;
import com.flansmod.common.types.elements.ItemStackDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class GrenadeDefinition extends JsonDefinition
{
	public static final GrenadeDefinition INVALID = new GrenadeDefinition(new ResourceLocation(FlansMod.MODID, "grenades/null"));
	public static final String TYPE = "grenade";
	@Override
	public String GetTypeName() { return TYPE; }

	public GrenadeDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	// Hmm, does this have an item?!
	@JsonField(Docs = "Most likely a throw action")
	public ActionDefinition[] primaryActions = new ActionDefinition[0];
	@JsonField(Docs = "Could be a cook, drop, detonate etc")
	public ActionDefinition[] secondaryActions = new ActionDefinition[0];

	@JsonField
	public float bounciness = 0.0f;
	@JsonField
	public boolean sticky = false;
	@JsonField
	public boolean canStickToThrower = false;
	@JsonField(Docs = "Keep this <= 0 if you don't want a proximity trigger")
	public float livingProximityTrigger = -1f;
	@JsonField(Docs = "Keep this <= 0 if you don't want a proximity trigger")
	public float vehicleProximityTrigger = -1f;
	@JsonField
	public boolean detonateWhenShot = false;
	@JsonField
	public boolean detonateWhenDamaged = false;
	@JsonField(Docs = "Keep this <= 0 for no fuse")
	public float fuse = -1.0f;
	@JsonField(Docs = "This will spin like a frisbee")
	public float spinForceX = 0.0f;
	@JsonField(Docs = "This will tumble forwards")
	public float spinForceY = 0.0f;

	@JsonField
	public ImpactDefinition impact = new ImpactDefinition();

	@JsonField
	public float lifetimeAfterDetonation = 0.0f;
	@JsonField
	public String[] smokeParticles = new String[0];
	@JsonField
	public String[] effectsToApplyInSmoke = new String[0];
	@JsonField
	public float smokeRadius = 0.0f;
}
