package com.flansmod.common.types.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.*;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class GunDefinition extends JsonDefinition
{
	public static final String TYPE = "gun";
	public static final String FOLDER = "guns";
	public static final GunDefinition INVALID = new GunDefinition(new ResourceLocation(FlansMod.MODID, "guns/null"));
	@Override
	public String GetTypeName() { return TYPE; }

	public GunDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public PaintableDefinition paints = new PaintableDefinition();
	@JsonField
	public ReloadDefinition primaryReload = new ReloadDefinition();
	@JsonField
	public ReloadDefinition secondaryReload = new ReloadDefinition();

	@JsonField(Docs = "Actions on the primary mouse button, this is where a shoot action normally goes")
	public ActionDefinition[] primaryActions = new ActionDefinition[0];
	@JsonField(Docs = "Actions on the alternate mouse button, like scopes")
	public ActionDefinition[] secondaryActions = new ActionDefinition[0];
	@JsonField(Docs = "Actions to trigger when pressing the 'Look At' key")
	public ActionDefinition[] lookAtActions = new ActionDefinition[0];

	@JsonField
	public ActionDefinition[] startSpinUpActions = new ActionDefinition[0];
	@JsonField
	public ActionDefinition[] reachMaxSpinActions = new ActionDefinition[0];
	@JsonField
	public ActionDefinition[] startSpinDownActions = new ActionDefinition[0];
	@JsonField
	public ActionDefinition[] reachZeroSpinActions = new ActionDefinition[0];
	@JsonField
	public SoundDefinition[] loopingSounds = new SoundDefinition[0];

	@JsonField(Min = 0, Max = 100)
	public int numBullets = 0;
	@JsonField
	public EAmmoConsumeMode AmmoConsumeMode = EAmmoConsumeMode.RoundRobin;

	@JsonField
	public AttachmentSettingsDefinition barrelAttachments = new AttachmentSettingsDefinition();
	@JsonField
	public AttachmentSettingsDefinition gripAttachments = new AttachmentSettingsDefinition();
	@JsonField
	public AttachmentSettingsDefinition stockAttachments = new AttachmentSettingsDefinition();
	@JsonField
	public AttachmentSettingsDefinition scopeAttachments = new AttachmentSettingsDefinition();
	@JsonField
	public AttachmentSettingsDefinition genericAttachments = new AttachmentSettingsDefinition();
	@JsonField
	public String[] modelParts = new String[0];
	@JsonField
	public String animationSet = "";


	 //
	@Override
	public void LoadExtra(JsonElement jRoot)
	{
		super.LoadExtra(jRoot);
	}

	public ReloadDefinition GetReload(EActionInput inputType)
	{
		switch(inputType)
		{
			case PRIMARY, RELOAD_PRIMARY -> { return primaryReload; }
			case SECONDARY, RELOAD_SECONDARY -> { return secondaryReload; }
			default -> { return null; }
		}
	}

	@Nonnull
	public ActionDefinition[] GetActions(EActionInput set)
	{
		switch(set)
		{
			case PRIMARY: return primaryActions;
			case SECONDARY: return secondaryActions;
			case RELOAD_PRIMARY: return primaryReload.GetReloadActions(EReloadStage.Start);
			case RELOAD_SECONDARY: return secondaryReload.GetReloadActions(EReloadStage.Start);
			case LOOK_AT: return lookAtActions;
			default: return new ActionDefinition[0];
		}
	}

	public AttachmentSettingsDefinition GetAttachmentSettings(EAttachmentType type)
	{
		switch(type)
		{
			case Grip -> { return gripAttachments; }
			case Sights -> { return scopeAttachments; }
			case Stock -> { return stockAttachments; }
			case Barrel -> { return barrelAttachments; }
			case Generic -> { return genericAttachments; }
			default -> { return null; }
		}
	}

}
