package com.flansmod.common.actions;

import javax.annotation.Nonnull;

// The list of actions one can input to a gun
public enum EActionInput
{
	PRIMARY,
	SECONDARY,
	LOOK_AT,
	RELOAD_PRIMARY,
	RELOAD_SECONDARY;

	public boolean IsPrimary()
	{
		switch(this)
		{
			case RELOAD_PRIMARY, PRIMARY -> { return true; }
			default -> { return false; }
		}
	}

	public boolean IsSecondary()
	{
		switch(this)
		{
			case RELOAD_SECONDARY, SECONDARY -> { return true; }
			default -> { return false; }
		}
	}

	public boolean IsReload()
	{
		switch(this)
		{
			case RELOAD_PRIMARY, RELOAD_SECONDARY -> { return true; }
			default -> { return false; }
		}
	}

	public boolean IsShoot()
	{
		switch(this)
		{
			case PRIMARY, SECONDARY -> { return true; }
			default -> { return false; }
		}
	}

	public boolean IsLookAt()
	{
		return this == LOOK_AT;
	}

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

	@Nonnull
	public String GetRootTagName()
	{
		switch(this)
		{
			case PRIMARY, RELOAD_PRIMARY -> { return "primary"; }
			case SECONDARY, RELOAD_SECONDARY -> { return "secondary"; }
			default -> { return "lookat"; }
		}
	}
	public String GetMagazineTagName()
	{
		switch(this)
		{
			case PRIMARY, RELOAD_PRIMARY -> { return "mag_primary"; }
			case SECONDARY, RELOAD_SECONDARY -> { return "mag_secondary"; }
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
