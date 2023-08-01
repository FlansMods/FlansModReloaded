package com.flansmod.common.types.crafting;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public enum EMaterialType
{
	Misc,
	Wood,
	Glass,
	Metal,
	Composite,
	Electronic,
	Fabric;

	private static final Pattern REGEX = Pattern.compile("(flansmod:items/)(misc|wood|glass|metal|composite|electronic|fabric)_tier_([0-9]*)");



	public static class MaterialAndTier
	{
		public final int Tier;
		public final EMaterialType MaterialType;

		public boolean IsValid() { return Tier != INVALID_TIER; }

		protected MaterialAndTier(int tier, EMaterialType type)
		{
			Tier = tier;
			MaterialType = type;
		}

		public static final int INVALID_TIER = -1;
		public static final MaterialAndTier Invalid = new MaterialAndTier(INVALID_TIER, EMaterialType.Misc);
	}

	public static MaterialAndTier Resolve(String tag)
	{
		MatchResult result = REGEX.matcher(tag).toMatchResult();
		if(result.groupCount() == 3)
		{
			int tier = Integer.parseInt(result.group(2));
			EMaterialType type = parse(result.group(1));
			if(type != null)
				return new MaterialAndTier(tier, type);
		}
		else if(result.groupCount() == 2)
		{
			EMaterialType type = parse(result.group(1));
			if(type != null)
				return new MaterialAndTier(1, type);
		}

		return MaterialAndTier.Invalid;
	}

	public static EMaterialType parse(String value)
	{
		return switch (value)
		{
			case "wood" -> Wood;
			case "glass" -> Glass;
			case "metal" -> Metal;
			case "composite" -> Composite;
			case "electronic" -> Electronic;
			case "fabric" -> Fabric;
			case "misc" -> Misc;
			default -> null;
		};
	}

	public static EMaterialType GetType(String tag)
	{
		MatchResult result = REGEX.matcher(tag).toMatchResult();
		if(result.groupCount() > 2)
		{
			return switch (result.group(1))
			{
				case "wood" -> Wood;
				case "glass" -> Glass;
				case "metal" -> Metal;
				case "composite" -> Composite;
				case "electronic" -> Electronic;
				case "fabric" -> Fabric;
				case "misc" -> Misc;
				default -> null;
			};
		}
		return null;
	}

	public static int GetTier(String tag)
	{
		MatchResult result = REGEX.matcher(tag).toMatchResult();
		if(result.groupCount() > 2)
			return Integer.parseInt(result.group(2));
		return -1;
	}
}
