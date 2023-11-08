package com.flansmod.common.types.attachments;

public enum EAttachmentType
{
	// Gun types
	Barrel,
	Sights,
	Stock,
	Grip,

	// Generic
	Generic,

	// Mecha types
	Tool,
	Arm,
	Leg,
	Head,
	Shoulder,
	Feet,
	Hips;

	public static EAttachmentType Parse(String attachType)
	{
		return switch (attachType.toLowerCase())
		{
			case "barrel" -> EAttachmentType.Barrel;
			case "sights" -> EAttachmentType.Sights;
			case "stock" -> EAttachmentType.Stock;
			case "grip" -> EAttachmentType.Grip;
			case "tool" -> EAttachmentType.Tool;
			case "arm" -> EAttachmentType.Arm;
			case "leg" -> EAttachmentType.Leg;
			case "head" -> EAttachmentType.Head;
			case "shoulder" -> EAttachmentType.Shoulder;
			case "feet" -> EAttachmentType.Feet;
			case "hips" -> EAttachmentType.Hips;
			default -> EAttachmentType.Generic;
		};
	}

}
