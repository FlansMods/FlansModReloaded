package com.flansmod.common.types.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Action;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.*;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.units.qual.A;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	public ItemDefinition itemSettings = new ItemDefinition();

	@JsonField
	public PaintableDefinition paints = new PaintableDefinition();
	@JsonField
	public ReloadDefinition primaryReload = new ReloadDefinition();
	@JsonField
	public ReloadDefinition secondaryReload = new ReloadDefinition();

	@JsonField(Docs = "Actions on the primary mouse button, this is where a shoot action normally goes")
	public ActionGroupDefinition primary = new ActionGroupDefinition();
	@JsonField(Docs = "Actions on the alternate mouse button, like scopes")
	public ActionGroupDefinition secondary = new ActionGroupDefinition();
	@JsonField(Docs = "Actions to trigger when pressing the 'Look At' key")
	public ActionGroupDefinition lookAt = new ActionGroupDefinition();

	@JsonField(Docs = "Defines which magazine options there are for the primary shoot action")
	public MagazineSlotSettingsDefinition primaryMagazines = new MagazineSlotSettingsDefinition();
	@JsonField(Docs = "If there is a secondary slot, defines which magazines are applicable")
	public MagazineSlotSettingsDefinition secondaryMagazines = new MagazineSlotSettingsDefinition();

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
	public ActionGroupDefinition GetActionGroup(EActionInput set)
	{
		switch(set)
		{
			case PRIMARY: return primary;
			case SECONDARY: return secondary;
			case RELOAD_PRIMARY, RELOAD_SECONDARY: return ActionGroupDefinition.INVALID;
			case LOOK_AT: return lookAt;
			default: return ActionGroupDefinition.INVALID;
		}
	}
	@Nonnull
	public ActionDefinition[] GetActions(EActionInput set)
	{
		return switch (set)
		{
			case PRIMARY -> primary.actions;
			case SECONDARY -> secondary.actions;
			case RELOAD_PRIMARY -> primaryReload.GetReloadActionGroup(EReloadStage.Start).actions;
			case RELOAD_SECONDARY -> secondaryReload.GetReloadActionGroup(EReloadStage.Start).actions;
			case LOOK_AT -> lookAt.actions;
		};
	}

	@Nonnull
	public AttachmentSettingsDefinition GetAttachmentSettings(EAttachmentType type)
	{
		switch(type)
		{
			case Grip -> { return gripAttachments; }
			case Sights -> { return scopeAttachments; }
			case Stock -> { return stockAttachments; }
			case Barrel -> { return barrelAttachments; }
			case Generic -> { return genericAttachments; }
			default -> { return new AttachmentSettingsDefinition(); }
		}
	}

	@Nonnull
	public MagazineSlotSettingsDefinition GetMagazineSettings(EActionInput input)
	{
		switch(input)
		{
			case PRIMARY, RELOAD_PRIMARY -> { return primaryMagazines;}
			case SECONDARY, RELOAD_SECONDARY -> { return secondaryMagazines;}
			default -> { return new MagazineSlotSettingsDefinition(); }
		}
	}

}
