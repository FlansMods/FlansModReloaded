package com.flansmod.common.types.guns;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.GunInputContext;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.*;
import com.flansmod.common.types.guns.elements.*;
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

	public GunDefinition(@Nonnull ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public ItemDefinition itemSettings = new ItemDefinition();

	@JsonField
	public PaintableDefinition paints = new PaintableDefinition();


	@JsonField(Docs = "For each key input this gun should accept, you need a handler")
	public HandlerDefinition[] inputHandlers = new HandlerDefinition[0];
	@JsonField(Docs = "The possible actions of this gun")
	public ActionGroupDefinition[] actionGroups = new ActionGroupDefinition[0];
	@JsonField(Docs = "Defines which magazine options there are")
	public MagazineSlotSettingsDefinition[] magazines = new MagazineSlotSettingsDefinition[0];
	@JsonField(Docs = "These are triggered actions that fire when certain conditions are met")
	public AbilityDefinition[] staticAbilities = new AbilityDefinition[0];
	@JsonField
	public ReloadDefinition[] reloads = new ReloadDefinition[0];
	@JsonField
	public ModeDefinition[] modes = new ModeDefinition[0];

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
	public AttachmentSettingsDefinition charmAttachments = new AttachmentSettingsDefinition();
	@JsonField
	public String[] modelParts = new String[0];
	@JsonField
	public String animationSet = "";
	@JsonField
	public int particleCount = 1;
	@JsonField
	public String  casingModel = "";

	 //
	@Override
	public void LoadExtra(JsonElement jRoot)
	{
		super.LoadExtra(jRoot);
	}

	public ActionGroupDefinition GetActionGroup(String key)
	{
		for(ActionGroupDefinition group : actionGroups)
			if(group.key.equals(key))
				return group;
		return ActionGroupDefinition.INVALID;
	}

	public HandlerDefinition GetInputHandler(GunInputContext inputContext)
	{
		for(HandlerDefinition handler : inputHandlers)
			if(handler.inputType == inputContext.InputType)
				return handler;
		return HandlerDefinition.INVALID;
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
			case Charm -> { return charmAttachments; }
			default -> { return new AttachmentSettingsDefinition(); }
		}
	}

	@Nonnull
	public MagazineSlotSettingsDefinition GetMagazineSettings(String groupPath)
	{
		for(MagazineSlotSettingsDefinition magazineSettings: magazines)
		{
			if(groupPath.contains(magazineSettings.key))
			{
				return magazineSettings;
			}
		}
		return MagazineSlotSettingsDefinition.INVALID;
	}

}
