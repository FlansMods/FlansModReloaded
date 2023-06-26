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
	public ReloadStageDefinition start = new ReloadStageDefinition();
	@JsonField(Docs = "The eject stage is played once")
	public ReloadStageDefinition eject = new ReloadStageDefinition();
	@JsonField(Docs = "The loadOne stage is played once per ammo item used. This could be once per magazine, once per bullet/shell etc.")
	public ReloadStageDefinition loadOne = new ReloadStageDefinition();
	@JsonField(Docs = "The end stage should return the animations to neutral positions")
	public ReloadStageDefinition end = new ReloadStageDefinition();

	@Nonnull
	public ActionDefinition[] GetReloadActions(EReloadStage stage)
	{
		switch(stage)
		{
			case Start -> { return start.actions; }
			case Eject -> { return eject.actions; }
			case LoadOne -> { return loadOne.actions; }
			case End -> { return end.actions; }
		}
		return new ActionDefinition[0];
	}
}
