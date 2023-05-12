package com.flansmod.client.render.animation;

import java.util.HashMap;

public class GunAnimations
{
	private static HashMap<String, GunAnimation> Animations = new HashMap<>();

	public static GunAnimation SLIDE_SEMI_AUTO = RegisterGunAnimation("SlideSemiAuto", new GunAnimation.SlideSemiAuto());
	public static GunAnimation PUMP_ACTION = RegisterGunAnimation("PumpAction", new GunAnimation.PumpAction());
	public static GunAnimation INSPECT = RegisterGunAnimation("Inspect", new GunAnimation.Inspect());
	public static GunAnimation Spin = RegisterGunAnimation("Spin", new GunAnimation.Spin());


	public static GunAnimation GetGunAnimation(String key)
	{
		return Animations.get("Spin");
		//return Animations.get(key);
	}

	public static GunAnimation RegisterGunAnimation(String key, GunAnimation gunAnim)
	{
		Animations.put(key, gunAnim);
		return gunAnim;
	}
}
