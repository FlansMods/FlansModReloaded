package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

public class ReloadDefinition
{
	@JsonField(Docs = "If true, the player can press [R] to reload manually")
	public boolean manualReloadAllowed = true;

	@JsonField(Docs = "The start stage normally covers the player moving their hands into position to enact the reload")
	public ReloadStageDefinition start = new ReloadStageDefinition();
	@JsonField(Docs = "The eject stage is played once")
	public ReloadStageDefinition eject = new ReloadStageDefinition();
	@JsonField(Docs = "The loadOne stage is played once per ammo item used. This could be once per magazine, once per bullet/shell etc.")
	public ReloadStageDefinition loadOne = new ReloadStageDefinition();
	@JsonField(Docs = "The end stage should return the animations to neutral positions")
	public ReloadStageDefinition end = new ReloadStageDefinition();
}
