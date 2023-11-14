package com.flansmod.common.types.guns.elements;

import com.flansmod.common.types.JsonField;

import javax.annotation.Nonnull;

public class ReloadDefinition
{
	@JsonField(Docs = "This should match the action group key")
	public String key = "primary";
	@JsonField(Docs = "If true, the player can press [R] to reload manually")
	public boolean manualReloadAllowed = true;
	@JsonField(Docs = "If true, attempting to fire on empty will trigger a reload")
	public boolean autoReloadWhenEmpty = true;

	@JsonField
	public String startActionKey = "primary_reload_start";
	@JsonField
	public String ejectActionKey = "primary_reload_eject";
	@JsonField
	public String loadOneActionKey = "primary_reload_load_one";
	@JsonField
	public String endActionKey = "primary_reload_end";

	public boolean Contains(String actionGroupPath)
	{
		return actionGroupPath.contains(startActionKey)
			|| actionGroupPath.contains(ejectActionKey)
			|| actionGroupPath.contains(loadOneActionKey)
			|| actionGroupPath.contains(endActionKey);
	}
	public EReloadStage GetStage(String actionGroupPath)
	{
		if(actionGroupPath.contains(startActionKey)) return EReloadStage.Start;
		if(actionGroupPath.contains(ejectActionKey)) return EReloadStage.Eject;
		if(actionGroupPath.contains(loadOneActionKey)) return EReloadStage.LoadOne;
		if(actionGroupPath.contains(endActionKey)) return EReloadStage.End;
		return null;
	}
	@Nonnull
	public String GetReloadActionKey(EReloadStage stage)
	{
		switch(stage)
		{
			case Start -> { return startActionKey; }
			case Eject -> { return ejectActionKey; }
			case LoadOne -> { return loadOneActionKey; }
			case End -> { return endActionKey; }
		}
		return "";
	}
}
