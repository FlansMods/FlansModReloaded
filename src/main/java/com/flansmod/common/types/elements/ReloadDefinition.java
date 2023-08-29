package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.guns.EReloadStage;

import javax.annotation.Nonnull;

public class ReloadDefinition
{
	@JsonField(Docs = "If true, the player can press [R] to reload manually")
	public boolean manualReloadAllowed = true;
	@JsonField(Docs = "If true, attempting to fire on empty will trigger a reload")
	public boolean autoReloadWhenEmpty = true;

	@JsonField(Docs = "The start stage normally covers the player moving their hands into position to enact the reload")
	public ActionGroupDefinition start = new ActionGroupDefinition();
	@JsonField(Docs = "The eject stage is played once")
	public ActionGroupDefinition eject = new ActionGroupDefinition();
	@JsonField(Docs = "The loadOne stage is played once per ammo item used. This could be once per magazine, once per bullet/shell etc.")
	public ActionGroupDefinition loadOne = new ActionGroupDefinition();
	@JsonField(Docs = "The end stage should return the animations to neutral positions")
	public ActionGroupDefinition end = new ActionGroupDefinition();

	@Nonnull
	public ActionGroupDefinition GetReloadActionGroup(EReloadStage stage)
	{
		switch(stage)
		{
			case Start -> { return start; }
			case Eject -> { return eject; }
			case LoadOne -> { return loadOne; }
			case End -> { return end; }
		}
		return new ActionGroupDefinition();
	}
}
