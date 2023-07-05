package com.flansmod.common.actions;

// The list of actions one can input to a gun
public enum EActionInput
{
	PRIMARY(false),
	SECONDARY(false),
	LOOK_AT(false),
	RELOAD_PRIMARY(true),
	RELOAD_SECONDARY(true);

	private EActionInput(boolean isReload)
	{
		IsReload = isReload;
	}

	public final boolean IsReload;

	public EActionInput GetActionType()
	{
		switch(this)
		{
			case PRIMARY, RELOAD_PRIMARY -> { return PRIMARY; }
			case SECONDARY, RELOAD_SECONDARY -> { return SECONDARY; }
			default -> { return null; }
		}
	}
	public EActionInput GetReloadType()
	{
		switch(this)
		{
			case PRIMARY, RELOAD_PRIMARY -> { return RELOAD_PRIMARY; }
			case SECONDARY, RELOAD_SECONDARY -> { return RELOAD_SECONDARY; }
			default -> { return null; }
		}
	}
	public String GetAmmoTagName()
	{
		switch(this)
		{
			case PRIMARY, RELOAD_PRIMARY -> { return "ammo_primary"; }
			case SECONDARY, RELOAD_SECONDARY -> { return "ammo_secondary"; }
			default -> { return null; }
		}
	}
	public String GetChamberTagName()
	{
		switch(this)
		{
			case PRIMARY, RELOAD_PRIMARY -> { return "chamber_primary"; }
			case SECONDARY, RELOAD_SECONDARY -> { return "chamber_secondary"; }
			default -> { return null; }
		}
	}
}
