package com.flansmod.common.types.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Action;
import com.flansmod.common.actions.EActionSet;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.AttachmentSettingsDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.elements.ReloadDefinition;
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
	public ReloadDefinition reload = new ReloadDefinition();

	@JsonField(Docs = "Actions on the primary mouse button, this is where a shoot action normally goes")
	public ActionDefinition[] primaryActions = new ActionDefinition[0];
	@JsonField(Docs = "Actions on the alternate mouse button, like scopes")
	public ActionDefinition[] secondaryActions = new ActionDefinition[0];
	@JsonField(Docs = "Actions to trigger when pressing the 'Look At' key")
	public ActionDefinition[] lookAtActions = new ActionDefinition[0];

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

	@Nonnull
	public ActionDefinition[] GetActions(EActionSet set)
	{
		ActionDefinition[] ret = set == EActionSet.PRIMARY ? primaryActions : secondaryActions;
		return ret != null ? ret : new ActionDefinition[0];
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
