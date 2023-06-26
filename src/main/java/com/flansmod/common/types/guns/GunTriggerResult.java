package com.flansmod.common.types.guns;

public class GunTriggerResult
{
	public static final int Flag_None = 0;
	public static final int Flag_Shoot = 1 << 0;
	public static final int Flag_RotateChambers = 1 << 1;
	public static final int Flag_AutoReloadIfEnabled = 1 << 2;
	public static final int Flag_AutoReloadForced = 1 << 3;


}
