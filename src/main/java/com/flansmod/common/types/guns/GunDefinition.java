package com.flansmod.common.types.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.EActionSet;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
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

	@JsonField
	public ActionDefinition[] primaryActions = new ActionDefinition[0];

	@JsonField
	public ActionDefinition[] secondaryActions = new ActionDefinition[0];

	@JsonField(Min = 0, Max = 100)
	public int numBullets = 0;


	@JsonField
	public AttachmentSettingsDefinition barrelAttachments = new AttachmentSettingsDefinition();
	@JsonField
	public AttachmentSettingsDefinition gripAttachments = new AttachmentSettingsDefinition();
	@JsonField
	public AttachmentSettingsDefinition stockAttachments = new AttachmentSettingsDefinition();
	@JsonField
	public AttachmentSettingsDefinition scopeAttachments = new AttachmentSettingsDefinition();


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

}
